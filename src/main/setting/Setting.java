package main.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import main.App;
import main.puzzle.Chip;
import main.util.IO;
import main.util.Version;

/**
 *
 * @author Bunnyspa
 */
public class Setting {

    public static final String SECTION_GENERAL = "General";
    public static final String SECTION_BOARD = "Board";

    public static final boolean ASCENDING = true;
    public static final boolean DESCENDING = false;

    public static final int DISPLAY_STAT = 0;
    public static final int DISPLAY_PT = 1;
    public static final int NUM_DISPLAY = 2;

    public static final int COLOR_NORMAL = 0;
    public static final int COLOR_COLORBLIND = 1;
    public static final int NUM_COLOR = 2;

    public static final int BOARD_MARKTYPE_CELL = 0;
    public static final int BOARD_MARKTYPE_CHIP = 1;
    public static final int NUM_BOARD_MARKTYPE = 2;

    public static final int BOARD_SORTTYPE_TICKET = 0;
    public static final int BOARD_SORTTYPE_XP = 1;
    public static final int NUM_BOARD_SORTTYPE = 2;

    //// Default ////
    public static final int DEFAULT_FONTSIZE = 12;

    //// Variables ////
    // Display
    public Locale locale;
    public int fontSize;
    public int colorPreset;

    // Pool
    public boolean poolOrder;
    public int poolColor;
    public int poolStar;
    public boolean poolPanelVisible;

    // Inventory Display
    public int displayType;

    // Chip
    public boolean chipLevelMax;
    public boolean chipMatchColor;
    public boolean chipAllowRotation;

    // Board
    public int boardMarkMin;
    public int boardMarkMax;
    public int boardMarkType;
    public int boardSortType;

    // Combinator
    public boolean showProgImage;

    // Board
    public BoardSetting board;

    public Setting() {
        init();
    }

    public Setting(List<String> generalLines, List<String> boardStatLines) {
        init();
        try {
            Version v = new Version(generalLines.get(0));
            if (v.isCurrent(4, 2, 0)) {
                generalLines.forEach((line) -> {
                    // Display
                    if (line.startsWith("APPEARANCE_LANG=") || line.startsWith("DISPLAY_LANG=")) {
                        locale = Locale.forLanguageTag(afterEqual(line).replace("_", "-"));
                    } else if (line.startsWith("APPEARANCE_FONT=") || line.startsWith("DISPLAY_FONT=")) {
                        String[] parts = afterEqual(line).split(",");
                        fontSize = Integer.valueOf(parts[1]);
                    } else if (line.startsWith("DISPLAY_FONTSIZE=")) {
                        fontSize = Integer.valueOf(afterEqual(line));
                    } else if (line.startsWith("APPEARANCE_COLOR=") || line.startsWith("DISPLAY_COLOR=")) {
                        colorPreset = Integer.valueOf(afterEqual(line));
                    }//
                    // Pool
                    else if (line.startsWith("POOL_ORDER=")) {
                        poolOrder = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("POOL_STAR=")) {
                        poolStar = Integer.valueOf(afterEqual(line));
                    } else if (line.startsWith("POOL_COLOR=")) {
                        poolColor = Integer.valueOf(afterEqual(line));
                    } else if (line.startsWith("POOL_VISIBLE=")) {
                        poolPanelVisible = IO.parseBoolean(afterEqual(line));
                    }//
                    // Inventory Display
                    else if (line.startsWith("DISPLAY_TYPE=")) {
                        displayType = Integer.valueOf(afterEqual(line));
                    }//
                    // Chip
                    else if (line.startsWith("CHIP_MAXLEVEL=")) {
                        chipLevelMax = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("CHIP_MATCHCOLOR=")) {
                        chipMatchColor = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("CHIP_ROTATABLE=")) {
                        chipAllowRotation = IO.parseBoolean(afterEqual(line));
                    }//
                    // Board
                    else if (line.startsWith("BOARD_SORT=")) {
                        boardSortType = Integer.valueOf(afterEqual(line));
                    }//
                    // Combinator
                    else if (line.startsWith("COMB_HIDEPROG=") || line.startsWith("COMB_SHOWPROG=")) {
                        showProgImage = IO.parseBoolean(afterEqual(line));
                    }
                });
                // Board
                board = IO.parseBS(boardStatLines);
            } else {
                fontSize = Integer.valueOf(generalLines.get(1));

                poolOrder = IO.parseBoolean(generalLines.get(2));
                poolStar = 5 - Integer.valueOf(generalLines.get(3));
                poolColor = Integer.valueOf(generalLines.get(4));

                poolPanelVisible = IO.parseBoolean(generalLines.get(5));
                displayType = Integer.valueOf(generalLines.get(6));

                chipLevelMax = IO.parseBoolean(generalLines.get(7));
                chipMatchColor = IO.parseBoolean(generalLines.get(8));
                chipAllowRotation = IO.parseBoolean(generalLines.get(9));
                colorPreset = Integer.valueOf(generalLines.get(11));
            }
        } catch (Exception ex) {
        }
    }

    public final void init() {
        // Display
        locale = Locale.getDefault();
        fontSize = DEFAULT_FONTSIZE;
        colorPreset = COLOR_NORMAL;

        // Pool
        poolOrder = DESCENDING;
        poolColor = Chip.COLOR_ORANGE;
        poolStar = 5;
        poolPanelVisible = true;

        // Inventory Display
        displayType = DISPLAY_STAT;

        // Chip
        chipLevelMax = true;
        chipMatchColor = true;
        chipAllowRotation = true;

        // Board
        boardMarkMin = 0;
        boardMarkMax = 64;
        boardMarkType = BOARD_MARKTYPE_CELL;
        boardSortType = BOARD_SORTTYPE_TICKET;

        // Combinator
        showProgImage = true;

        // Board
        board = new BoardSetting();
    }

    private static String afterEqual(String line) {
        return line.split("=")[1].trim();
    }

    public String toData() {
        List<String> lines = new ArrayList<>();
        lines.add(App.VERSION.toData());
        lines.add("[" + Setting.SECTION_GENERAL + "]");

        lines.add("DISPLAY_LANG=" + locale.getLanguage() + "_" + locale.getCountry());
        lines.add("DISPLAY_FONTSIZE=" + fontSize);
        lines.add("DISPLAY_COLOR=" + colorPreset);

        lines.add("POOL_VISIBLE=" + IO.data(poolPanelVisible));
        lines.add("POOL_ORDER=" + IO.data(poolOrder));
        lines.add("POOL_STAR=" + poolStar);
        lines.add("POOL_COLOR=" + poolColor);

        lines.add("DISPLAY_TYPE=" + displayType);

        lines.add("CHIP_MAXLEVEL=" + IO.data(chipLevelMax));
        lines.add("CHIP_MATCHCOLOR=" + IO.data(chipMatchColor));
        lines.add("CHIP_ROTATABLE=" + IO.data(chipAllowRotation));

        lines.add("BOARD_SORT=" + boardSortType);

        lines.add("COMB_SHOWPROG=" + IO.data(showProgImage));

        lines.add("[" + Setting.SECTION_BOARD + "]");
        lines.add(board.toData());

        return String.join(System.lineSeparator(), lines);
    }

}
