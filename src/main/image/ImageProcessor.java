package main.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import main.data.Unit;
import main.puzzle.Chip;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.ui.resource.AppColor;
import main.ui.resource.AppImage;
import main.util.DoubleKeyHashMap;
import main.util.Fn;

public class ImageProcessor {

    private static final Color STAR = AppColor.YELLOW_STAR;
    private static final Color LEVEL = AppColor.LEVEL;
    private static final Color GRAY = new Color(66, 66, 66);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;

    private static final double FACTOR_USED = 0.6;

    private static final double THRESHOLD_STAR = 0.05;
    private static final double THRESHOLD_COLOR = 0.01;

    private static class DebugInfo {

        ColorMatrix image;
        boolean used, leveled;
        Unit.Color color;
    }

    private static class ShapeRot {

        final Shape shape;
        final Integer rotation;

        public ShapeRot(Shape shape, Integer rotation) {
            this.shape = shape;
            this.rotation = rotation;
        }
    }

    public static List<Rectangle> detectChips(BufferedImage image) {
        return detectChips(new ColorMatrix(image));
    }

    private static List<Rectangle> detectChips(ColorMatrix matrix) {
        List<Rectangle> rects_ratio = new ArrayList<>();

        // Ratio
        matrix.monochrome(0.5).findRects().stream()
                .filter((r) -> 90 <= r.width)
                .filter((r) -> {
                    double ratio = (double) r.height / r.width;
                    return 1.3 < ratio && ratio < 1.7;
                }).forEach((r) -> rects_ratio.add(r));

        // Remove outliers using width median
        rects_ratio.sort((o1, o2) -> Integer.compare(o1.width, o2.width));
        int medianWidth = rects_ratio.get(rects_ratio.size() / 2).width;
        List<Rectangle> rects = rects_ratio.stream().filter((r) -> Math.abs(r.width - medianWidth) < 10).collect(Collectors.toList());

        return rects;
    }

    public static Chip idChip(BufferedImage image, Rectangle rect) {
        return idChip(new ColorMatrix(image).crop(rect), null);
    }

