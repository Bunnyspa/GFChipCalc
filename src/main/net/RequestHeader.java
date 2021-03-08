package main.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Bunnyspa
 */
public class RequestHeader extends HttpHeader {

    public final String method, url, version;

    private RequestHeader(String method, String url, String version, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        this.version = version;
        updateHeaders(headers);
    }

    static RequestHeader parse(InputStream clientIn) throws IOException {
        String firstLine = NetUtil.readLine(clientIn);
        String[] firstLineSplit = firstLine.split(" ");
        String method = firstLineSplit[0];
        String url = firstLineSplit[1];
        String version = firstLineSplit[2];
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = NetUtil.readLine(clientIn)).isEmpty()) {
            String[] headerLineSplit = headerLine.split(":", 2);
            headers.put(headerLineSplit[0], headerLineSplit[1]);
        }
        return new RequestHeader(method, url, version, headers);
    }

    @Override
    public String toString() {
        return "{" + method + " " + url + " " + version + " : " + super.toString() + "}";
    }
}
