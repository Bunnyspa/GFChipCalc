package main.puzzle;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Bunnyspa
 * @param <E>
 */
public class PuzzleMatrix<E> implements Serializable {

    private int nRow, nCol;
    private List<E> list;

    public PuzzleMatrix(E[][] data) {
        nRow = data.length;
        nCol = nRow > 0 ? data[0].length : 0;
        list = new ArrayList<>(nCol * nRow);
        for (int r = 0; r < nRow; r++) {
            for (int c = 0; c < nCol; c++) {
                list.add(data[r][c]);
            }
        }
    }

    public PuzzleMatrix(PuzzleMatrix<E> a) {
        nRow = a.nRow;
        nCol = a.nCol;
        list = new ArrayList<>(a.list);
    }

    public PuzzleMatrix(int height, int width, E initial) {
        this.nRow = height;
        this.nCol = width;
        list = new ArrayList<>(width * height);
        for (int i = 0; i < height * width; i++) {
            list.add(initial);
        }
    }

    private boolean isValid(int row, int col) {
        return (0 <= row && row < nRow && 0 <= col && col < nCol);
    }

    public E get(int row, int col) {
        if (isValid(row, col)) {
            return list.get(row * nCol + col);
        }
        return null;
    }

    public void set(int row, int col, E value) {
        if (isValid(row, col)) {
            list.set(row * nCol + col, value);
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int getNumCol() {
        return nCol;
    }

    public int getNumRow() {
        return nRow;
    }

    public boolean contains(E e) {
        return list.contains(e);
    }

    public int getNumContaining(E e) {
        return (int) list.stream().filter((cell) -> (cell.equals(e))).count();
    }

    public int getNumNotContaining(E e) {
        return (int) list.stream().filter((cell) -> (!cell.equals(e))).count();
    }

    public Point getPivot(E e) {
        for (int row = 0; row < nRow; row++) {
            for (int col = 0; col < nCol; col++) {
                if (get(row, col) == e) {
                    return new Point(row, col);
                }
            }
        }
        return null;
    }

    public Set<Point> getCoords(E e) {
        Set<Point> ps = new HashSet<>();
        for (int row = 0; row < nRow; row++) {
            for (int col = 0; col < nCol; col++) {
                if (get(row, col) == e) {
                    ps.add(new Point(row, col));
                }
            }
        }
        return ps;
    }

    private Set<Point> getCoordsExcept(E e) {
        Set<Point> ps = new HashSet<>();
        for (int row = 0; row < nRow; row++) {
            for (int col = 0; col < nCol; col++) {
                if (get(row, col) != e) {
                    ps.add(new Point(row, col));
                }
            }
        }
        return ps;
    }

    public void rotate() {
        List<E> newList = new ArrayList<>(nCol * nRow);
        int newHeight = nCol;
        int newWidth = nRow;
        for (int row = 0; row < newHeight; row++) {
            for (int col = 0; col < newWidth; col++) {
                newList.add(list.get((newWidth - col - 1) * newHeight + row));
            }
        }
        nRow = newHeight;
        nCol = newWidth;
        list = newList;
    }

    public void rotate(int num) {
        num = num % 4;
        for (int i = 0; i < num; i++) {
            rotate();
        }
    }

    public void rotateInside(int num, E e) {
        if (num == 2) {
            int xMin = nCol;
            int xMax = 0;
            int yMin = nRow;
            int yMax = 0;
            for (Point p : getCoordsExcept(e)) {
                if (xMin > p.x) {
                    xMin = p.x;
                }
                if (xMax < p.x) {
                    xMax = p.x;
                }
                if (yMin > p.y) {
                    yMin = p.y;
                }
                if (yMax < p.y) {
                    yMax = p.y;
                }
            }
            List<Point> pts = new ArrayList<>(getCoordsExcept(e));
            while (!pts.isEmpty()) {
                Point p1 = pts.get(0);
                Point p2 = new Point(xMin + xMax - p1.x, yMin + yMax - p1.y);
                if (p1.equals(p2)) {
                    pts.remove(p1);
                } else {
                    E e1 = get(p1.x, p1.y);
                    E e2 = get(p2.x, p2.y);
                    // Swap
                    set(p1.x, p1.y, e2);
                    set(p2.x, p2.y, e1);
                    // Remove
                    pts.remove(p1);
                    pts.remove(p2);
                }
            }
        } else {
            rotate(num);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < nRow; row++) {
            for (int col = 0; col < nCol; col++) {
                String data = get(row, col).toString();
                switch (data) {
                    case "-1":
                        sb.append("  ");
                        break;
                    case "-2":
                        sb.append("X ");
                        break;
                    default:
                        sb.append(data).append(" ");
                        break;
                }
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    // For Chip.CHIP_ROTATION_MAP
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        PuzzleMatrix<E> array = (PuzzleMatrix<E>) obj;
        return this.nCol == array.nCol && this.nRow == array.nRow && this.list.equals(array.list);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.nCol;
        hash = 83 * hash + this.nRow;
        hash = 83 * hash + Objects.hashCode(this.list);
        return hash;
    }
}
