package main.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Proxy extends Thread {

    public static final int PORT = 8080;

    private final ServerSocket serverSocket;
    private final Interceptor interceptor;

    public Proxy(Interceptor interceptor) throws IOException {
        this.serverSocket = getServerSocket();
        this.interceptor = interceptor;
    }

    @Override
    public void run() {
        try {
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                new Session(socket, interceptor).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface i = interfaces.nextElement();
                for (Enumeration<InetAddress> addresses = i.getInetAddresses(); addresses.hasMoreElements();) {
                    InetAddress a = addresses.nextElement();
                    if (a.isSiteLocalAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (SocketException | UnknownHostException ex) {
        }
        return "";
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private ServerSocket getServerSocket() throws IOException {
        try {
            return new ServerSocket(PORT);
        } catch (IOException ex) {
            return new ServerSocket(0);
        }
    }
}
