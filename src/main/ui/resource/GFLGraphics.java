/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.ui.resource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Shape;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class GFLGraphics {

    public static final Color COLOR_STAR_YELLOW = new Color(255, 170, 0);
    public static final Color COLOR_STAR_RED = Color.RED;
    public static final Color COLOR_LEVEL = new Color(10, 205, 171);

    public static final Map<Integer, Color> COLORS_CHIP = new HashMap<Integer, Color>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(Chip.COLOR_ORANGE, new Color(240, 107, 65));
            put(Chip.COLOR_BLUE, new Color(111, 137, 218));
        }
    }; // </editor-fold>

    private static final int CHIP_TILESIZE = 12;
    private static final int CHIP_GAP = 2;
    private static final int CHIP_HEIGHT1 = CHIP_TILESIZE * 6 + CHIP_GAP * 2;
    private static final int CHIP_HEIGHT2 = CHIP_TILESIZE * 3 + CHIP_GAP * 4;

    private static Color getPoolColor(App app, Chip chip) {
        if (app == null) {
            return Color.GRAY;
        }
        if (chip.getSize() < 5) {
            int i = (chip.getSize() + 1) % 3;
            return i == 0 ? app.orange() : i == 1 ? app.green() : app.blue();
        }
        if (chip.getShape().getType() == Shape.Type._5A) {
            return app.orange();
        }
        if (chip.getShape().getType() == Shape.Type._5B) {
            return app.green();
        }
        return app.blue();
    }

    public static int chip_imageHeight(boolean statExists) {
        return statExists ? CHIP_HEIGHT1 + CHIP_HEIGHT2 : CHIP_HEIGHT1;
    }

    public static int chip_imageWidth(boolean statExists) {
        int width = CHIP_TILESIZE * 6 + CHIP_GAP * 2;
        if (statExists) {
            width = Math.max(width, CHIP_TILESIZE * 5);
        }
        return width;
    }

    private static final int DISPLAYTYPE_STAT = 0;
    private static final int DISPLAYTYPE_PT = 1;

    public static ImageIcon chip(App app, Chip chip) {
        boolean statExists = chip.statExists();

        // Chip
        int star = chip.getStar();
        int boardIndex = chip.getBoardIndex();
        boolean marked = chip.isMarked();
        int color = chip.getColor();
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
        int iw = chip_imageWidth(statExists);
        int ih = chip_imageHeight(statExists);
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

                    g.setColor(boardIndex > -1 ? app.colors()[boardIndex % app.colors().length]
                            : !statExists ? getPoolColor(app, chip)
                                    : marked ? COLORS_CHIP.get(color).darker().darker()
                                            : COLORS_CHIP.get(color)
                    );
                    g.fillRect(tileXOffset + 1, tileYOffset + 1, CHIP_TILESIZE - 1, CHIP_TILESIZE - 1);
                }
            }
        }

        if (statExists) {
            g.setColor(COLOR_STAR_YELLOW);
            String starString = "";
            for (int i = 0; i < star; i++) {
                starString += GFLTexts.TEXT_STAR_FULL;
            }
            int xOffset = iw / 2;

            g.drawString(starString, CHIP_GAP, CHIP_TILESIZE + CHIP_GAP);

            // Level
            g.setFont(GFLResources.FONT_DIGIT);
            if (0 < level) {
                String levelStr = (initLevel == level)
                        ? "+" + level
                        : initLevel + "→" + level;
                int levelWidth = Fn.getWidth(levelStr, g.getFont());

                g.setColor(COLOR_LEVEL);
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
                g.drawImage(GFLResources.CHIP_EQUIPPED,
                        0, yOffset1,
                        CHIP_TILESIZE, CHIP_TILESIZE, null);
            }

            // Rotation
            if (initRotation != rotation) {
                g.drawImage(GFLResources.CHIP_ROTATED,
                        iw - CHIP_TILESIZE + 1, yOffset1,
                        CHIP_TILESIZE, CHIP_TILESIZE, null);
            }

            // Mark
            if (marked) {
                g.drawImage(GFLResources.CHIP_MARKED,
                        0, yOffset2 - CHIP_TILESIZE,
                        CHIP_TILESIZE, CHIP_TILESIZE, null);
            }

            if (chip.isPtValid()) {
                int[] stats = (displayType == DISPLAYTYPE_STAT ? chip.getStat() : chip.getPt()).toArray();
                Point[] iPts = {
                    new Point(CHIP_GAP, yOffset2 + CHIP_TILESIZE + CHIP_GAP),
                    new Point(xOffset, yOffset2 + CHIP_TILESIZE + CHIP_GAP),
                    new Point(CHIP_GAP, yOffset2 + CHIP_GAP),
                    new Point(xOffset, yOffset2 + CHIP_GAP)};
                Point[] sPts = {
                    new Point(CHIP_TILESIZE + CHIP_GAP + 1, yOffset2 + CHIP_TILESIZE * 2 + CHIP_GAP - 1),
                    new Point(CHIP_TILESIZE + 1 + xOffset, yOffset2 + CHIP_TILESIZE * 2 + CHIP_GAP - 1),
                    new Point(CHIP_TILESIZE + CHIP_GAP + 1, yOffset2 + CHIP_TILESIZE + CHIP_GAP - 1),
                    new Point(CHIP_TILESIZE + 1 + xOffset, yOffset2 + CHIP_TILESIZE + CHIP_GAP - 1)};

                int pi = 0;
                for (int i = 0; i < 4; i++) {
                    if (stats[i] > 0) {
                        g.setColor(Color.WHITE);
                        g.fillRect(iPts[pi].x, iPts[pi].y, CHIP_TILESIZE, CHIP_TILESIZE);
                        Image image = GFLResources.STATS[i].getImage();
                        g.drawImage(image, iPts[pi].x, iPts[pi].y, CHIP_TILESIZE, CHIP_TILESIZE, null);
                        g.setColor(level == 0 || displayType == DISPLAYTYPE_PT ? Color.WHITE : COLOR_LEVEL);
                        int x = sPts[pi].x + (xOffset - CHIP_TILESIZE - Fn.getWidth(String.valueOf(stats[i]), GFLResources.FONT_DIGIT)) / 2;
                        int y = sPts[pi].y;
                        g.drawString(String.valueOf(stats[i]), x, y);
                        pi++;
                    }
                }
            }
        }
        return new ImageIcon(bi);
    }

    public static ImageIcon board(App app, int size, String boardName, int boardStar) {
        return board(app, size, Board.initMatrix(boardName, boardStar));
    }

    public static ImageIcon board(App app, int size, Board board) {
        return board(app, size, board.getMatrix());
    }

    public static ImageIcon board(App app, int size, PuzzleMatrix<Integer> matrix) {
        int tileSize = size / 8;
        int h = Board.HEIGHT;
        int w = Board.WIDTH;
        BufferedImage i = new BufferedImage(
                h * tileSize + 1,
                w * tileSize + 1,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int s = matrix.get(row, col);
                int x = col * tileSize;
                int y = row * tileSize;

                // Tiles
                g.setColor(s == Board.UNUSED ? Color.BLACK : s == Board.EMPTY ? Color.WHITE : app.colors()[s % app.colors().length]);
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
