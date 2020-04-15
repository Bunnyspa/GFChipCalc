package main.puzzle.assembly;

import main.puzzle.Chip;

/**
 *
 * @author Bunnyspa
 */
public class ChipFreq {

    public final Chip chip;
    public final int count;
    public final double freq;

    public ChipFreq(Chip chip, int count, double freq) {
        this.chip = chip;
        this.count = count;
        this.freq = freq;
    }

}
