package main.puzzle;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import main.util.DoubleKeyHashMap;
import main.util.Fn;

public enum Shape {

    NONE(0),
    // 1 = A
    _1(1),
    // 2 = B
    _2(2),
    // 3 = C
    _3_I(3), _3_L(4),
    // 4 = D
    _4_I(5), _4_O(6), _4_Lm(7), _4_L(8), _4_Zm(9), _4_Z(10), _4_T(11),
    // 5A = E
    _5A_Pm(12), _5A_P(13), _5A_I(14), _5A_C(15), _5A_Z(16), _5A_Zm(17), _5A_V(18), _5A_L(19), _5A_Lm(20),
    // 5B = F
    _5B_W(21), _5B_Nm(22), _5B_N(23), _5B_Ym(24), _5B_Y(25), _5B_X(26), _5B_T(27), _5B_F(28), _5B_Fm(29),
    // 6 = G
    _6_O(30), _6_A(31), _6_D(32), _6_Z(33), _6_Zm(34), _6_Y(35), _6_T(36), _6_I(37), _6_C(38), _6_R(39);

    public static final Shape DEFAULT = Shape._1;

    public enum Type {
        NONE(0), _1(1), _2(2), _3(3), _4(4), _5A(5), _5B(6), _6(7);

        public final int id;

        private static final Map<Integer, Type> byId = new HashMap<Integer, Type>() {
            {
                for (Type e : Type.values()) {
                    put(e.id, e);
                }
            }
        };

        Type(int id) {
            this.id = id;
        }

        public static Type byId(int id) {
            return byId.get(id);
        }

        public static Type byName(String s) {
            switch (s) {
                case "6":
                    return _6;
                case "5B":
                case "5b":
                    return _5B;
                case "5A":
                case "5a":
                    return _5A;
                case "4":
                    return _4;
                case "3":
                    return _3;
                case "2":
                    return _2;
                case "1":
                    return _1;
                default:
                    return NONE;
            }
        }

        public int getSize() {
            return id - (6 <= id ? 1 : 0);
        }

        public static int compare(Type o1, Type o2) {
            return Integer.compare(o1.id, o2.id);
        }

        @Override
        public String toString() {
            switch (id) {
                case 7:
                    return "6";
                case 6:
                    return "5B";
                case 5:
                    return "5A";
                case 4:
                case 3:
                case 2:
                case 1:
                    return String.valueOf(id);
                default:
                    return "";
            }
        }
    }

    private static final Map<Integer, Shape> byId = new HashMap<Integer, Shape>() {
        {
            for (Shape e : Shape.values()) {
                put(e.id, e);
            }
        }
    };

    private static final Shape[] SHAPES_1 = {_1};
    private static final Shape[] SHAPES_2 = {_2};
    private static final Shape[] SHAPES_3 = {_3_I, _3_L};
    private static final Shape[] SHAPES_4 = {_4_I, _4_O, _4_Lm, _4_L, _4_Zm, _4_Z, _4_T};
    private static final Shape[] SHAPES_5A = {_5A_Pm, _5A_P, _5A_I, _5A_C, _5A_Z, _5A_Zm, _5A_V, _5A_L, _5A_Lm};
    private static final Shape[] SHAPES_5B = {_5B_W, _5B_Nm, _5B_N, _5B_Ym, _5B_Y, _5B_X, _5B_T, _5B_F, _5B_Fm};
    private static final Shape[] SHAPES_5 = Fn.concatAll(SHAPES_5A, SHAPES_5B);
    private static final Shape[] SHAPES_6 = {_6_O, _6_A, _6_D, _6_Z, _6_Zm, _6_Y, _6_T, _6_I, _6_C, _6_R};
    public final int id;

    Shape(int id) {
        this.id = id;
    }

    public static Shape byId(int id) {
        return byId.get(id);
    }

    public static Shape byName(String name) {
        switch (name) {
            // 1 = A
            case "1":
                return _1;
            // 2 = B
            case "2":
                return _2;
            // 3 = C
            case "3I":
                return _3_I;
            case "3L":
                return _3_L;
            // 4 = D
            case "4I":
                return _4_I;
            case "4O":
                return _4_O;
            case "4Lm":
                return _4_Lm;
            case "4L":
                return _4_L;
            case "4Zm":
                return _4_Zm;
            case "4Z":
                return _4_Z;
            case "4T":
                return _4_T;
            // 5A = E
            case "5Pm":
                return _5A_Pm;
            case "5P":
                return _5A_P;
            case "5I":
                return _5A_I;
            case "5C":
                return _5A_C;
            case "5Z":
                return _5A_Z;
            case "5Zm":
                return _5A_Zm;
            case "5V":
                return _5A_V;
            case "5L":
                return _5A_L;
            case "5Lm":
                return _5A_Lm;
            // 5B = F
            case "5W":
                return _5B_W;
            case "5Nm":
                return _5B_Nm;
            case "5N":
                return _5B_N;
            case "5Ym":
                return _5B_Ym;
            case "5Y":
                return _5B_Y;
            case "5X":
                return _5B_X;
            case "5T":
                return _5B_T;
            case "5F":
                return _5B_F;
            case "5Fm":
                return _5B_Fm;
            // 6 = G
            case "6O":
                return _6_O;
            case "6A":
                return _6_A;
            case "6D":
                return _6_D;
            case "6Z":
                return _6_Z;
            case "6Zm":
                return _6_Zm;
            case "6Y":
                return _6_Y;
            case "6T":
                return _6_T;
            case "6I":
                return _6_I;
            case "6C":
                return _6_C;
            case "6R":
                return _6_R;
            default:
                return NONE;
        }
    }

