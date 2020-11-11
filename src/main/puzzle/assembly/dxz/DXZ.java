/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.puzzle.assembly.dxz;

import main.puzzle.assembly.dxz.zdd.ZDDNode;
import main.puzzle.assembly.dxz.zdd.ZDD;
import main.puzzle.assembly.dxz.zdd.ZDDNodeTable;
import main.puzzle.assembly.dxz.zdd.ZDDMemoCache;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.BooleanSupplier;
import main.puzzle.assembly.dxz.dlx.ColumnNode;
import main.puzzle.assembly.dxz.dlx.DLXNode;
import main.puzzle.assembly.dxz.dlx.DancingLinksMatrix;

/**
 *
 * @author Bunnyspa
 */
public class DXZ {

    public static Set<Integer> solve(List<boolean[]> rows, BooleanSupplier checkPause) {
        ExactCoverMatrix X = new ExactCoverMatrix(rows);
        return dxz(X, checkPause);
    }

    private static Set<Integer> dxz(ExactCoverMatrix X, BooleanSupplier checkPause) {
        ZDDNode node = dxz_search(X, new ZDDMemoCache(), new ZDDNodeTable(), checkPause);
        if (node == null) {
            return null;
        }
        Set<Set<Integer>> sets = node.get();
        if (sets.isEmpty()) {
            return null;
        }
        return sets.iterator().next();
    }

    private static ZDDNode dxz_search(ExactCoverMatrix A, ZDDMemoCache C, ZDDNodeTable Z, BooleanSupplier checkPause) {
        if (A.isEmpty()) {
            return ZDD.TRUE_TERMINAL;
        }
        Set<Integer> colA = A.getCols();
        if (C.containsKey(colA)) {
            return C.get(colA);
        }
        Set<Integer> rowA = A.getRows();
        int c = A.getCol();
        ZDDNode x = null;
        for (int r : rowA) {
            if (checkPause.getAsBoolean() && A.get(r, c)) {
                A.cover(r);
                ZDDNode y = dxz_search(A, C, Z, checkPause);
                if (y != null) {
                    x = ZDD.unique(r, x, y, Z);
                }
                A.uncover();
            }
        }
        C.put(colA, x);
        return x;
    }

    private static Set<Integer> dlx(DancingLinksMatrix X, BooleanSupplier checkPause) {
        Set<Integer> ans = new HashSet<>();
        dlx_search(X, new Stack<>(), ans, checkPause, 0);
        if (ans.isEmpty()) {
            return null;
        }
        return ans;
    }

    private static boolean dlx_search(DancingLinksMatrix A, Stack<Integer> R, Set<Integer> ans, BooleanSupplier checkPause, int depth) {
        Set<Integer> colA = A.getColumns();
        if (colA.isEmpty()) {
            ans.addAll(R);
            return true;
        }
        ColumnNode c = A.selectColumn();
        c.cover();
        for (DLXNode r = c.D; r != c; r = r.D) {
            R.push(r.rowIndex);
            for (DLXNode j = r.R; j != r; j = j.R) {
                j.column.cover();
            }
            if (dlx_search(A, R, ans, checkPause, depth + 1)) {
                return true;
            }
            R.pop();
            for (DLXNode j = r.L; j != r; j = j.L) {
                j.column.uncover();
            }
        }
        c.uncover();
        return false;
    }

    private static Set<Integer> dxz_v2(DancingLinksMatrix X, BooleanSupplier checkPause) {
        ZDDNode node = dxz_v2_search(X, new ZDDMemoCache(), new ZDDNodeTable(), checkPause, 0);
        if (node == null) {
            return null;
        }
        Set<Set<Integer>> sets = node.get();
        if (sets.isEmpty()) {
            return null;
        }
        return sets.iterator().next();
    }

    private static ZDDNode dxz_v2_search(DancingLinksMatrix A, ZDDMemoCache C, ZDDNodeTable Z, BooleanSupplier checkPause, int depth) {
        // System.out.println(depth + ": " + A);
        Set<Integer> colA = A.getColumns();
        if (colA.isEmpty()) {
            return ZDD.TRUE_TERMINAL;
        }
        if (C.containsKey(colA)) {
            return C.get(colA);
        }
        ColumnNode c = A.selectColumn();
        ZDDNode x = null;
        c.cover();
        for (DLXNode r = c.D; r != c; r = r.D) {
            for (DLXNode j = r.R; j != r; j = j.R) {
                j.column.cover();
            }
            ZDDNode y = dxz_v2_search(A, C, Z, checkPause, depth + 1);
            if (y != null) {
                x = ZDD.unique(r.rowIndex, x, y, Z);
            }
            for (DLXNode j = r.L; j != r; j = j.L) {
                j.column.uncover();
            }

        }
        c.uncover();
        C.put(colA, x);
        return x;
    }
}
