package main.puzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.swing.ImageIcon;
import main.App;
import main.resource.Language;
import main.resource.Resources;
import main.util.FRational;
import main.util.Fn;
import main.util.IO;
import main.util.Rational;
import main.util.Version3;

/**
 *
 * @author Bunnyspa
 */
public class Chip implements Serializable {

    private static final boolean O = true;
    private static final boolean X = false;

    public static final FRational RATE_DMG = new FRational(44, 10);
    public static final FRational RATE_BRK = new FRational(127, 10);
    public static final FRational RATE_HIT = new FRational(71, 10);
    public static final FRational RATE_RLD = new FRational(57, 10);
    public static final FRational[] RATES = {Chip.RATE_DMG, Chip.RATE_BRK, Chip.RATE_HIT, Chip.RATE_RLD};

    public static final String TYPE_6 = "6";
    public static final String TYPE_5B = "5B";
    public static final String TYPE_5A = "5A";
    public static final String TYPE_4 = "4";
    public static final String TYPE_3 = "3";
    public static final String TYPE_2 = "2";
    public static final String TYPE_1 = "1";
    public static final String[] TYPES = {TYPE_6, TYPE_5B, TYPE_5A, TYPE_4, TYPE_3, TYPE_2, TYPE_1};

    public static final String STR_STAR = Board.STR_STAR_FULL;

    private static final String[] NAMES_6 = {"6R", "6C", "6I", "6T", "6Y", "6Zm", "6Z", "6D", "6A", "6O"};
    private static final String[] NAMES_5B = {"5Fm", "5F", "5T", "5X", "5Y", "5Ym", "5N", "5Nm", "5W"};
    private static final String[] NAMES_5A = {"5Lm", "5L", "5V", "5Zm", "5Z", "5C", "5I", "5P", "5Pm"};
    // public static final String[] NAMES_5 = Fn.concatAll(NAMES_5B, NAMES_5A);
    private static final String[] NAMES_4 = {"4T", "4Z", "4Zm", "4L", "4Lm", "4O", "4I"};
    private static final String[] NAMES_3 = {"3L", "3I"};
    private static final String[] NAMES_2 = {"2"};
    private static final String[] NAMES_1 = {"1"};
    private static final String[][] NAMES_N = {NAMES_6, NAMES_5B, NAMES_5A, NAMES_4, NAMES_3, NAMES_2, NAMES_1};
    public static final String NAME_DEFAULT = NAMES_1[0];

