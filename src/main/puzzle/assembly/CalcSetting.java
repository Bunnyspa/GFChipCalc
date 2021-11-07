package main.puzzle.assembly;

import main.data.Unit;
import main.puzzle.Stat;

public class CalcSetting {

    public final Unit unit;
    public final int unitStar;
    public final boolean maxLevel, rotation, symmetry;
    public final Stat stat, pt;

    public CalcSetting(Unit unit, int unitStar, boolean maxLevel, boolean rotation, boolean symmetry, Stat stat, Stat pt) {
        this.unit = unit;
        this.unitStar = unitStar;
        this.maxLevel = maxLevel;
        this.rotation = rotation;
        this.symmetry = symmetry;
        this.stat = stat;
        this.pt = pt;
    }
}
