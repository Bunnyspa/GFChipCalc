package main.image;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import main.util.Fn;

public class ColorMatrix {

    private static final int MIN = 0;
    private static final int MAX = 255;

    private static final int SQSUMMAX = MAX * MAX * 3;

    private int[][] red, green, blue;

    // <editor-fold defaultstate="collapsed" desc="Init">
    public ColorMatrix() {
        setDimension(0, 0);
    }

    public ColorMatrix(int width, int height) {
        setDimension(Integer.max(0, width), Integer.max(0, height));
    }

    public ColorMatrix(int width, int height, boolean white) {
        setDimension(Integer.max(0, width), Integer.max(0, height));
        if (white) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    red[x][y] = MAX;
                    green[x][y] = MAX;
                    blue[x][y] = MAX;
                }
            }
        }
    }

    public ColorMatrix(BufferedImage image) {
        setData(image);
    }

    public ColorMatrix(ColorMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        setDimension(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                red[x][y] = matrix.red[x][y];
                green[x][y] = matrix.green[x][y];
                blue[x][y] = matrix.blue[x][y];
            }
        }
    }

    private void setDimension(int width, int height) {
        red = new int[width][height];
        green = new int[width][height];
        blue = new int[width][height];
    }

    private void setData(BufferedImage image) {
        setDimension(image.getWidth(), image.getHeight());
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                Color c = new Color(rgb);
                red[x][y] = c.getRed();
                green[x][y] = c.getGreen();
                blue[x][y] = c.getBlue();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get Methods">
    public Color getColor(int x, int y) {
        int xx = Fn.limit(x, 0, getWidth() - 1);
        int yy = Fn.limit(y, 0, getHeight() - 1);
        return new Color(getRed(xx, yy), getGreen(xx, yy), getBlue(xx, yy));
    }

    public int[] getColorArray(int x, int y) {
        int xx = Fn.limit(x, 0, getWidth() - 1);
        int yy = Fn.limit(y, 0, getHeight() - 1);
        return new int[]{getRed(xx, yy), getGreen(xx, yy), getBlue(xx, yy)};
    }

    public int getRed(int x, int y) {
        int xx = Fn.limit(x, 0, getWidth() - 1);
        int yy = Fn.limit(y, 0, getHeight() - 1);
        return red[xx][yy];
    }

    public int getGreen(int x, int y) {
        int xx = Fn.limit(x, 0, getWidth() - 1);
        int yy = Fn.limit(y, 0, getHeight() - 1);
        return green[xx][yy];
    }

    public int getBlue(int x, int y) {
        int xx = Fn.limit(x, 0, getWidth() - 1);
        int yy = Fn.limit(y, 0, getHeight() - 1);
        return blue[xx][yy];
    }

    public int getWidth() {
        return red.length;
    }

    public int getHeight() {
        if (red.length == 0) {
            return 0;
        }
        return red[0].length;
    }

    public BufferedImage getImage() {
        BufferedImage out = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Color c = new Color(getRed(x, y), getGreen(x, y), getBlue(x, y));
                out.setRGB(x, y, c.getRGB());
            }
        }
        return out;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Methods">
    public void setBlack(int x, int y) {
        setColor(x, y, MIN, MIN, MIN);
    }

    public void setWhite(int x, int y) {
        setColor(x, y, MAX, MAX, MAX);
    }

    public void setColor(int x, int y, int red, int green, int blue) {
        if (!isOB(x, y)) {
            setRed(x, y, red);
            setGreen(x, y, green);
            setBlue(x, y, blue);
        }
    }

    public void setColor(int x, int y, Color color) {
        if (!isOB(x, y)) {
            setRed(x, y, color.getRed());
            setGreen(x, y, color.getGreen());
            setBlue(x, y, color.getBlue());
        }
    }

    public void setColor(int x, int y, int[] color) {
        if (!isOB(x, y)) {
            setRed(x, y, color[0]);
            setGreen(x, y, color[1]);
            setBlue(x, y, color[2]);
        }
    }

    private static boolean isOB(int x, int y, int width, int height) {
        return (x < 0) || (width - 1 < x) || (y < 0) || (height - 1 < y);
    }

    private boolean isOB(int x, int y) {
        return isOB(x, y, getWidth(), getHeight());
    }

    public void setRed(int x, int y, int value) {
        red[x][y] = Fn.limit(value, 0, 255);
    }

    public void setGreen(int x, int y, int value) {
        green[x][y] = Fn.limit(value, 0, 255);
    }

    public void setBlue(int x, int y, int value) {
        blue[x][y] = Fn.limit(value, 0, 255);
    }

    public void drawRect(Rectangle r, int red, int green, int blue) {
        for (int x = 0; x < r.width; x++) {
            setColor(r.x + x, r.y, red, green, blue);
            setColor(r.x + x, r.y + r.height, red, green, blue);
        }
        for (int y = 0; y < r.height; y++) {
            setColor(r.x, r.y + y, red, green, blue);
            setColor(r.x + r.width, r.y + y, red, green, blue);
        }
    }

    public void fillWhiteRect(int x1, int y1, int x2, int y2) {
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                setColor(x, y, MAX, MAX, MAX);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Modification Methods">
    public ColorMatrix appendRight(ColorMatrix matrix) {
        int newWidth = getWidth() + matrix.getWidth() + 1;
        int newHeight = Math.max(getHeight(), matrix.getHeight());

        ColorMatrix out = new ColorMatrix(newWidth, newHeight);

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                out.setColor(x, y, getColorArray(x, y));
            }
            out.setColor(getWidth(), y, 255, 0, 0);
        }
        int xOffset = getWidth() + 1;
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                out.setColor(x + xOffset, y, matrix.getColorArray(x, y));
            }
        }
        return out;
    }

    public ColorMatrix appendDown(ColorMatrix matrix) {
        int newWidth = Math.max(getWidth(), matrix.getWidth());
        int newHeight = getHeight() + matrix.getHeight() + 1;

        ColorMatrix out = new ColorMatrix(newWidth, newHeight);

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                out.setColor(x, y, getColorArray(x, y));
            }
            out.setColor(x, getHeight(), 255, 0, 0);
        }
        int yOffset = getHeight() + 1;
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                out.setColor(x, y + yOffset, matrix.getColorArray(x, y));
            }
        }

        return out;
    }

    public ColorMatrix crop(Rectangle rect) {
        ColorMatrix out = new ColorMatrix(rect.width + 1, rect.height + 1);
        for (int y = 0; y < rect.height + 1; y++) {
            for (int x = 0; x < rect.width + 1; x++) {
                out.setColor(x, y, getColorArray(rect.x + x, rect.y + y));
            }
        }
        return out;
    }

    private ColorMatrix resize(int newWidth, int newHeight, boolean smooth) {
        ColorMatrix out = new ColorMatrix(newWidth, newHeight);
        if (smooth) {
            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    double newX = (double) x * getWidth() / newWidth;
                    double newY = (double) y * getHeight() / newHeight;
                    int xInt = (int) newX;
                    int yInt = (int) newY;
                    double xDec = newX - xInt;
                    double yDec = newY - yInt;

                    double[][] factor = {
                        {(1 - xDec) * (1 - yDec), (xDec) * (1 - yDec)},
                        {(1 - xDec) * (yDec), (xDec) * (yDec)}
                    };

                    int r = convR(xInt, yInt, factor);
                    int g = convG(xInt, yInt, factor);
                    int b = convB(xInt, yInt, factor);
                    out.setColor(x, y, r, g, b);
                }
            }
            return out;
        }
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int x2 = Math.round((float) x * getWidth() / newWidth);
                int y2 = Math.round((float) y * getHeight() / newHeight);
                out.setColor(x, y, getColorArray(x2, y2));
            }
        }
        return out;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Filter Methods">
    public ColorMatrix grayscale() {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix out = new ColorMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] c = getColorArray(x, y);
                int avg = (int) (0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]);
                out.setColor(x, y, avg, avg, avg);
            }
        }
        return out;
    }

    public ColorMatrix monochrome(double threshold) {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix out = new ColorMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] c = getColorArray(x, y);
                int avg = (c[0] + c[1] + c[2]) / 3;
                avg = avg > (255 * threshold) ? 255 : 0;
                out.setColor(x, y, avg, avg, avg);
            }
        }
        return out;
    }

    public ColorMatrix monochrome(Color color) {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix out = new ColorMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = isSameColor(x, y, color) ? 0 : 255;
                out.setColor(x, y, c, c, c);
            }
        }
        return out;
    }

    public ColorMatrix monochrome(Color color, double threshold) {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix out = new ColorMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = isSimColor(x, y, color, threshold) ? 0 : 255;
                out.setColor(x, y, c, c, c);
            }
        }
        return out;
    }
