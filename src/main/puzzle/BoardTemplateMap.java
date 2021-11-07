package main.puzzle;

import java.util.List;
import main.data.Unit;
import main.util.DoubleKeyHashMap;

public class BoardTemplateMap {

    private final DoubleKeyHashMap<Unit, Integer, List<BoardTemplate>> data = new DoubleKeyHashMap<>();
    private final DoubleKeyHashMap<Unit, Integer, Shape.Type> minTypeMap = new DoubleKeyHashMap<>();

    public void put(Unit unit, int star, List<BoardTemplate> templates, Shape.Type minType) {
        if (!templates.isEmpty()) {
            data.put(unit, star, templates);
        }
        minTypeMap.put(unit, star, minType);
    }

    public List<BoardTemplate> get(Unit unit, int star) {
        if (containsKey(unit, star)) {
            return data.get(unit, star);
        }
        return null;
    }

    public Shape.Type getMinType(Unit unit, int star) {
        if (containsMinTypeKey(unit, star)) {
            return minTypeMap.get(unit, star);
        }
        return Shape.Type.NONE;
    }

    public boolean containsKey(Unit unit, int star) {
        return data.containsKey(unit, star);
    }

    private boolean containsMinTypeKey(Unit unit, int star) {
        return minTypeMap.containsKey(unit, star);
    }
}
