package main.puzzle.assembly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import main.puzzle.Chip;
import main.puzzle.preset.PuzzlePreset;

/**
 *
 * @author Bunnyspa
 */
public class ChipCombinationIterator implements Iterator<List<Chip>> {

    private final Map<String, List<Chip>> candidateMap;
    private final List<String> names;
    private final List<int[]> combs;

    public ChipCombinationIterator(Collection<Chip> candidates) {
        candidateMap = new HashMap<>();
        candidates.forEach((c) -> {
            String name = c.getName();
            if (!candidateMap.containsKey(name)) {
                candidateMap.put(name, new ArrayList<>());
            }
            candidateMap.get(name).add(c);
        });
        names = new ArrayList<>();
        combs = new ArrayList<>();
    }

    @Override
    public boolean hasNext() {
        return combs.isEmpty() || combs.get(combs.size() - 1) != null;
    }

    @Override
    public List<Chip> next() {
        // Generate output
        List<Chip> out = new ArrayList<>();
        for (int i = 0; i < combs.size(); i++) {
            int[] comb = combs.get(i);
            List<Chip> candidates = candidateMap.get(names.get(i));
            for (int index : comb) {
                out.add(candidates.get(index));
            }
        }
        // Generate next
        for (int i = 0; i < combs.size(); i++) {
            int[] comb = nextComb(i);
            if (comb == null) {
                combs.set(i, (i < combs.size() - 1) ? nCrInit(combs.get(i).length) : null);
            } else {
                combs.set(i, comb);
                break;
            }
        }
        return out;
    }

    public void init(PuzzlePreset preset) {
        init(preset.getNameCountMap());
    }

    public void init(Map<String, Integer> nameCountMap) {
        names.clear();
        combs.clear();
        List<String> keys = new ArrayList<>(nameCountMap.keySet());
        keys.sort(Chip.getComparator());
        keys.forEach((name) -> {
            int count = nameCountMap.get(name);
            names.add(name);
            combs.add(nCrInit(count));
        });
    }

    private int getSize(String name) {
        if (!candidateMap.containsKey(name)) {
            return 0;
        }
        return candidateMap.get(name).size();
    }

    public boolean hasEnoughChips(PuzzlePreset preset) {
        Map<String, Integer> nameCountMap = preset.getNameCountMap();
        return nameCountMap.keySet().stream()
                .allMatch((name) -> (nameCountMap.get(name) <= getSize(name)));
    }

    private int[] nextComb(int i) {
        return nCrNext(combs.get(i), getSize(names.get(i)));
    }

    private static int[] nCrInit(int n) {
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = i;
        }
        return out;
    }

    private static int[] nCrNext(int[] l, int max) {
        int currentMax = max - 1;
        int index = l.length - 1;
        while (-1 < index) {
            int nextNum = l[index] + 1;
            if (nextNum > currentMax) {
                currentMax--;
                index--;
            } else {
                int[] out = new int[l.length];
                System.arraycopy(l, 0, out, 0, index);
                for (int i = index; i < l.length; i++) {
                    out[i] = nextNum + i - index;
                }
                return out;
            }
        }
        return null;
    }
}
