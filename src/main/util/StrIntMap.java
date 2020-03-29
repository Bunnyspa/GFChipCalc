package main.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Bunnyspa
 * @param <V>
 */
public class StrIntMap<V> {

    private final Map<String, Map<Integer, V>> data = new HashMap<>();

    public void put(String s, int i, V value) {
        init(s);
        data.get(s).put(i, value);
    }

    public void put(String s, Map<Integer, V> m) {
        init(s);
        data.get(s).putAll(m);
    }

    public V get(String s, int i) {
        if (containsKey(s, i)) {
            return data.get(s).get(i);
        }
        return null;
    }

    public boolean containsKey(String s, int i) {
        return data.containsKey(s) && data.get(s).containsKey(i);
    }

    public Set<Integer> keySet(String s) {
        if (data.containsKey(s)) {
            return data.get(s).keySet();
        }
        return null;
    }

    public int size(String s) {
        return data.get(s).size();
    }

    private void init(String s) {
        if (!data.containsKey(s)) {
            data.put(s, new HashMap<>());
        }
    }
}
