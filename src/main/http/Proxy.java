package main.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.swing.SwingUtilities;
import main.json.JsonParser;
import main.ui.dialog.ProxyDialog;

/**
 *
 * @author Bunnyspa
 */
public class Proxy {

    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static final int PORT = 8080;

    private final ProxyDialog dialog;
    private final ServerSocket serverSocket = new ServerSocket(PORT);

    private final List<Thread> threads = new ArrayList<>();
    private boolean isRunning = true;
    private boolean keyReceived = false;
    private String key;

    private final Thread mainThread = new Thread(() -> {
        while (isRunning) {
            try {
                Socket socket = serverSocket.accept();
                ProxyHandlerThread thread = new ProxyHandlerThread(this, socket);
                threads.add(thread);
                thread.start();
            } catch (IOException ex) {
            }
        }
    });

    public Proxy(ProxyDialog dialog) throws IOException {
        this.dialog = dialog;
    }

    public String getAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface i = interfaces.nextElement();
                for (Enumeration<InetAddress> addresses = i.getInetAddresses(); addresses.hasMoreElements();) {
                    InetAddress a = addresses.nextElement();
                    if (!a.isLoopbackAddress() && !a.isLinkLocalAddress() && a.isSiteLocalAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return "";
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void start() {
        isRunning = true;
        mainThread.start();
    }

    public void process_sign(ProxyMessage pm) {
        String data = getBody(pm);
        try {
            key = JsonParser.parseSign(decode(data, AuthCode.SIGN_KEY));
            keyReceived = true;
        } catch (Exception ex) {
        }
    }

    public synchronized void process_index(ProxyMessage pm) {
        String data = getBody(pm);

        if (!data.contains("{")) {
            try {
                while (!keyReceived) {
                    wait();
                }
                String decoded = decode(data, key);
                data = decoded;
            } catch (Exception ex) {
            }
        }
        parse(data);
    }

    private String getBody(ProxyMessage pm) {
        List<Byte> byteList = pm.resBody;
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byte b = byteList.get(i);
            byteArray[i] = b;
        }
        if (pm.containsResHeader(CONTENT_ENCODING) && "gzip".equals(pm.getResHeader(CONTENT_ENCODING).toLowerCase())) {
            try {
                return decode_gzip(byteArray);
            } catch (IOException ex) {
            }
        }
        return new String(byteArray);
    }

    private static String decode(String text, String key) {
        try {
            if (text == null || text.isEmpty()) {
                return "";
            }
            if (text.length() <= 1) {
                return text;
            }
            if (text.startsWith("#")) {
                return decode_gzip(AuthCode.decodeWithGzip(text.substring(1), key));
            }
            return AuthCode.decode(text, key);
        } catch (Exception ex) {
        }
        return "";
    }

    public static String decode_gzip(byte[] array) throws IOException {
        GZIPInputStream gzipIS = new GZIPInputStream(new ByteArrayInputStream(array));
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int cl;
        while ((cl = gzipIS.read(buffer)) != -1) {
            byteOS.write(buffer, 0, cl);
        }
        return byteOS.toString();
    }

    private void parse(String s) {
        SwingUtilities.invokeLater(() -> dialog.parse(s));
    }

    public void stop() {
        isRunning = false;
        threads.stream()
                .filter((t) -> (t.isAlive()))
                .forEach((t) -> t.interrupt());
        threads.clear();
        try {
            serverSocket.close();
        } catch (IOException ex) {
        }
        try {
            mainThread.join();
        } catch (InterruptedException ex) {
        }
    }
}
