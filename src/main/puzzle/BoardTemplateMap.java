package main.puzzle;

import java.util.List;
import main.util.DoubleKeyHashMap;

/**
 *
 * @author Bunnyspa
 */
public class BoardTemplateMap {

    private final DoubleKeyHashMap<String, Integer, List<BoardTemplate>> data = new DoubleKeyHashMap<>();
    private final DoubleKeyHashMap<String, Integer, Shape.Type> minTypeMap = new DoubleKeyHashMap<>();

    public void put(String name, int star, List<BoardTemplate> templates, Shape.Type minType) {
        if (!templates.isEmpty()) {
            data.put(name, star, templates);
        }
        minTypeMap.put(name, star, minType);
    }

    public List<BoardTemplate> get(String name, int star) {
        if (containsKey(name, star)) {
            return data.get(name, star);
        }
        return null;
    }

    public Shape.Type getMinType(String name, int star) {
        if (containsMinTypeKey(name, star)) {
            return minTypeMap.get(name, star);
        }
        return Shape.Type.NONE;
    }

    public boolean containsKey(String name, int star) {
        return data.containsKey(name, star);
    }

    private boolean containsMinTypeKey(String name, int star) {
        return minTypeMap.containsKey(name, star);
    }
}
