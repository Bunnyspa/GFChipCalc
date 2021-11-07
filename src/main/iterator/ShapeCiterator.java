package main.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.data.Unit;
import main.puzzle.Chip;
import main.puzzle.Shape;

public class ShapeCiterator implements Iterator<List<Shape>> {

    private final Map<Shape, Integer> chipNameCountMap;
    private final List<PerTypeShapeCiterator> cits = new ArrayList<>();
    private final boolean limited;
    private int iteratorIndex = 0;

    public ShapeCiterator(Unit unit, int star, List<Chip> chips) {
        List<Shape.Type> chipTypes = new ArrayList<>();
        List<Shape> chipShapes = new ArrayList<>();
        for (Chip c : chips) {
            chipTypes.add(c.getType());
            chipShapes.add(c.getShape());
        }
        Map<Shape.Type, Integer> typeCandidateCountMap = getTypeCount(chipTypes);
        chipNameCountMap = getShapeCount(chipShapes);

        List<Map<Shape.Type, Integer>> typeCountMaps = new ArrayList<>();
        Set<Shape.Type> types = typeCandidateCountMap.keySet();
        for (Map<Shape.Type, Integer> typeCountMap : PerTypeShapeCiterator.getTypeCountMaps(unit, star, types)) {
            if (allTypeEnough(typeCountMap, typeCandidateCountMap)) {
                typeCountMaps.add(typeCountMap);
            }
        }

        for (Map<Shape.Type, Integer> map : typeCountMaps) {
            cits.add(new PerTypeShapeCiterator(map));
        }

        limited = true;
    }

    public ShapeCiterator(Unit unit, int star, Set<Shape.Type> types) {
        chipNameCountMap = new HashMap<>();

        List<Map<Shape.Type, Integer>> typeCountMaps = PerTypeShapeCiterator.getTypeCountMaps(unit, star, types);
        for (Map<Shape.Type, Integer> map : typeCountMaps) {
            cits.add(new PerTypeShapeCiterator(map));
        }

        limited = false;
    }

    private PerTypeShapeCiterator getCiterator() {
        return cits.get(iteratorIndex);
    }

    public int total() {
        int sum = 0;
        for (PerTypeShapeCiterator cit : cits) {
            int total = cit.total();
            sum += total;
        }
        return sum;
    }

    public void skip() {
        next();
    }

    public void skip(int progress) {
        for (int i = 0; i < progress; i++) {
            skip();
        }
    }

    @Override
    public boolean hasNext() {
        if (cits.isEmpty()) {
            return false;
        }
        if (iteratorIndex < cits.size() - 1) {
            return true;
        }
        return getCiterator().hasNext();
    }

    @Override
    public List<Shape> next() {
        if (cits.isEmpty()) {
            return new ArrayList<>();
        }
        List<Shape> next = getCiterator().next();
        if (next.isEmpty() && hasNext()) {
            iteratorIndex++;
            next = getCiterator().next();
        }
        return next;
    }

    public boolean isNextValid() {
        if (cits.isEmpty()) {
            return false;
        }
        if (!limited) {
            return true;
        }

        List<Shape> next = getCiterator().peek();
        if (next.isEmpty() && hasNext()) {
            next = cits.get(iteratorIndex + 1).peek();
        }
        Map<Shape, Integer> nameCount = getShapeCount(next);
        return allShapeEnough(nameCount, chipNameCountMap);
    }

    private static Map<Shape.Type, Integer> getTypeCount(Collection<Shape.Type> types) {
        Map<Shape.Type, Integer> out = new HashMap<>();
        for (Shape.Type t : types) {
            if (!out.containsKey(t)) {
                out.put(t, 0);
            }
            int nc = out.get(t);
            nc++;
            out.put(t, nc);
        }
        return out;
    }

    private static Map<Shape, Integer> getShapeCount(Collection<Shape> shapes) {
        Map<Shape, Integer> out = new HashMap<>();
        for (Shape s : shapes) {
            if (!out.containsKey(s)) {
                out.put(s, 0);
            }
            int nc = out.get(s);
            nc++;
            out.put(s, nc);
        }
        return out;
    }

    private static boolean allTypeEnough(Map<Shape.Type, Integer> required, Map<Shape.Type, Integer> candidates) {
        for (Shape.Type type : required.keySet()) {
            if (!candidates.containsKey(type) || required.get(type) > candidates.get(type)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allShapeEnough(Map<Shape, Integer> required, Map<Shape, Integer> candidates) {
        for (Shape type : required.keySet()) {
            if (!candidates.containsKey(type) || required.get(type) > candidates.get(type)) {
                return false;
            }
        }
        return true;
    }
}
