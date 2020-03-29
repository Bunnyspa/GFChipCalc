package main.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

/**
 *
 * @author Bunnyspa
 */
public class ProxyTunnel extends Thread {

    private final InputStream is;
    private final OutputStream os;

    /**
     * Creates Object to Listen to Client and Transmit that data to the server
     *
     * @param is Stream that proxy uses to receive data from client
     * @param os Stream that proxy uses to transmit data to remote server
     */
    public ProxyTunnel(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            // Read byte by byte from client and send directly to server
            byte[] buffer = new byte[4096];
            int read;
            do {
                read = is.read(buffer);
                if (read > 0) {
                    os.write(buffer, 0, read);
                    if (is.available() < 1) {
                        os.flush();
                    }
                }
            } while (read >= 0);
        } catch (SocketTimeoutException ex) {
        } catch (IOException ex) {
        }
    }
}
