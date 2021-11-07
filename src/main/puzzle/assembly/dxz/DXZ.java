package main.puzzle.assembly.dxz;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import main.puzzle.assembly.dxz.dlx.ColumnNode;
import main.puzzle.assembly.dxz.dlx.DLXNode;
import main.puzzle.assembly.dxz.dlx.DancingLinksMatrix;
import main.puzzle.assembly.dxz.zdd.ZDD;
import main.puzzle.assembly.dxz.zdd.ZDDMemoCache;
import main.puzzle.assembly.dxz.zdd.ZDDNode;
import main.puzzle.assembly.dxz.zdd.ZDDNodeTable;
import main.util.Pair;

public class DXZ {

    public static Set<Integer> solve(List<boolean[]> rows, BooleanSupplier checkPause) {
        DancingLinksMatrix X = new DancingLinksMatrix(rows);
        return dxz(X, checkPause);
    }

//    private static Set<Integer> axz(ExactCoverMatrix X, BooleanSupplier checkPause) {
//        ZDDNode node = axz_search(X, new ZDDMemoCache(), new ZDDNodeTable(), checkPause);
//        if (node == null) {
//            return null;
//        }
//        Set<Set<Integer>> sets = node.get();
//        if (sets.isEmpty()) {
//            return null;
//        }
//        return sets.iterator().next();
//    }
//
//    private static ZDDNode axz_search(ExactCoverMatrix A, ZDDMemoCache C, ZDDNodeTable Z, BooleanSupplier checkPause) {
//        if (A.isEmpty()) {
//            return ZDD.TRUE_TERMINAL;
//        }
//        Set<Integer> colA = A.getCols();
//        if (C.containsKey(colA)) {
//            return C.get(colA);
//        }
//        Set<Integer> rowA = A.getRows();
//        int c = A.getCol();
//        ZDDNode x = null;
//        for (int r : rowA) {
//            if (checkPause.getAsBoolean() && A.get(r, c)) {
//                A.cover(r);
//                ZDDNode y = axz_search(A, C, Z, checkPause);
//                if (y != null) {
//                    x = ZDD.unique(r, x, y, Z);
//                }
//                A.uncover();
//            }
//        }
//        C.put(colA, x);
//        return x;
//    }
//
//    private static Set<Integer> dlx(DancingLinksMatrix X, BooleanSupplier checkPause) {
//        Set<Integer> ans = new HashSet<>();
//        dlx_search(X, new Stack<>(), ans, checkPause);
//        if (ans.isEmpty()) {
//            return null;
//        }
//        return ans;
//    }
//
//    private static boolean dlx_search(DancingLinksMatrix A, Stack<Integer> R, Set<Integer> ans, BooleanSupplier checkPause) {
//        Set<Integer> colA = A.getColumns();
//        if (colA.isEmpty()) {
//            ans.addAll(R);
//            return true;
//        }
//        ColumnNode c = A.selectColumn();
//        c.cover();
//        for (DLXNode r = c.D; r != c; r = r.D) {
//            R.push(r.rowIndex);
//            for (DLXNode j = r.R; j != r; j = j.R) {
//                j.column.cover();
//            }
//            if (dlx_search(A, R, ans, checkPause)) {
//                return true;
//            }
//            R.pop();
//            for (DLXNode j = r.L; j != r; j = j.L) {
//                j.column.uncover();
//            }
//        }
//        c.uncover();
//        return false;
//    }
    private static Set<Integer> dxz(DancingLinksMatrix X, BooleanSupplier checkPause) {
        ZDDNode node = dxz_search(X, new ZDDMemoCache(), new ZDDNodeTable(), checkPause).first;
        if (node == null) {
            return null;
        }
        Set<Set<Integer>> sets = node.get();
        if (sets.isEmpty()) {
            return null;
        }
        return sets.iterator().next();
    }

    private static Pair<ZDDNode, Boolean> dxz_search(DancingLinksMatrix A, ZDDMemoCache C, ZDDNodeTable Z, BooleanSupplier checkPause) {
        // System.out.println(depth + ": " + A);
        Set<Integer> colA = A.getColumns();
        if (colA.isEmpty()) {
            return new Pair<>(ZDD.TRUE_TERMINAL, true);
        }
        if (C.containsKey(colA)) {
            return new Pair<>(C.get(colA), false);
        }
        ColumnNode c = A.selectColumn();
        ZDDNode x = null;
        c.cover();
        for (DLXNode r = c.D; r != c; r = r.D) {
            for (DLXNode j = r.R; j != r; j = j.R) {
                j.column.cover();
            }
            Pair<ZDDNode, Boolean> p = dxz_search(A, C, Z, checkPause);
            ZDDNode y = p.first;
            if (y != null) {
                x = ZDD.unique(r.rowIndex, x, y, Z);
            }
            if (p.second) {
                return new Pair<>(x, true);
            }
            for (DLXNode j = r.L; j != r; j = j.L) {
                j.column.uncover();
            }

        }
        C.put(colA, x);
        c.uncover();
        return new Pair<>(x, false);
    }
}
