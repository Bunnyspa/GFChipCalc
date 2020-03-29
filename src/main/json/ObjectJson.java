package main.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bunnyspa
 */
public class ObjectJson implements Json {

    private final List<String> keys;
    private final Map<String, Json> data;

    public ObjectJson(String data) {
        // Init
        this.keys = new ArrayList<>();
        this.data = new HashMap<>();
        // Trim
        data = data.trim().replaceAll("^\\{|\\}$", "");

        int si = 0;
        while (si < data.length()) {
            int ei = Json.getEndIndex(data, si);
            String key = data.substring(si, ei + 1).trim().replaceAll("^\"|\"$", "");

            int vsi = data.indexOf(':', ei) + 1;
            int vei = Json.getEndIndex(data, vsi);
            String vStr = data.substring(vsi, vei + 1);
            Json value = Json.parse(vStr);

            this.keys.add(key);
            this.data.put(key, value);

            int ci = data.indexOf(',', vei) + 1;
            if (ci == 0) {
                break;
            }
            si = ci;
        }
    }

    
    
    public List<String> getKeys() {
        return new ArrayList<>(keys);
    }
    
    public boolean isEmpty(){
        return getKeys().isEmpty();
    }
    
    public boolean containsKey(String key){
        return keys.contains(key);
    }

    public Json getValue(String s) {
        if (keys.contains(s)) {
            return data.get(s);
        }
        return null;
    }

    public void set(String key, Json j) {
        if (!keys.contains(key)) {
            keys.add(key);
        }
        data.put(key, j);
    }

    @Override
    public int getType() {
        return Json.OBJECT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Json d = data.get(key);
            sb.append("\"").append(key).append("\":").append(d.toString());
            if (i < keys.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
