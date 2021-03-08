package main.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bunnyspa
 */
public class ResponseHeader extends HttpHeader {

    public final int code;
    public final String msg;

    private ResponseHeader(HttpURLConnection connection) throws IOException {
        code = connection.getResponseCode();
        msg = connection.getResponseMessage();
        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        for (String key : headerFields.keySet()) {
            List<String> resHeaderValues = headerFields.get(key);
            if (resHeaderValues != null && !resHeaderValues.isEmpty()) {
                String value = String.join(", ", resHeaderValues);
                headers.put(key, value);
            }
        }
        updateHeaders(headers);
    }

    static ResponseHeader parse(HttpURLConnection connection) throws IOException {
        return new ResponseHeader(connection);
    }

    @Override
    public String toString() {
        return "{" + code + " " + msg + " : " + super.toString() + "}";
    }
}