    private static Chip idChip(ColorMatrix matrix, DebugInfo debug) {
        //System.out.println("====================");
        float factor = (float) matrix.getWidth() / 100;

        // System.out.println("Factor: " + factor);
        // Used
        Rectangle preStarRect = new Rectangle(0, 0, (int) (56 * factor), (int) (18 * factor));
        ColorMatrix usedCM = matrix.crop(preStarRect);

        int colorNUnused = usedCM.monochromeCount(STAR, THRESHOLD_STAR);
        int colorNUsed = usedCM.monochromeCount(used(STAR), THRESHOLD_STAR);
        boolean used = colorNUsed > colorNUnused;
        if (debug != null) {
            debug.used = used;
        }
        // System.out.println("Used: " + used);

        // Color
        Color orange = AppColor.CHIPS.get(Unit.Color.ORANGE);
        Color blue = AppColor.CHIPS.get(Unit.Color.BLUE);
        int colorNOrange = matrix.monochromeCount(used ? used(orange) : orange, THRESHOLD_COLOR);
        int colorNBlue = matrix.monochromeCount(used ? used(blue) : blue, THRESHOLD_COLOR);
        Unit.Color color = colorNBlue > colorNOrange ? Unit.Color.BLUE : Unit.Color.ORANGE;
        if (debug != null) {
            debug.color = color;
        }
        // System.out.println("Color: " + color + "=" + Chip.COLORSTRS.get(color));

        // Simplify
        ColorMatrix cm = simplify(matrix, used, color);

        // Star
        ColorMatrix starCM = cm.crop(preStarRect);
        Set<Rectangle> starRects = filterRects_star(starCM.monochrome(STAR).findRects(), factor);
        int star = Fn.limit(starRects.size(), Chip.STAR_MIN, Chip.STAR_MAX); //idStar(starRect, factor);
        // System.out.println("Star: " + star);

        // Level
        Set<Rectangle> levelRects = cm.monochrome(LEVEL).findRects();
        Rectangle levelRect = filterRect_level(levelRects, factor);

        int level = 0;
        if (levelRect != null) {
            ColorMatrix levelCM = cm.crop(levelRect).monochrome(LEVEL).invert();
            Set<Rectangle> levelDigitRects = filterRects_levelDigit(levelCM.findRects(), factor);
            level = Fn.limit(idDigits(levelCM, levelDigitRects), 0, 20);

            // idChip_drawRect(debug, used(LEVEL), levelRect);
            // idChip_drawRect(debug, LEVEL, levelDigitRects, levelRect.x, levelRect.y);
        }
        // System.out.println("Level: " + level);

        // Stat Icon
        Set<Rectangle> statIconAreaRects = filterRects_statIconArea(cm.monochrome(WHITE).findRects(), factor);

        // Color and Shape
        int shapeY1 = (int) (getMaxY(starRects, (int) (10 * factor)) + 8 * factor);
        int shapeY2 = (int) (getMinY(statIconAreaRects, (int) (56 * factor)) + 4 * factor);
        Rectangle shapeAreaRect = new Rectangle(0, shapeY1, cm.getWidth(), shapeY2 - shapeY1);
        ColorMatrix shapeAreaCM = cm.crop(shapeAreaRect).monochrome(AppColor.CHIPS.get(color));
        Rectangle shapeRect = filterRect_shape(shapeAreaCM.findRects(), factor);

        ShapeRot shapeRot = new ShapeRot(Shape.DEFAULT, 0);
        if (shapeRect != null) {
            ColorMatrix shapeCM = shapeAreaCM.crop(shapeRect);
            shapeRot = idShape(shapeCM);
        }
        Shape shape = shapeRot.shape;
        int rotation = shapeRot.rotation;
        // System.out.println("Name: " + name);
        // System.out.println("Rotation: " + rotation);

        // Stat
        boolean leveled = level != 0;
        if (debug != null) {
            debug.leveled = leveled;
        }

        int dmg = 0, brk = 0, hit = 0, rld = 0;
        for (Rectangle r : statIconAreaRects) {
            Rectangle statIconRect = merge(cm.crop(new Rectangle(r.x + 1, r.y + 1, r.width - 2, r.height - 2)).monochrome(WHITE).invert().findRects());
            //  testImage(cm.crop(r).monochrome(WHITE).invert());
            // System.out.println("SIR: " + statIconRect);
            // idChip_drawRect(debug, Color.MAGENTA.darker(), statIconRect, r.x, r.y);
            int statType = idStatType(cm.crop(r).crop(statIconRect));
            if (statType != -1) {
                Rectangle statRect = new Rectangle(r.x + r.width, (int) (r.y + 2 * factor), (int) (28 * factor), r.height);

                ColorMatrix statCM = simplify_statDigits(matrix.crop(statRect), used, leveled).monochrome(GRAY).invert();
                Set<Rectangle> statDigitRects = filterRects_statDigit(statCM.findRects(), factor);
                int stat = idDigits(statCM, statDigitRects);
                // System.out.println(statType + ": " + stat);
                switch (statType) {
                    case Stat.DMG:
                        dmg = Chip.getPt(Chip.RATE_DMG, shape.getType(), star, level, stat);
                        break;
                    case Stat.BRK:
                        brk = Chip.getPt(Chip.RATE_BRK, shape.getType(), star, level, stat);
                        break;
                    case Stat.HIT:
                        hit = Chip.getPt(Chip.RATE_HIT, shape.getType(), star, level, stat);
                        break;
                    default:
                        rld = Chip.getPt(Chip.RATE_RLD, shape.getType(), star, level, stat);
                }

                // idChip_drawRect(debug, Color.MAGENTA, statRect);
                // idChip_drawRect(debug, Color.MAGENTA.darker(), statDigitRects, statRect.x, statRect.y);
            }
        }
        int[] stats = new int[]{dmg, brk, hit, rld};
        for (int i = 0; i < 4; i++) {
            if (stats[i % 4] < 0 && stats[(i + 1) % 4] >= 0 && stats[(i + 2) % 4] >= 0 && stats[(i + 3) % 4] >= 0) {
                stats[i % 4] = shape.getSize() - stats[(i + 1) % 4] - stats[(i + 2) % 4] - stats[(i + 3) % 4];
                break;
            }
        }

        Stat pt = new Stat(
                Fn.limit(stats[0], 0, Chip.PT_MAX),
                Fn.limit(stats[1], 0, Chip.PT_MAX),
                Fn.limit(stats[2], 0, Chip.PT_MAX),
                Fn.limit(stats[3], 0, Chip.PT_MAX)
        );
        // System.out.println("PT: " + pt.toString());

        // System.out.println("-----");
        // idChip_drawRect(debug, WHITE, new Rectangle(0, 0, cm.getWidth(), cm.getHeight()));
        // Star
        // idChip_drawRect(debug, used(STAR), preStarRect);
        // idChip_drawRect(debug, STAR, starRects);
        // Stat Icon
        // statIconRects.forEach((r) -> System.out.println("StatIcon: " + r));
        // idChip_drawRect(debug, Color.MAGENTA, statIconAreaRects);
        //idChip_drawHorizontalLine(image, Color.RED, shapeY1);
        //idChip_drawHorizontalLine(image, Color.RED, shapeY2);
        // Shape
        // idChip_drawRect(debug, Color.GREEN, shapeAreaRect);
        // System.out.println("Name: " + nameRect);
        // idChip_drawRect(debug, Color.GREEN.darker(), shapeRect, shapeAreaRect.x, shapeAreaRect.y);
        // System.out.println("PT: " + pt.toString());
        return new Chip(shape, star, color, pt, level, rotation);
    }

//    private static void idChip_drawRect(DebugInfo debug, Color color, Rectangle rect) {
//        idChip_drawRect(debug, color, rect, 0, 0);
//    }
//
//    private static void idChip_drawRect(DebugInfo debug, Color color, Collection<Rectangle> rects) {
//        rects.forEach((rect) -> idChip_drawRect(debug, color, rect));
//    }
//
//    private static void idChip_drawRect(DebugInfo debug, Color color, Collection<Rectangle> rects, int xOffset, int yOffset) {
//        rects.forEach((rect) -> idChip_drawRect(debug, color, rect, xOffset, yOffset));
//    }
//
//    private static void idChip_drawRect(DebugInfo debug, Color color, Rectangle rect, int xOffset, int yOffset) {
//        if (debug != null && debug.image != null && rect != null) {
//            debug.image.drawRect(new Rectangle(
//                    rect.x + xOffset,
//                    rect.y + yOffset,
//                    rect.width + 1,
//                    rect.height + 1
//            ), color.getRed(), color.getGreen(), color.getBlue());
//        }
//    }
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

