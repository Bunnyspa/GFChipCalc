package main.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NetUtil {

    private static final char CR = '\r';
    private static final char LF = '\n';

    static String readLine(InputStream in) throws IOException {
        List<Byte> bytes = new ArrayList<>();
        int prev = -1;
        int current;
        while ((current = in.read()) != -1) {
            if (prev == CR && current == LF) {
                return new String(toByteArray(bytes));
            } else if (prev != -1) {
                bytes.add((byte) prev);
            }
            prev = current;
        }
        if (prev != -1) {
            bytes.add((byte) prev);
        }
        return new String(toByteArray(bytes));
    }

    static byte[] toByteArray(List<Byte> bytes) {
        byte[] out = new byte[bytes.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = bytes.get(i);
        }
        return out;
    }
}
