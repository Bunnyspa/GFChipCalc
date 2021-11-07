package main.net;

import java.io.InputStream;
import java.io.OutputStream;

public class Tunnel extends Thread {

    private final InputStream in;
    private final OutputStream out;

    public Tunnel(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            int len;
            byte[] buf = new byte[4096];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (Exception ex) {
        }
    }
}
