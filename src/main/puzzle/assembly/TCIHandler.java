package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import main.puzzle.Chip;
import main.puzzle.Shape;

/**
 *
 * @author Bunnyspa
 */
public class TCIHandler {

    private final Map<Shape, Integer> chipNameCountMap;
    private final List<TypeCombinationIterator> iterators = new ArrayList<>();
    private final boolean limited;
    private int iteratorIndex = 0;

    public TCIHandler(String name, int star, List<Chip> chips) {
        Map<Shape.Type, Integer> typeCandidateCountMap = getTypeCount(chips.stream().map((c) -> c.getType()));
        chipNameCountMap = getShapeCount(chips.stream().map((c) -> c.getShape()));

        List<Map<Shape.Type, Integer>> typeCountMaps = new ArrayList<>();
        Set<Shape.Type> types = typeCandidateCountMap.keySet();
        TypeCombinationIterator.getTypeCountMaps(name, star, types).stream()
                .filter((typeCountMap) -> allTypeEnough(typeCountMap, typeCandidateCountMap))
                .forEach((typeCountMap) -> typeCountMaps.add(typeCountMap));

        typeCountMaps.forEach((map) -> iterators.add(new TypeCombinationIterator(map)));

        limited = true;
    }

    public TCIHandler(String name, int star, Set<Shape.Type> types) {
        chipNameCountMap = new HashMap<>();

        List<Map<Shape.Type, Integer>> typeCountMaps = TypeCombinationIterator.getTypeCountMaps(name, star, types);
        typeCountMaps.forEach((map) -> iterators.add(new TypeCombinationIterator(map)));

        limited = false;
    }

    private TypeCombinationIterator getIterator() {
        return iterators.get(iteratorIndex);
    }

    public int total() {
        return iterators.stream().mapToInt((it) -> it.total()).sum();
    }

    public void skip() {
        next();
    }

    public void skip(int progress) {
        for (int i = 0; i < progress; i++) {
            skip();
        }
    }

    public boolean hasNext() {
        if (iterators.isEmpty()) {
            return false;
        }
        if (iteratorIndex < iterators.size() - 1) {
            return true;
        }
        return getIterator().hasNext();
    }

    public List<Shape> next() {
        if (iterators.isEmpty()) {
            return new ArrayList<>();
        }
        List<Shape> next = getIterator().next();
        if (next.isEmpty() && hasNext()) {
            iteratorIndex++;
            next = getIterator().next();
        }
        return next;
    }

    public boolean isNextValid() {
        if (iterators.isEmpty()) {
            return false;
        }
        if (!limited) {
            return true;
        }

        List<Shape> next = getIterator().peek();
        if (next.isEmpty() && hasNext()) {
            next = iterators.get(iteratorIndex + 1).peek();
        }
        Map<Shape, Integer> nameCount = getShapeCount(next.stream());
        return allShapeEnough(nameCount, chipNameCountMap);
    }

    private static Map<Shape.Type, Integer> getTypeCount(Stream<Shape.Type> types) {
        Map<Shape.Type, Integer> out = new HashMap<>();
        types.forEach((s) -> {
            if (!out.containsKey(s)) {
                out.put(s, 0);
            }
            int nc = out.get(s);
            nc++;
            out.put(s, nc);
        });
        return out;
    }

    private static Map<Shape, Integer> getShapeCount(Stream<Shape> shapes) {
        Map<Shape, Integer> out = new HashMap<>();
        shapes.forEach((s) -> {
            if (!out.containsKey(s)) {
                out.put(s, 0);
            }
            int nc = out.get(s);
            nc++;
            out.put(s, nc);
        });
        return out;
    }

    private static boolean allTypeEnough(Map<Shape.Type, Integer> required, Map<Shape.Type, Integer> candidates) {
        return required.keySet().stream().allMatch((type)
                -> candidates.containsKey(type)
                && required.get(type) <= candidates.get(type)
        );
    }

    private static boolean allShapeEnough(Map<Shape, Integer> required, Map<Shape, Integer> candidates) {
        return required.keySet().stream().allMatch((type)
                -> candidates.containsKey(type)
                && required.get(type) <= candidates.get(type)
        );
    }
}
