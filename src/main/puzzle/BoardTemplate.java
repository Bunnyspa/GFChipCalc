package main.puzzle;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import main.util.IO;
import main.util.Pair;

/**
 *
 * @author Bunnyspa
 */
public class BoardTemplate implements Comparable<BoardTemplate> {

    private final List<Puzzle> puzzles;
    private final Map<Shape, Integer> shapeCountMap;
    private final boolean symmetry;

    private final PuzzleMatrix<Integer> placement;

    public static final int END = 0;
    public static final int EMPTY = 1;
    public static final int NORMAL = 2;

    private final int state;

    private static BoardTemplate BOARD_EMPTY = new BoardTemplate(false);
    private static BoardTemplate BOARD_END = new BoardTemplate(true);

    private BoardTemplate(boolean isEnd) {
        puzzles = null;
        shapeCountMap = null;
        placement = null;
        symmetry = false;
        state = isEnd ? END : EMPTY;
    }

    public static BoardTemplate empty() {
        return BOARD_EMPTY;
    }

    public static BoardTemplate end() {
        return BOARD_END;
    }

    public BoardTemplate(String name, int star, List<Puzzle> puzzles) {
        this.puzzles = puzzles;
        shapeCountMap = new HashMap<>();

        // Placement
        placement = Board.toPlacement(name, star, puzzles);

        // Symmetry
        this.symmetry = placement.isSymmetric(Board.UNUSED);

        init();
        state = NORMAL;
    }

    public BoardTemplate(String name, int star, List<Puzzle> puzzles, boolean symmetry) {
        this.puzzles = puzzles;
        this.shapeCountMap = new HashMap<>();

        // Placement
        this.placement = Board.toPlacement(name, star, puzzles);

        // Symmetry
        this.symmetry = symmetry;

        init();
        state = NORMAL;
    }

    private void init() {
        for (Puzzle p : puzzles) {
            Shape shape = p.shape;
            if (!shapeCountMap.containsKey(shape)) {
                shapeCountMap.put(shape, 0);
            }
            shapeCountMap.put(shape, shapeCountMap.get(shape) + 1);
        }
    }

    private BoardTemplate(Map<Shape, Integer> shapeCountMap, PuzzleMatrix<Integer> placement, List<Puzzle> puzzles, boolean symmetry) {
        this.puzzles = puzzles;
        this.shapeCountMap = new HashMap<>(shapeCountMap);

        // Placement
        this.placement = placement;

        // Symmetry
        this.symmetry = symmetry;

        state = NORMAL;
    }

    public int getNumRotationNeeded(int rotation, List<Chip> chips) {
        Map<Pair<Shape, Integer>, Integer> shape_rotation_count = new HashMap<>();
        for (int i = 0; i < puzzles.size(); i++) {
            Puzzle puzzle = puzzles.get(i);
            Shape s = puzzle.shape;
            int r = (puzzle.rotation + rotation) % s.getMaxRotation();
            Pair<Shape, Integer> key = new Pair<>(s, r);
            if (shape_rotation_count.containsKey(key)) {
                shape_rotation_count.put(key, shape_rotation_count.get(key) + 1);
            } else {
                shape_rotation_count.put(key, 1);
            }
        }

        for (Chip chip : chips) {
            Pair<Shape, Integer> key = new Pair<>(chip.getShape(), chip.getInitRotation());
            if (shape_rotation_count.containsKey(key)) {
                if (shape_rotation_count.get(key) == 1) {
                    shape_rotation_count.remove(key);
                } else {
                    shape_rotation_count.put(key, shape_rotation_count.get(key) - 1);
                }
            }
        }

        int count = 0;
        for (Pair<Shape, Integer> key : shape_rotation_count.keySet()) {
            count += shape_rotation_count.get(key);
        }
        return count;
    }

    public BoardTemplate getRotatedTemplate(int rotation) {
        rotation = rotation % 4;
        if (rotation == 0) {
            return this;
        }
        PuzzleMatrix<Integer> matrix = new PuzzleMatrix<>(placement);
        matrix.rotate(rotation);

        List<Puzzle> newPuzzles = new ArrayList<>();
        for (int i = 0; i < puzzles.size(); i++) {
            Puzzle puzzle = puzzles.get(i);
            Shape s = puzzle.shape;
            int r = (puzzle.rotation + rotation) % s.getMaxRotation();
            Point l = matrix.getPivot(i);
            newPuzzles.add(new Puzzle(s, r, l));
        }
        return new BoardTemplate(shapeCountMap, matrix, newPuzzles, symmetry);
    }

    public List<Puzzle> getPuzzles() {
        return puzzles;
    }

    public PuzzleMatrix<Integer> getMatrix() {
        return new PuzzleMatrix<>(placement);
    }

    public boolean isEnd() {
        return state == END;
    }

    public boolean isEmpty() {
        return state == EMPTY;
    }

    public boolean isSymmetric() {
        return symmetry;
    }

    public boolean calcSymmetry() {
        return placement.isSymmetric(Board.UNUSED);
    }

    public Map<Shape, Integer> getShapeCountMap() {
        return shapeCountMap;
    }

    public List<Integer> getChipRotations() {
        List<Integer> list = new ArrayList<>();
        for (Puzzle p : puzzles) {
            Integer rotation = p.rotation;
            list.add(rotation);
        }
        return list;
    }

    public List<Point> getChipLocations() {
        List<Point> list = new ArrayList<>();
        for (Puzzle p : puzzles) {
            Point location = p.location;
            list.add(location);
        }
        return list;
    }

    public String toData() {
        StringBuilder sb = new StringBuilder();
        // Names
        sb.append(puzzles.stream().map(p -> String.valueOf(p.shape.id)).collect(Collectors.joining(",")));
        sb.append(";");

        // Rotations
        sb.append(puzzles.stream().map(p -> String.valueOf(p.rotation)).collect(Collectors.joining(",")));
        sb.append(";");

        // Locations
        sb.append(puzzles.stream().map(p -> IO.data(p.location)).collect(Collectors.joining(",")));
        sb.append(";");

        // Symmetry
        sb.append(IO.data(calcSymmetry()));

        return sb.toString();
    }

    public void sortPuzzle() {
        Collections.sort(puzzles);
    }

    @Override
    public int compareTo(BoardTemplate o) {
        for (int i = 0; i < Math.min(puzzles.size(), o.puzzles.size()); i++) {
            int nameC = Shape.compare(puzzles.get(i).shape, o.puzzles.get(i).shape);
            if (nameC != 0) {
                return nameC;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        if (state == EMPTY) {
            return "EMPTY";
        }
        if (state == END) {
            return "end";
        }
        return puzzles.toString();
    }
}
