package main.ui.resource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import main.App;
import main.data.Unit;
import main.puzzle.PuzzleMatrix;
import main.setting.Setting;
import main.util.Fn;

public class AppImage {

    public static final Image FAVICON = getImage("favicon.png");
    public static final ImageIcon BANNER = getIcon("banner.png");

    public static final ImageIcon DONATION = getIcon("donation.png");
    public static final ImageIcon PAYPALQR = getIcon("paypalqr.png");
    public static final ImageIcon PAYPAL = getIcon("paypal.png");

    public static final Image CHIP_MARKED = getImage("chip_marked.png");
    public static final Image CHIP_ROTATED = getImage("chip_rotated.png");
    public static final Image CHIP_EQUIPPED = getImage("chip_equipped.png");

    public static final Icon UI_INFO = UIManager.getIcon("OptionPane.informationIcon");
    public static final Icon UI_WARNING = UIManager.getIcon("OptionPane.warningIcon");

    public static final ImageIcon MP448 = getIcon("MP448.png");

    public static final ImageIcon FONT = getIcon("font.png");
    public static final ImageIcon QUESTION = getIcon("question.png");

    public static final ImageIcon PICTURE = getIcon("picture.png");
    public static final ImageIcon PHONE = getIcon("phone.png");

    public static final ImageIcon ASCNEDING = getIcon("ascending.png");
    public static final ImageIcon DESCENDING = getIcon("descending.png");

    public static final ImageIcon ROTATE_LEFT = getIcon("rotate_left.png");
    public static final ImageIcon ROTATE_RIGHT = getIcon("rotate_right.png");

    public static final ImageIcon PANEL_OPEN = getIcon("panel_open.png");
    public static final ImageIcon PANEL_CLOSE = getIcon("panel_close.png");
    public static final ImageIcon ADD = getIcon("add.png");

    public static final ImageIcon DMG = getIcon("dmg.png");
    public static final ImageIcon BRK = getIcon("brk.png");
    public static final ImageIcon HIT = getIcon("hit.png");
    public static final ImageIcon RLD = getIcon("rld.png");
    public static final ImageIcon[] STATS = new ImageIcon[]{DMG, BRK, HIT, RLD};

    public static final ImageIcon SAVE = getIcon("save.png");
    public static final ImageIcon NEW = getIcon("new.png");
    public static final ImageIcon SAVEAS = getIcon("saveas.png");
    public static final ImageIcon OPEN = getIcon("open.png");

    public static final ImageIcon DISPLAY_PT = getIcon("display_pt.png");
    public static final ImageIcon DISPLAY_STAT = getIcon("display_stat.png");

    public static final ImageIcon FILTER = getIcon("filter.png");
    public static final ImageIcon FILTER_APPLY = getIcon("filter_apply.png");

    public static final ImageIcon DELETE = getIcon("delete.png");

    public static final ImageIcon SETTING = getIcon("setting.png");
    public static final ImageIcon SETTING_PRESET = getIcon("setting_preset.png");
    public static final ImageIcon SETTING_PT = getIcon("setting_pt.png");
    public static final ImageIcon SETTING_STAT = getIcon("setting_stat.png");

    public static final ImageIcon COMB_START = getIcon("combine_start.png");
    public static final ImageIcon COMB_PAUSE = getIcon("combine_pause.png");
    public static final ImageIcon COMB_STOP = getIcon("combine_stop.png");

    public static final ImageIcon LOADING = getIcon("loading.gif");
    public static final ImageIcon PAUSED = getIcon("paused.png");

    public static final ImageIcon TICKET = getIcon("ticket.png");

    public static final ImageIcon CHECKED = getIcon("checked.png");
    public static final ImageIcon UNCHECKED = getIcon("unchecked.png");

    public static final ImageIcon HELP_PROXY = getIcon("help/proxy.jpg");

    private static final BufferedImage IP_0 = getImage("imgproc/0.png");
    private static final BufferedImage IP_1 = getImage("imgproc/1.png");
    private static final BufferedImage IP_2 = getImage("imgproc/2.png");
    private static final BufferedImage IP_3 = getImage("imgproc/3.png");
    private static final BufferedImage IP_4 = getImage("imgproc/4.png");
    private static final BufferedImage IP_5 = getImage("imgproc/5.png");
    private static final BufferedImage IP_6 = getImage("imgproc/6.png");
    private static final BufferedImage IP_7 = getImage("imgproc/7.png");
    private static final BufferedImage IP_8 = getImage("imgproc/8.png");
    private static final BufferedImage IP_9 = getImage("imgproc/9.png");
    public static final BufferedImage[] IP_DIGITS = new BufferedImage[]{
        IP_0, IP_1, IP_2, IP_3, IP_4,
        IP_5, IP_6, IP_7, IP_8, IP_9
    };