    private static final Map<String, Boolean[][]> CHIP_MATRIX_MAP = new HashMap<String, Boolean[][]>() // <editor-fold defaultstate="collapsed">
    {
        {
            // 1
            put("1", new Boolean[][]{
                {O}
            });

            // 2
            put("2", new Boolean[][]{
                {O},
                {O}
            });

            // 3
            put("3I", new Boolean[][]{
                {O},
                {O},
                {O}
            });
            put("3L", new Boolean[][]{
                {O, X},
                {O, O}
            });

            // 4
            put("4I", new Boolean[][]{
                {O, O, O, O}
            });
            put("4L", new Boolean[][]{
                {O, X},
                {O, X},
                {O, O}
            });
            put("4Lm", new Boolean[][]{
                {O, X, X},
                {O, O, O}
            });
            put("4O", new Boolean[][]{
                {O, O},
                {O, O}
            });
            put("4T", new Boolean[][]{
                {X, O, X},
                {O, O, O}
            });
            put("4Z", new Boolean[][]{
                {O, O, X},
                {X, O, O}
            });
            put("4Zm", new Boolean[][]{
                {X, O, O},
                {O, O, X}
            });

            // 5A
            put("5C", new Boolean[][]{
                {O, O, O},
                {O, X, O}
            });
            put("5I", new Boolean[][]{
                {O, O, O, O, O}
            });
            put("5L", new Boolean[][]{
                {O, X},
                {O, X},
                {O, X},
                {O, O}
            });
            put("5Lm", new Boolean[][]{
                {X, O},
                {X, O},
                {X, O},
                {O, O}
            });
            put("5P", new Boolean[][]{
                {X, O},
                {O, O},
                {O, O}
            });
            put("5Pm", new Boolean[][]{
                {O, X},
                {O, O},
                {O, O}
            });
            put("5V", new Boolean[][]{
                {O, X, X},
                {O, X, X},
                {O, O, O}
            });
            put("5Z", new Boolean[][]{
                {X, X, O},
                {O, O, O},
                {O, X, X}
            });
            put("5Zm", new Boolean[][]{
                {O, X, X},
                {O, O, O},
                {X, X, O}
            });

            // 5B
            put("5F", new Boolean[][]{
                {O, X, X},
                {O, O, O},
                {X, O, X}
            });
            put("5Fm", new Boolean[][]{
                {X, X, O},
                {O, O, O},
                {X, O, X}
            });
            put("5N", new Boolean[][]{
                {X, O},
                {O, O},
                {O, X},
                {O, X}
            });
            put("5Nm", new Boolean[][]{
                {O, X},
                {O, O},
                {X, O},
                {X, O}
            });
            put("5T", new Boolean[][]{
                {X, O, X},
                {X, O, X},
                {O, O, O}
            });
            put("5W", new Boolean[][]{
                {X, O, O},
                {O, O, X},
                {O, X, X}
            });
            put("5X", new Boolean[][]{
                {X, O, X},
                {O, O, O},
                {X, O, X}
            });
            put("5Y", new Boolean[][]{
                {X, O},
                {O, O},
                {X, O},
                {X, O}
            });
            put("5Ym", new Boolean[][]{
                {O, X},
                {O, O},
                {O, X},
                {O, X}
            });

            // 6
            put("6A", new Boolean[][]{
                {O, X, X},
                {O, O, X},
                {O, O, O}
            });
            put("6C", new Boolean[][]{
                {O, X, X, O},
                {O, O, O, O}
            });
            put("6D", new Boolean[][]{
                {X, O, O, X},
                {O, O, O, O}
            });
            put("6I", new Boolean[][]{
                {O, O, O, O, O, O}
            });
            put("6O", new Boolean[][]{
                {O, O},
                {O, O},
                {O, O}
            });
            put("6R", new Boolean[][]{
                {X, O, X},
                {O, O, O},
                {O, O, X}
            });
            put("6T", new Boolean[][]{
                {X, X, O, X},
                {O, O, O, O},
                {X, X, O, X}
            });
            put("6Y", new Boolean[][]{
                {X, O, X},
                {O, O, O},
                {O, X, O}
            });
            put("6Z", new Boolean[][]{
                {O, O, O, X},
                {X, O, O, O}
            });
            put("6Zm", new Boolean[][]{
                {X, O, O, O},
                {O, O, O, X}
            });
        }
    }; // </editor-fold>
    private static final Map<String, Integer> CHIP_ROTATION_MAP = new HashMap<String, Integer>() // <editor-fold defaultstate="collapsed">
    {
        {
            for (String[] names : NAMES_N) {
                for (String name : names) {
                    PuzzleMatrix<Boolean> a = new PuzzleMatrix<>(CHIP_MATRIX_MAP.get(name));
                    for (int i = 1; i <= 4; i++) {
                        PuzzleMatrix<Boolean> b = new PuzzleMatrix<>(CHIP_MATRIX_MAP.get(name));
                        b.rotate(i);
                        if (a.equals(b)) {
                            put(name, i);
                            break;
                        }
                    }
                }
            }
        }
    }; // </editor-fold>

    public static final int COLOR_NA = -1;
    public static final int COLOR_ORANGE = 0;
    public static final int COLOR_BLUE = 1;

    public static final Color COLOR_STAR = new Color(255, 170, 0);
    public static final Color COLOR_LEVEL = new Color(10, 205, 171);

