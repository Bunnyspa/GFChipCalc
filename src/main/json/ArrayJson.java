package main.json;

import java.util.ArrayList;
import java.util.List;

public class ArrayJson implements Json {

    private final List<Json> data;

    public ArrayJson(String data) {
        // Init
        this.data = new ArrayList<>();
        // Trim
        data = data.trim().replaceAll("^\\[|\\]$", "");

        int si = 0;
        while (si < data.length()) {
            int ei = Json.getEndIndex(data, si);
            String eStr = data.substring(si, ei + 1);
            Json element = Json.parse(eStr);

            this.data.add(element);

            int ci = data.indexOf(',', ei) + 1;
            if (ci == 0) {
                break;
            }
            si = ci;
        }
    }

    public List<Json> getList() {
        return new ArrayList<>(data);
    }

    @Override
    public int getType() {
        return Json.ARRAY;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < data.size(); i++) {
            Json d = data.get(i);
            sb.append(d.toString());
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
