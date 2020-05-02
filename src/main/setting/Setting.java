package main.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import main.App;
import main.puzzle.Chip;
import main.util.IO;
import main.util.Version2;
import main.util.Version3;

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

    public static final int DEFAULT_FONTSIZE = 12;

    //// Variables ////
    public Version2 updateVersion = new Version2(1, 0);

    // Display
    public Locale locale = Locale.getDefault();
    public int fontSize = DEFAULT_FONTSIZE;
    public int colorPreset = COLOR_NORMAL;

    // Pool
    public boolean poolOrder = DESCENDING;
    public int poolColor = Chip.COLOR_ORANGE;
    public int poolStar = 5;
    public boolean poolPanelVisible = true;

    // Inventory Display
    public int displayType = DISPLAY_STAT;

    // Chip
    public boolean levelMax = true;
    public boolean colorMatch = true;
    public boolean rotation = true;
    public boolean symmetry = false;

    // Board
    public int boardMarkMin = 0;
    public int boardMarkMax = 64;
    public int boardMarkType = BOARD_MARKTYPE_CELL;
    public int boardSortType = BOARD_SORTTYPE_TICKET;

    // Combinator
    public boolean advancedSetting = false;
    public boolean showProgImage = true;

    // Board
    public BoardSetting board = new BoardSetting();

    public Setting() {
    }

    public Setting(List<String> generalLines, List<String> boardStatLines) {
        try {
            Version3 v = new Version3(generalLines.get(0));
            if (v.isCurrent(4, 2, 0)) {
                generalLines.forEach((line) -> {
                    // Display
                    if (line.startsWith("UPDATE_VERSION=")) {
                        updateVersion = new Version2(afterEqual(line));
                    } else if (line.startsWith("APPEARANCE_LANG=") || line.startsWith("DISPLAY_LANG=")) {
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
                        levelMax = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("CHIP_MATCHCOLOR=")) {
                        colorMatch = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("CHIP_ROTATABLE=")) {
                        rotation = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("CHIP_SYMMETRY=")) {
                        symmetry = IO.parseBoolean(afterEqual(line));
                    }//
                    // Board
                    else if (line.startsWith("BOARD_SORT=")) {
                        boardSortType = Integer.valueOf(afterEqual(line));
                    }//
                    // Combinator
                    else if (line.startsWith("ADVANCED_SETTING=")) {
                        advancedSetting = IO.parseBoolean(afterEqual(line));
                    } else if (line.startsWith("COMB_HIDEPROG=") || line.startsWith("COMB_SHOWPROG=")) {
                        showProgImage = IO.parseBoolean(afterEqual(line));
                    }
                });
                if (advancedSetting) {
                    colorMatch = true;
                }
                // Board
                board = IO.parseBS(boardStatLines, advancedSetting);
            } else {
                fontSize = Integer.valueOf(generalLines.get(1));

                poolOrder = IO.parseBoolean(generalLines.get(2));
                poolStar = 5 - Integer.valueOf(generalLines.get(3));
                poolColor = Integer.valueOf(generalLines.get(4));

                poolPanelVisible = IO.parseBoolean(generalLines.get(5));
                displayType = Integer.valueOf(generalLines.get(6));

                levelMax = IO.parseBoolean(generalLines.get(7));
                colorMatch = IO.parseBoolean(generalLines.get(8));
                rotation = IO.parseBoolean(generalLines.get(9));
                colorPreset = Integer.valueOf(generalLines.get(11));
            }
        } catch (Exception ex) {
        }
    }

    private static String afterEqual(String line) {
        return line.split("=")[1].trim();
    }

    public String toData() {
        List<String> lines = new ArrayList<>();
        lines.add(App.VERSION.toData());
        lines.add("[" + Setting.SECTION_GENERAL + "]");
        lines.add("UPDATE_VERSION=" + updateVersion.toData());

        lines.add("DISPLAY_LANG=" + locale.getLanguage() + "-" + locale.getCountry());
        lines.add("DISPLAY_FONTSIZE=" + fontSize);
        lines.add("DISPLAY_COLOR=" + colorPreset);

        lines.add("POOL_VISIBLE=" + IO.data(poolPanelVisible));
        lines.add("POOL_ORDER=" + IO.data(poolOrder));
        lines.add("POOL_STAR=" + poolStar);
        lines.add("POOL_COLOR=" + poolColor);

        lines.add("DISPLAY_TYPE=" + displayType);

        lines.add("CHIP_MAXLEVEL=" + IO.data(levelMax));
        lines.add("CHIP_MATCHCOLOR=" + IO.data(colorMatch));
        lines.add("CHIP_ROTATABLE=" + IO.data(rotation));
        lines.add("CHIP_SYMMETRY=" + IO.data(symmetry));

        lines.add("BOARD_SORT=" + boardSortType);

        lines.add("ADVANCED_SETTING=" + IO.data(advancedSetting));
        lines.add("COMB_SHOWPROG=" + IO.data(showProgImage));

        lines.add("[" + Setting.SECTION_BOARD + "]");
        lines.add(board.toData());

        return String.join(System.lineSeparator(), lines);
    }

}
