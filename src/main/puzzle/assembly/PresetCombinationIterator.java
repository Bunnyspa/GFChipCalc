package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import main.puzzle.Chip;

/**
 *
 * @author Bunnyspa
 */
public class PresetCombinationIterator {

    private final Map<String, Integer> chipNameCountMap;
    private final List<TypeCombinationIterator> iterators = new ArrayList<>();
    private final boolean limited;
    private int iteratorIndex = 0;

    public PresetCombinationIterator(String name, int star, List<Chip> chips) {
        Map<String, Integer> typeCandidateCountMap = getStringCount(chips.stream().map((c) -> c.getType()));
        chipNameCountMap = getStringCount(chips.stream().map((c) -> c.getName()));

        List<Map<String, Integer>> typeCountMaps = new ArrayList<>();
        Set<String> types = typeCandidateCountMap.keySet();
        TypeCombinationIterator.getTypeCountMaps(name, star, types).stream()
                .filter((typeCountMap) -> allEnough(typeCountMap, typeCandidateCountMap))
                .forEach((typeCountMap) -> typeCountMaps.add(typeCountMap));

        typeCountMaps.forEach((m) -> iterators.add(new TypeCombinationIterator(m)));

        limited = true;
    }

    public PresetCombinationIterator(String name, int star, Set<String> types) {
        chipNameCountMap = new HashMap<>();

        List<Map<String, Integer>> typeCountMaps = TypeCombinationIterator.getTypeCountMaps(name, star, types);
        typeCountMaps.forEach((m) -> iterators.add(new TypeCombinationIterator(m)));

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

    public List<String> next() {
        if (iterators.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> next = getIterator().next();
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

        List<String> next = getIterator().peek();
        if (next.isEmpty() && hasNext()) {
            next = iterators.get(iteratorIndex + 1).peek();
        }
        Map<String, Integer> nameCount = getStringCount(next.stream());
        return allEnough(nameCount, chipNameCountMap);
    }

    private static Map<String, Integer> getStringCount(Stream<String> strs) {
        Map<String, Integer> out = new HashMap<>();
        strs.forEach((s) -> {
            if (!out.containsKey(s)) {
                out.put(s, 0);
            }
            int nc = out.get(s);
            nc++;
            out.put(s, nc);
        });
        return out;
    }

    private static boolean allEnough(Map<String, Integer> required, Map<String, Integer> candidates) {
        return required.keySet().stream().allMatch((type)
                -> candidates.containsKey(type)
                && required.get(type) <= candidates.get(type)
        );
    }
}
