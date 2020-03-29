package main.setting;

import java.util.ArrayList;
import java.util.List;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.FStat;
import main.puzzle.Stat;
import main.util.StrIntMap;

/**
 *
 * @author Bunnyspa
 */
public class BoardSetting {

    public static final int MAX_DEFAULT = 0;
    public static final int MAX_STAT = 1;
    public static final int MAX_PT = 2;
    public static final int MAX_PRESET = 3;

    public static final StatPresetMap PRESET = new StatPresetMap() // <editor-fold defaultstate="collapsed">
    {
        {
            put(Board.NAME_BGM71, 5, Chip.TYPE_5B,
                    new FStat(157, 328, 191, 45), new FStat(13, 10, 10, 3),
                    new FStat(1, 0, 0, 0), new FStat(3, 3, 3, 3)
            );
            put(Board.NAME_BGM71, 5, Chip.TYPE_5B,
                    new FStat(189, 328, 140, 45), new FStat(16, 10, 7, 3),
                    new FStat(1, 0, 1, 0), new FStat(4, 3, 1, 3)
            );
            put(Board.NAME_AGS30, 5, Chip.TYPE_5A,
                    new FStat(Board.getMaxStat(Board.NAME_AGS30, 5)), new FStat(Board.getMaxPt(Board.NAME_AGS30, 5)),
                    new FStat(0, 0, 0, 0), new FStat(3, 5, 5, 5)
            );
            put(Board.NAME_2B14, 5, Chip.TYPE_5B,
                    new FStat(220, 58, 90, 90), new FStat(19, 2, 5, 6),
                    new FStat(2, 0, 0, 0), new FStat(4, 2, 3, 3)
            );
            put(Board.NAME_2B14, 5, Chip.TYPE_5A,
                    new FStat(227, 58, 80, 90), new FStat(20, 2, 4, 6),
                    new FStat(2, 0, 0, 0), new FStat(5, 2, 1, 3)
            );
            put(Board.NAME_2B14, 5, Chip.TYPE_5A,
                    new FStat(227, 33, 90, 90), new FStat(20, 1, 5, 6),
                    new FStat(2, 0, 0, 0), new FStat(5, 1, 5, 3)
            );
            put(Board.NAME_M2, 5, Chip.TYPE_4,
                    new FStat(Board.getMaxStat(Board.NAME_M2, 5)), new FStat(Board.getMaxPt(Board.NAME_M2, 5)),
                    new FStat(0, 0, 0, 0), new FStat(5, 2, 5, 5)
            );
            put(Board.NAME_AT4, 5, Chip.TYPE_6,
                    new FStat(166, 261, 174, 65), new FStat(14, 8, 9, 5),
                    new FStat(1, 0, 1, 0), new FStat(3, 3, 2, 4)
            );
            put(Board.NAME_AT4, 5, Chip.TYPE_5A,
                    new FStat(167, 261, 174, 65), new FStat(14, 8, 9, 5),
                    new FStat(1, 0, 0, 0), new FStat(4, 3, 2, 4)
            );
            put(Board.NAME_QLZ04, 5, Chip.TYPE_5B,
                    new FStat(Board.getMaxStat(Board.NAME_QLZ04, 5)), new FStat(Board.getMaxPt(Board.NAME_QLZ04, 5)),
                    new FStat(0, 0, 0, 0), new FStat(3, 4, 3, 4)
            );
        }
    }; // </editor-fold>

    private final StrIntMap<Stat> statMap, ptMap;
    private final StrIntMap<Integer> modeMap, presetIndexMap;

    public BoardSetting() {
        this.statMap = new StrIntMap<>();
        this.ptMap = new StrIntMap<>();
        this.modeMap = new StrIntMap<>();
        this.presetIndexMap = new StrIntMap<>();

        for (String name : Board.NAMES) {
            for (int star = 1; star <= 5; star++) {
                statMap.put(name, star, Board.getMaxStat(name, star));
                ptMap.put(name, star, Board.getMaxPt(name, star));
                modeMap.put(name, star, MAX_DEFAULT);
                presetIndexMap.put(name, star, 0);
            }
        }
    }

    public String toData() {
        List<String> lines = new ArrayList<>();
        for (String name : Board.NAMES) {
            for (int star = 1; star <= 5; star++) {
                String ss = String.join(";",
                        name,
                        String.valueOf(star),
                        String.valueOf(getMode(name, star)),
                        getStat(name, star).toData(),
                        getPt(name, star).toData(),
                        String.valueOf(getPresetIndex(name, star))
                );
                lines.add(ss);
            }
        }
        return String.join(System.lineSeparator(), lines);
    }

    public void setStat(String name, int star, Stat stat) {
        statMap.put(name, star, stat);
    }

    public void setPt(String name, int star, Stat pt) {
        ptMap.put(name, star, pt);
    }

    public void setMode(String name, int star, int mode) {
        modeMap.put(name, star, mode);
    }

    public void setPresetIndex(String name, int star, int index) {
        presetIndexMap.put(name, star, index);
    }

    public Stat getStat(String name, int star) {
        if (statMap.containsKey(name, star)) {
            return statMap.get(name, star);
        }
        return new Stat();
    }

    public Stat getPt(String name, int star) {
        if (ptMap.containsKey(name, star)) {
            return ptMap.get(name, star);
        }
        return new Stat();
    }

    public int getMode(String name, int star) {
        if (modeMap.containsKey(name, star)) {
            return modeMap.get(name, star);
        }
        return MAX_DEFAULT;
    }

    public int getPresetIndex(String name, int star) {
        if (presetIndexMap.containsKey(name, star)) {
            return presetIndexMap.get(name, star);
        }
        return -1;
    }

    public boolean hasDefaultPreset(String name, int star) {
        if (!PRESET.containsKey(name, star)) {
            return false;
        }
        if (PRESET.size(name, star) != 1) {
            return false;
        }
        return PRESET.get(name, star, 0).stat.toStat().equals(Board.getMaxStat(name, star));
    }

    public static StatPreset getPreset(String name, int star, int index) {
        return PRESET.get(name, star, index);
    }
}
