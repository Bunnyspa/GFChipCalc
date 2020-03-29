package main.puzzle.preset;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.PuzzleMatrix;
import main.util.IO;

/**
 *
 * @author Bunnyspa
 */
public class PuzzlePreset implements Comparable<PuzzlePreset> {

    private final List<String> names;
    private final List<Integer> rotations;
    private final List<Point> locations;
    private final Map<String, Integer> nameCountMap;

    private final PuzzleMatrix<Integer> placement;

    public static final int END = 0;
    public static final int EMPTY = 1;
    public static final int NORMAL = 2;

    private final int state;

    private static PuzzlePreset empty = new PuzzlePreset(false);
    private static PuzzlePreset end = new PuzzlePreset(true);

    private PuzzlePreset(boolean isEnd) {
        names = null;
        rotations = null;
        locations = null;
        nameCountMap = null;
        placement = null;
        state = isEnd ? END : EMPTY;
    }

    public static PuzzlePreset empty() {
        return empty;
    }

    public static PuzzlePreset end() {
        return end;
    }

    public PuzzlePreset(PuzzleMatrix<Integer> placement) {
        // Names
        this.names = new ArrayList<>();
        nameCountMap = new HashMap<>();

        // Rotations
        this.rotations = new ArrayList<>();

        // Placement
        this.placement = placement;

        int index = 0;
        while (placement.contains(index)) {
            PuzzleMatrix<Boolean> chip = getChip(placement, index);
            boolean done = false;
            for (String type : Chip.TYPES) {
                for (String name : Chip.getNames(type)) {
                    for (int rotation = 0; rotation < Chip.getMaxRotation(name); rotation++) {
                        PuzzleMatrix<Boolean> candidate = Chip.generateMatrix(name, rotation);
                        if (candidate.equals(chip)) {
                            names.add(name);
                            rotations.add(rotation);
                            done = true;
                            break;
                        }
                    }
                    if (done) {
                        break;
                    }
                }
            }
            index++;
        }

        // Locations
        locations = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            locations.add(placement.getPivot(i));
        }

        init();
        state = NORMAL;
    }

    private static PuzzleMatrix<Boolean> getChip(PuzzleMatrix<Integer> placement, int i) {
        int rMin = placement.getNumRow();
        int rMax = 0;
        int cMin = placement.getNumCol();
        int cMax = 0;
        for (Point p : placement.getCoords(i)) {
            if (rMin > p.x) {
                rMin = p.x;
            }
            if (rMax < p.x) {
                rMax = p.x;
            }
            if (cMin > p.y) {
                cMin = p.y;
            }
            if (cMax < p.y) {
                cMax = p.y;
            }
        }
        PuzzleMatrix<Boolean> out = new PuzzleMatrix<>(rMax - rMin + 1, cMax - cMin + 1, false);
        for (Point coord : placement.getCoords(i)) {
            out.set(coord.x - rMin, coord.y - cMin, true);
        }
        return out;
    }

    public PuzzlePreset(String name, int star, List<String> names, List<Integer> rotations, List<Point> locations) {
        // Names
        this.names = names;
        nameCountMap = new HashMap<>();

        // Rotations
        this.rotations = rotations;

        // Locations
        this.locations = locations;

        // Placement
        placement = Board.toPlacement(name, star, names, rotations, locations);

        init();
        state = NORMAL;
    }

    private void init() {
        names.forEach((cn) -> {
            if (!nameCountMap.containsKey(cn)) {
                nameCountMap.put(cn, 0);
            }
            nameCountMap.put(cn, nameCountMap.get(cn) + 1);
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

    public Map<String, Integer> getNameCountMap() {
        return nameCountMap;
    }

    public List<String> getChipNames() {
        return names;
    }

    public List<Integer> getChipRotations() {
        return rotations;
    }

    public List<Point> getChipLocations() {
        return locations;
    }

    public String toData() {
        StringBuilder sb = new StringBuilder();
        // Names
        List<String> chipNames = getChipNames();
        sb.append(String.join(",", chipNames));
        sb.append(";");

        // Rotations
        List<String> chipRots = new ArrayList<>(chipNames.size());
        getChipRotations().forEach((r) -> chipRots.add(String.valueOf(r)));
        sb.append(String.join(",", chipRots));
        sb.append(";");

        // Locations
        List<String> chipLocs = new ArrayList<>(chipNames.size());
        getChipLocations().forEach((p) -> chipLocs.add(IO.data(p)));
        sb.append(String.join(",", chipLocs));
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

    @Override
    public int compareTo(PuzzlePreset o) {
        List<String> l1 = getChipNames();
        List<String> l2 = o.getChipNames();
        if (l1.size() - l2.size() != 0) {
            return l1.size() - l2.size();
        }
        for (int i = 0; i < l1.size(); i++) {
            int c = l1.get(i).compareTo(l2.get(i));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        if (state == END) {
            return "null";
        }
        if (state == EMPTY) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        names.forEach((n) -> sb.append(n).append(";"));
        sb.append(System.lineSeparator());
        rotations.forEach((n) -> sb.append(n).append(";"));
        sb.append(System.lineSeparator());
        locations.forEach((n) -> sb.append(n.x).append(",").append(n.y).append(";"));
        sb.append(System.lineSeparator());
        sb.append(placement);
        return sb.toString();
    }
}
