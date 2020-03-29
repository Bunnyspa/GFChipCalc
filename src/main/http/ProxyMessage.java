package main.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Bunnyspa
 */
public class ProxyMessage {

    public String reqMethod, reqUrl, reqVersion;
    private final Map<String, String> reqHeader;
    public List<Byte> reqBody;

    public int resCode;
    public String resMsg;
    private final Map<String, String> resHeader;
    public List<Byte> resBody;

    public ProxyMessage() {
        reqHeader = new HashMap<>();
        resHeader = new HashMap<>();
        reqBody = new ArrayList<>();
        resBody = new ArrayList<>();
    }

    public ProxyMessage(
            String reqMethod, String reqUrl, String reqVersion,
            Map<String, String> reqHeader,
            int resCode, String resMsg,
            Map<String, String> resHeader
    ) {
        this.reqMethod = reqMethod;
        this.reqUrl = reqUrl;
        this.reqVersion = reqVersion;
        this.reqHeader = new HashMap<>(reqHeader);
        this.reqBody = new ArrayList<>();
        this.resCode = resCode;
        this.resMsg = resMsg;
        this.resHeader = new HashMap<>(resHeader);
        this.resBody = new ArrayList<>();
    }

    public Set<String> getReqHeaders() {
        return getHeaders(reqHeader);
    }

    public Set<String> getResHeaders() {
        return getHeaders(resHeader);
    }

    public void addReqHeader(String header, String field) {
        addHeader(header, field, reqHeader);
    }

    public void addResHeader(String header, String field) {
        addHeader(header, field, resHeader);
    }

    public boolean containsReqHeader(String header) {
        return containsHeader(header, reqHeader);
    }

    public boolean containsResHeader(String header) {
        return containsHeader(header, resHeader);
    }

    public String getReqHeader(String header) {
        return getHeader(header, reqHeader);
    }

    public String getResHeader(String header) {
        return getHeader(header, resHeader);
    }

    private static Set<String> getHeaders(Map<String, String> headerMap) {
        return headerMap.keySet();
    }

    private static void addHeader(String header, String field, Map<String, String> headerMap) {
        headerMap.put(header, field);
    }

    private static boolean containsHeader(String header, Map<String, String> headerMap) {
        return headerMap.keySet().stream().map((key) -> key.toLowerCase()).anyMatch((key) -> key.equals(header.toLowerCase()));
    }

    private static String getHeader(String header, Map<String, String> headerMap) {
        for (String key : headerMap.keySet()) {
            if (key.toLowerCase().equals(header.toLowerCase())) {
                return headerMap.get(key);
            }
        }
        return null;
    }

    private void fixForConnect() {
        if ("CONNECT".equals(reqMethod) && resCode == 0) {
            resCode = 200;
            resMsg = "Connection Established";
        }
    }

    public String getRequestHeader() {
        String out = reqMethod + " " + reqUrl + " " + reqVersion + System.lineSeparator();
        List<String> headers = new ArrayList<>();
        reqHeader.keySet().stream().map((key) -> key + ": " + reqHeader.get(key)).forEach((s) -> headers.add(s));
        Collections.sort(headers);
        out += String.join(System.lineSeparator(), headers);
        return out;
    }

    public String getRepsonseHeader() {
        fixForConnect();
        String out = reqVersion + " " + resCode + " " + resMsg + System.lineSeparator();
        List<String> headers = new ArrayList<>();
        resHeader.keySet().stream().map((key) -> key + ": " + resHeader.get(key)).forEach((s) -> headers.add(s));
        Collections.sort(headers);
        out += String.join(System.lineSeparator(), headers);
        return out;
    }

    public String getRequest() {
        String out = getRequestHeader();
        out += System.lineSeparator() + System.lineSeparator();
        if (reqBody.isEmpty()) {
            out += "(No Body)";
        } else {
            out += "Body Length: " + reqBody.size() + System.lineSeparator();
            out += getBodyHex(reqBody);
        }
        return out;
    }

    public String getRepsonse() {
        String out = getRepsonseHeader();
        out += System.lineSeparator() + System.lineSeparator();
        if (resBody.isEmpty()) {
            out += "(No Body)";
        } else {
            out += "Body Length: " + resBody.size() + System.lineSeparator();
            out += getBodyHex(resBody);
        }
        return out;
    }

    public static String byteToHex(byte b) {
        int v = b & 0xFF;
        return Integer.toHexString(v);
    }

    private static String getBodyHex(List<Byte> byteList) {
        StringBuilder sb = new StringBuilder();
        byteList.forEach((b) -> sb.append(byteToHex(b)));
        return sb.toString();
    }

    private static String getBodyString(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return new String(byteArray);
    }

    public String toData() {
        String out = getRequest() + System.lineSeparator();
        out += "-----" + System.lineSeparator();
        out += getRepsonse();
        return out.trim();
    }
}
