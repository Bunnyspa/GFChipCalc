package main.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Bunnyspa
 * @param <KA>
 * @param <KB>
 * @param <V>
 */
public class DoubleKeyHashMap<KA, KB, V> {

    private final Map<KA, Map<KB, V>> data = new HashMap<>();

    public void put(KA s, KB i, V value) {
        init(s);
        data.get(s).put(i, value);
    }

    public void put(KA s, Map<KB, V> m) {
        init(s);
        data.get(s).putAll(m);
    }

    public V get(KA s, KB i) {
        if (containsKey(s, i)) {
            return data.get(s).get(i);
        }
        return null;
    }

    public boolean containsKey(KA s, KB i) {
        return data.containsKey(s) && data.get(s).containsKey(i);
    }

    public Set<KB> keySet(KA s) {
        if (data.containsKey(s)) {
            return data.get(s).keySet();
        }
        return null;
    }

    public int size(String s) {
        return data.get(s).size();
    }

    private void init(KA s) {
        if (!data.containsKey(s)) {
            data.put(s, new HashMap<>());
        }
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
