package main.puzzle;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import main.ui.resource.AppColor;
import main.ui.resource.AppText;
import main.util.DoubleKeyHashMap;
import main.util.Fn;
import main.util.IO;
import main.util.Rational;

/**
 *
 * @author Bunnyspa
 */
public class Board implements Comparable<Board>, Serializable {

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
    public static final String NAME_MK153 = "Mk 153";
    public static String[] NAMES = {NAME_BGM71, NAME_AGS30, NAME_2B14, NAME_M2, NAME_AT4, NAME_QLZ04, NAME_MK153};

    public static String getTrueName(String fileName) {
        for (String name : NAMES) {
            if (IO.toFileName(name).equals(fileName)) {
                return name;
            }
        }
        return "";
    }

    private static final Map<String, Integer[][]> MAP_MATRIX = new HashMap<String, Integer[][]>() // <editor-fold defaultstate="collapsed">
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
            put(NAME_MK153, new Integer[][]{
                {6, 6, 2, 2, 6, 6, 6, 6},
                {6, 6, 2, 2, 5, 5, 5, 6},
                {6, 6, 2, 2, 4, 4, 4, 6},
                {6, 6, 2, 2, 3, 3, 4, 6},
                {1, 1, 1, 1, 1, 1, 3, 3},
                {1, 1, 1, 1, 1, 1, 3, 3},
                {6, 5, 1, 1, 6, 6, 6, 6},
                {6, 6, 1, 1, 6, 6, 6, 6}
            });
        }
    }; // </editor-fold>
    private static final Map<String, Integer> MAP_COLOR = new HashMap<String, Integer>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, Chip.COLOR_BLUE);
            put(NAME_AGS30, Chip.COLOR_ORANGE);
            put(NAME_2B14, Chip.COLOR_ORANGE);
            put(NAME_M2, Chip.COLOR_BLUE);
            put(NAME_AT4, Chip.COLOR_BLUE);
            put(NAME_QLZ04, Chip.COLOR_ORANGE);
            put(NAME_MK153, Chip.COLOR_BLUE);
        }
    }; // </editor-fold>
    private static final Map<String, Stat> MAP_STAT_UNIT = new HashMap<String, Stat>() // <editor-fold defaultstate="collapsed"> 
    {
        {
            put(NAME_BGM71, new Stat(155, 402, 349, 83));
            put(NAME_AGS30, new Stat(78, 144, 198, 386));
            put(NAME_2B14, new Stat(152, 58, 135, 160));
            put(NAME_M2, new Stat(113, 49, 119, 182));
            put(NAME_AT4, new Stat(113, 261, 284, 134));
            put(NAME_QLZ04, new Stat(77, 136, 188, 331));
            put(NAME_MK153, new Stat(107, 224, 233, 107));
        }
    }; // </editor-fold>
    private static final Map<String, Stat[]> MAP_STAT_CHIP = new HashMap<String, Stat[]>() // <editor-fold defaultstate="collapsed"> 
    {
        {
            put(NAME_BGM71, new Stat[]{
                new Stat(95, 165, 96, 23),
                new Stat(114, 198, 115, 28),
                new Stat(133, 231, 134, 32),
                new Stat(162, 280, 162, 39),
                new Stat(190, 329, 191, 46)
            });
            put(NAME_AGS30, new Stat[]{
                new Stat(53, 65, 60, 117),
                new Stat(64, 78, 72, 140),
                new Stat(75, 91, 84, 163),
                new Stat(90, 111, 102, 198),
                new Stat(106, 130, 120, 233)
            });
            put(NAME_2B14, new Stat[]{
                new Stat(114, 29, 45, 54),
                new Stat(136, 35, 54, 64),
                new Stat(159, 41, 63, 75),
                new Stat(193, 49, 77, 91),
                new Stat(227, 58, 90, 107)
            });
            put(NAME_M2, new Stat[]{
                new Stat(103, 30, 49, 74),
                new Stat(124, 36, 59, 89),
                new Stat(145, 42, 68, 104),
                new Stat(176, 51, 83, 126),
                new Stat(206, 60, 97, 148)
            });
            put(NAME_AT4, new Stat[]{
                new Stat(85, 131, 95, 45),
                new Stat(102, 157, 114, 54),
                new Stat(118, 183, 133, 63),
                new Stat(144, 222, 161, 76),
                new Stat(169, 261, 190, 90)
            });
            put(NAME_QLZ04, new Stat[]{
                new Stat(61, 72, 66, 117),
                new Stat(73, 86, 79, 140),
                new Stat(85, 100, 93, 163),
                new Stat(103, 122, 112, 198),
                new Stat(122, 143, 132, 233)
            });
            put(NAME_MK153, new Stat[]{
                new Stat(98, 137, 95, 44),
                new Stat(117, 164, 114, 52),
                new Stat(137, 191, 133, 61),
                new Stat(166, 232, 162, 74),
                new Stat(195, 273, 190, 87)
            });
        }
    }; // </editor-fold>
    public static final DoubleKeyHashMap<String, Integer, Stat> MAP_STAT_RESONANCE = new DoubleKeyHashMap<String, Integer, Stat>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(16, 0, 6, 0));
                    put(10, new Stat(0, 8, 0, 3));
                    put(16, new Stat(36, 0, 8, 0));
                    put(22, new Stat(0, 14, 10, 0));
                    put(28, new Stat(46, 0, 0, 6));
                    put(32, new Stat(0, 18, 14, 0));
                    put(36, new Stat(60, 26, 0, 0));
                }
            });
            put(NAME_AGS30, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(8, 0, 4, 0));
                    put(10, new Stat(0, 4, 0, 8));
                    put(16, new Stat(14, 0, 6, 0));
                    put(24, new Stat(0, 8, 0, 10));
                    put(30, new Stat(26, 0, 12, 0));
                    put(34, new Stat(0, 14, 0, 12));
                    put(38, new Stat(36, 0, 0, 16));
                }
            });
            put(NAME_2B14, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(16, 0, 6, 0));
                    put(10, new Stat(0, 3, 0, 5));
                    put(16, new Stat(36, 0, 0, 0));
                    put(20, new Stat(0, 4, 8, 0));
                    put(24, new Stat(58, 0, 0, 7));
                    put(28, new Stat(0, 8, 0, 10));
                    put(32, new Stat(82, 0, 8, 0));
                }
            });
            put(NAME_M2, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(13, 0, 6, 0));
                    put(10, new Stat(0, 3, 0, 6));
                    put(16, new Stat(30, 0, 0, 0));
                    put(20, new Stat(0, 4, 8, 0));
                    put(24, new Stat(48, 0, 0, 9));
                    put(28, new Stat(0, 8, 0, 13));
                    put(32, new Stat(68, 0, 8, 0));
                }
            });
            put(NAME_AT4, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(12, 0, 5, 0));
                    put(10, new Stat(0, 5, 0, 5));
                    put(16, new Stat(27, 0, 7, 0));
                    put(22, new Stat(0, 10, 9, 0));
                    put(28, new Stat(35, 0, 0, 10));
                    put(32, new Stat(0, 12, 12, 0));
                    put(36, new Stat(46, 18, 0, 0));
                }
            });
            put(NAME_QLZ04, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(9, 0, 6, 0));
                    put(10, new Stat(0, 6, 0, 6));
                    put(16, new Stat(15, 0, 6, 0));
                    put(24, new Stat(0, 9, 0, 9));
                    put(30, new Stat(28, 0, 12, 0));
                    put(34, new Stat(0, 15, 0, 10));
                    put(38, new Stat(38, 0, 0, 14));
                }
            });
            put(NAME_MK153, new HashMap<Integer, Stat>() {
                {
                    put(4, new Stat(24, 0, 6, 0));
                    put(10, new Stat(0, 12, 0, 10));
                    put(16, new Stat(24, 0, 6, 0));
                    put(24, new Stat(0, 12, 12, 0));
                    put(30, new Stat(32, 0, 0, 10));
                    put(34, new Stat(0, 18, 12, 0));
                    put(38, new Stat(32, 18, 0, 0));
                }
            });
        }
    }; // </editor-fold>
    private static final Map<String, Stat[]> MAP_STAT_ITERATION = new HashMap<String, Stat[]>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(NAME_BGM71, new Stat[]{
                new Stat(4, 0, 6, 0),
                new Stat(0, 10, 9, 0),
                new Stat(5, 0, 0, 6),
                new Stat(0, 12, 10, 0),
                new Stat(7, 15, 0, 0),
                new Stat(8, 0, 12, 0),
                new Stat(0, 16, 14, 0),
                new Stat(9, 0, 0, 10),
                new Stat(0, 18, 18, 0),
                new Stat(12, 24, 0, 0)
            });
            put(NAME_AGS30, new Stat[]{
                new Stat(2, 0, 5, 0),
                new Stat(0, 4, 0, 8),
                new Stat(3, 0, 8, 0),
                new Stat(0, 6, 0, 12),
                new Stat(3, 0, 0, 13),
                new Stat(4, 0, 12, 0),
                new Stat(0, 10, 0, 12),
                new Stat(4, 0, 16, 0),
                new Stat(0, 16, 0, 16),
                new Stat(8, 0, 0, 18)
            });
            put(NAME_2B14, new Stat[]{
                new Stat(2, 0, 4, 0),
                new Stat(2, 3, 0, 4),
                new Stat(3, 0, 6, 0),
                new Stat(4, 4, 0, 4),
                new Stat(5, 0, 0, 5),
                new Stat(6, 0, 9, 0),
                new Stat(7, 3, 0, 6),
                new Stat(7, 0, 10, 0),
                new Stat(4, 5, 0, 9),
                new Stat(10, 0, 0, 6)
            });
            put(NAME_M2, new Stat[]{
                new Stat(2, 0, 4, 0),
                new Stat(1, 3, 0, 5),
                new Stat(3, 0, 6, 0),
                new Stat(3, 4, 0, 5),
                new Stat(4, 0, 0, 6),
                new Stat(5, 0, 9, 0),
                new Stat(6, 2, 0, 8),
                new Stat(6, 0, 9, 0),
                new Stat(3, 5, 0, 11),
                new Stat(8, 0, 0, 8)
            });
            put(NAME_AT4, new Stat[]{
                new Stat(3, 0, 5, 0),
                new Stat(0, 7, 8, 0),
                new Stat(4, 0, 0, 10),
                new Stat(0, 8, 9, 0),
                new Stat(5, 10, 0, 0),
                new Stat(6, 0, 10, 0),
                new Stat(0, 11, 12, 0),
                new Stat(7, 0, 0, 17),
                new Stat(0, 12, 15, 0),
                new Stat(9, 16, 0, 0)
            });
            put(NAME_QLZ04, new Stat[]{
                new Stat(3, 0, 3, 0),
                new Stat(2, 3, 0, 4),
                new Stat(4, 0, 6, 0),
                new Stat(3, 3, 0, 4),
                new Stat(5, 0, 0, 5),
                new Stat(5, 0, 10, 0),
                new Stat(6, 4, 0, 6),
                new Stat(6, 0, 10, 0),
                new Stat(4, 6, 0, 10),
                new Stat(8, 0, 0, 8)
            });
            put(NAME_MK153, new Stat[]{
                new Stat(4, 0, 4, 0),
                new Stat(0, 8, 6, 0),
                new Stat(6, 0, 0, 10),
                new Stat(0, 8, 8, 0),
                new Stat(6, 6, 0, 0),
                new Stat(10, 0, 10, 0),
                new Stat(0, 10, 10, 0),
                new Stat(10, 0, 0, 10),
                new Stat(0, 12, 12, 0),
                new Stat(16, 16, 0, 0)
            });
        }
    }; // </editor-fold>
    private static final DoubleKeyHashMap<String, Integer, Integer> MAP_ROTATIONSTEP = new DoubleKeyHashMap<String, Integer, Integer>() // <editor-fold defaultstate="collapsed">
    {
        {
            // generateRotationStep();
            put(NAME_BGM71, 1, 1);
            put(NAME_BGM71, 2, 2);
            put(NAME_BGM71, 3, 2);
            put(NAME_BGM71, 4, 4);
            put(NAME_BGM71, 5, 1);
            put(NAME_AGS30, 1, 1);
            put(NAME_AGS30, 2, 1);
            put(NAME_AGS30, 3, 2);
            put(NAME_AGS30, 4, 2);
            put(NAME_AGS30, 5, 2);
            put(NAME_2B14, 1, 1);
            put(NAME_2B14, 2, 2);
            put(NAME_2B14, 3, 2);
            put(NAME_2B14, 4, 2);
            put(NAME_2B14, 5, 2);
            put(NAME_M2, 1, 2);
            put(NAME_M2, 2, 2);
            put(NAME_M2, 3, 2);
            put(NAME_M2, 4, 2);
            put(NAME_M2, 5, 2);
            put(NAME_AT4, 1, 4);
            put(NAME_AT4, 2, 4);
            put(NAME_AT4, 3, 4);
            put(NAME_AT4, 4, 4);
            put(NAME_AT4, 5, 1);
            put(NAME_QLZ04, 1, 4);
            put(NAME_QLZ04, 2, 4);
            put(NAME_QLZ04, 3, 4);
            put(NAME_QLZ04, 4, 4);
            put(NAME_QLZ04, 5, 4);
            put(NAME_MK153, 1, 4);
            put(NAME_MK153, 2, 4);
            put(NAME_MK153, 3, 4);
            put(NAME_MK153, 4, 4);
            put(NAME_MK153, 5, 4);
        }
    };

    private static void generateRotationStep() {
        for (String name : NAMES) {
            for (int star = 1; star <= 5; star++) {
                PuzzleMatrix<Integer> unrotated = initMatrix(name, star);
                for (int i = 1; i <= 4; i++) {
                    PuzzleMatrix<Integer> b = initMatrix(name, star);
                    b.rotateContent(i, UNUSED);
                    if (unrotated.equals(b)) {
                        System.out.println("put(\"" + name + "\"," + star + "," + i + ");");
                        break;
                    }
                }
            }
        }
    }
    // </editor-fold>

    private final String name;
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
        this.name = board.name;
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

    // Combination File / Board Template
    public Board(String name, int star, Stat maxStat, List<Chip> chips, List<Point> chipLocs) {
        this.name = name;
        this.star = star;

        this.chips = new ArrayList<>(chips);
        colorChips();

        this.matrix = toPlacement(name, star, chips, chipLocs);
        this.maxStat = maxStat;

        this.stat = Stat.chipStatSum(chips);
        this.pt = Stat.chipPtSum(chips);

        statPerc = getStatPerc(this.stat, this.maxStat);

        int xpSum = 0;
        for (Chip chip : chips) {
            xpSum += chip.getCumulXP();
        }
        xp = xpSum;
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
    public final int getColor() {
        return MAP_COLOR.get(name);
    }

    public static int getColor(String name) {
        if (MAP_COLOR.containsKey(name)) {
            return MAP_COLOR.get(name);
        }
        return -1;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Rotation and Ticket">
    private void rotate(int i) {
        matrix.rotateContent(i, UNUSED);
        for (Chip chip : chips) {
            chip.rotate(i);
        }
    }

    public int getTicketCount() {
        int sum = 0;
        for (Chip chip : chips) {
            sum += chip.getNumTicket();
        }
        return sum;
    }

    public void minimizeTicket() {
        for (int rotation = 0; rotation < 4; rotation += MAP_ROTATIONSTEP.get(name, star)) {
            // Start a new board
            Board b = new Board(this);
            Set<Shape> cShapes = new HashSet<>();
            Chip[] newUsedChips = new Chip[b.chips.size()];
            // Rotate board
            b.rotate(rotation);

            for (Chip chip : b.chips) {
                cShapes.add(chip.getShape());
            }

            // Get indicies and candidates
            for (Shape cs : cShapes) {
                Set<Integer> cIndices = new HashSet<>();
                List<Chip> cCandidates = new ArrayList<>();
                for (int i = 0; i < b.chips.size(); i++) {
                    Chip c = b.chips.get(i);
                    if (c.getShape() == cs) {
                        cIndices.add(i);
                        cCandidates.add(new Chip(c));
                    }
                }
                // Put matching initial rotation
                for (Integer cIndex : cIndices) {
                    int r = b.chips.get(cIndex).getRotation();
                    for (Chip c : cCandidates) {
                        if (c.getInitRotation() == r) {
                            c.setRotation(c.getInitRotation());
                            newUsedChips[cIndex] = c;
                            cCandidates.remove(c);
                            break;
                        }
                    }
                }
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
            }
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
        return pt;
    }

    public static Stat getMaxPt(String name, int star) {
        return getMaxPt(name, star, getMaxStat(name, star));
    }

    public static Stat getMaxPt(String name, int star, Stat stat) {
        int[] statArray = stat.toArray();
        int[] optimalPtArray = new int[4];

        for (Integer nChip : get56ChipCount(name, star)) {
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

        int residue = getCellCount(name, star)
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
        return getMaxStat(name, star);
    }

    public static Stat getMaxStat(String name, int star) {
        return MAP_STAT_CHIP.get(name)[Fn.limit(star - 1, 0, MAP_STAT_CHIP.get(name).length)];
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
    public static Stat getHOCStat(String name) {
        return MAP_STAT_UNIT.get(name);
    }

    public Stat getResonance() {
        int numCell = 0;
        for (Chip chip : chips) {
            if (chip.getColor() == getColor()) {
                numCell += chip.getSize();
            }
        }

        List<Stat> stats = new ArrayList<>();
        for (int key : MAP_STAT_RESONANCE.keySet(name)) {
            if (key <= numCell) {
                stats.add(MAP_STAT_RESONANCE.get(name, key));
            }
        }
        return new Stat(stats);
    }

    public static Stat getVersionStat(String name, int v) {
        List<Stat> stats = new ArrayList<>(v);
        Stat[] array = MAP_STAT_ITERATION.get(name);
        stats.addAll(Arrays.asList(array).subList(0, v));
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
    public static PuzzleMatrix<Integer> initMatrix(String name, int star) {
        PuzzleMatrix<Integer> matrix = new PuzzleMatrix<>(MAP_MATRIX.get(name));
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

    public static int getCellCount(String name, int star) {
        PuzzleMatrix<Integer> s = initMatrix(name, star);
        return s.getNumNotContaining(UNUSED);
    }

    public static boolean rs_isValid(String name, int star, String data) {
        String[] split = data.split(";");
        String[] shapeStrs = split[0].split(",");
        Integer[] rotations = Stream.of(split[1].split(","))
                .map(Integer::valueOf)
                .toArray(Integer[]::new);
        Point[] locations = Stream.of(split[2].split(","))
                .map((s) -> s.split("\\."))
                .map((sp) -> new Point(Integer.valueOf(sp[0]), Integer.valueOf(sp[1])))
                .toArray(Point[]::new);
        PuzzleMatrix<Boolean> board = rs_getBoolMatrix(name, star);
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

    private static PuzzleMatrix<Boolean> rs_getBoolMatrix(String name, int star) {
        Integer[][] im = MAP_MATRIX.get(name);
        PuzzleMatrix<Boolean> bm = new PuzzleMatrix<>(HEIGHT, WIDTH, false);
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                bm.set(r, c, im[r][c] <= star);
            }
        }
        return bm;
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
    public static PuzzleMatrix<Integer> toPlacement(String name, int star, List<Chip> chips, List<Point> locations) {
        List<Puzzle> puzzles = new ArrayList<>(chips.size());
        for (int i = 0; i < chips.size(); i++) {
            Chip c = chips.get(i);
            Shape s = c.getShape();
            int r = c.getRotation();
            Point l = locations.get(i);
            puzzles.add(new Puzzle(s, r, l));
        }

        return toPlacement(name, star, puzzles);
    }

    public static PuzzleMatrix<Integer> toPlacement(String name, int star, List<Puzzle> puzzles) {
        // Placement
        PuzzleMatrix<Integer> placement = initMatrix(name, star);
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
