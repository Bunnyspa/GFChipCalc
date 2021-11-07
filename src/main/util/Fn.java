package main.util;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.FontUIResource;
import main.puzzle.Chip;

public class Fn {

    // <editor-fold defaultstate="collapsed" desc="GUI Methods"> 
    public static Set<Component> getAllComponents(Component component) {
        Set<Component> components = new HashSet<>();
        components.add(component);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                components.addAll(getAllComponents(child));
            }
        }
        return components;
    }

    public static void setUIFont(Font font) {
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    public static void addEscDisposeListener(JDialog aDialog) {
        getAllComponents(aDialog).forEach((c) -> {
            c.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            aDialog.dispose();
                            break;
                        default:
                    }
                }
            });
        });
    }

    public static void addEscListener(JDialog aDialog, Runnable r) {
        getAllComponents(aDialog).forEach((c) -> {
            c.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    switch (evt.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            r.run();
                            break;
                        default:
                    }
                }
            });
        });
    }

    public static void open(Component c, JDialog dialog) {
        dialog.setLocationRelativeTo(c);
        dialog.setVisible(true);
    }

    public static void open(Component c, JFrame frame) {
        frame.setLocationRelativeTo(c);
        frame.setVisible(true);
    }

    public static int getWidth(String str, Font font) {
        Canvas c = new Canvas();
        return c.getFontMetrics(font).stringWidth(str);
    }

    public static int getHeight(Font font) {
        Canvas c = new Canvas();
        return c.getFontMetrics(font).getHeight();
    }

    public static void popup(JComponent comp, String title, String text) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 0, 5)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

        JLabel textLabel = new JLabel(text);
        textLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(textLabel, BorderLayout.CENTER);

        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                dialog.dispose();
            }
        });
        dialog.add(panel);
        dialog.pack();

        Point p = comp.getLocationOnScreen();
        p.translate((comp.getWidth() - dialog.getWidth()) / 2, 0);
        dialog.setLocation(p);
        dialog.setVisible(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="String">
    public static String toHTML(String s) {
        String out = "<html>";
        out += s.replaceAll("\\r\\n|\\r|\\n", "<br>").trim();
        out += "</html>";
        return out;
    }

    public static String htmlColor(String s, Color c) {
        return "<font color=" + Fn.colorToHexcode(c) + ">" + s + "</font>";
    }

    public static String htmlColor(int i, Color c) {
        return htmlColor(String.valueOf(i), c);
    }

    public static String getTime(long s) {
        long hour = s / 3600;
        long min = (s % 3600) / 60;
        long sec = s % 60;
        return hour + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
    }

    public static String thousandComma(int i) {
        return String.format("%,d", i);
    }

    public static String fStr(double d, int len) {
        return String.format("%." + len + "f", d);
    }

    public static String fPercStr(double d) {
        return String.format("%.2f", d * 100) + "%";
    }

    public static String iPercStr(double d) {
        return String.valueOf(Math.round(d * 100)) + "%";
    }

    public static String pad(String s, int i) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append(s);
        }
        return sb.toString();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Number">
    public static int limit(int i, int min, int max) {
        return Math.min(Math.max(i, min), max);
    }

    public static int max(int... ints) {
        if (ints.length == 0) {
            return 0;
        }
        int out = ints[0];
        for (int i : ints) {
            out = Math.max(out, i);
        }
        return out;
    }

    public static int floor(int n, int d) {
        return n / d;
    }

    public static int ceil(int n, int d) {
        return floor(n, d) + (n % d == 0 ? 0 : 1);
    }

    public static int sum(int... ints) {
        int out = 0;
        for (int i : ints) {
            out += i;
        }
        return out;
    }

    private static double getPerc(double value, double min, double max) {
        if (min >= max) {
            return 0.0;
        }
        if (value < min) {
            return 0.0;
        }
        if (max < value) {
            return 1.0;
        }
        return (value - min) / (max - min);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Array"> 
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Color">
    public static String colorToHexcode(Color c) {
        String colorHex = Integer.toHexString(c.getRGB());
        return "#" + colorHex.substring(2, colorHex.length());
    }

    public static Color percColor(Color c1, Color c2, Color c3, double value, double min, double max) {
        return percColor(c1, c2, c3, getPerc(value, min, max));
    }

    public static Color percColor(Color c1, Color c2, double value, double min, double max) {
        return percColor(c1, c2, getPerc(value, min, max));
    }

    public static Color percColor(Color c1, Color c2, double d) {
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();

        int r3 = r1 + (int) Math.round((r2 - r1) * d);
        int g3 = g1 + (int) Math.round((g2 - g1) * d);
        int b3 = b1 + (int) Math.round((b2 - b1) * d);

        return new Color(r3, g3, b3);
    }

    private static Color percColor(Color c1, Color c2, Color c3, double d) {
        if (d < 0.5f) {
            return percColor(c1, c2, d * 2);
        } else {
            return percColor(c2, c3, (d - 0.5f) * 2);
        }
    }

    public static Color getColor(float hue) {
        return Color.getHSBColor(hue, 0.75f, 0.75f);
    }

    public static Color getSizeColor(int size) {
        float hue = ((float) 6 - size) / Chip.SIZE_MAX;
        return Color.getHSBColor(hue, 0.5f, 0.75f);
    }

    public static float[] getHSB(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return hsb;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Dimension">
    public static boolean isInside(Point p, Rectangle r) {
        if (p == null) {
            return false;
        }
        return isInside(p.x, p.y, r);
    }

    public static boolean isInside(int x, int y, Rectangle r) {
        return r.x < x
                && x < r.x + r.width
                && r.y < y
                && y < r.y + r.height;
    }

    public static boolean isOverlapped(Rectangle r1, Rectangle r2) {
        return r2.x < r1.x + r1.width
                && r1.x < r2.x + r2.width
                && r2.y < r1.y + r1.height
                && r1.y < r2.y + r2.height;
    }

    public static Rectangle fit(int width, int height, Rectangle container) {
        int newWidth = container.width;
        int newHeight = height * container.width / width;
        if (container.height < newHeight) {
            newWidth = width * container.height / height;
            newHeight = container.height;
        }
        int x = container.x + container.width / 2 - newWidth / 2;
        int y = container.y + container.height / 2 - newHeight / 2;
        Rectangle out = new Rectangle(x, y, newWidth, newHeight);
        return out;
    }
    // </editor-fold>
}