    private static ColorMatrix simplify(ColorMatrix matrix, boolean used, Unit.Color color) {
        return matrix.simplify(used, STAR, LEVEL, WHITE, GRAY, BLACK, AppColor.CHIPS.get(color));
    }

    private static ColorMatrix simplify_statDigits(ColorMatrix matrix, boolean used, boolean leveled) {
        Color textColor = leveled ? LEVEL : WHITE;
        return matrix.simplify(used, textColor, GRAY, Fn.percColor(textColor, GRAY, 0.5));
    }

    private static Set<Rectangle> filterRects_star(Set<Rectangle> rects, float factor) {
        Set<Rectangle> out = new HashSet<>();
        rects.stream()
                .filter((r) -> 3 * factor < r.width)
                .filter((r) -> 9 * factor > r.width)
                .filter((r) -> 3 * factor < r.height)
                .filter((r) -> 9 * factor > r.height)
                .forEach((r) -> out.add(r));
        return out;
    }

    private static Rectangle filterRect_level(Set<Rectangle> rects, float factor) {
        for (Rectangle r : rects) {
            if (15 * factor < r.width
                    && 70 * factor < r.x
                    && 90 * factor > r.x) {
                return r;
            }
        }
        return null;
    }

    private static Set<Rectangle> filterRects_levelDigit(Set<Rectangle> rects, float factor) {
        Set<Rectangle> out = new HashSet<>();
        rects.stream()
                .filter((r) -> 6 * factor > r.width)
                .filter((r) -> 8 * factor < r.height)
                .filter((r) -> 12 * factor > r.height)
                .forEach((r) -> out.add(r));
        return out;
    }

