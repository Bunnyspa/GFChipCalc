package main.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bunnyspa
 */
public class Session extends Thread {

    private static final String CRLF = "\r\n";
    private static final String STATUS_404 = "Not Found";
    private static final String STATUS_504 = "Gateway Timeout";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final int TIMEOUT = 10000;

    private final Socket client;
    private final Interceptor interceptor;

    public Session(Socket client, Interceptor interceptor) {
        this.client = client;
        this.interceptor = interceptor;
    }

    @Override
    public void run() {
        try {
            InputStream clientIn = client.getInputStream();
            OutputStream clientOut = client.getOutputStream();
            RequestHeader requestHeader = RequestHeader.parse(clientIn);
            try {
                if ("CONNECT".equals(requestHeader.method)) {
                    handleConnect(requestHeader, clientIn, clientOut);
                } else {
                    handleDefault(interceptor, requestHeader, clientIn, clientOut);
                }
            } catch (UnknownHostException | MalformedURLException ex) {
                handleEx(clientOut, requestHeader.version, 404, STATUS_404);
            } catch (SocketTimeoutException ex) {
                handleEx(clientOut, requestHeader.version, 504, STATUS_504);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void handleDefault(Interceptor interceptor, RequestHeader requestHeader, InputStream clientIn, OutputStream clientOut) throws IOException {
        URL url = new URL(requestHeader.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(TIMEOUT);
        handleRequest(interceptor, connection, requestHeader, clientIn);
        handleResponse(interceptor, connection, requestHeader, clientOut);
    }

    private static void handleConnect(RequestHeader requestHeader, InputStream clientIn, OutputStream clientOut) throws IOException, InterruptedException {
        String[] split = requestHeader.url.split(":");
        String url = split[0];
        int port = Integer.parseInt(split[1]);

        InetAddress address = InetAddress.getByName(url);

        try (Socket server = new Socket(address, port)) {
            server.setSoTimeout(TIMEOUT);
            InputStream serverIn = server.getInputStream();
            OutputStream serverOut = server.getOutputStream();

            clientOut.write((requestHeader.version + " 200 Connection Established" + CRLF + CRLF).getBytes());
            clientOut.flush();

            Thread clientToServer = new Tunnel(clientIn, serverOut);
            Thread serverToClient = new Tunnel(serverIn, clientOut);

            clientToServer.start();
            serverToClient.start();

            clientToServer.join();
            serverToClient.join();
        }
    }

    private static void handleEx(OutputStream clientOut, String version, int code, String msg) {
        try {
            clientOut.write((version + " " + code + " " + msg + CRLF + CRLF).getBytes());
        } catch (IOException ignored) {
        }
    }

    private static void handleRequest(Interceptor interceptor, HttpURLConnection connection, RequestHeader requestHeader, InputStream clientIn) throws IOException {
        connection.setRequestMethod(requestHeader.method);
        // Header
        for (String key : requestHeader.getHeaderKeys()) {
            connection.setRequestProperty(key, requestHeader.getHeader(key));
        }
        boolean intercept = interceptor == null ? false : interceptor.interceptRequest(requestHeader);

        // Body
        boolean chunked = isChunked(requestHeader);
        int cl = getContentLength(requestHeader);

        if (chunked || cl > 0) {
            connection.setDoOutput(true);
            try (OutputStream serverOut = connection.getOutputStream()) {
                List<Byte> body = chunked
                        ? handleBodyChunked(intercept, clientIn, serverOut, true)
                        : handleBodyFixedLength(intercept, clientIn, serverOut, cl);
                if (intercept) {
                    interceptor.onRequestIntercept(requestHeader, NetUtil.toByteArray(body));
                }
            }
        }
    }

    private static void handleResponse(Interceptor interceptor, HttpURLConnection connection, RequestHeader requestHeader, OutputStream clientOut) throws IOException {
        // Header
        ResponseHeader responseHeader = ResponseHeader.parse(connection);
        clientOut.write((requestHeader.version + " " + responseHeader.code + " " + responseHeader.msg + CRLF).getBytes());
        for (String key : responseHeader.getHeaderKeys()) {
            String value = responseHeader.getHeader(key);
            clientOut.write((key + ": " + value + CRLF).getBytes());
        }
        clientOut.write(CRLF.getBytes());
        boolean intercept = interceptor == null ? false : interceptor.interceptResponse(requestHeader, responseHeader);

        // Body
        boolean chunked = isChunked(responseHeader);
        int cl = getContentLength(responseHeader);

        if (chunked || cl > 0) {
            try (InputStream serverIn = responseHeader.code < 400 ? connection.getInputStream() : connection.getErrorStream()) {
                List<Byte> body = chunked
                        ? handleBodyChunked(intercept, serverIn, clientOut, false)
                        : handleBodyFixedLength(intercept, serverIn, clientOut, cl);
                if (intercept) {
                    interceptor.onResponseIntercept(requestHeader, responseHeader, NetUtil.toByteArray(body));
                }
            }
        }
    }

    private static boolean isChunked(HttpHeader header) {
        return header.containsHeader(TRANSFER_ENCODING) && "chunked".equals(header.getHeader(TRANSFER_ENCODING).toLowerCase());
    }

    private static int getContentLength(HttpHeader header) {
        return header.containsHeader(CONTENT_LENGTH) ? Integer.parseInt(header.getHeader(CONTENT_LENGTH)) : 0;
    }

    private static List<Byte> handleBodyChunked(boolean intercept, InputStream in, OutputStream out, boolean request) throws IOException {
        List<Byte> save = new ArrayList<>();
        int len;
        byte[] buf = new byte[Math.max(in.available(), 1)];
        while ((len = in.read(buf)) != -1) {
            out.write((Integer.toHexString(len) + CRLF).getBytes());
            for (int i = 0; i < len; i++) {
                if (intercept) {
                    save.add(buf[i]);
                }
                out.write(buf[i]);
            }
            out.write(CRLF.getBytes());
            out.flush();
            buf = new byte[Math.max(in.available(), 1)];
        }
        out.write(("0" + CRLF + CRLF).getBytes());
        out.flush();
        return save;
    }

    private static List<Byte> handleBodyFixedLength(boolean intercept, InputStream in, OutputStream out, int cl) throws IOException {
        List<Byte> save = new ArrayList<>();
        int len;
        byte[] buf = new byte[4096];
        while (0 < cl && (len = in.read(buf)) != -1) {
            for (int i = 0; i < len; i++) {
                if (intercept) {
                    save.add(buf[i]);
                }
                out.write(buf[i]);
            }
            out.flush();
            cl -= len;
        }
        out.flush();
        return save;
    }
}
