package main.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class HttpHeader {

    private Map<String, String> headerMap;
    private Set<String> headerKeys;

    protected final void updateHeaders(Map<String, String> headers) {
        this.headerMap = new HashMap<>();
        this.headerKeys = new HashSet<>();
        for (String key : headers.keySet()) {
            if (key != null) {
                this.headerMap.put(key.toLowerCase(), headers.get(key).trim());
                this.headerKeys.add(key);
            }
        }
    }

    boolean containsHeader(String key) {
        return headerMap.containsKey(key.toLowerCase());
    }

    String getHeader(String key) {
        return headerMap.get(key.toLowerCase());
    }

    Set<String> getHeaderKeys() {
        return headerKeys;
    }

    @Override
    public String toString() {
        return headerMap.toString();
    }
}