    private static Set<Rectangle> filterRects_statIconArea(Set<Rectangle> rects, float factor) {
        Set<Rectangle> out = new HashSet<>();
        rects.stream()
                .filter((r) -> 18 * factor < r.width)
                .filter((r) -> 22 * factor > r.width)
                .filter((r) -> (0 <= r.x && r.x <= 5 * factor)
                || (45 * factor <= r.x && r.x <= 55 * factor))
                .forEach((r) -> out.add(r));
        return out;
    }

    private static Set<Rectangle> filterRects_statDigit(Set<Rectangle> rects, float factor) {
        Set<Rectangle> out = new HashSet<>();
        rects.stream()
                .filter((r) -> 9 * factor > r.width)
                .filter((r) -> 12 * factor < r.height)
                .filter((r) -> 18 * factor > r.height)
                .forEach((r) -> out.add(r));
        return out;
    }

    private static final ColorMatrix[] CM_STATS = new ColorMatrix[]{
        new ColorMatrix(AppImage.IP_DMG),
        new ColorMatrix(AppImage.IP_BRK),
        new ColorMatrix(AppImage.IP_HIT),
        new ColorMatrix(AppImage.IP_RLD)
    };

    private static int idStatType(ColorMatrix matrix) {
        double[] sims = new double[4];
        for (int i = 0; i < 4; i++) {
            sims[i] = matrix.similarity(CM_STATS[i]);
        }
        // System.out.println("sim: " + Arrays.toString(sims));
        if (sims[1] < sims[0] && sims[2] < sims[0] && sims[3] < sims[0]) {
            return Stat.DMG;
        }
        if (sims[2] < sims[1] && sims[3] < sims[1]) {
            return Stat.BRK;
        }
        if (sims[3] < sims[2]) {
            return Stat.HIT;
        }
        return Stat.RLD;
    }

    private static final ColorMatrix[] CM_DIGITS = new ColorMatrix[]{
        new ColorMatrix(AppImage.IP_DIGITS[0]),
        new ColorMatrix(AppImage.IP_DIGITS[1]),
        new ColorMatrix(AppImage.IP_DIGITS[2]),
        new ColorMatrix(AppImage.IP_DIGITS[3]),
        new ColorMatrix(AppImage.IP_DIGITS[4]),
        new ColorMatrix(AppImage.IP_DIGITS[5]),
        new ColorMatrix(AppImage.IP_DIGITS[6]),
        new ColorMatrix(AppImage.IP_DIGITS[7]),
        new ColorMatrix(AppImage.IP_DIGITS[8]),
        new ColorMatrix(AppImage.IP_DIGITS[9])
    };

    private static int idDigits(ColorMatrix monochromed, Set<Rectangle> rects) {
        int out = 0;
        List<Rectangle> rectList = new ArrayList<>(rects);
        rectList.sort((o1, o2) -> Integer.compare(o1.x, o2.x));

        for (Rectangle r : rectList) {
            ColorMatrix cropped = monochromed.crop(r);
            int digit = idDigit(cropped);
            out = out * 10 + digit;
        }
        return out;
    }

    private static int idDigit(ColorMatrix monochromed) {
        int digit = -1;
        double maxSim = 0;
        // double[] sims = new double[10];
        for (int i = 0; i <= 9; i++) {
            ColorMatrix resource = CM_DIGITS[i];
            double sim = monochromed.similarity(resource);
            // sims[i] = sim;
            if (maxSim < sim) {
                digit = i;
                maxSim = sim;
            }
        }
        // System.out.println(Arrays.toString(sims));
        return digit;
    }

