package main.setting;

import main.puzzle.Shape;
import main.puzzle.Stat;

/**
 *
 * @author Bunnyspa
 */
public class StatPreset {

    public final Stat stat, pt, ptMin, ptMax;
    public final Shape.Type typeMin;

    public StatPreset(Stat stat, Stat pt, Stat ptMin, Stat ptMax, Shape.Type typeMin) {
        this.stat = stat;
        this.pt = pt;
        this.ptMin = ptMin;
        this.ptMax = ptMax;
        this.typeMin = typeMin;
    }

}
