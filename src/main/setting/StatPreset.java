package main.setting;

import main.puzzle.FStat;

/**
 *
 * @author Bunnyspa
 */
public class StatPreset {

    public final FStat stat, pt, ptMin, ptMax;
    public final String typeMin;

    public StatPreset(FStat stat, FStat pt, FStat ptMin, FStat ptMax, String typeMin) {
        this.stat = stat;
        this.pt = pt;
        this.ptMin = ptMin;
        this.ptMax = ptMax;
        this.typeMin = typeMin;
    }

}