//
//    public ColorMatrix xor(ColorMatrix matrix) {
//        int width = getWidth();
//        int height = getHeight();
//        ColorMatrix resized = matrix.resize(width, height, false);
//        ColorMatrix out = new ColorMatrix(width, height);
//
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                if (isSimColor(x, y, resized.getColor(x, y), THRESHOLD_SIMILARITY)) {
//                    out.setWhite(x, y);
//                } else {
//                    out.setBlack(x, y);
//                }
//            }
//        }
//        return out;
//    }

    public ColorMatrix invert() {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix out = new ColorMatrix(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out.setColor(x, y, MAX - getRed(x, y), MAX - getGreen(x, y), MAX - getBlue(x, y));
            }
        }
        return out;
    }

    private static final double FACTOR_USED = 0.6;

    private static Color used(Color c) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        return new Color(
                Fn.limit((int) (red * FACTOR_USED), 0, 255),
                Fn.limit((int) (green * FACTOR_USED), 0, 255),
                Fn.limit((int) (blue * FACTOR_USED), 0, 255)
        );
    }

    public ColorMatrix simplify(boolean used, Color... colors) {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix out = new ColorMatrix(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color mc = null;
                double mv = 1.0;
                for (Color c : colors) {
                    double v = variance(x, y, used ? used(c) : c);
                    if (v < mv) {
                        mc = c;
                        mv = v;
                    }
                }
                out.setColor(x, y, mc);
            }
        }
        return out;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Convolution Methods">
    public int convR(int x, int y, double[][] factor) {
        if (factor.length == 0) {
            return 0;
        }
        int height = factor.length;
        int width = factor[0].length;

        double out = 0;
        for (int yd = 0; yd < height; yd++) {
            for (int xd = 0; xd < width; xd++) {
                double r = getRed(x + xd, y + yd);
                double f = factor[yd][xd];
                out += r * f;
            }
        }
        return (int) out;
    }

    public int convG(int x, int y, double[][] factor) {
        if (factor.length == 0) {
            return 0;
        }
        int height = factor.length;
        int width = factor[0].length;

        double out = 0;
        for (int yd = 0; yd < height; yd++) {
            for (int xd = 0; xd < width; xd++) {
                double r = getGreen(x + xd, y + yd);
                double f = factor[yd][xd];
                out += r * f;
            }
        }
        return (int) out;
    }

    public int convB(int x, int y, double[][] factor) {
        if (factor.length == 0) {
            return 0;
        }
        int height = factor.length;
        int width = factor[0].length;

        double out = 0;
        for (int yd = 0; yd < height; yd++) {
            for (int xd = 0; xd < width; xd++) {
                double r = getBlue(x + xd, y + yd);
                double f = factor[yd][xd];
                out += r * f;
            }
        }
        return (int) out;
    }
    // </editor-fold>

    public int monochromeCount(Color color, double threshold) {
        int out = 0;
        ColorMatrix cm = monochrome(color, threshold);
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (cm.getRed(x, y) == 0) {
                    out++;
                }
            }
        }
        return out;
    }

    public Set<Rectangle> findRects() {
        int width = getWidth();
        int height = getHeight();

        ColorMatrix temp = new ColorMatrix(this);
        Set<Rectangle> rects = new HashSet<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (temp.getRed(x, y) == 0) {
                    int xMin = x;
                    int xMax = x;
                    int yMin = y;
                    int yMax = y;
                    List<Point> pts = new LinkedList<>();
                    pts.add(new Point(x, y));

                    while (!pts.isEmpty()) {
                        Point pt = pts.get(0);
                        pts.remove(0);
                        temp.setRed(pt.x, pt.y, 127);

                        if (xMin > pt.x) {
                            xMin = pt.x;
                        }
                        if (xMax < pt.x) {
                            xMax = pt.x;
                        }
                        if (yMin > pt.y) {
                            yMin = pt.y;
                        }
                        if (yMax < pt.y) {
                            yMax = pt.y;
                        }

                        get8Neighbors(pt, width, height).stream()
                                .filter((p) -> temp.getRed(p.x, p.y) == 0)
                                .filter((p) -> !pts.contains(p))
                                .forEach((p) -> pts.add(p));
                    }
                    rects.add(new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin));
                }
            }
        }

        return rects;
    }

    private static final double THRESHOLD_SIMILARITY = 0.01;

    public double similarity(ColorMatrix matrix) {
        int width = getWidth();
        int height = getHeight();
        ColorMatrix resized = matrix.resize(width, height, false);
        double count = 0;
        int total = width * height;

        if (total == 0) {
            return 0.0;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isSimColor(x, y, resized.getColor(x, y), THRESHOLD_SIMILARITY)) {
                    count++;
                }
            }
        }
        return count / total;
    }

    private double variance(int x, int y, Color c) {
        int dR = Math.abs(getRed(x, y) - c.getRed());
        int dG = Math.abs(getGreen(x, y) - c.getGreen());
        int dB = Math.abs(getBlue(x, y) - c.getBlue());
        double sqSum = dR * dR + dG * dG + dB * dB;
        return sqSum / SQSUMMAX;
    }

    private boolean isSimColor(int x, int y, Color c, double threshold) {
        return variance(x, y, c) < threshold;
    }

    private boolean isSameColor(int x, int y, Color c) {
        return getColor(x, y).equals(c);
    }

//    private static Set<Point> get4Neighbors(Point p, int width, int height) {
//        Point[] pts = new Point[]{
//            new Point(p.x, p.y - 1),
//            new Point(p.x, p.y + 1),
//            new Point(p.x - 1, p.y),
//            new Point(p.x + 1, p.y)
//        };
//        Set<Point> out = new HashSet<>();
//        for (Point pt : pts) {
//            if (0 <= pt.x && pt.x < width && 0 <= pt.y && pt.y < height) {
//                out.add(pt);
//            }
//        }
//        return out;
//    }
    private static Set<Point> get8Neighbors(Point p, int width, int height) {
        Point[] pts = new Point[]{
            new Point(p.x, p.y - 1),
            new Point(p.x, p.y + 1),
            new Point(p.x - 1, p.y),
            new Point(p.x + 1, p.y),
            new Point(p.x - 1, p.y - 1),
            new Point(p.x - 1, p.y + 1),
            new Point(p.x + 1, p.y - 1),
            new Point(p.x + 1, p.y + 1)
        };
        Set<Point> out = new HashSet<>();
        for (Point pt : pts) {
            if (0 <= pt.x && pt.x < width && 0 <= pt.y && pt.y < height) {
                out.add(pt);
            }
        }
        return out;
    }
}
