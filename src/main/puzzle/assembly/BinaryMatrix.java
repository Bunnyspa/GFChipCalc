package main.puzzle.assembly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author Bunnyspa
 */
public class BinaryMatrix {

    private final boolean[][] array;
    private final int nRow, nCol;

    private final Stack<Set<Integer>> hiddenRowSetStack = new Stack<>();
    private final Stack<Set<Integer>> hiddenColSetStack = new Stack<>();
    private final boolean[] hiddenRows, hiddenCols;
    private int nVisibleRow, nVisibleCol;

    public BinaryMatrix(List<boolean[]> rows) {
        nRow = rows.size();
        nCol = rows.isEmpty() ? 0 : rows.get(0).length;

        hiddenRows = new boolean[nRow];
        hiddenCols = new boolean[nCol];
        nVisibleRow = nRow;
        nVisibleCol = nCol;

        array = new boolean[nRow][nCol];
        for (int r = 0; r < nRow; r++) {
            boolean[] rowArray = rows.get(r);
            for (int c = 0; c < nCol; c++) {
                if (rowArray[c]) {
                    array[r][c] = true;
                }
            }
        }
    }

    public void revertLastHiding() {
        Set<Integer> hiddenColSet = hiddenColSetStack.pop();
        Set<Integer> hiddenRowSet = hiddenRowSetStack.pop();

        hiddenColSet.forEach((j) -> hiddenCols[j] = false);
        hiddenRowSet.forEach((j) -> hiddenRows[j] = false);

        nVisibleCol += hiddenColSet.size();
        nVisibleRow += hiddenRowSet.size();
    }

    public void hideRowChain(int r) {
        Set<Integer> hiddenColSet = new HashSet<>();
        Set<Integer> hiddenRowSet = new HashSet<>();

        getCols().stream().filter((j) -> get(r, j)).forEach((j) -> {
            hiddenColSet.add(j);
            hiddenCols[j] = true;
            getRows().stream().filter((i) -> get(i, j)).forEach((i) -> {
                hiddenRowSet.add(i);
                hiddenRows[i] = true;
            });
        });

        nVisibleCol -= hiddenColSet.size();
        nVisibleRow -= hiddenRowSet.size();

        hiddenColSetStack.push(hiddenColSet);
        hiddenRowSetStack.push(hiddenRowSet);
    }

    public boolean get(int row, int col) {
        return array[row][col];
    }

    public void set(int row, int col, boolean b) {
        array[row][col] = b;
    }

    public Set<Integer> getRows() {
        return getVisibleIndices(hiddenRows);
    }

    public Set<Integer> getCols() {
        return getVisibleIndices(hiddenCols);
    }

    private static Set<Integer> getVisibleIndices(boolean[] hidden) {
        Set<Integer> out = new HashSet<>();
        for (int i = 0; i < hidden.length; i++) {
            if (!hidden[i]) {
                out.add(i);
            }
        }
        return out;
    }

    public boolean isEmpty() {
        return nVisibleRow == 0 && nVisibleCol == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < hiddenRows.length; r++) {
            boolean rHidden = hiddenRows[r];
            String rStr = String.valueOf(r);
            for (int i = 0; i < 2 - rStr.length(); i++) {
                sb.append(" ");
            }
            sb.append(rStr).append(":");
            for (int c = 0; c < hiddenCols.length; c++) {
                boolean cHidden = hiddenCols[c];
                if (rHidden && cHidden) {
                    sb.append("+");
                } else if (rHidden) {
                    sb.append("-");
                } else if (cHidden) {
                    sb.append("|");
                } else {
                    sb.append(array[r][c] ? "1" : "0");
                }
            }
            if (r < hiddenRows.length - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
