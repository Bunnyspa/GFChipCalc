package main.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import main.json.JsonParser;

public class GFLInterceptor implements Interceptor {

    private static final String CONTENT_ENCODING = "Content-Encoding";

    private static final String FILTER = ".*(\\.girlfrontline\\.co\\.kr|\\.ppgame\\.com|\\.txwy\\.tw|\\.sunborngame\\.com).*\\/Index\\/(getDigitalSkyNbUid|getUidTianxiaQueue|getUidEnMicaQueue|index)";
    private static final String FILTER_INDEX = ".*(\\.girlfrontline\\.co\\.kr|\\.ppgame\\.com|\\.txwy\\.tw|\\.sunborngame\\.com).*\\/Index\\/index";

    private final Consumer<String> consumer;
    private String key = null;

    public GFLInterceptor(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public boolean interceptRequest(RequestHeader requestHeader) {
        return false;
    }

    @Override
    public boolean interceptResponse(RequestHeader requestHeader, ResponseHeader responseHeader) {
        return requestHeader.url.matches(FILTER);
    }

    @Override
    public void onRequestIntercept(RequestHeader requestHeader, byte[] request) {
    }

    @Override
    public void onResponseIntercept(RequestHeader requestHeader, ResponseHeader responseHeader, byte[] response) {
        String data = getBody(responseHeader, response);
        if (requestHeader.url.matches(FILTER_INDEX)) {
            if (key != null) {
                if (!data.contains("{")) {
                    try {
                        String decoded = decode(data, key);
                        data = decoded;
                    } catch (Exception ex) {
                    }
                }
                consumer.accept(data);
            }
        } else {
            try {
                key = JsonParser.parseSign(decode(data, AuthCode.SIGN_KEY));
            } catch (Exception ex) {
            }
        }
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

    private static String getBody(ResponseHeader header, byte[] bytes) {
        if (header.containsHeader(CONTENT_ENCODING) && "gzip".equals(header.getHeader(CONTENT_ENCODING).toLowerCase())) {
            try {
                return decode_gzip(bytes);
            } catch (IOException ex) {
            }
        }
        return new String(bytes);
    }

    private static String decode_gzip(byte[] array) throws IOException {
        GZIPInputStream gzipIS = new GZIPInputStream(new ByteArrayInputStream(array));
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int cl;
        while ((cl = gzipIS.read(buffer)) != -1) {
            byteOS.write(buffer, 0, cl);
        }
        return byteOS.toString();
    }
}
