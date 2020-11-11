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
public class DLXNode {

    public DLXNode U, D, L, R;
    public ColumnNode column;
    public final int rowIndex;

    DLXNode(int rowIndex) {
        U = D = L = R = this;
        this.rowIndex = rowIndex;
    }

    DLXNode(int rowIndex, ColumnNode c) {
        this(rowIndex);
        column = c;
    }

    DLXNode linkDown(DLXNode node) {
        node.D = this.D;
        node.D.U = node;
        node.U = this;
        this.D = node;
        return node;
    }

    DLXNode linkRight(DLXNode node) {
        node.R = this.R;
        node.R.L = node;
        node.L = this;
        this.R = node;
        return node;
    }

    public void coverLR() {
       // System.out.println(L + "-" + this + "-" + R);
       // System.out.println(L + "-" + L.R + " ... " + R.L + "-" + R);
        L.R = R;
        R.L = L;
    }

    public void coverUD() {
        U.D = D;
        D.U = U;
    }

    public void uncoverLR() {
        L.R = this;
        R.L = this;
    }

    public void uncoverUD() {
        U.D = this;
        D.U = this;
    }

    @Override
    public String toString() {
        return "(" + rowIndex + ", " + column.colIndex + ")";
    }
}
