package main.puzzle;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import main.data.Unit;
import main.ui.resource.AppColor;
import main.ui.resource.AppText;
import main.util.Fn;
import main.util.Rational;

public class Board implements Comparable<Board>, Serializable {

    public static int UNUSED = -2;
    public static int EMPTY = -1;

    public static int HEIGHT = 8;
    public static int WIDTH = 8;

    private final Unit unit;
    private final int star;
    private List<Chip> chips;
    private PuzzleMatrix<Integer> matrix;
    private final Stat maxStat;

    private final Stat stat, pt;
    private final double statPerc;
    private final int xp;

    @Override
    public int compareTo(Board o) {
        int size = Integer.compare(chips.size(), o.chips.size());
        if (size != 0) {
            return size;
        }
        for (int i = 0; i < chips.size(); i++) {
            int id = chips.get(i).getID().compareTo(o.chips.get(i).getID());
            if (id != 0) {
                return id;
            }
        }
        return 0;
    }

    // Combinator - fitness
    public Board(Board board) {
        this.unit = board.unit;
        this.star = board.star;

        this.chips = new ArrayList<>();
        for (Chip c : board.chips) {
            this.chips.add(new Chip(c));
        }
        colorChips();

        this.matrix = new PuzzleMatrix<>(board.matrix);
        this.maxStat = board.maxStat;

        this.stat = board.stat;
        this.pt = board.pt;
        this.statPerc = board.statPerc;
        this.xp = board.xp;
    }

    // Combination File
    public Board(Unit unit, int star, Stat maxStat, List<Chip> chips_, List<Point> chipLocs) {
        this.unit = unit;
        this.star = star;

        this.chips = new ArrayList<>(chips_);
        colorChips();

        this.matrix = toPlacement(unit, star, chips, chipLocs);
        this.maxStat = maxStat;

        this.stat = Stat.chipStatSum(chips);
        this.pt = Stat.chipPtSum(chips);

        this.statPerc = getStatPerc(this.stat, this.maxStat);

        int xp_ = 0;
        for (Chip chip : chips) {
            xp_ += chip.getCumulXP();
        }
        xp = xp_;
    }

    // Board Template
    public Board(Unit unit, int star, Stat maxStat, List<Chip> candidates, BoardTemplate template) {
        this.unit = unit;
        this.star = star;

        int rotation = 0;
        int min = candidates.size();
        for (int r = 0; r < 4; r += unit.getRotationStep(star)) {
            int count = template.getNumRotationNeeded(r, candidates);
            if (count < min) {
                min = count;
                rotation = r;
            }
        }

        BoardTemplate newTemplate = template.getRotatedTemplate(rotation);

        List<Puzzle> puzzles = newTemplate.getPuzzles();

        this.chips = new ArrayList<>();
        for (Chip candidate : candidates) {
            chips.add(new Chip(candidate));
        }

        int sa = 0;
        boolean[] sortCache = new boolean[puzzles.size()];
        while (sa < puzzles.size()) {
            Shape shape = puzzles.get(sa).shape;
            int sb = sa;
            while (sb + 1 < puzzles.size() && shape == puzzles.get(sb + 1).shape) {
                sb++;
            }

            for (int i = sa; i <= sb; i++) {
                int r = puzzles.get(i).rotation;
                if (chips.get(i).getInitRotation() == r) {
                    sortCache[i] = true;
                    continue;
                }
                for (int j = sa; j <= sb; j++) {
                    if (i == j || sortCache[j]) {
                        continue;
                    }
                    Chip chip = chips.get(j);
                    if (chip.getInitRotation() == r) {
                        Collections.swap(chips, i, j);
                        sortCache[i] = true;
                        break;
                    }
                }
            }
            sa = sb + 1;
        }

        for (int i = 0; i < chips.size(); i++) {
            Chip chip = chips.get(i);
            chip.setRotation(puzzles.get(i).rotation);
        }
        colorChips();

        this.matrix = newTemplate.getMatrix();
        this.maxStat = maxStat;

        this.stat = Stat.chipStatSum(chips);
        this.pt = Stat.chipPtSum(chips);

        this.statPerc = getStatPerc(this.stat, this.maxStat);

        int xp_ = 0;
        for (Chip chip : chips) {
            xp_ += chip.getCumulXP();
        }
        xp = xp_;
    }