    public int getSize() {
        if (30 <= id) {
            return 6;
        }
        if (21 <= id) {
            return 5;
        }
        if (12 <= id) {
            return 5;
        }
        if (5 <= id) {
            return 4;
        }
        if (3 <= id) {
            return 3;
        }
        if (2 == id) {
            return 2;
        }
        if (1 == id) {
            return 1;
        }
        return 0;
    }

    public Type getType() {
        if (30 <= id) {
            return Type._6;
        }
        if (21 <= id) {
            return Type._5B;
        }
        if (12 <= id) {
            return Type._5A;
        }
        if (5 <= id) {
            return Type._4;
        }
        if (3 <= id) {
            return Type._3;
        }
        if (2 == id) {
            return Type._2;
        }
        if (1 == id) {
            return Type._1;
        }
        return Type.NONE;
    }

    public static Shape[] getShapes(Type type) {
        switch (type) {
            case _6:
                return SHAPES_6;
            case _5B:
                return SHAPES_5B;
            case _5A:
                return SHAPES_5A;
            case _4:
                return SHAPES_4;
            case _3:
                return SHAPES_3;
            case _2:
                return SHAPES_2;
            case _1:
                return SHAPES_1;
            default:
                return new Shape[0];
        }
    }

    public static Shape[] getShapes(int size) {
        switch (size) {
            case 6:
                return SHAPES_6;
            case 5:
                return SHAPES_5;
            case 4:
                return SHAPES_4;
            case 3:
                return SHAPES_3;
            case 2:
                return SHAPES_2;
            case 1:
                return SHAPES_1;
            default:
                return new Shape[0];
        }
    }

    public int getMaxRotation() {
        return MAX_ROTATIONS.get(this);
    }

    public Point getPivot(int rotation) {
        return new Point(PIVOTS.get(this, rotation));
    }

    public Set<Point> getPoints(int rotation) {
        Set<Point> points = new HashSet<>();
        for (Point point : POINTS.get(this, rotation)) {
            points.add(new Point(point));
        }
        return points;
    }