    public static final BufferedImage IP_DMG = getImage("imgproc/dmg.png");
    public static final BufferedImage IP_BRK = getImage("imgproc/brk.png");
    public static final BufferedImage IP_HIT = getImage("imgproc/hit.png");
    public static final BufferedImage IP_RLD = getImage("imgproc/rld.png");

    private static ImageIcon getIcon(String path) {
        return new ImageIcon(App.getResource(path));
    }

    private static BufferedImage getImage(String path) {
        try {
            return ImageIO.read(App.getResource(path));
        } catch (IOException ex) {
            App.log(ex);
        }
        return null;
    }

    public static ImageIcon getScaledIcon(Icon icon, int width, int height) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public static class Chip {

        private static final int CHIP_TILESIZE = 12;
        private static final int CHIP_GAP = 2;
        private static final int CHIP_HEIGHT1 = CHIP_TILESIZE * 6 + CHIP_GAP * 2;
        private static final int CHIP_HEIGHT2 = CHIP_TILESIZE * 3 + CHIP_GAP * 4;

        public static ImageIcon get(App app, main.puzzle.Chip chip) {
            boolean statExists = chip.statExists();
            // Chip
            int star = chip.getStar();
            int boardIndex = chip.getBoardIndex();
            boolean marked = chip.isMarked();
            Unit.Color color = chip.getColor();
            int level = chip.getLevel();
            int initLevel = chip.getInitLevel();
            int rotation = chip.getRotation();
            int initRotation = chip.getInitRotation();
            int displayType = chip.getDisplayType();
            // Matrix
            PuzzleMatrix<Boolean> matrix = chip.generateMatrix();
            int mw = matrix.getNumCol();
            int mh = matrix.getNumRow();
            // Image
            int iw = width(statExists);
            int ih = height(statExists);
            int yOffset1 = CHIP_TILESIZE + CHIP_GAP * 2;
            int yOffset2 = CHIP_TILESIZE * 7 + CHIP_GAP * 4;
            BufferedImage bi = new BufferedImage(iw + 1, ih + 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bi.getGraphics();
            if (statExists) {
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, iw, ih);
                g.fillRect(0, 0, iw, yOffset1 + 1);
                g.fillRect(0, yOffset2, iw, ih - yOffset2);
            }
            for (int row = 0; row < mh; row++) {
                for (int col = 0; col < mw; col++) {
                    if (matrix.get(row, col)) {
                        int tileXOffset = (int) (iw / 2 + (col - (double) mw / 2) * CHIP_TILESIZE);
                        int tileYOffset = (int) ((3 + row - (double) mh / 2) * CHIP_TILESIZE) + (statExists ? CHIP_TILESIZE + CHIP_GAP * 2 : 0) + CHIP_GAP;
                        g.setColor(Color.BLACK);
                        g.fillRect(tileXOffset, tileYOffset, CHIP_TILESIZE + 1, CHIP_TILESIZE + 1);
                        g.setColor(boardIndex > -1 ? app.colors()[boardIndex % app.colors().length] : !statExists ? AppColor.getPoolColor(app, chip) : marked ? AppColor.CHIPS.get(color).darker().darker() : AppColor.CHIPS.get(color));
                        g.fillRect(tileXOffset + 1, tileYOffset + 1, CHIP_TILESIZE - 1, CHIP_TILESIZE - 1);
                    }
                }
            }
            if (statExists) {
                g.setColor(AppColor.YELLOW_STAR);
                String starString = "";
                for (int i = 0; i < star; i++) {
                    starString += AppText.TEXT_STAR_FULL;
                }
                int xOffset = iw / 2;
                g.drawString(starString, CHIP_GAP, CHIP_TILESIZE + CHIP_GAP);
                // Level
                g.setFont(AppFont.FONT_DIGIT);
                if (0 < level) {
                    String levelStr = (initLevel == level) ? "+" + level : initLevel + "\u2192" + level;
                    int levelWidth = Fn.getWidth(levelStr, g.getFont());
                    g.setColor(AppColor.LEVEL);
                    if (initLevel == level) {
                        g.fillPolygon(new int[]{iw, iw, iw - CHIP_TILESIZE * 2 - CHIP_GAP}, new int[]{yOffset2, yOffset2 - CHIP_TILESIZE * 2 - CHIP_GAP, yOffset2}, 3);
                    } else {
                        g.fillRect(iw - levelWidth - 1, yOffset2 - CHIP_TILESIZE - CHIP_GAP, levelWidth + 1, CHIP_TILESIZE + CHIP_GAP);
                    }
                    g.setColor(Color.WHITE);
                    g.drawString(levelStr, iw - levelWidth, yOffset2 - 1);
                }
                // Equipped
                if (chip.containsHOCTagName()) {
                    g.drawImage(AppImage.CHIP_EQUIPPED, 0, yOffset1, CHIP_TILESIZE, CHIP_TILESIZE, null);
                }
                // Rotation
                if (initRotation != rotation) {
                    g.drawImage(AppImage.CHIP_ROTATED, iw - CHIP_TILESIZE + 1, yOffset1, CHIP_TILESIZE, CHIP_TILESIZE, null);
                }
                // Mark
                if (marked) {
                    g.drawImage(AppImage.CHIP_MARKED, 0, yOffset2 - CHIP_TILESIZE, CHIP_TILESIZE, CHIP_TILESIZE, null);
                }
                if (chip.isPtValid()) {
                    int[] stats = (displayType == Setting.DISPLAY_STAT ? chip.getStat() : chip.getPt()).toArray();
                    Point[] iPts = {new Point(CHIP_GAP, yOffset2 + CHIP_TILESIZE + CHIP_GAP), new Point(xOffset, yOffset2 + CHIP_TILESIZE + CHIP_GAP), new Point(CHIP_GAP, yOffset2 + CHIP_GAP), new Point(xOffset, yOffset2 + CHIP_GAP)};
                    Point[] sPts = {new Point(CHIP_TILESIZE + CHIP_GAP + 1, yOffset2 + CHIP_TILESIZE * 2 + CHIP_GAP - 1), new Point(CHIP_TILESIZE + 1 + xOffset, yOffset2 + CHIP_TILESIZE * 2 + CHIP_GAP - 1), new Point(CHIP_TILESIZE + CHIP_GAP + 1, yOffset2 + CHIP_TILESIZE + CHIP_GAP - 1), new Point(CHIP_TILESIZE + 1 + xOffset, yOffset2 + CHIP_TILESIZE + CHIP_GAP - 1)};
                    int pi = 0;
                    for (int i = 0; i < 4; i++) {
                        if (stats[i] > 0) {
                            g.setColor(Color.WHITE);
                            g.fillRect(iPts[pi].x, iPts[pi].y, CHIP_TILESIZE, CHIP_TILESIZE);
                            Image image = AppImage.STATS[i].getImage();
                            g.drawImage(image, iPts[pi].x, iPts[pi].y, CHIP_TILESIZE, CHIP_TILESIZE, null);
                            g.setColor(level == 0 || displayType == Setting.DISPLAY_PT ? Color.WHITE : AppColor.LEVEL);
                            int x = sPts[pi].x + (xOffset - CHIP_TILESIZE - Fn.getWidth(String.valueOf(stats[i]), AppFont.FONT_DIGIT)) / 2;
                            int y = sPts[pi].y;
                            g.drawString(String.valueOf(stats[i]), x, y);
                            pi++;
                        }
                    }
                }
            }
            return new ImageIcon(bi);
        }

        public static int height(boolean statExists) {
            return statExists ? CHIP_HEIGHT1 + CHIP_HEIGHT2 : CHIP_HEIGHT1;
        }

        public static int width(boolean statExists) {
            int width = CHIP_TILESIZE * 6 + CHIP_GAP * 2;
            if (statExists) {
                width = Math.max(width, CHIP_TILESIZE * 5);
            }
            return width;
        }
    }