    private static final Shape.Type[] TYPES = {Shape.Type._6, Shape.Type._5B, Shape.Type._5A, Shape.Type._4, Shape.Type._3};
    private static final DoubleKeyHashMap<Shape, Integer, ColorMatrix> SHAPES = new DoubleKeyHashMap<Shape, Integer, ColorMatrix>() // <editor-fold defaultstate="collapsed">
    {
        {
            for (Shape.Type type : TYPES) {
                for (Shape shape : Shape.getShapes(type)) {
                    for (int rotation = 0; rotation < shape.getMaxRotation(); rotation++) {
                        ColorMatrix resource = genShapeResource(shape, rotation);
                        put(shape, rotation, resource);
                    }
                }
            }
        }
    };

    private static final int SHAPE_EDGE = 1;
    private static final int SHAPE_SQUARE = 8;

    private static ColorMatrix genShapeResource(Shape shape, int rotation) {
        PuzzleMatrix<Boolean> pm = Chip.generateMatrix(shape, rotation);
        ColorMatrix out = new ColorMatrix(SHAPE_SQUARE * pm.getNumCol() + SHAPE_EDGE * 2, SHAPE_SQUARE * pm.getNumRow() + SHAPE_EDGE * 2);
        for (int row = 0; row < pm.getNumRow(); row++) {
            for (int col = 0; col < pm.getNumCol(); col++) {
                if (pm.get(row, col)) {
                    genShape_rect(out,
                            col * SHAPE_SQUARE,
                            row * SHAPE_SQUARE,
                            (col + 1) * SHAPE_SQUARE,
                            (row + 1) * SHAPE_SQUARE
                    );
                } else {
                    genShape_rect(out,
                            col * SHAPE_SQUARE + SHAPE_EDGE,
                            row * SHAPE_SQUARE + SHAPE_EDGE,
                            (col + 1) * SHAPE_SQUARE - SHAPE_EDGE,
                            (row + 1) * SHAPE_SQUARE - SHAPE_EDGE
                    );
                    // Up
                    if (pm.get(row - 1, col) == null || !pm.get(row - 1, col)) {
                        genShape_rect(out,
                                col * SHAPE_SQUARE + SHAPE_EDGE,
                                row * SHAPE_SQUARE - SHAPE_EDGE,
                                (col + 1) * SHAPE_SQUARE - SHAPE_EDGE,
                                row * SHAPE_SQUARE + SHAPE_EDGE
                        );
                    }
                    // Down
                    if (pm.get(row + 1, col) == null || !pm.get(row + 1, col)) {
                        genShape_rect(out,
                                col * SHAPE_SQUARE + SHAPE_EDGE,
                                (row + 1) * SHAPE_SQUARE - SHAPE_EDGE,
                                (col + 1) * SHAPE_SQUARE - SHAPE_EDGE,
                                (row + 1) * SHAPE_SQUARE + SHAPE_EDGE
                        );
                    }
                    // Left
                    if (pm.get(row, col - 1) == null || !pm.get(row, col - 1)) {
                        genShape_rect(out,
                                col * SHAPE_SQUARE - SHAPE_EDGE,
                                row * SHAPE_SQUARE + SHAPE_EDGE,
                                col * SHAPE_SQUARE + SHAPE_EDGE,
                                (row + 1) * SHAPE_SQUARE - SHAPE_EDGE
                        );
                    }
                    // Right
                    if (pm.get(row, col + 1) == null || !pm.get(row, col + 1)) {
                        genShape_rect(out,
                                (col + 1) * SHAPE_SQUARE - SHAPE_EDGE,
                                row * SHAPE_SQUARE + SHAPE_EDGE,
                                (col + 1) * SHAPE_SQUARE + SHAPE_EDGE,
                                (row + 1) * SHAPE_SQUARE - SHAPE_EDGE
                        );
                    }
                }
            }
        }
        return out;
    }

    private static void genShape_rect(ColorMatrix out, int x1, int y1, int x2, int y2) {
        out.fillWhiteRect(x1 + SHAPE_EDGE, y1 + SHAPE_EDGE, x2 + SHAPE_EDGE, y2 + SHAPE_EDGE);
    }
    // </editor-fold>

