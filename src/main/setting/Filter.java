package main.setting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import main.puzzle.Chip;
import main.puzzle.Stat;
import main.puzzle.Tag;

/**
 *
 * @author Bunnyspa
 */
public class Filter {

    public static final int NUM_STAR = 4;
    public static final int NUM_COLOR = 2;
    public static final int NUM_TYPE = 7;
    public static final int NUM_MARK = 2;

    private final boolean[] stars = new boolean[NUM_STAR];
    private final boolean[] colors = new boolean[NUM_COLOR];
    private final boolean[] types = new boolean[NUM_TYPE];
    private final boolean[] marks = new boolean[NUM_MARK];
    public int levelMin = 0;
    public int levelMax = Chip.LEVEL_MAX;
    public Stat ptMin = new Stat();
    public Stat ptMax = new Stat(Chip.PT_MAX);
    public final Set<Tag> includedTags = new HashSet<>();
    public final Set<Tag> excludedTags = new HashSet<>();

    // get
    public boolean getStar(int i) {
        return stars[i];
    }

    public boolean getColor(int i) {
        return colors[i];
    }

    public boolean getType(int i) {
        return types[i];
    }

    public boolean getMark(int i) {
        return marks[i];
    }

    // set
    public void setStar(int i, boolean b) {
        set(stars, i, b);
    }

    public void setColor(int i, boolean b) {
        set(colors, i, b);
    }

    public void setType(int i, boolean b) {
        set(types, i, b);
    }

    public void setMark(int i, boolean b) {
        set(marks, i, b);
    }

    // setAll
    public void setStars(boolean... bools) {
        setAll(stars, bools);
    }

    public void setColors(boolean... bools) {
        setAll(colors, bools);
    }

    public void setTypes(boolean... bools) {
        setAll(types, bools);
    }

    public void setMarks(boolean... bools) {
        setAll(marks, bools);
    }

    // anyTrue
    public boolean anySCTMTrue() {
        return anyStarTrue() || anyColorTrue() || anyTypeTrue() || anyMarkTrue();
    }

    public boolean anyStarTrue() {
        return anyTrue(stars);
    }

    public boolean anyColorTrue() {
        return anyTrue(colors);
    }

    public boolean anyTypeTrue() {
        return anyTrue(types);
    }

    public boolean anyMarkTrue() {
        return anyTrue(marks);
    }

    // reset
    public void reset() {
        setAll(stars, new boolean[NUM_STAR]);
        setAll(colors, new boolean[NUM_COLOR]);
        setAll(types, new boolean[NUM_TYPE]);
        setAll(marks, new boolean[NUM_MARK]);
        levelMin = 0;
        levelMax = Chip.LEVEL_MAX;
        ptMin = new Stat();
        ptMax = new Stat(Chip.PT_MAX);
        includedTags.clear();
        excludedTags.clear();
    }

    public boolean equals(boolean[] stars, boolean[] types, Stat ptMin, Stat ptMax) {
        return Arrays.equals(this.stars, stars)
                && Arrays.equals(this.types, types)
                && this.ptMin.equals(ptMin)
                && this.ptMax.equals(ptMax);
    }

    private static void set(boolean[] data, int i, boolean b) {
        if (i < data.length) {
            data[i] = b;
        }
    }

    private static void setAll(boolean[] data, boolean[] bools) {
        System.arraycopy(bools, 0, data, 0, Math.min(data.length, bools.length));
    }

    private static boolean anyTrue(boolean[] list) {
        for (boolean b : list) {
            if (b) {
                return true;
            }
        }
        return false;
    }

}