    public static class Board {

        public static ImageIcon get(App app, int size, Unit unit, int star) {
            return get(app, size, main.puzzle.Board.initMatrix(unit, star));
        }

        public static ImageIcon get(App app, int size, main.puzzle.Board board) {
            return get(app, size, board.getMatrix());
        }

        public static ImageIcon get(App app, int size, PuzzleMatrix<Integer> matrix) {
            int tileSize = size / 8;
            int h = main.puzzle.Board.HEIGHT;
            int w = main.puzzle.Board.WIDTH;
            BufferedImage i = new BufferedImage(h * tileSize + 1, w * tileSize + 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) i.getGraphics();
            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    int s = matrix.get(row, col);
                    int x = col * tileSize;
                    int y = row * tileSize;
                    // Tiles
                    g.setColor(s == main.puzzle.Board.UNUSED ? Color.BLACK : s == main.puzzle.Board.EMPTY ? Color.WHITE : app.colors()[s % app.colors().length]);
                    g.fillRect(x, y, tileSize, tileSize);
                    // Horizontal Border
                    g.setColor(Color.BLACK);
                    if (0 < row && matrix.get(row - 1, col) != s) {
                        g.drawLine(x, y, x + tileSize, y);
                    }
                    // Vertical Border
                    if (0 < col && matrix.get(row, col - 1) != s) {
                        g.drawLine(x, y, x, y + tileSize);
                    }
                }
            }
            // Border
            g.setColor(Color.BLACK);
            g.drawLine(0, 0, tileSize * w, 0);
            g.drawLine(0, 0, 0, tileSize * h);
            g.drawLine(0, tileSize * h, tileSize * w, tileSize * h);
            g.drawLine(tileSize * w, 0, tileSize * w, tileSize * h);
            return new ImageIcon(i);
        }
    }
}