    private static ColorMatrix getShapeResource(Shape shape, int rotation) {
        if (!SHAPES.containsKey(shape, rotation)) {
            return null;
        }
        return SHAPES.get(shape, rotation);
    }

    private static ShapeRot idShape(ColorMatrix monochromed) {
        Shape s = Shape.DEFAULT;
        int r = 0;
        double maxSim = 0;
        double monoRatio = getRatio(monochromed);

        for (Shape.Type type : TYPES) {
            for (Shape shape : Shape.getShapes(type)) {
                for (int rotation = 0; rotation < shape.getMaxRotation(); rotation++) {
                    ColorMatrix resource = getShapeResource(shape, rotation);
                    double resourceRatio = getRatio(resource);
                    if (Math.abs(monoRatio - resourceRatio) < 0.5) {
                        double sim = monochromed.similarity(resource);
                        if (maxSim < sim) {
                            maxSim = sim;
                            s = shape;
                            r = rotation;
                        }
                    }
                }
            }
        }
        return new ShapeRot(s, r);
    }

    private static double getRatio(ColorMatrix matrix) {
        return (double) Math.max(matrix.getWidth(), matrix.getHeight()) / Math.min(matrix.getWidth(), matrix.getHeight());
    }

    private static Rectangle filterRect_shape(Set<Rectangle> rects, double factor) {
        Set<Rectangle> filtered = new HashSet<>();
        rects.stream()
                .filter((r) -> 10 * factor < r.width)
                .filter((r) -> 10 * factor < r.height)
                .forEach((r) -> filtered.add(r));
        return merge(filtered);
    }

    private static int getMinY(Set<Rectangle> rects, int defaultValue) {
        if (rects.isEmpty()) {
            return defaultValue;
        }
        return rects.stream().map((r) -> r.y).min(Integer::compare).get();
    }

    private static int getMaxY(Set<Rectangle> rects, int defaultValue) {
        if (rects.isEmpty()) {
            return defaultValue;
        }
        return rects.stream().map((r) -> r.y + r.height).max(Integer::compare).get();
    }

    private static Rectangle biggest(Collection<Rectangle> rects) {
        Rectangle out = null;
        for (Rectangle r : rects) {
            if (out == null || out.width * out.height < r.width * r.height) {
                out = r;
            }
        }
        return out;
    }

    private static Rectangle merge(Collection<Rectangle> rects) {
        int xMin = -1;
        int xMax = -1;
        int yMin = -1;
        int yMax = -1;
        for (Rectangle r : rects) {
            int x1 = r.x;
            int x2 = r.x + r.width;
            int y1 = r.y;
            int y2 = r.y + r.height;
            if (-1 == xMin) {
                xMin = x1;
                xMax = x2;
                yMin = y1;
                yMax = y2;
            }
            if (xMin > x1) {
                xMin = x1;
            }
            if (xMax < x2) {
                xMax = x2;
            }
            if (yMin > y1) {
                yMin = y1;
            }
            if (yMax < y2) {
                yMax = y2;
            }
        }
        if (-1 != xMin) {
            return new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
        }
        return null;
    }

