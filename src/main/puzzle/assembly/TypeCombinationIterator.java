package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import main.puzzle.Board;
import main.puzzle.Chip;

/**
 *
 * @author Bunnyspa
 */
public class TypeCombinationIterator implements Iterator<List<String>> {

    private List<List<String>> next;
    private final int total;

    public TypeCombinationIterator(Map<String, Integer> typeCountMap) {
        next = initComb(typeCountMap);
        total = total(typeCountMap);
    }

    public TypeCombinationIterator(List<String> progress) {
        next = toComb(progress);
        total = total(getTypeCount(progress));
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }

    @Override
    public List<String> next() {
        List<String> out = peek();
        next = nextComb(next);
        return out;
    }

    public List<String> peek() {
        List<String> out = new ArrayList<>();
        next.forEach((next_type) -> next_type.forEach((name) -> out.add(name)));
        return out;
    }

    public int total() {
        return total;
    }

    private static List<List<String>> nextComb(List<List<String>> comb) {
        int n = comb.size() - 1;
        while (0 <= n) {
            List<String> chips = nextComb_type(comb.get(n));
            if (chips.isEmpty()) {
                n--;
            } else {
                List<List<String>> out = new ArrayList<>(comb.size());
                for (int i = 0; i < n; i++) {
                    out.add(comb.get(i));
                }
                out.add(chips);
                for (int i = n + 1; i < comb.size(); i++) {
                    String chip = comb.get(i).get(0);
                    String type = Chip.getType(chip);
                    int length = comb.get(i).size();
                    out.add(initComb_type(type, length));
                }
                return out;
            }
        }
        return new ArrayList<>();
    }

    private static List<String> nextComb_type(List<String> chips) {
        int n = chips.size() - 1;
        while (0 <= n) {
            String chip = nextChip(chips.get(n));
            if (chip.isEmpty()) {
                n--;
            } else {
                List<String> out = new ArrayList<>(chips.size());
                for (int i = 0; i < n; i++) {
                    out.add(chips.get(i));
                }
                for (int i = n; i < chips.size(); i++) {
                    out.add(chip);
                }
                return out;
            }
        }
        return new ArrayList<>();
    }

    private static String nextChip(String chip) {
        List<String> chips = Arrays.asList(Chip.getNames(Chip.getType(chip)));
        int i = chips.indexOf(chip) + 1;
        if (chips.size() <= i) {
            return "";
        }
        return chips.get(i);
    }

    private static List<List<String>> initComb(Map<String, Integer> combType) {
        List<List<String>> out = new ArrayList<>(combType.keySet().size());
        combType.keySet().stream().sorted((o1, o2) -> Chip.compareType(o1, o2)).forEach((type) -> out.add(initComb_type(type, combType.get(type))));
        return out;
    }

    private static List<String> initComb_type(String type, int length) {
        String[] chips = Chip.getNames(type);
        String chip = chips[0];
        List<String> out = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            out.add(chip);
        }
        return out;
    }

    public static Map<String, Integer> getTypeCount(List<String> comb) {
        Map<String, Integer> combType = new HashMap<>();
        comb.forEach((chip) -> {
            String type = Chip.getType(chip);
            if (!combType.containsKey(type)) {
                combType.put(type, 0);
            }
            int count = combType.get(type);
            count++;
            combType.put(type, count);
        });
        return combType;
    }

    public static int total(Map<String, Integer> typeCountMap) {
        return typeCountMap.keySet().stream().map((type)
                -> nHr(Chip.getNames(type).length, typeCountMap.get(type))
        ).reduce(1, (a, b) -> a * b);
    }

    private static int nHr(int n, int r) {
        return nCr(n + r - 1, r);
    }

    private static int nCr(int n, int r) {
        r = Math.min(r, n - r);
        int num = 1;
        for (int i = n - r + 1; i <= n; i++) {
            num *= i;
        }
        for (int i = 1; i <= r; i++) {
            num /= i;
        }
        return num;
    }

    public static List<Map<String, Integer>> getTypeCountMaps(String name, int star, Set<String> types) {
        int nCell = Board.getCellCount(name, star);
        return getTypeCountMaps(nCell, types);
    }

    private static List<Map<String, Integer>> getTypeCountMaps(int nCell, Set<String> types) {
        List<Map<String, Integer>> out = new ArrayList<>();
        List<String> typesAvail = new ArrayList<>(types);
        typesAvail.sort((o1, o2) -> Chip.compareType(o1, o2));
        getTypeCountMaps_rec(out, typesAvail, nCell, new Stack<>());
        return out;
    }

    private static void getTypeCountMaps_rec(List<Map<String, Integer>> out, List<String> typesAvail, int nCell, Stack<String> typeBuffer) {
        if (nCell == 0) {
            Map<String, Integer> e = new HashMap<>();
            typeBuffer.forEach((s) -> {
                if (!e.containsKey(s)) {
                    e.put(s, 0);
                }
                e.put(s, e.get(s) + 1);
            });
            out.add(e);
            return;
        }
        int range = Math.min(Chip.SIZE_MAX, nCell);
        typesAvail.stream().filter((t) -> Chip.getSize(t) <= range).forEach((t) -> {
            typeBuffer.push(t);
            List<String> typeAvail_new = new ArrayList<>();
            typesAvail.stream().filter((ta) -> Chip.compareType(t, ta) <= 0).forEach((ta) -> typeAvail_new.add(ta));
            int nCell_new = nCell - Chip.getSize(t);
            getTypeCountMaps_rec(out, typeAvail_new, nCell_new, typeBuffer);
            typeBuffer.pop();
        });
    }

    private static List<List<String>> toComb(List<String> strs) {
        Map<String, List<String>> map = new HashMap<>();
        strs.stream().forEach((chip) -> {
            String type = Chip.getType(chip);
            if (!map.containsKey(type)) {
                map.put(type, new ArrayList<>());
            }
            map.get(type).add(chip);
        });

        List<List<String>> out = new ArrayList<>();
        for (String type : Chip.TYPES) {
            if (map.containsKey(type)) {
                out.add(map.get(type));
            }
        }
        return out;
    }
}
