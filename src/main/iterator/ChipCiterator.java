package main.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import main.puzzle.BoardTemplate;
import main.puzzle.Chip;
import main.puzzle.Shape;

public class ChipCiterator implements Iterator<List<Chip>> {

    private final Map<Shape, List<Chip>> candidateMap;
    private final List<Shape> shapes;
    private final List<int[]> combs;

    public ChipCiterator(Collection<Chip> candidates) {
        candidateMap = new HashMap<>();
        for (Chip c : candidates) {
            Shape shape = c.getShape();
            if (!candidateMap.containsKey(shape)) {
                candidateMap.put(shape, new ArrayList<>());
            }
            candidateMap.get(shape).add(c);
        }
        shapes = new ArrayList<>();
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
            List<Chip> candidates = candidateMap.get(shapes.get(i));
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

    public void init(BoardTemplate bt) {
        init(bt.getShapeCountMap());
    }

    public void init(Map<Shape, Integer> shapeCountMap) {
        shapes.clear();
        combs.clear();
        List<Shape> keys = new ArrayList<>(shapeCountMap.keySet());
        Collections.sort(keys, (o1, o2) -> Shape.compare(o1, o2));
        for (Shape shape : keys) {
            int count = shapeCountMap.get(shape);
            shapes.add(shape);
            combs.add(nCrInit(count));
        }
    }

    private int getCandidateSize(Shape shape) {
        if (!candidateMap.containsKey(shape)) {
            return 0;
        }
        return candidateMap.get(shape).size();
    }

    public boolean hasEnoughChips(BoardTemplate template) {
        Map<Shape, Integer> nameCountMap = template.getShapeCountMap();
        for (Shape shape : nameCountMap.keySet()) {
            if (nameCountMap.get(shape) > getCandidateSize(shape)) {
                return false;
            }
        }
        return true;
    }

    private int[] nextComb(int i) {
        return nCrNext(combs.get(i), getCandidateSize(shapes.get(i)));
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
