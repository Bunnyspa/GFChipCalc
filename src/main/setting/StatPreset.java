package main.setting;

import main.puzzle.Shape;
import main.puzzle.Stat;

public class StatPreset {

    public final Shape.Type typeMin;
    public final Stat stat, pt, ptFilterMin, ptFilterMax;

    public StatPreset(Shape.Type typeMin, Stat stat, Stat pt, Stat ptFilterMin, Stat ptFilterMax) {
        this.typeMin = typeMin;
        this.stat = stat;
        this.pt = pt;
        this.ptFilterMin = ptFilterMin;
        this.ptFilterMax = ptFilterMax;
    }

}