    private static final boolean O = true;
    private static final boolean X = false;
    public static final Map<Shape, Boolean[][]> MATRIX_MAP = new HashMap<Shape, Boolean[][]>() // <editor-fold defaultstate="collapsed">
    {
        {
            // 1
            put(Shape._1, new Boolean[][]{
                {O}
            });

            // 2
            put(Shape._2, new Boolean[][]{
                {O},
                {O}
            });

            // 3
            put(Shape._3_I, new Boolean[][]{
                {O},
                {O},
                {O}
            });
            put(Shape._3_L, new Boolean[][]{
                {O, X},
                {O, O}
            });

            // 4
            put(Shape._4_I, new Boolean[][]{
                {O, O, O, O}
            });
            put(Shape._4_L, new Boolean[][]{
                {O, X},
                {O, X},
                {O, O}
            });
            put(Shape._4_Lm, new Boolean[][]{
                {O, X, X},
                {O, O, O}
            });
            put(Shape._4_O, new Boolean[][]{
                {O, O},
                {O, O}
            });
            put(Shape._4_T, new Boolean[][]{
                {X, O, X},
                {O, O, O}
            });
            put(Shape._4_Z, new Boolean[][]{
                {O, O, X},
                {X, O, O}
            });
            put(Shape._4_Zm, new Boolean[][]{
                {X, O, O},
                {O, O, X}
            });

            // 5A
            put(Shape._5A_C, new Boolean[][]{
                {O, O, O},
                {O, X, O}
            });
            put(Shape._5A_I, new Boolean[][]{
                {O, O, O, O, O}
            });
            put(Shape._5A_L, new Boolean[][]{
                {O, X},
                {O, X},
                {O, X},
                {O, O}
            });
            put(Shape._5A_Lm, new Boolean[][]{
                {X, O},
                {X, O},
                {X, O},
                {O, O}
            });
            put(Shape._5A_P, new Boolean[][]{
                {X, O},
                {O, O},
                {O, O}
            });
            put(Shape._5A_Pm, new Boolean[][]{
                {O, X},
                {O, O},
                {O, O}
            });
            put(Shape._5A_V, new Boolean[][]{
                {O, X, X},
                {O, X, X},
                {O, O, O}
            });
            put(Shape._5A_Z, new Boolean[][]{
                {X, X, O},
                {O, O, O},
                {O, X, X}
            });
            put(Shape._5A_Zm, new Boolean[][]{
                {O, X, X},
                {O, O, O},
                {X, X, O}
            });

            // 5B
            put(Shape._5B_F, new Boolean[][]{
                {O, X, X},
                {O, O, O},
                {X, O, X}
            });
            put(Shape._5B_Fm, new Boolean[][]{
                {X, X, O},
                {O, O, O},
                {X, O, X}
            });
            put(Shape._5B_N, new Boolean[][]{
                {X, O},
                {O, O},
                {O, X},
                {O, X}
            });
            put(Shape._5B_Nm, new Boolean[][]{
                {O, X},
                {O, O},
                {X, O},
                {X, O}
            });
            put(Shape._5B_T, new Boolean[][]{
                {X, O, X},
                {X, O, X},
                {O, O, O}
            });
            put(Shape._5B_W, new Boolean[][]{
                {X, O, O},
                {O, O, X},
                {O, X, X}
            });
            put(Shape._5B_X, new Boolean[][]{
                {X, O, X},
                {O, O, O},
                {X, O, X}
            });
            put(Shape._5B_Y, new Boolean[][]{
                {X, O},
                {O, O},
                {X, O},
                {X, O}
            });
            put(Shape._5B_Ym, new Boolean[][]{
                {O, X},
                {O, O},
                {O, X},
                {O, X}
            });

            // 6
            put(Shape._6_A, new Boolean[][]{
                {O, X, X},
                {O, O, X},
                {O, O, O}
            });
            put(Shape._6_C, new Boolean[][]{
                {O, X, X, O},
                {O, O, O, O}
            });
            put(Shape._6_D, new Boolean[][]{
                {X, O, O, X},
                {O, O, O, O}
            });
            put(Shape._6_I, new Boolean[][]{
                {O, O, O, O, O, O}
            });
            put(Shape._6_O, new Boolean[][]{
                {O, O},
                {O, O},
                {O, O}
            });
            put(Shape._6_R, new Boolean[][]{
                {X, O, X},
                {O, O, O},
                {O, O, X}
            });
            put(Shape._6_T, new Boolean[][]{
                {X, X, O, X},
                {O, O, O, O},
                {X, X, O, X}
            });
            put(Shape._6_Y, new Boolean[][]{
                {X, O, X},
                {O, O, O},
                {O, X, O}
            });
            put(Shape._6_Z, new Boolean[][]{
                {O, O, O, X},
                {X, O, O, O}
            });
            put(Shape._6_Zm, new Boolean[][]{
                {X, O, O, O},
                {O, O, O, X}
            });
        }
    }; // </editor-fold>

    private static final Map<Shape, Integer> MAX_ROTATIONS = new HashMap<Shape, Integer>() // <editor-fold defaultstate="collapsed">
    {
        {
            for (Shape shape : Shape.values()) {
                if (shape == Shape.NONE) {
                    continue;
                }
                PuzzleMatrix<Boolean> a = new PuzzleMatrix<>(MATRIX_MAP.get(shape));
                for (int i = 1; i <= 4; i++) {
                    PuzzleMatrix<Boolean> b = new PuzzleMatrix<>(MATRIX_MAP.get(shape));
                    b.rotate(i);
                    if (a.equals(b)) {
                        put(shape, i);
                        break;
                    }
                }
            }
        }
    }; // </editor-fold>
    public static final DoubleKeyHashMap<Shape, Integer, Point> PIVOTS = new DoubleKeyHashMap<Shape, Integer, Point>() // <editor-fold defaultstate="collapsed">
    {
        {
            for (Shape shape : Shape.values()) {
                if (shape == Shape.NONE) {
                    continue;
                }
                PuzzleMatrix<Boolean> matrix = new PuzzleMatrix<>(MATRIX_MAP.get(shape));
                for (int r = 0; r < shape.getMaxRotation(); r++) {
                    Point pivot = matrix.getPivot(true);
                    put(shape, r, pivot);
                    matrix.rotate();
                }
            }
        }
    }; // </editor-fold>
    public static final DoubleKeyHashMap<Shape, Integer, Set<Point>> POINTS = new DoubleKeyHashMap<Shape, Integer, Set<Point>>() // <editor-fold defaultstate="collapsed">
    {
        {
            for (Shape shape : Shape.values()) {
                if (shape == Shape.NONE) {
                    continue;
                }
                PuzzleMatrix<Boolean> matrix = new PuzzleMatrix<>(MATRIX_MAP.get(shape));
                for (int r = 0; r < shape.getMaxRotation(); r++) {
                    Set<Point> pts = matrix.getPoints(true);
                    put(shape, r, pts);
                    matrix.rotate();
                }
            }
        }
    }; // </editor-fold>

    public static int compare(Shape o1, Shape o2) {
        return Integer.compare(o1.id, o2.id);
    }
}