    // <editor-fold defaultstate="collapsed" desc="Name">
    public Unit getUnit() {
        return unit;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Star">
    public int getStar() {
        return star;
    }

    public static String getStarHTML_star(int star) {
        String starStr = "";
        for (int i = 0; i < star; i++) {
            starStr += AppText.TEXT_STAR_FULL;
        }
        return Fn.toHTML(Fn.htmlColor(starStr, AppColor.YELLOW_STAR));
    }

    public static String getStarHTML_version(int version) {
        int nFullRed = version / 2;
        String fullRedStr = "";
        for (int i = 0; i < nFullRed; i++) {
            fullRedStr += AppText.TEXT_STAR_FULL;
        }

        int nHalfRed = version % 2;
        String halfRedStr = "";
        for (int i = 0; i < nHalfRed; i++) {
            halfRedStr += AppText.TEXT_STAR_EMPTY;
        }

        String yellowStr = "";
        for (int i = fullRedStr.length() + halfRedStr.length(); i < 5; i++) {
            yellowStr += AppText.TEXT_STAR_FULL;
        }

        return Fn.toHTML(Fn.htmlColor(fullRedStr + halfRedStr, AppColor.RED_STAR)
                + Fn.htmlColor(yellowStr, AppColor.YELLOW_STAR)
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Color">
    public final Unit.Color getColor() {
        return unit.getColor();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Rotation and Ticket">
    public int getTicketCount() {
        int sum = 0;
        for (Chip chip : chips) {
            sum += chip.getNumTicket();
        }
        return sum;
    }
//    public void minimizeTicket() {
//        for (int rotation = 0; rotation < 4; rotation += MAP_ROTATIONSTEP.get(name, star)) {
//            // Start a new board
//            Board b = new Board(this);
//            Set<Shape> cShapes = new HashSet<>();
//            Chip[] newUsedChips = new Chip[b.chips.size()];
//            // Rotate board
//            b.rotate(rotation);
//
//            for (Chip chip : b.chips) {
//                cShapes.add(chip.getShape());
//            }
//
//            // Get indicies and candidates
//            for (Shape cs : cShapes) {
//                Set<Integer> cIndices = new HashSet<>();
//                List<Chip> cCandidates = new ArrayList<>();
//                for (int i = 0; i < b.chips.size(); i++) {
//                    Chip c = b.chips.get(i);
//                    if (c.getShape() == cs) {
//                        cIndices.add(i);
//                        cCandidates.add(new Chip(c));
//                    }
//                }
//                // Put matching initial rotation
//                for (Integer cIndex : cIndices) {
//                    int r = b.chips.get(cIndex).getRotation();
//                    for (Chip c : cCandidates) {
//                        if (c.getInitRotation() == r) {
//                            c.setRotation(c.getInitRotation());
//                            newUsedChips[cIndex] = c;
//                            cCandidates.remove(c);
//                            break;
//                        }
//                    }
//                }
//                // Put remaining
//                if (!cCandidates.isEmpty()) {
//                    int i = 0;
//                    for (Integer ci : cIndices) {
//                        if (newUsedChips[ci] == null) {
//                            Chip c = cCandidates.get(i);
//                            int r = b.chips.get(ci).getRotation();
//                            c.setRotation(r);
//                            newUsedChips[ci] = cCandidates.get(i);
//                            i++;
//                        }
//                    }
//                }
//            }
//            b.chips = Arrays.asList(newUsedChips);
//            // Replace if better
//            if (getTicketCount() > b.getTicketCount()) {
//                matrix = b.matrix;
//                chips = b.chips;
//            }
//            // Exit if 0
//            if (getTicketCount() == 0) {
//                break;
//            }
//        }
//    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PT">
    public Stat getPt() {
        return pt;
    }

    public static Stat getMaxPt(Unit unit, int star) {
        return getMaxPt(unit, star, getMaxStat(unit, star));
    }

    public static Stat getMaxPt(Unit unit, int star, Stat stat) {
        int[] statArray = stat.toArray();
        int[] optimalPtArray = new int[4];

        for (Integer nChip : get56ChipCount(unit, star)) {
            for (int i = 0; i < 4; i++) {
                int[] dist = getPtDistribution(Chip.RATES[i], nChip, statArray[i]);
                int total = 0;
                for (int d : dist) {
                    total += d;
                }
                if (optimalPtArray[i] < total) {
                    optimalPtArray[i] = total;
                }
            }
        }

        int residue = getCellCount(unit, star)
                - (optimalPtArray[0]
                + optimalPtArray[1]
                + optimalPtArray[2]
                + optimalPtArray[3]);
        if (residue > 0) {
            for (int i = 0; i < 4; i++) {
                optimalPtArray[i] += residue;
            }
        }

        Stat pt = new Stat(optimalPtArray);

        return pt;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Stat">
    public Stat getStat() {
        return stat;
    }

    public Stat getOldStat() {
        return Stat.chipOldStatSum(chips);
    }

    public Stat getCustomMaxStat() {
        return maxStat;
    }

    public Stat getOrigMaxStat() {
        return getMaxStat(unit, star);
    }

    public static Stat getMaxStat(Unit unit, int star) {
        return unit.getBoardStats()[Fn.limit(star - 1, 0, unit.getBoardStats().length)];
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Stat Perc">
    public double getStatPerc() {
        return statPerc;
    }

    public double getStatPerc(int type) {
        Stat s = getStat();
        Stat m = getCustomMaxStat();
        return getStatPerc(type, s, m);
    }

    private static double getStatPerc(Stat stat, Stat max) {
        if (max.allZero()) {
            return 1.0;
        }
        if (stat.allGeq(max)) {
            return 1.0;
        }
        int[] sArray = stat.limit(max).toArray();
        int[] mArray = max.toArray();
        double s = 0;
        double m = 0;
        for (int i = 0; i < 4; i++) {
            s += new Rational(sArray[i]).div(Chip.RATES[i]).getDouble();
            m += new Rational(mArray[i]).div(Chip.RATES[i]).getDouble();
        }
        if (m == 0) {
            return 1.0;
        }
        return s / m;
    }

    public static double getStatPerc(int type, Stat stat, Stat max) {
        int s = stat.toArray()[type];
        int m = max.toArray()[type];

        if (m == 0) {
            return 1.0;
        }
        return (double) Math.min(s, m) / m;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="HOC, Resonance, and Version">
    public Stat getResonance() {
        int numCell = 0;
        for (Chip chip : chips) {
            if (chip.getColor() == getColor()) {
                numCell += chip.getSize();
            }
        }

        List<Stat> stats = new ArrayList<>();
        for (int key : unit.GetResonanceStats().keySet()) {
            if (key <= numCell) {
                stats.add(unit.GetResonanceStats().get(key));
            }
        }
        return new Stat(stats);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Stat Calc">
    private static int[] getPtDistribution(Rational rate, int nChip, int stat) {
        int stat_1pt = Chip.getMaxEffStat(rate, 1);
        int[] ptArray = new int[nChip];
        int i = 0;
        while (calcStat(rate, ptArray) < stat) {
            ptArray[i]++;

            int iPt = ptArray[i];
            int prevLoss = iPt > 0 ? (iPt - 1) * stat_1pt - Chip.getMaxEffStat(rate, (iPt - 1)) : 0;
            int currentLoss = iPt * stat_1pt - Chip.getMaxEffStat(rate, iPt);
            int nextLoss = (iPt + 1) * stat_1pt - Chip.getMaxEffStat(rate, (iPt + 1));
            if (currentLoss * 2 < prevLoss + nextLoss) {
                i = (i + 1) % nChip;
            }
        }
        return ptArray;
    }

    private static int calcStat(Rational rate, int[] pts) {
        int out = 0;
        for (int pt : pts) {
            out += Chip.getMaxEffStat(rate, pt);
        }
        return out;
    }

    private static List<Integer> get56ChipCount(Unit unit, int star) {
        int nCell = getCellCount(unit, star);
        List<Integer> out = new ArrayList<>();
        for (int nSix = 0; nSix < nCell / 6; nSix++) {
            int rest = nCell - nSix * 6;
            if (rest % 5 == 0) {
                int nFive = rest / 5;
                out.add(nFive + nSix);
            }
        }
        return out;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="XP">
    public int getXP() {
        return xp;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Mark">
    public int getMarkedCellCount() {
        int sum = 0;
        for (Chip c : chips) {
            if (c.isMarked()) {
                sum += c.getSize();
            }
        }
        return sum;
    }

    public int getMarkedChipCount() {
        int count = 0;
        for (Chip c : chips) {
            if (c.isMarked()) {
                count++;
            }
        }
        return count;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Chips">
    public int getChipCount() {
        return chips.size();
    }

    public List<String> getChipIDs() {
        List<String> IDs = new ArrayList<>();
        for (Chip c : chips) {
            IDs.add(c.getID());
        }
        return IDs;
    }

    public void forEachChip(Consumer<? super Chip> action) {
        chips.forEach(action);
    }

    public Chip getChip(String id) {
        for (Chip c : chips) {
            if (c.getID().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public List<Chip> getChips() {
        List<Chip> out = new ArrayList<>();
        for (Chip chip : chips) {
            out.add(new Chip(chip));
        }
        return out;
    }

    public static boolean isChipPlaceable(PuzzleMatrix<Integer> matrix, Set<Point> cps) {
        for (Point cp : cps) {
            if (matrix.get(cp.x, cp.y) == null || matrix.get(cp.x, cp.y) != EMPTY) {
                return false;
            }
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Matrix and Cells">
    public static PuzzleMatrix<Integer> initMatrix(Unit unit, int star) {
        PuzzleMatrix<Integer> matrix = new PuzzleMatrix<>(unit.getGrid());
        for (int r = 0; r < matrix.getNumRow(); r++) {
            for (int c = 0; c < matrix.getNumCol(); c++) {
                matrix.set(r, c, matrix.get(r, c) <= star ? EMPTY : UNUSED);
            }
        }
        return matrix;
    }

    public PuzzleMatrix<Integer> getMatrix() {
        return matrix;
    }

    public Point getLocation(Chip c) {
        int i = chips.indexOf(c);
        if (i < 0) {
            return null;
        }
        return matrix.getPivot(i);
    }

    public static int getCellCount(Unit unit, int star) {
        PuzzleMatrix<Integer> s = initMatrix(unit, star);
        return s.getNumNotContaining(UNUSED);
    }

    public static boolean rs_isValid(Unit unit, int star, String data) {
        String[] split = data.split(";");
        String[] shapeStrs = split[0].split(",");
        Integer[] rotations = Stream.of(split[1].split(","))
                .map(Integer::valueOf)
                .toArray(Integer[]::new);
        Point[] locations = Stream.of(split[2].split(","))
                .map((s) -> s.split("\\."))
                .map((sp) -> new Point(Integer.valueOf(sp[0]), Integer.valueOf(sp[1])))
                .toArray(Point[]::new);
        PuzzleMatrix<Boolean> board = rs_getBoolMatrix(unit, star);
        for (int i = 0; i < shapeStrs.length; i++) {
            Shape shape = Shape.byId(Integer.parseInt(shapeStrs[i]));
            int rotation = rotations[i];
            Point location = locations[i];

            Set<Point> pts = rs_getPts(shape, rotation, location);
            for (Point p : pts) {
                if (p.x < 0 || WIDTH - 1 < p.x) {
                    return false;
                }
                if (p.y < 0 || HEIGHT - 1 < p.y) {
                    return false;
                }
                if (!board.get(p.x, p.y)) {
                    return false;
                }
                board.set(p.x, p.y, false);
            }
        }
        return true;
    }

    private static PuzzleMatrix<Boolean> rs_getBoolMatrix(Unit unit, int star) {
        Integer[][] levelGrid = unit.getGrid();
        PuzzleMatrix<Boolean> out = new PuzzleMatrix<>(HEIGHT, WIDTH, false);
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                out.set(r, c, levelGrid[r][c] <= star);
            }
        }
        return out;
    }

    private static Set<Point> rs_getPts(Shape shape, int rotation, Point location) {
        PuzzleMatrix<Boolean> cm = new PuzzleMatrix<>(Chip.generateMatrix(shape, rotation));
        Point pivot = cm.getPivot(true);
        Set<Point> pts = cm.getPoints(true);
        pts.forEach((p) -> p.translate(location.x - pivot.x, location.y - pivot.y));
        return pts;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image">
    public final void colorChips() {
        for (int i = 0; i < chips.size(); i++) {
            Chip c = chips.get(i);
            c.setBoardIndex(i);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="File">
    public static PuzzleMatrix<Integer> toPlacement(Unit unit, int star, List<Chip> chips, List<Point> locations) {
        List<Puzzle> puzzles = new ArrayList<>(chips.size());
        for (int i = 0; i < chips.size(); i++) {
            Chip c = chips.get(i);
            Shape s = c.getShape();
            int r = c.getRotation();
            Point l = locations.get(i);
            puzzles.add(new Puzzle(s, r, l));
        }

        return toPlacement(unit, star, puzzles);
    }

    public static PuzzleMatrix<Integer> toPlacement(Unit unit, int star, List<Puzzle> puzzles) {
        // Placement
        PuzzleMatrix<Integer> placement = initMatrix(unit, star);
        for (int i = 0; i < puzzles.size(); i++) {
            PuzzleMatrix<Boolean> matrix = Chip.generateMatrix(puzzles.get(i).shape, puzzles.get(i).rotation);
            Set<Point> pts = matrix.getPoints(true);
            Point fp = matrix.getPivot(true);
            Point bp = puzzles.get(i).location;
            for (Point p : pts) {
                p.translate(bp.x - fp.x, bp.y - fp.y);
                placement.set(p.x, p.y, i);
            }
        }
        return placement;
    }

    public static List<Point> toLocation(PuzzleMatrix<Integer> placement) {
        List<Point> location = new ArrayList<>();
        int i = 0;
        boolean found = true;
        while (found) {
            found = false;
            Point p = placement.getPivot(i);
            if (p != null) {
                found = true;
                location.add(p);
            }
            i++;
        }
        return location;
    }
    // </editor-fold>
}
