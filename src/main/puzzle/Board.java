package main.puzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import main.App;
import main.util.FRational;
import main.util.Fn;
import main.util.Rational;
import main.util.StrIntMap;

/**
 *
 * @author Bunnyspa
 */
public class Board {

    public static int UNUSED = -2;
    public static int EMPTY = -1;

    public static int HEIGHT = 8;
    public static int WIDTH = 8;

    public static final String NAME_BGM71 = "BGM-71";
    public static final String NAME_AGS30 = "AGS-30";
    public static final String NAME_2B14 = "2B14";
    public static final String NAME_M2 = "M2";
    public static final String NAME_AT4 = "AT4";
    public static final String NAME_QLZ04 = "QLZ-04";
    public static String[] NAMES = {NAME_BGM71, NAME_AGS30, NAME_2B14, NAME_M2, NAME_AT4, NAME_QLZ04};

    public static final String STR_STAR_FULL = "★";
    public static final String STR_STAR_EMPTY = "☆";

    public static final Color COLOR_STAR_YELLOW = Chip.COLOR_STAR;
    public static final Color COLOR_STAR_RED = Color.RED;

    private static final Map<String, Integer[][]> MATRIX_MAP = new HashMap<String, Integer[][]>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, new Integer[][]{
                {6, 6, 6, 6, 6, 6, 6, 6},
                {6, 4, 4, 4, 3, 3, 3, 6},
                {6, 4, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 5, 6},
                {6, 3, 3, 3, 5, 5, 5, 6},
                {6, 6, 6, 6, 6, 6, 6, 6}
            });
            put(NAME_AGS30, new Integer[][]{
                {6, 6, 5, 5, 6, 6, 6, 6},
                {6, 3, 3, 2, 2, 6, 6, 6},
                {4, 3, 1, 1, 1, 1, 6, 6},
                {4, 2, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 2, 4},
                {6, 6, 1, 1, 1, 1, 3, 4},
                {6, 6, 6, 2, 2, 3, 3, 6},
                {6, 6, 6, 6, 5, 5, 6, 6}
            });
            put(NAME_2B14, new Integer[][]{
                {6, 6, 6, 6, 6, 6, 6, 6},
                {6, 6, 5, 6, 6, 5, 6, 6},
                {6, 2, 1, 1, 1, 1, 3, 6},
                {4, 2, 1, 1, 1, 1, 3, 4},
                {4, 2, 1, 1, 1, 1, 3, 4},
                {6, 2, 1, 1, 1, 1, 3, 6},
                {6, 6, 5, 6, 6, 5, 6, 6},
                {6, 6, 6, 6, 6, 6, 6, 6}
            });
            put(NAME_M2, new Integer[][]{
                {5, 3, 3, 6, 6, 6, 6, 5},
                {6, 3, 1, 1, 6, 6, 2, 4},
                {6, 6, 1, 1, 6, 2, 2, 4},
                {6, 6, 1, 1, 1, 1, 2, 6},
                {6, 2, 1, 1, 1, 1, 6, 6},
                {4, 2, 2, 6, 1, 1, 6, 6},
                {4, 2, 6, 6, 1, 1, 3, 6},
                {5, 6, 6, 6, 6, 3, 3, 5}
            });
            put(NAME_AT4, new Integer[][]{
                {6, 6, 6, 1, 1, 6, 6, 6},
                {6, 6, 1, 1, 1, 1, 6, 6},
                {6, 1, 1, 1, 1, 1, 1, 6},
                {2, 1, 1, 6, 6, 1, 1, 3},
                {2, 2, 2, 6, 6, 3, 3, 3},
                {6, 2, 2, 4, 4, 3, 3, 6},
                {6, 6, 5, 4, 4, 5, 6, 6},
                {6, 6, 6, 5, 5, 6, 6, 6}
            });
            put(NAME_QLZ04, new Integer[][]{
                {6, 6, 6, 6, 6, 6, 6, 6},
                {5, 3, 6, 6, 6, 6, 3, 5},
                {5, 3, 3, 6, 6, 3, 3, 5},
                {4, 1, 1, 1, 1, 1, 1, 4},
                {4, 1, 1, 1, 1, 1, 1, 4},
                {6, 1, 1, 2, 2, 1, 1, 6},
                {6, 6, 2, 2, 2, 2, 6, 6},
                {6, 6, 6, 2, 2, 6, 6, 6}
            });
        }
    }; // </editor-fold>
    private static final Map<String, Integer> COLOR_MAP = new HashMap<String, Integer>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, Chip.COLOR_BLUE);
            put(NAME_AGS30, Chip.COLOR_ORANGE);
            put(NAME_2B14, Chip.COLOR_ORANGE);
            put(NAME_M2, Chip.COLOR_BLUE);
            put(NAME_AT4, Chip.COLOR_BLUE);
            put(NAME_QLZ04, Chip.COLOR_ORANGE);
        }
    }; // </editor-fold>
    private static final Map<String, FStat> INNATE_MAX_MAP = new HashMap<String, FStat>() // <editor-fold defaultstate="collapsed"> 
    {
        {
            put(NAME_BGM71, new FStat(155, 402, 349, 83));
            put(NAME_AGS30, new FStat(78, 144, 198, 386));
            put(NAME_2B14, new FStat(152, 58, 135, 160));
            put(NAME_M2, new FStat(113, 49, 119, 182));
            put(NAME_AT4, new FStat(113, 261, 284, 134));
            put(NAME_QLZ04, new FStat(77, 136, 188, 331));
        }
    }; // </editor-fold>
    private static final Map<String, FStat[]> MAX_MAP = new HashMap<String, FStat[]>() // <editor-fold defaultstate="collapsed"> 
    {
        {
            put(NAME_BGM71, new FStat[]{
                new FStat(95, 165, 96, 23),
                new FStat(114, 198, 115, 28),
                new FStat(133, 231, 134, 32),
                new FStat(162, 280, 162, 39),
                new FStat(190, 329, 191, 46)
            });
            put(NAME_AGS30, new FStat[]{
                new FStat(53, 65, 60, 117),
                new FStat(64, 78, 72, 140),
                new FStat(75, 91, 84, 163),
                new FStat(90, 111, 102, 198),
                new FStat(106, 130, 120, 233)
            });
            put(NAME_2B14, new FStat[]{
                new FStat(114, 29, 45, 54),
                new FStat(136, 35, 54, 64),
                new FStat(159, 41, 63, 75),
                new FStat(193, 49, 77, 91),
                new FStat(227, 58, 90, 107)
            });
            put(NAME_M2, new FStat[]{
                new FStat(103, 30, 49, 74),
                new FStat(124, 36, 59, 89),
                new FStat(145, 42, 68, 104),
                new FStat(176, 51, 83, 126),
                new FStat(206, 60, 97, 148)
            });
            put(NAME_AT4, new FStat[]{
                new FStat(85, 131, 95, 45),
                new FStat(102, 157, 114, 54),
                new FStat(118, 183, 133, 63),
                new FStat(144, 222, 161, 76),
                new FStat(169, 261, 190, 90)
            });
            put(NAME_QLZ04, new FStat[]{
                new FStat(61, 72, 66, 117),
                new FStat(73, 86, 79, 140),
                new FStat(85, 100, 93, 163),
                new FStat(103, 122, 112, 198),
                new FStat(122, 143, 132, 233)
            });
        }
    }; // </editor-fold>
    public static final StrIntMap<FStat> RESONANCE_MAP = new StrIntMap<FStat>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, new HashMap<Integer, FStat>() {
                {
                    put(4, new FStat(16, 0, 6, 0));
                    put(10, new FStat(0, 8, 0, 3));
                    put(16, new FStat(36, 0, 8, 0));
                    put(22, new FStat(0, 14, 10, 0));
                    put(28, new FStat(46, 0, 0, 6));
                    put(32, new FStat(0, 18, 14, 0));
                    put(36, new FStat(60, 26, 0, 0));
                }
            });
            put(NAME_AGS30, new HashMap<Integer, FStat>() {
                {
                    put(4, new FStat(8, 0, 4, 0));
                    put(10, new FStat(0, 4, 0, 8));
                    put(16, new FStat(14, 0, 6, 0));
                    put(24, new FStat(0, 8, 0, 10));
                    put(30, new FStat(26, 0, 12, 0));
                    put(34, new FStat(0, 14, 0, 12));
                    put(38, new FStat(36, 0, 0, 16));
                }
            });
            put(NAME_2B14, new HashMap<Integer, FStat>() {
                {
                    put(4, new FStat(16, 0, 6, 0));
                    put(10, new FStat(0, 3, 0, 5));
                    put(16, new FStat(36, 0, 0, 0));
                    put(20, new FStat(0, 4, 8, 0));
                    put(24, new FStat(58, 0, 0, 7));
                    put(28, new FStat(0, 8, 0, 10));
                    put(32, new FStat(82, 0, 8, 0));
                }
            });
            put(NAME_M2, new HashMap<Integer, FStat>() {
                {
                    put(4, new FStat(13, 0, 6, 0));
                    put(10, new FStat(0, 3, 0, 6));
                    put(16, new FStat(30, 0, 0, 0));
                    put(20, new FStat(0, 4, 8, 0));
                    put(24, new FStat(48, 0, 0, 9));
                    put(28, new FStat(0, 8, 0, 13));
                    put(32, new FStat(68, 0, 8, 0));
                }
            });
            put(NAME_AT4, new HashMap<Integer, FStat>() {
                {
                    put(4, new FStat(12, 0, 5, 0));
                    put(10, new FStat(0, 5, 0, 5));
                    put(16, new FStat(27, 0, 7, 0));
                    put(22, new FStat(0, 10, 9, 0));
                    put(28, new FStat(35, 0, 0, 10));
                    put(32, new FStat(0, 12, 12, 0));
                    put(36, new FStat(46, 18, 0, 0));
                }
            });
            put(NAME_QLZ04, new HashMap<Integer, FStat>() {
                {
                    put(4, new FStat(9, 0, 6, 0));
                    put(10, new FStat(0, 6, 0, 6));
                    put(16, new FStat(15, 0, 6, 0));
                    put(24, new FStat(0, 9, 0, 9));
                    put(30, new FStat(28, 0, 12, 0));
                    put(34, new FStat(0, 15, 0, 10));
                    put(38, new FStat(38, 0, 0, 14));
                }
            });
        }
    }; // </editor-fold>
    private static final Map<String, FStat[]> VERSION_MAP = new HashMap<String, FStat[]>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, new FStat[]{
                new FStat(4, 0, 6, 0),
                new FStat(0, 10, 9, 0),
                new FStat(5, 0, 0, 6),
                new FStat(0, 12, 10, 0),
                new FStat(7, 15, 0, 0),
                new FStat(8, 0, 12, 0),
                new FStat(0, 16, 14, 0),
                new FStat(9, 0, 0, 10),
                new FStat(0, 18, 18, 0),
                new FStat(12, 24, 0, 0)
            });
            put(NAME_AGS30, new FStat[]{
                new FStat(2, 0, 5, 0),
                new FStat(0, 4, 0, 8),
                new FStat(3, 0, 8, 0),
                new FStat(0, 6, 0, 12),
                new FStat(3, 0, 0, 13),
                new FStat(4, 0, 12, 0),
                new FStat(0, 10, 0, 12),
                new FStat(4, 0, 16, 0),
                new FStat(0, 16, 0, 16),
                new FStat(8, 0, 0, 18)
            });
            put(NAME_2B14, new FStat[]{
                new FStat(2, 0, 4, 0),
                new FStat(2, 3, 0, 4),
                new FStat(3, 0, 6, 0),
                new FStat(4, 4, 0, 4),
                new FStat(5, 0, 0, 5),
                new FStat(6, 0, 9, 0),
                new FStat(7, 3, 0, 6),
                new FStat(7, 0, 10, 0),
                new FStat(4, 5, 0, 9),
                new FStat(10, 0, 0, 6)
            });
            put(NAME_M2, new FStat[]{
                new FStat(2, 0, 4, 0),
                new FStat(1, 3, 0, 5),
                new FStat(3, 0, 6, 0),
                new FStat(3, 4, 0, 5),
                new FStat(4, 0, 0, 6),
                new FStat(5, 0, 9, 0),
                new FStat(6, 2, 0, 8),
                new FStat(6, 0, 9, 0),
                new FStat(3, 5, 0, 11),
                new FStat(8, 0, 0, 8)
            });
            put(NAME_AT4, new FStat[]{
                new FStat(3, 0, 5, 0),
                new FStat(0, 7, 8, 0),
                new FStat(4, 0, 0, 10),
                new FStat(0, 8, 9, 0),
                new FStat(5, 10, 0, 0),
                new FStat(6, 0, 10, 0),
                new FStat(0, 11, 12, 0),
                new FStat(7, 0, 0, 17),
                new FStat(0, 12, 15, 0),
                new FStat(9, 16, 0, 0)
            });
            put(NAME_QLZ04, new FStat[]{
                new FStat(3, 0, 3, 0),
                new FStat(2, 3, 0, 4),
                new FStat(4, 0, 6, 0),
                new FStat(3, 3, 0, 4),
                new FStat(5, 0, 0, 5),
                new FStat(5, 0, 10, 0),
                new FStat(6, 4, 0, 6),
                new FStat(6, 0, 10, 0),
                new FStat(4, 6, 0, 10),
                new FStat(8, 0, 0, 8)
            });
        }
    }; // </editor-fold>
    private static final StrIntMap<Integer> ROTATION_MAP = new StrIntMap<Integer>() // <editor-fold defaultstate="collapsed">
    {
        {
            for (String name : NAMES) {
                for (int star = 1; star <= 5; star++) {
                    PuzzleMatrix<Integer> unrotated = initMatrix(name, star);
                    for (int i = 1; i <= 4; i++) {
                        PuzzleMatrix<Integer> b = initMatrix(name, star);
                        b.rotateInside(i, UNUSED);
                        if (unrotated.equals(b)) {
                            put(name, star, i);
                            break;
                        }
                    }
                }
            }
        }
    }; // </editor-fold>

    private final String name;
    private final int star;
    private List<Chip> chips;
    private PuzzleMatrix<Integer> matrix;
    private final FStat maxStat;

    private final FStat stat, pt;
    private final double statPerc;
    private final int xp;

    // Combinator - fitness
    public Board(Board board) {
        this.name = board.name;
        this.star = board.star;

        this.chips = new ArrayList<>();
        board.chips.forEach((c) -> this.chips.add(new Chip(c)));
        colorChips();

        this.matrix = new PuzzleMatrix<>(board.matrix);
        this.maxStat = board.maxStat;

        this.stat = board.stat;
        this.pt = board.pt;
        this.statPerc = board.statPerc;
        this.xp = board.xp;
    }

    // Combination File / Puzzle Preset
    public Board(String name, int star, Stat maxStat, List<Chip> chips, List<Point> chipLocs) {
        this.name = name;
        this.star = star;

        this.chips = new ArrayList<>(chips);
        colorChips();

        this.matrix = toPlacement(name, star, chips, chipLocs);
        this.maxStat = new FStat(maxStat);

        Stat s = new Stat();
        chips.forEach((c) -> s.add(c.getStat()));
        this.stat = new FStat(s);

        Stat p = new Stat();
        chips.forEach((c) -> p.add(c.getPt()));
        this.pt = new FStat(p);

        statPerc = getStatPerc(this.stat, this.maxStat);

        xp = chips.stream().mapToInt((c) -> c.getCumulXP()).sum();
    }

    // <editor-fold defaultstate="collapsed" desc="Name">
    public String getName() {
        return name;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Star">
    public int getStar() {
        return star;
    }

    public static String getStarHTML_star(int star) {
        String starStr = "";
        for (int i = 0; i < star; i++) {
            starStr += Board.STR_STAR_FULL;
        }
        return Fn.toHTML(Fn.htmlColor(starStr, Board.COLOR_STAR_YELLOW));
    }

    public static String getStarHTML_version(int version) {
        int nFullRed = version / 2;
        String fullRedStr = "";
        for (int i = 0; i < nFullRed; i++) {
            fullRedStr += Board.STR_STAR_FULL;
        }

        int nHalfRed = version % 2;
        String halfRedStr = "";
        for (int i = 0; i < nHalfRed; i++) {
            halfRedStr += Board.STR_STAR_EMPTY;
        }

        String yellowStr = "";
        for (int i = fullRedStr.length() + halfRedStr.length(); i < 5; i++) {
            yellowStr += Board.STR_STAR_FULL;
        }

        return Fn.toHTML(
                Fn.htmlColor(fullRedStr + halfRedStr, Board.COLOR_STAR_RED)
                + Fn.htmlColor(yellowStr, Board.COLOR_STAR_YELLOW)
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Color">
    public final int getColor() {
        return COLOR_MAP.get(name);
    }

    public static int getColor(String name) {
        if (COLOR_MAP.containsKey(name)) {
            return COLOR_MAP.get(name);
        }
        return -1;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Rotation and Ticket">
    private void rotate(int i) {
        matrix.rotate(i);
        chips.forEach((c) -> c.rotate(i));
    }

    public int getTicketCount() {
        return chips.stream().mapToInt((c) -> c.getNumTicket()).sum();
    }

    public void minimizeTicket() {
        for (int rotation = 0; rotation < 4; rotation += ROTATION_MAP.get(name, star)) {
            // Start a new board
            Board b = new Board(this);
            Set<String> cNames = new HashSet<>();
            Chip[] newUsedChips = new Chip[b.chips.size()];
            // Rotate board
            b.rotate(rotation);

            b.chips.forEach((c) -> {
                cNames.add(c.getName()); // Get names
            });

            cNames.forEach((cn) -> {
                // Get indicies and candidates
                Set<Integer> cIndices = new HashSet<>();
                List<Chip> cCandidates = new ArrayList<>();
                for (int i = 0; i < b.chips.size(); i++) {
                    Chip c = b.chips.get(i);
                    if (c.getName().equals(cn)) {
                        cIndices.add(i);
                        cCandidates.add(new Chip(c));
                    }
                }
                // Put matching initial rotation
                cIndices.forEach((ci) -> {
                    int r = b.chips.get(ci).getRotation();
                    for (Chip c : cCandidates) {
                        if (c.getInitRotation() == r) {
                            c.setRotation(c.getInitRotation());
                            newUsedChips[ci] = c;
                            cCandidates.remove(c);
                            break;
                        }
                    }
                });
                // Put remaining
                if (!cCandidates.isEmpty()) {
                    int i = 0;
                    for (Integer ci : cIndices) {
                        if (newUsedChips[ci] == null) {
                            Chip c = cCandidates.get(i);
                            int r = b.chips.get(ci).getRotation();
                            c.setRotation(r);
                            newUsedChips[ci] = cCandidates.get(i);
                            i++;
                        }
                    }
                }
            });
            b.chips = Arrays.asList(newUsedChips);
            // Replace if better
            if (getTicketCount() > b.getTicketCount()) {
                matrix = b.matrix;
                chips = b.chips;
            }
            // Exit if 0
            if (getTicketCount() == 0) {
                break;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PT">
    public Stat getPt() {
        return pt.toStat();
    }

    public static Stat getMaxPt(String name, int star) {
        return getMaxPt(name, star, getMaxStat(name, star));
    }

    public static Stat getMaxPt(String name, int star, Stat stat) {
        int[] statArray = stat.toArray();
        int[] optimalPtArray = new int[4];

        get56ChipCount(name, star).forEach((nChip) -> {
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
        });

        Stat pt = new Stat(optimalPtArray);
        int residue = getCellCount(name, star) - pt.sum();
        if (residue > 0) {
            pt.add(new Stat(residue));
        }
        return pt;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Stat">
    public Stat getStat() {
        return stat.toStat();
    }

    public Stat getOldStat() {
        Stat out = new Stat();
        chips.forEach((c) -> out.add(c.getOldStat()));
        return out;
    }

    public Stat getCustomMaxStat() {
        return maxStat.toStat();
    }

    public Stat getOrigMaxStat() {
        return getMaxStat(name, star);
    }

    public static Stat getMaxStat(String name, int star) {
        return new Stat(MAX_MAP.get(name)[Fn.limit(star - 1, 0, MAX_MAP.get(name).length)]);
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

    private static double getStatPerc(FStat stat, FStat max) {
        if (max.allZero()) {
            return 1.0;
        }
        if (stat.toStat().allGeq(max.toStat())) {
            return 1.0;
        }
        int[] sArray = stat.toStat().limit(max.toStat()).toArray();
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
        double d = (double) Math.min(s, m) / m;
        return d;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="HOC, Resonance, and Version">
    public static Stat getHOCStat(String name) {
        return new Stat(INNATE_MAX_MAP.get(name));
    }

    public Stat getResonance() {
        int numCell = chips.stream()
                .filter((c) -> (c.getColor() == getColor()))
                .mapToInt((c) -> c.getSize())
                .sum();

        Stat s = new Stat();

        RESONANCE_MAP.keySet(name).stream()
                .filter((i) -> (i <= numCell))
                .forEach((i) -> s.add(RESONANCE_MAP.get(name, i)));

        return s;
    }

    public static Stat getVersionStat(String name, int v) {
        Stat s = new Stat();
        FStat[] vus = VERSION_MAP.get(name);
        for (int i = 0; i < Math.min(v, 10); i++) {
            s.add(vus[i]);
        }
        return s;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Stat Calc">
    private static int[] getPtDistribution(FRational rate, int nChip, int stat) {
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

    private static int calcStat(FRational rate, int[] pts) {
        int out = 0;
        for (int pt : pts) {
            out += Chip.getMaxEffStat(rate, pt);
        }
        return out;
    }

    private static List<Integer> get56ChipCount(String name, int star) {
        int nCell = getCellCount(name, star);
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
        return chips.stream()
                .filter((c) -> c.isMarked())
                .mapToInt((c) -> c.getSize())
                .sum();
    }

    public int getMarkedChipCount() {
        return (int) chips.stream().filter((c) -> c.isMarked()).count();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Chips">
    public int getChipCount() {
        return chips.size();
    }

    public List<String> getChipIDs() {
        List<String> IDs = new ArrayList<>();
        chips.forEach((c) -> IDs.add(c.getID()));
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

    public boolean isChipUseful(Stat pt, Chip c) {
        pt.subtract(getPt()).subtract(c.getPt());
        return !pt.anyNeg();
    }

    public static boolean isChipPlaceable(PuzzleMatrix<Integer> matrix, Set<Point> cps) {
        return cps.stream().allMatch((cp)
                -> matrix.get(cp.x, cp.y) != null
                && matrix.get(cp.x, cp.y) == EMPTY
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Matrix and Cells">
    public static PuzzleMatrix<Integer> initMatrix(String name, int star) {
        PuzzleMatrix<Integer> matrix = new PuzzleMatrix<>(MATRIX_MAP.get(name));
        for (int r = 0; r < matrix.getNumRow(); r++) {
            for (int c = 0; c < matrix.getNumCol(); c++) {
                matrix.set(r, c, matrix.get(r, c) <= star ? EMPTY : UNUSED);
            }
        }
        return matrix;
    }

    public Point getLocation(Chip c) {
        int i = chips.indexOf(c);
        if (i < 0) {
            return null;
        }
        return matrix.getPivot(i);
    }

    public static int getCellCount(String name, int star) {
        PuzzleMatrix<Integer> s = initMatrix(name, star);
        return s.getNumNotContaining(UNUSED);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image">
    public final void colorChips() {
        for (int i = 0; i < chips.size(); i++) {
            Chip c = chips.get(i);
            c.setResultIndex(i);
        }
    }

    public static ImageIcon getImage(App app, int size, String name, int star) {
        return new ImageIcon(generateImage(app, size, initMatrix(name, star)));
    }

    public ImageIcon getImage(App app, int size) {
        return new ImageIcon(generateImage(app, size, matrix));
    }

    private static BufferedImage generateImage(App app, int size, PuzzleMatrix<Integer> matrix) {
        int tileSize = size / 8;
        int h = HEIGHT;
        int w = WIDTH;
        BufferedImage i = new BufferedImage(
                h * tileSize + 1,
                w * tileSize + 1,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int s = matrix.get(row, col);
                int x = col * tileSize;
                int y = row * tileSize;

                // Tiles
                g.setColor(s == UNUSED ? Color.BLACK : s == EMPTY ? Color.WHITE : app.colors()[s % app.colors().length]);
                g.fillRect(x, y, tileSize, tileSize);

                // Horizontal Border
                g.setColor(Color.BLACK);
                if (0 < row && matrix.get(row - 1, col) != s) {
                    g.drawLine(x, y, x + tileSize, y);
                }
                // Vertical Border
                if (0 < col && matrix.get(row, col - 1) != s) {
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="File">
    public static PuzzleMatrix<Integer> toPlacement(String name, int star, List<Chip> chips, List<Point> locations) {
        List<String> names = new ArrayList<>();
        List<Integer> rotations = new ArrayList<>();
        chips.forEach((c) -> {
            names.add(c.getName());
            rotations.add(c.getRotation());
        });
        return toPlacement(name, star, names, rotations, locations);
    }

    public static PuzzleMatrix<Integer> toPlacement(String name, int star, List<String> names, List<Integer> rotations, List<Point> locations) {
        // Placement
        PuzzleMatrix<Integer> placement = initMatrix(name, star);
        for (int i = 0; i < names.size(); i++) {
            PuzzleMatrix<Boolean> matrix = Chip.generateMatrix(names.get(i), rotations.get(i));
            Set<Point> pts = matrix.getCoords(true);
            Point fp = matrix.getPivot(true);
            Point bp = locations.get(i);
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
