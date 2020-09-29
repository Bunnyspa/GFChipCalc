package main.puzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import main.App;
import main.util.IO;

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
        shapeCountMap = new HashMap<>();

        // Placement
        placement = Board.toPlacement(name, star, puzzles);

        // Symmetry
        this.symmetry = symmetry;

        init();
        state = NORMAL;
    }

    private void init() {
        puzzles.stream().map(p -> p.shape).forEach(shape -> {
            if (!shapeCountMap.containsKey(shape)) {
                shapeCountMap.put(shape, 0);
            }
            shapeCountMap.put(shape, shapeCountMap.get(shape) + 1);
        });
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
        return puzzles.stream().map(p -> p.rotation).collect(Collectors.toList());
    }

    public List<Point> getChipLocations() {
        return puzzles.stream().map(p -> p.location).collect(Collectors.toList());
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

    public ImageIcon getImage(App app, int size) {
        return new ImageIcon(generateImage(app, size, placement));
    }

    private static BufferedImage generateImage(App app, int size, PuzzleMatrix<Integer> status) {
        int tileSize = size / 8;
        int h = Board.HEIGHT;
        int w = Board.WIDTH;
        BufferedImage i = new BufferedImage(
                h * tileSize + 1,
                w * tileSize + 1,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int s = status.get(row, col);
                int x = col * tileSize;
                int y = row * tileSize;

                // Tiles
                g.setColor(s == Board.UNUSED ? Color.BLACK : s == Board.EMPTY ? Color.WHITE : app.colors()[s % app.colors().length]);
                g.fillRect(x, y, tileSize, tileSize);

                // Horizontal Border
                g.setColor(Color.BLACK);
                if (0 < row && status.get(row - 1, col) != s) {
                    g.drawLine(x, y, x + tileSize, y);
                }
                // Vertical Border
                if (0 < col && status.get(row, col - 1) != s) {
                    g.drawLine(x, y, x, y + tileSize);
                }
            }
        }

        // Border
        g.setColor(Color.BLACK);
        g.drawLine(0, 0, tileSize * w, 0);
        g.drawLine(0, 0, 0, tileSize * h);
        g.drawLine(0, tileSize * h, tileSize * w, tileSize * h);
        g.drawLine(tileSize * w, 0, tileSize * w, tileSize * h);
        return i;
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
        return puzzles.toString();
    }
}
