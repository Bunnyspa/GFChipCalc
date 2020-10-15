package main.http;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bunnyspa
 */
public class ProxyHandlerThread extends Thread {

    private static final String REGEX_HOST
            = "("
            + "\\.girlfrontline\\.co\\.kr" + "|"
            + "\\.ppgame\\.com" + "|"
            + "\\.txwy\\.tw" + "|"
            + "\\.sunborngame\\.com"
            + ")";

    private static final String FILTER_SIGN = ".*" + REGEX_HOST + ".*" + "\\/Index\\/"
            + "("
            + "getDigitalSkyNbUid" + "|"
            + "getUidTianxiaQueue" + "|"
            + "getUidEnMicaQueue"
            + ")";
    private static final String FILTER_INDEX = ".*" + REGEX_HOST + ".*" + "\\/Index\\/index";

    private enum SaveType {
        NONE, SIGN, INDEX;
    }

    private static final String CRLF = "\r\n";
    private static final int TIMEOUT = 10000;
    private static final Map<Integer, String> STATUS = new HashMap<Integer, String>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(404, "Not Found");
            put(504, "Gateway Timeout");
        }
    }; // </editor-fold>

    private final Proxy proxy;
    private final Socket cpSocket;
    private DataInputStream ctpIS;
    private final ProxyMessage pm;
    private SaveType saveType = SaveType.NONE;

    public ProxyHandlerThread(Proxy proxy, Socket cpSocket) {
        this.cpSocket = cpSocket;
        try {
            this.cpSocket.setSoTimeout(5000);
        } catch (IOException ex) {
        }
        this.proxy = proxy;
        pm = new ProxyMessage();
    }

    @Override
    public void run() {
        try {
            ctpIS = getDIS(cpSocket.getInputStream());
            try {
                readRequestHeader();
                try {
                    if ("CONNECT".equals(pm.reqMethod)) {
                        handle_connect();
                    } else {
                        handle_default();
                    }
                } catch (UnknownHostException | MalformedURLException ex) {
                    handleEx(404);
                } catch (SocketTimeoutException ex) {
                    handleEx(504);
                } catch (InterruptedException ex) {
                }
            } catch (SocketTimeoutException ex) {
            }
            if (ctpIS != null) {
                ctpIS.close();
            }
        } catch (Exception ex) {
        }
        if (saveType == SaveType.SIGN) {
            proxy.process_sign(pm);
        } else if (saveType == SaveType.INDEX) {
            proxy.process_index(pm);
        }
    }

    private void handle_connect() throws IOException, InterruptedException {
        // Extract the URL and port of remote
        String pieces[] = pm.reqUrl.split(":");
        String url = pieces[0];
        int port = Integer.valueOf(pieces[1]);

        // Get actual IP associated with this URL through DNS
        InetAddress address = InetAddress.getByName(url);

        // Open a socket to the remote server
        try (Socket spSocket = new Socket(address, port)) {
            try (DataOutputStream ptcOS = getDOS(cpSocket.getOutputStream())) {
                spSocket.setSoTimeout(TIMEOUT);
                // Send Connection established to the client
                String line = pm.reqVersion + " 200 Connection Established" + CRLF + CRLF;
                ptcOS.writeBytes(line);
                ptcOS.flush();

                Thread ctsConnectThread = new ProxyTunnel(cpSocket.getInputStream(), spSocket.getOutputStream());
                Thread stcConnectThread = new ProxyTunnel(spSocket.getInputStream(), cpSocket.getOutputStream());

                ctsConnectThread.start();
                stcConnectThread.start();

                ctsConnectThread.join();
                stcConnectThread.join();
            }
        }
    }

    private void handle_default() throws IOException {
        HttpURLConnection connection = getConnection();
        handleRequest(connection);
        handleResponse(connection);
    }

    private void readRequestHeader() throws IOException {
        String requestLine = readLine(ctpIS);
        String[] requestParts = requestLine.split(" ");
        pm.reqMethod = requestParts[0];
        pm.reqUrl = requestParts[1];
        pm.reqVersion = requestParts[2];
        // Header
        String line;
        while ((line = readLine(ctpIS)) != null && !line.isEmpty()) {
            String[] parts = line.split(":");
            pm.addReqHeader(parts[0].trim(), parts[1].trim());
        }
        // Save if filtered
        if (pm.reqUrl.matches(FILTER_SIGN)) {
            saveType = SaveType.SIGN;
        } else if (pm.reqUrl.matches(FILTER_INDEX)) {
            saveType = SaveType.INDEX;
        }
    }

    private static String readLine(DataInputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int i1, i2;
        while ((i1 = is.read()) != -1) {
            switch (i1) {
                case '\r':
                    switch (i2 = is.read()) {
                        case -1: // +a return
                            sb.append((char) i1);
                        case '\n': // - return
                            return sb.toString();
                        default: // +ab
                            sb.append((char) i1).append((char) i2);
                    }
                default:
                    sb.append((char) i1);
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    private void handleRequest(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(pm.reqMethod);
        // Header
        pm.getReqHeaders().forEach((key) -> {
            connection.setRequestProperty(key, pm.getReqHeader(key));
        });
        // Body
        boolean bodyTE = pm.containsReqHeader(Proxy.TRANSFER_ENCODING) && "chunked".equals(pm.getReqHeader(Proxy.TRANSFER_ENCODING).toLowerCase());
        boolean bodyCL = pm.containsReqHeader(Proxy.CONTENT_LENGTH);

        if (bodyTE || bodyCL) {
            connection.setDoOutput(true);
            try (DataOutputStream ptsOS = getDOS(connection.getOutputStream())) {
                if (bodyTE) {
                    handleBody_chunkedTransferEncoding(ctpIS, ptsOS, pm.reqBody, saveType != SaveType.NONE);
                } else if (bodyCL) {
                    handleBody_contentLength(ctpIS, ptsOS, pm.reqBody, saveType != SaveType.NONE, Integer.valueOf(pm.getReqHeader(Proxy.CONTENT_LENGTH)));
                }
            }
        }
    }

    private void handleResponse(HttpURLConnection connection) throws IOException {
        pm.resCode = connection.getResponseCode();
        pm.resMsg = connection.getResponseMessage();
        try (DataOutputStream ptcOS = getDOS(cpSocket.getOutputStream())) {
            // Header
            ptcOS.writeBytes(pm.reqVersion + " " + pm.resCode + " " + pm.resMsg + CRLF);
            Map<String, List<String>> resHeaderFields = connection.getHeaderFields();
            for (String key : resHeaderFields.keySet()) {
                List<String> resHeaderValues = resHeaderFields.get(key);
                if (key != null && !resHeaderValues.isEmpty()) {
                    String value = String.join(", ", resHeaderFields.get(key));
                    pm.addResHeader(key, value);
                    ptcOS.writeBytes(key + ": " + value + CRLF);
                }
            }
            ptcOS.writeBytes(CRLF);

            // Body
            boolean bodyTE = pm.containsResHeader(Proxy.TRANSFER_ENCODING) && "chunked".equals(pm.getResHeader(Proxy.TRANSFER_ENCODING).toLowerCase());
            boolean bodyCL = pm.containsResHeader(Proxy.CONTENT_LENGTH);

            if (bodyTE || bodyCL) {
                InputStream cis;
                try {
                    cis = connection.getInputStream();
                } catch (IOException ex) {
                    cis = connection.getErrorStream();
                }
                try (DataInputStream stpIS = getDIS(cis)) {
                    if (bodyTE) {
                        handleBody_chunkedTransferEncoding(stpIS, ptcOS, pm.resBody, saveType != SaveType.NONE);
                    } else if (bodyCL) {
                        handleBody_contentLength(stpIS, ptcOS, pm.resBody, saveType != SaveType.NONE, Integer.valueOf(pm.getResHeader(Proxy.CONTENT_LENGTH)));
                    }
                } catch (NullPointerException ex) {
                }
            }
        }
    }

    private void handleEx(int code) throws IOException {
        pm.resCode = code;
        pm.resMsg = STATUS.get(code);
        try (DataOutputStream dos = getDOS(cpSocket.getOutputStream())) {
            dos.writeBytes(pm.reqVersion + " " + pm.resCode + " " + pm.resMsg + CRLF + CRLF);
        }
    }

    private static void handleBody_chunkedTransferEncoding(DataInputStream is, DataOutputStream os, List<Byte> cache, boolean saveEnabled) throws IOException {
        int readLen;
        byte[] buffer = new byte[Math.max(is.available(), 1)];
        while ((readLen = is.read(buffer)) != -1) {
            os.writeBytes(Integer.toHexString(readLen) + CRLF);
            for (int i = 0; i < readLen; i++) {
                if (saveEnabled) {
                    cache.add(buffer[i]);
                }
                os.writeByte(buffer[i]);
            }
            os.writeBytes(CRLF);
            os.flush();
            buffer = new byte[Math.max(is.available(), 1)];
        }
        os.writeBytes("0" + CRLF + CRLF);
        os.flush();
    }

    private static void handleBody_contentLength(DataInputStream is, DataOutputStream os, List<Byte> cache, boolean saveEnabled, int cl) throws IOException {
        while (0 < cl) {
            byte[] buffer = new byte[cl];
            int readLen = is.read(buffer);
            for (int i = 0; i < readLen; i++) {
                if (saveEnabled) {
                    cache.add(buffer[i]);
                }
                os.writeByte(buffer[i]);
            }
            os.flush();
            cl -= readLen;
        }
        os.flush();
    }

    private HttpURLConnection getConnection() throws IOException {
        URL remoteURL = new URL(pm.reqUrl);
        HttpURLConnection connection = (HttpURLConnection) remoteURL.openConnection();
        connection.setReadTimeout(TIMEOUT);
        return connection;
    }

    private static DataInputStream getDIS(InputStream is) {
        return new DataInputStream(is);
    }

    private static DataOutputStream getDOS(OutputStream os) {
        return new DataOutputStream(os);
    }
}