    // Test
//    public static void test_open() {
//        JFileChooser fileChooser = new JFileChooser(new File("."));
//        fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일 (.png, .jpg, .gif, .bmp)", "png", "jpg", "gif", "bmp"));
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        int retval = fileChooser.showOpenDialog(frame);
//        if (retval == JFileChooser.APPROVE_OPTION) {
//            try {
//                File filePath = fileChooser.getSelectedFile();
//                System.out.println(filePath.getAbsolutePath());
//                ColorMatrix matrix = new ColorMatrix(ImageIO.read(filePath));
//                ColorMatrix image = new ColorMatrix(matrix).grayscale();
//                List<Rectangle> rects = detectChips(matrix, image);
//                if (!rects.isEmpty()) {
//                    Set<Chip> chips = idChips(matrix, rects, image);
//                } else {
//                    System.out.println("WARNING: None detected");
//                }
//                JLabel label = new JLabel(new ImageIcon(image.getImage()));
//                JScrollPane pane = new JScrollPane(label);
//                frame.getContentPane().add(pane, BorderLayout.CENTER);
//            } catch (IOException ex) {
//                App.log(ex);
//                frame.dispose();
//            }
//            frame.pack();
//            frame.setVisible(true);
//        } else {
//            frame.dispose();
//        }
//    }
//
//    public static void test() {
//        test_image("C:\\Users\\bunny\\Documents\\GitHub\\GFChipCalc\\private\\image test\\test.png");
//    }
//
//    private static void test_shape() {
//        ColorMatrix appended = new ColorMatrix();
//        for (String[] names : Chip.NAMES_N) {
//            ColorMatrix line = new ColorMatrix();
//            for (String name : names) {
//                for (int rotation = 0; rotation < Chip.getMaxRotation(name); rotation++) {
//                    line = line.appendRight(genShapeResource(name, rotation));
//                }
//            }
//            appended = appended.appendDown(line);
//        }
//        testImage(appended);
//    }
//
//    private static void test_image(String filePath) {
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        try {
//            ColorMatrix matrix = new ColorMatrix(ImageIO.read(new File(filePath)));
//            ColorMatrix appended = new ColorMatrix();
//            List<Rectangle> rects = detectChips(matrix);
//            if (!rects.isEmpty()) {
//                for (Rectangle r : rects) {
//                    ColorMatrix crop = matrix.crop(r);
//                    DebugInfo debug = new DebugInfo();
//                    debug.image = crop.grayscale();
//                    Chip chip = idChip(crop, debug);
//                    Icon icon = chip.getImage(null);
//                    // System.out.println(chip.toData());
//                    BufferedImage bi = new BufferedImage(
//                            icon.getIconWidth(),
//                            icon.getIconHeight(),
//                            BufferedImage.TYPE_INT_RGB);
//                    Graphics g = bi.createGraphics();
//                    // paint the Icon to the BufferedImage.
//                    g.setColor(Color.WHITE);
//                    g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
//                    icon.paintIcon(null, g, 0, 0);
//                    g.dispose();
//                    ColorMatrix simp = simplify(crop, debug.used, debug.color);
//                    debug.image = debug.image
//                            .appendRight(simp)
//                            .appendRight(crop.monochrome(debug.used ? used(STAR) : STAR, THRESHOLD_STAR))
//                            .appendRight(crop.monochrome(debug.used ? used(GFLGraphics.COLORS_CHIP.get(debug.color)) : GFLGraphics.COLORS_CHIP.get(debug.color), THRESHOLD_COLOR))
//                            //
//                            .appendRight(simp.monochrome(GFLGraphics.COLORS_CHIP.get(debug.color)))
//                            .appendRight(simp.monochrome(STAR))
//                            .appendRight(simp.monochrome(LEVEL))
//                            .appendRight(simplify_statDigits(crop, debug.used, debug.leveled).monochrome(GRAY).invert())
//                            .appendRight(simp.monochrome(WHITE))
//                            .appendRight(simp.monochrome(debug.leveled ? LEVEL : WHITE))
//                            .appendRight(new ColorMatrix(bi));
//                    appended = appended.appendDown(debug.image);
//                }
//            } else {
//                // System.out.println("WARNING: None detected");
//            }
//            JLabel label = new JLabel(new ImageIcon(appended.getImage()));
//            JScrollPane pane = new JScrollPane(label);
//            frame.getContentPane().add(pane, BorderLayout.CENTER);
//        } catch (IOException ex) {
//            App.log(ex);
//            frame.dispose();
//        }
//        frame.pack();
//        frame.setVisible(true);
//    }
    private static void testImage(ColorMatrix matrix) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel label = new JLabel(new ImageIcon(matrix.getImage()));
        JScrollPane pane = new JScrollPane(label);
        frame.getContentPane().add(pane, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
