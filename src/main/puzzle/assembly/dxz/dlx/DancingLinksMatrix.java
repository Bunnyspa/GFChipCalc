package main.puzzle.assembly.dxz.dlx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DancingLinksMatrix {

    private final ColumnNode header;

    public DancingLinksMatrix(List<boolean[]> matrix) {
        header = create(matrix);
    }

    private ColumnNode create(List<boolean[]> matrix) {
        final int nCol = matrix.get(0).length;
        ColumnNode headerNode = new ColumnNode(-1);
        List<ColumnNode> columnNodes = new ArrayList<>();

        for (int i = 0; i < nCol; i++) {
            ColumnNode n = new ColumnNode(i);
            columnNodes.add(n);
            headerNode = (ColumnNode) headerNode.linkRight(n);
        }

        headerNode = headerNode.R.column;

        for (int r = 0; r < matrix.size(); r++) {
            boolean[] row = matrix.get(r);
            DLXNode prev = null;
            for (int c = 0; c < nCol; c++) {
                if (row[c]) {
                    ColumnNode colNode = columnNodes.get(c);
                    DLXNode newNode = new DLXNode(r, colNode);

                    if (prev == null) {
                        prev = newNode;
                    }

                    colNode.U.linkDown(newNode);
                    prev = prev.linkRight(newNode);
                    colNode.size++;
                }
            }
        }

        headerNode.size = nCol;
        return headerNode;
    }

    public ColumnNode selectColumn() {
        ColumnNode out = header.R.column;
        int minSize = out.size;

        for (ColumnNode c = out.R.column; c != header; c = c.R.column) {
            if (c.size < minSize) {
                out = c;
                minSize = c.size;
            }
        }

        return out;
    }

    public Set<Integer> getColumns() {
        Set<Integer> out = new HashSet<>();

        for (ColumnNode c = header.R.column; c != header; c = c.R.column) {
            out.add(c.colIndex);
        }

        return out;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ColumnNode c = header.R.column; c != header; c = c.R.column) {
            sb.append(c.colIndex);
            sb.append(" ");
        }
        return sb.toString();
    }
}
