/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.puzzle.assembly;

import main.puzzle.Stat;

/**
 *
 * @author Bunnyspa
 */
public class CalcSetting {

    public final String boardName;
    public final int boardStar;
    public final boolean maxLevel, rotation, symmetry;
    public final Stat stat, pt;

    public CalcSetting(String boardName, int boardStar, boolean maxLevel, boolean rotation, boolean symmetry, Stat stat, Stat pt) {
        this.boardName = boardName;
        this.boardStar = boardStar;
        this.maxLevel = maxLevel;
        this.rotation = rotation;
        this.symmetry = symmetry;
        this.stat = stat;
        this.pt = pt;
    }
}