    public static final Map<Integer, Color> COLORS = new HashMap<Integer, Color>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(Chip.COLOR_ORANGE, new Color(240, 107, 65));
            put(Chip.COLOR_BLUE, new Color(111, 137, 218));
        }
    }; // </editor-fold>
    public static final Map<Integer, String> COLORSTRS = new HashMap<Integer, String>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(COLOR_ORANGE, Language.CHIP_COLOR_ORANGE);
            put(COLOR_BLUE, Language.CHIP_COLOR_BLUE);
        }
    }; // </editor-fold>

    public static final boolean COUNTERCLOCKWISE = true;
    public static final boolean CLOCKWISE = false;

    public static final int SIZE_MAX = 6;
    public static final int LEVEL_MAX = 20;
    public static final int PT_MAX = 5;

    public static final int STAR_MIN = 2;
    public static final int STAR_MAX = 5;

    private static final int STAT = 0;
    private static final int PT = 1;

    private final String id;
    private final String name;

    private final Stat pt;
    private int initRotation, rotation, initLevel, level, star, color, displayType;
    private int combinationIndex = -1;
    private boolean marked;
    private boolean imageUpdated;
    private boolean matrixUpdated;
    private PuzzleMatrix<Boolean> matrix;
    private ImageIcon icon;
    private final Set<Tag> tags;

    // Pool init
    public Chip(String name) {
        this.id = null;
        this.name = name;
        pt = null;
        tags = new HashSet<>();
    }

    // Pool to inventory init
    public Chip(Chip c, int star, int color) {
        this.id = UUID.randomUUID().toString();
        name = c.name;
        rotation = c.rotation;
        initRotation = c.initRotation;

        pt = new Stat();
        this.star = star;
        this.color = color;

        tags = new HashSet<>();
    }

    // Chip deep copy
    public Chip(Chip c) {
        id = c.getID();
        name = c.name;
        star = c.star;
        color = c.color;

        pt = new Stat(c.pt);

        initLevel = c.initLevel;
        level = c.level;

        initRotation = c.initRotation;
        rotation = c.rotation;

        matrixUpdated = c.matrixUpdated;
        imageUpdated = c.imageUpdated;

        displayType = c.displayType;
        marked = c.marked;
        tags = new HashSet<>(c.tags);
    }

    public static final int INVENTORY = 0;
    public static final int COMBINATION = 1;

    // Pre 5.3.0
    public Chip(Version3 version, String[] data, int type) {
        if (version.isCurrent(4, 0, 0)) {
            // 4.0.0+
            int i = 0;
            id = data.length > i ? data[i] : UUID.randomUUID().toString();
            i++;
            name = data.length > i ? data[i] : "";
            i++;

            int dmgPt = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, getMaxPt()) : 0;
            i++;
            int brkPt = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, getMaxPt()) : 0;
            i++;
            int hitPt = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, getMaxPt()) : 0;
            i++;
            int rldPt = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, getMaxPt()) : 0;
            i++;
            pt = new Stat(dmgPt, brkPt, hitPt, rldPt);

            rotation = data.length > i ? Integer.parseInt(data[i]) % getMaxRotation() : 0;
            i++;

            if (type == INVENTORY) {
                initRotation = rotation;
                marked = data.length > i && "1".equals(data[i]);
                combinationIndex = -1;
            } else {
                initRotation = data.length > i ? Integer.parseInt(data[i]) % getMaxRotation() : 0;
            }
            i++;

            star = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 2, 5) : 5;
            i++;

            level = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, LEVEL_MAX) : 0;
            i++;
            if (version.isCurrent(4, 7, 0) && type == COMBINATION) {
                initLevel = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, LEVEL_MAX) : level;
                i++;
            } else {
                initLevel = level;
            }

            color = data.length > i ? Fn.limit(Integer.parseInt(data[i]), 0, COLORSTRS.size()) : 0;
            i++;

            tags = new HashSet<>();
            if (type == INVENTORY && data.length > i) {
                for (String tagStr : data[i].split(",")) {
                    tags.add(IO.parseTag(tagStr));
                }
            }
        } else {
            // 1.0.0 - 3.0.0
            if (type == INVENTORY) {
                name = data.length > 0 ? data[0] : "";
                rotation = data.length > 1 ? Integer.parseInt(data[1]) % getMaxRotation() : 0;
                initRotation = rotation;

                int dmgPt = data.length > 2 ? Fn.limit(Integer.parseInt(data[2]), 0, getMaxPt()) : 0;
                int brkPt = data.length > 3 ? Fn.limit(Integer.parseInt(data[3]), 0, getMaxPt()) : 0;
                int hitPt = data.length > 4 ? Fn.limit(Integer.parseInt(data[4]), 0, getMaxPt()) : 0;
                int rldPt = data.length > 5 ? Fn.limit(Integer.parseInt(data[5]), 0, getMaxPt()) : 0;
                pt = new Stat(dmgPt, brkPt, hitPt, rldPt);

                star = data.length > 6 ? Fn.limit(Integer.parseInt(data[6]), 2, 5) : 5;
                level = data.length > 7 ? Fn.limit(Integer.parseInt(data[7]), 0, LEVEL_MAX) : 0;
                color = data.length > 8 ? Fn.limit(Integer.parseInt(data[8]), 0, COLORSTRS.size()) : 0;

                marked = data.length > 9 && "1".equals(data[9]);
                id = data.length > 10 ? data[10] : UUID.randomUUID().toString();

                combinationIndex = -1;
            } else {
                name = data.length > 0 ? data[0] : "";
                rotation = data.length > 1 ? Integer.parseInt(data[1]) % getMaxRotation() : 0;
                initRotation = data.length > 2 ? Integer.parseInt(data[2]) % getMaxRotation() : 0;

                star = data.length > 3 ? Fn.limit(Integer.parseInt(data[3]), 2, 5) : 5;
                level = data.length > 4 ? Fn.limit(Integer.parseInt(data[4]), 0, LEVEL_MAX) : 0;
                color = data.length > 5 ? Fn.limit(Integer.parseInt(data[5]), 0, COLORSTRS.size()) : 0;

                int dmgPt = data.length > 6 ? Fn.limit(Integer.parseInt(data[6]), 0, getMaxPt()) : 0;
                int brkPt = data.length > 7 ? Fn.limit(Integer.parseInt(data[7]), 0, getMaxPt()) : 0;
                int hitPt = data.length > 8 ? Fn.limit(Integer.parseInt(data[8]), 0, getMaxPt()) : 0;
                int rldPt = data.length > 9 ? Fn.limit(Integer.parseInt(data[9]), 0, getMaxPt()) : 0;

                id = data.length > 10 ? data[10] : UUID.randomUUID().toString();

                pt = new Stat(dmgPt, brkPt, hitPt, rldPt);
            }
            tags = new HashSet<>();
        }
    }

    // ImageProcessor
    public Chip(String name, int star, int color, Stat pt,
            int level, int rotation) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.star = star;
        this.color = color;

        this.pt = pt;

        this.initLevel = level;
        this.level = level;
        this.initRotation = rotation;
        this.rotation = rotation;

        this.tags = new HashSet<>();
    }

    // json (Inventory)
    public Chip(String id, String name, int star, int color, Stat pt,
            int level, int rotation) {
        this.id = id;
        this.name = name;
        this.star = star;
        this.color = color;

        this.pt = pt;

        this.initLevel = level;
        this.level = level;
        this.initRotation = rotation;
        this.rotation = rotation;

        this.tags = new HashSet<>();
    }

    public Chip(String id,
            String name, int star, int color, Stat pt,
            int level, int rotation,
            boolean marked, Set<Tag> tags) {
        this.id = id;
        this.name = name;
        this.star = star;
        this.color = color;

        this.pt = pt;

        this.initLevel = level;
        this.level = level;
        this.initRotation = rotation;
        this.rotation = rotation;

        this.marked = marked;
        this.tags = tags;
    }

    // <editor-fold defaultstate="collapsed" desc="ID">
    public String getID() {
        return id;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name">
    public String getName() {
        return name;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size and Type">
    private static int indexOfType(String type) {
        return Arrays.asList(TYPES).indexOf(type);
    }

    public static String[] getNames(String type) {
        return NAMES_N[indexOfType(type)];
    }

    public int getSize() {
        return getSize(name);
    }

    public static int getSize(String name) {
        return Integer.parseInt(name.substring(0, 1));
    }

    public String getType() {
        return getType(name);
    }

    public boolean typeGeq(String type) {
        if (getSize() < getSize(type)) {
            return false;
        }
        return !(TYPE_5A.equals(getType()) && TYPE_5B.equals(type));
    }

    public static String getType(String name) {
        return name.matches("5[C|I|L|V|P|Z]m?") ? TYPE_5A
                : getSize(name) == 5 ? TYPE_5B
                : name.substring(0, 1);
    }

    public static Rational getTypeMult(String type, int star) {
        int a = getSize(type) < 5 ? 16 : 20;
        int b = getSize(type) < 3 || TYPE_5A.equals(type) ? 4 : 0;
        int c = getSize(type) < 4 || TYPE_5A.equals(type) ? 4 : 0;
        return new Rational(star * a - b - (3 < star ? c : 0), 100);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Star">
    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Color">
    public void setColor(int color) {
        this.color = color;
        imageUpdated = false;
    }

    public int getColor() {
        return color;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Rotation and Ticket">
    public final int getMaxRotation() {
        return getMaxRotation(name);
    }

    public static int getMaxRotation(String name) {
        return CHIP_ROTATION_MAP.get(name);
    }

    public int getInitRotation() {
        return initRotation;
    }

    public int getRotation() {
        return rotation;
    }

    public void resetRotation() {
        this.rotation = this.initRotation;
    }

    public void setInitRotation(int rotation) {
        this.initRotation = rotation % getMaxRotation();
    }

    public void setRotation(int rotation) {
        int r = rotation % getMaxRotation();
        if (this.rotation != r) {
            this.rotation = r;
            repaint();
        }
    }

    public void initRotate(boolean direction) {
        rotation = (rotation + (direction ? 3 : 1)) % getMaxRotation();
        initRotation = rotation;
        repaint();
    }

    public void initRotate(int i) {
        rotation = (rotation + i) % getMaxRotation();
        initRotation = rotation;
        repaint();
    }

    public void rotate(int i) {
        rotation = (rotation + i) % getMaxRotation();
        repaint();
    }

    public int getNumTicket() {
        return rotation != initRotation ? getStar() * 10 : 0;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PT and Stat">
    public final int getMaxPt() {
        return getMaxPt(getSize());
    }

    public static int getMaxPt(int size) {
        return size > 4 ? size - 1 : size;
    }

    private int getTotalPts() {
        return pt.sum();
    }

    public boolean isPtValid() {
        return getTotalPts() == getSize();
    }

    public Stat getPt() {
        return pt;
    }

    public void setPt(int dmg, int brk, int hit, int rld) {
        pt.dmg = dmg;
        pt.brk = brk;
        pt.hit = hit;
        pt.rld = rld;
    }

    public boolean anyPtOver(Stat ptLimit) {
        return pt.dmg > ptLimit.dmg
                || pt.brk > ptLimit.brk
                || pt.hit > ptLimit.hit
                || pt.rld > ptLimit.rld;
    }

    public static int getPt(FRational rate, String type, int star, int level, int stat) {
        for (int pt = 0; pt < PT_MAX; pt++) {
            if (getStat(rate, type, star, level, pt) == stat) {
                return pt;
            }
        }
        return -1;
    }

    public Stat getStat() {
        int dmg = getStat(RATE_DMG, this, pt.dmg);
        int brk = getStat(RATE_BRK, this, pt.brk);
        int hit = getStat(RATE_HIT, this, pt.hit);
        int rld = getStat(RATE_RLD, this, pt.rld);
        return new Stat(dmg, brk, hit, rld);
    }

    public static int getStat(FRational rate, Chip c, int pt) {
        return getStat(rate, c.getType(), c.star, c.level, pt);
    }

    public static int getStat(FRational rate, String type, int star, int level, int pt) {
        int base = new Rational(pt).mult(rate).mult(getTypeMult(type, star)).getIntCeil();
        return getLevelMult(level).mult(base).getIntCeil();
    }

    public Stat getOldStat() {
        int dmg = getOldStat(RATE_DMG, this, pt.dmg);
        int brk = getOldStat(RATE_BRK, this, pt.brk);
        int hit = getOldStat(RATE_HIT, this, pt.hit);
        int rld = getOldStat(RATE_RLD, this, pt.rld);
        return new Stat(dmg, brk, hit, rld);
    }

    private static int getOldStat(FRational rate, Chip c, int pt) {
        return getOldStat(rate, c.name, c.star, c.level, pt);
    }

    private static int getOldStat(FRational rate, String name, int star, int level, int pt) {
        Rational base = new Rational(pt).mult(rate).mult(getTypeMult(getType(name), star));
        return getLevelMult(level).mult(base).getIntCeil();
    }

    public static Stat getPtMultStat(Chip c) {
        return new Stat(
                getStat(Chip.RATE_DMG, c, 1) * c.pt.dmg,
                getStat(Chip.RATE_BRK, c, 1) * c.pt.brk,
                getStat(Chip.RATE_HIT, c, 1) * c.pt.hit,
                getStat(Chip.RATE_RLD, c, 1) * c.pt.rld
        );
    }

    public static int getMaxEffStat(FRational rate, int pt) {
        int base = new Rational(pt).mult(rate).getIntCeil();
        return getLevelMult(LEVEL_MAX).mult(base).getIntCeil();
    }

    public static int getMaxEffStat(FRational rate, int pt, int level) {
        int base = new Rational(pt).mult(rate).getIntCeil();
        return getLevelMult(level).mult(base).getIntCeil();
    }

    public double getFitness(Stat maxSG) {
        double out = 0.0;
        int[] s = getStat().toArray();
        int[] m = maxSG.toArray();

        for (int i = 0; i < 4; i++) {
            out += (double) s[i] * m[i];
        }
        return out / getSize();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Level and XP">
    public int getLevel() {
        return level;
    }

    public int getInitLevel() {
        return initLevel;
    }

    public void resetLevel() {
        this.level = this.initLevel;
        imageUpdated = false;
    }

    public void setInitLevel(int initLevel) {
        this.initLevel = initLevel;
        this.level = initLevel;
        imageUpdated = false;
    }

    public void setMinInitLevel() {
        setInitLevel(0);
    }

    public void setMaxLevel() {
        level = LEVEL_MAX;
        imageUpdated = false;
    }

    public void setMaxInitLevel() {
        setInitLevel(LEVEL_MAX);
    }

    public static Rational getLevelMult(int level) {
        return level < 10 ? new Rational(level).mult(8, 100).add(1) : new Rational(level).mult(7, 100).add(11, 10);
    }

    public int getCumulXP() {
        int xp = 0;
        for (int l = initLevel + 1; l <= LEVEL_MAX; l++) {
            xp += getXP(star, l);
        }
        return xp;

    }

    private static int getXP(int star, int level) {
        int xp = 150 + (level - 1) * 75 + (6 <= level ? (level - 5) * 75 : 0) + (17 <= level ? 150 : 0) + (20 == level ? 150 : 0);
        return xp * Math.max(0, star - 2) / 3;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Mark">
    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
        imageUpdated = false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Tag">
    public Set<Tag> getTags() {
        return new HashSet<>(tags);
    }

    public boolean containsTag(Tag tag) {
        return tags.stream().anyMatch((t) -> (t.equals(tag)));
    }

    public void setTag(Tag t, boolean enabled) {
        if (enabled) {
            addTag(t);
        } else {
            removeTag(t);
        }
        imageUpdated = false;
    }

    private void addTag(Tag t) {
        tags.add(t);
    }

    private void removeTag(Tag t) {
        tags.remove(t);
    }

    private boolean containsHOCTagName() {
        for (String hoc : Board.NAMES) {
            if (tags.stream().anyMatch((t) -> hoc.equals(t.getName()))) {
                return true;
            }
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Matrix">
    public static PuzzleMatrix<Boolean> generateMatrix(String name, int rotation) {
        PuzzleMatrix<Boolean> matrix = new PuzzleMatrix<>(CHIP_MATRIX_MAP.get(name));
        matrix.rotate(rotation);
        return matrix;
    }

    private void generateMatrix() {
        matrix = generateMatrix(name, rotation);
    }

    private PuzzleMatrix<Boolean> getMatrix() {
        if (matrix == null || !matrixUpdated) {
            generateMatrix();
            matrixUpdated = true;
        }
        return matrix;
    }

    private int getWidth() {
        return getMatrix().getNumCol();
    }

    private int getHeight() {
        return getMatrix().getNumRow();
    }

    public Point getPivot() {
        return getMatrix().getPivot(true);
    }

    public Set<Point> getAllCoords() {
        return getMatrix().getCoords(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image">
    public void setDisplayType(int displayType) {
        this.displayType = displayType;
        imageUpdated = false;
    }

    public void repaint() {
        matrixUpdated = false;
        imageUpdated = false;
    }

    public ImageIcon getImage(App app) {
        if (!imageUpdated) {
            generateImage(app);
        }
        return icon;
    }

    private static final int TILESIZE = 12;
    private static final int GAP = 2;
    private static int HEIGHT1 = TILESIZE * 6 + GAP * 2;
    private static int HEIGHT2 = TILESIZE * 3 + GAP * 4;

    private Color getPoolColor(App app) {
        if (app == null) {
            return Color.GRAY;
        }
        if (getSize() < 5) {
            int i = (getSize() + 1) % 3;
            return i == 0 ? app.orange() : i == 1 ? app.green() : app.blue();
        }
        if (TYPE_5A.equals(getType())) {
            return app.orange();
        }
        if (TYPE_5B.equals(getType())) {
            return app.green();
        }
        return app.blue();
    }

    public void setResultIndex(int i) {
        combinationIndex = i;
        imageUpdated = false;
    }

    public static int getImageHeight(boolean isInventory) {
        return isInventory ? HEIGHT1 + HEIGHT2 : HEIGHT1;
    }

    public static int getImageWidth(boolean isInventory) {
        int width = TILESIZE * 6 + GAP * 2;
        if (isInventory) {
            width = Math.max(width, TILESIZE * 5);
        }
        return width;
    }

    private boolean showPt() {
        return null != pt;
    }

    private void generateImage(App app) {
        int width = getImageWidth(showPt());
        int height = getImageHeight(showPt());

        int yOffset1 = TILESIZE + GAP * 2;
        int yOffset2 = TILESIZE * 7 + GAP * 4;

        BufferedImage bi = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();

        if (showPt()) {
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, width, height);
            g.fillRect(0, 0, width, yOffset1 + 1);
            g.fillRect(0, yOffset2, width, height - yOffset2);
        }

        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (getMatrix().get(row, col)) {
                    int tileXOffset = (int) (width / 2 + (col - (double) getWidth() / 2) * TILESIZE);
                    int tileYOffset = (int) ((3 + row - (double) getHeight() / 2) * TILESIZE) + (showPt() ? TILESIZE + GAP * 2 : 0) + GAP;

                    g.setColor(Color.BLACK);
                    g.fillRect(tileXOffset, tileYOffset, TILESIZE + 1, TILESIZE + 1);

                    g.setColor(
                            combinationIndex > -1 ? app.colors()[combinationIndex % app.colors().length]
                                    : !showPt() ? getPoolColor(app)
                                            : isMarked() ? Chip.COLORS.get(color).darker().darker()
                                                    : Chip.COLORS.get(color)
                    );
                    g.fillRect(tileXOffset + 1, tileYOffset + 1, TILESIZE - 1, TILESIZE - 1);
                }
            }
        }

        if (showPt()) {
            g.setColor(COLOR_STAR);
            String starString = "";
            for (int i = 0; i < star; i++) {
                starString += STR_STAR;
            }
            int xOffset = width / 2;

            g.drawString(starString, GAP, TILESIZE + GAP);

            // Level
            g.setFont(Resources.FONT_DIGIT);
            if (0 < level) {
                String levelStr = (initLevel == level)
                        ? "+" + level
                        : initLevel + "→" + level;
                int levelWidth = Fn.getWidth(levelStr, g.getFont());

                g.setColor(COLOR_LEVEL);
                if (initLevel == level) {
                    g.fillPolygon(new int[]{width, width, width - TILESIZE * 2 - GAP}, new int[]{yOffset2, yOffset2 - TILESIZE * 2 - GAP, yOffset2}, 3);
                } else {
                    g.fillRect(width - levelWidth - 1, yOffset2 - TILESIZE - GAP, levelWidth + 1, TILESIZE + GAP);
                }
                g.setColor(Color.WHITE);
                g.drawString(levelStr, width - levelWidth, yOffset2 - 1);
            }

            // Equipped
            if (containsHOCTagName()) {
                g.drawImage(Resources.CHIP_EQUIPPED,
                        0, yOffset1,
                        TILESIZE, TILESIZE, null);
            }

            // Rotation
            if (initRotation != rotation) {
                g.drawImage(Resources.CHIP_ROTATED,
                        width - TILESIZE + 1, yOffset1,
                        TILESIZE, TILESIZE, null);
            }

            // Mark
            if (isMarked()) {
                g.drawImage(Resources.CHIP_MARKED,
                        0, yOffset2 - TILESIZE,
                        TILESIZE, TILESIZE, null);
            }

            if (isPtValid()) {
                int[] stats = (displayType == STAT ? getStat() : pt).toArray();
                Point[] iPts = {
                    new Point(GAP, yOffset2 + TILESIZE + GAP),
                    new Point(xOffset, yOffset2 + TILESIZE + GAP),
                    new Point(GAP, yOffset2 + GAP),
                    new Point(xOffset, yOffset2 + GAP)};
                Point[] sPts = {
                    new Point(TILESIZE + GAP + 1, yOffset2 + TILESIZE * 2 + GAP - 1),
                    new Point(TILESIZE + 1 + xOffset, yOffset2 + TILESIZE * 2 + GAP - 1),
                    new Point(TILESIZE + GAP + 1, yOffset2 + TILESIZE + GAP - 1),
                    new Point(TILESIZE + 1 + xOffset, yOffset2 + TILESIZE + GAP - 1)};

                int pi = 0;
                for (int i = 0; i < 4; i++) {
                    if (stats[i] > 0) {
                        g.setColor(Color.WHITE);
                        g.fillRect(iPts[pi].x, iPts[pi].y, TILESIZE, TILESIZE);
                        Image image = Resources.STATS[i].getImage();
                        g.drawImage(image, iPts[pi].x, iPts[pi].y, TILESIZE, TILESIZE, null);
                        g.setColor(level == 0 || displayType == PT ? Color.WHITE : COLOR_LEVEL);
                        int x = sPts[pi].x + (xOffset - TILESIZE - Fn.getWidth(String.valueOf(stats[i]), Resources.FONT_DIGIT)) / 2;
                        int y = sPts[pi].y;
                        g.drawString(String.valueOf(stats[i]), x, y);
                        pi++;
                    }
                }
            }

        }

        icon = new ImageIcon(bi);
        imageUpdated = true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Compare">
    private static class ChipComparator implements Comparator<String> {

        private final Map<String, Integer> indices;

        public ChipComparator() {
            indices = new HashMap<>();
            int i = 1;
            for (String[] names : NAMES_N) {
                for (String name : names) {
                    indices.put(name, i);
                    i++;
                }
            }
        }

        @Override
        public int compare(String o1, String o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            return indices.get(o1) - indices.get(o2);
        }
    }

    private static Comparator<String> comparator = new ChipComparator();

    public static Comparator<String> getComparator() {
        return comparator;
    }

    public static int compare(Chip c1, Chip c2) {
        return comparator.compare(c1.getName(), c2.getName());
    }

    public static int compareName(String s1, String s2) {
        return comparator.compare(s1, s2);
    }

    public static int compareType(String t1, String t2) {
        List<String> types = Arrays.asList(TYPES);
        return Integer.compare(types.indexOf(t1), types.indexOf(t2));
    }

    public static int compareStar(Chip c1, Chip c2) {
        return c1.getStar() - c2.getStar();
    }

    public static int compareLevel(Chip c1, Chip c2) {
        return c1.getLevel() - c2.getLevel();
    }
    // </editor-fold>

    public String toData() {
        String[] s = {
            id,
            name,
            String.valueOf(star),
            String.valueOf(color),
            pt.toData(),
            String.valueOf(initLevel),
            String.valueOf(initRotation),
            IO.data(marked),
            IO.data(getTags().stream().map((t) -> t.toData()), ",")
        };

        return String.join(";", s);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Chip chip = (Chip) obj;
        return this.id.equals(chip.id);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public String toString() {
        return id == null ? "null" : id.substring(0, 4);
    }
}
