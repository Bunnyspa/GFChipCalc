/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.puzzle.assembly;

import java.util.List;
import main.puzzle.Chip;

/**
 *
 * @author Bunnyspa
 */
public class CalcExtraSetting {

    public static final int CALCMODE_FINISHED = 0;
    public static final int CALCMODE_DICTIONARY = 1;
    public static final int CALCMODE_ALGX = 2;

    public int calcMode;
    public final int calcModeTag;
    public final boolean matchColor;
    public final int markMin, markMax, markType, sortType;
    public final List<Chip> chips;

    public CalcExtraSetting(int calcMode, int calcModeTag, boolean matchColor, int markMin, int markMax, int markType, int sortType, List<Chip> chips) {
        this.calcMode = calcMode;
        this.calcModeTag = calcModeTag;
        this.matchColor = matchColor;
        this.markMin = markMin;
        this.markMax = markMax;
        this.markType = markType;
        this.sortType = sortType;
        this.chips = chips;
    }
}
