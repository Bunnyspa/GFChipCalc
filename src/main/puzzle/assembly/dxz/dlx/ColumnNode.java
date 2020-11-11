/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.puzzle.assembly.dxz.dlx;

/**
 *
 * @author Bunnyspa
 */
public class ColumnNode extends DLXNode {

    int size;
    public final int colIndex;

    ColumnNode(int colIndex) {
        super(-1);
        size = 0;
        this.colIndex = colIndex;
        column = this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.colIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ColumnNode other = (ColumnNode) obj;
        return this.colIndex == other.colIndex;
    }

    public void cover() {
        //    System.out.println("Cover");
        //System.out.println("cover: " + colIndex);
        coverLR();
        for (DLXNode i = this.D; i != this; i = i.D) {
            for (DLXNode j = i.R; j != i; j = j.R) {
                j.coverUD();
            }
        }
        // System.out.println("Cover Done");
    }

    public void uncover() {
        // System.out.println("Uncover");
        //System.out.println("uncover: " + colIndex);
        for (DLXNode i = this.U; i != this; i = i.U) {
            for (DLXNode j = i.L; j != i; j = j.L) {
                j.uncoverUD();
            }
        }
        uncoverLR();
        // System.out.println("Uncover Done");
    }
}
