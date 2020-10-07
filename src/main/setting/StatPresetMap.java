package main.setting;

import java.util.ArrayList;
import java.util.List;
import main.App;
import main.puzzle.Board;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.ui.resource.GFLTexts;
import main.util.DoubleKeyHashMap;

/**
 *
 * @author Bunnyspa
 */
public class StatPresetMap {

    public static final StatPresetMap PRESET = new StatPresetMap() // <editor-fold defaultstate="collapsed">
    {
        {
            // BGM71
            put(Board.NAME_BGM71, 5, Shape.Type._5B,
                    new Stat(157, 328, 191, 45), new Stat(13, 10, 10, 3),
                    new Stat(1, 0, 0, 0), new Stat(3, 3, 3, 3)
            );
            put(Board.NAME_BGM71, 5, Shape.Type._5B,
                    new Stat(189, 328, 140, 45), new Stat(16, 10, 7, 3),
                    new Stat(1, 0, 1, 0), new Stat(4, 3, 1, 3)
            );

            // AGS30
            put(Board.NAME_AGS30, 5, Shape.Type._5A,
                    Board.getMaxStat(Board.NAME_AGS30, 5), Board.getMaxPt(Board.NAME_AGS30, 5),
                    new Stat(), new Stat(3, 5, 5, 5)
            );

            // 2B14
            put(Board.NAME_2B14, 5, Shape.Type._5A,
                    new Stat(227, 33, 90, 90), new Stat(20, 1, 5, 6),
                    new Stat(2, 0, 0, 0), new Stat(5, 1, 5, 3)
            );
            put(Board.NAME_2B14, 5, Shape.Type._5A,
                    new Stat(227, 58, 80, 90), new Stat(20, 2, 4, 6),
                    new Stat(2, 0, 0, 0), new Stat(5, 2, 1, 3)
            );
            put(Board.NAME_2B14, 5, Shape.Type._5B,
                    new Stat(220, 58, 90, 90), new Stat(19, 2, 5, 6),
                    new Stat(2, 0, 0, 0), new Stat(4, 2, 3, 3)
            );

            // M2
            put(Board.NAME_M2, 5, Shape.Type._4,
                    Board.getMaxStat(Board.NAME_M2, 5), Board.getMaxPt(Board.NAME_M2, 5),
                    new Stat(), new Stat(5, 2, 5, 5)
            );

            // AT4
            put(Board.NAME_AT4, 5, Shape.Type._5A,
                    new Stat(167, 261, 174, 65), new Stat(14, 8, 9, 5),
                    new Stat(1, 0, 0, 0), new Stat(4, 3, 2, 4)
            );
            put(Board.NAME_AT4, 5, Shape.Type._6,
                    new Stat(166, 261, 174, 65), new Stat(14, 8, 9, 5),
                    new Stat(1, 0, 1, 0), new Stat(3, 3, 2, 4)
            );

            // QLZ04
            put(Board.NAME_QLZ04, 5, Shape.Type._5B,
                    Board.getMaxStat(Board.NAME_QLZ04, 5), Board.getMaxPt(Board.NAME_QLZ04, 5),
                    new Stat(), new Stat(3, 4, 3, 4)
            );

            // Mk 153
            put(Board.NAME_MK153, 5, Shape.Type._5B,
                    new Stat(195, 273, 140, 75), new Stat(17, 9, 7, 5),
                    new Stat(0, 0, 1, 0), new Stat(4, 5, 2, 3)
            );
            put(Board.NAME_MK153, 5, Shape.Type._5B,
                    new Stat(189, 263, 176, 75), new Stat(16, 8, 9, 5),
                    new Stat(1, 1, 1, 0), new Stat(4, 2, 2, 3)
            );
        }
    }; // </editor-fold>

    private final DoubleKeyHashMap<String, Integer, List<StatPreset>> data = new DoubleKeyHashMap<>();

    public void put(String name, int star, Shape.Type typeMin, Stat stat, Stat pt, Stat ptMin, Stat ptMax) {
        init(name, star);
        data.get(name, star).add(new StatPreset(stat, pt, ptMin, ptMax, typeMin));
    }

    private void init(String name, int star) {
        if (!data.containsKey(name, star)) {
            data.put(name, star, new ArrayList<>());
        }
    }

    public boolean containsKey(String name, int star) {
        return data.containsKey(name, star);
    }

    private List<StatPreset> get(String name, int star) {
        if (containsKey(name, star)) {
            return data.get(name, star);
        }
        return null;
    }

    public StatPreset get(String name, int star, int index) {
        if (containsKey(name, star)) {
            return data.get(name, star).get(index);
        }
        return null;
    }

    public int size(String name, int star) {
        if (containsKey(name, star)) {
            return data.get(name, star).size();
        }
        return 0;
    }

    public List<String> getStrings(App app, String name, int star) {
        List<StatPreset> bps = get(name, star);
        List<String> out = new ArrayList<>(bps.size());
        for (int i = 0; i < bps.size(); i++) {
            Stat pt = bps.get(i).pt;
            String item;
            if (pt == null) {
                item = app.getText(GFLTexts.CSET_PRESET_OPTION, String.valueOf(i + 1));
            } else {
                item = pt.toStringSlash();
            }
            Shape.Type type = data.get(name, star).get(i).typeMin;

            item += " (" + type.toString() + (type == Shape.Type._6 ? "" : "-6") + ")";

            out.add(item);
        }

        return out;
    }

    public boolean[] getTypeFilter(String name, int star, int index) {
        if (!containsKey(name, star)) {
            return new boolean[Filter.NUM_TYPE];
        }

        Shape.Type type = data.get(name, star).get(index).typeMin;

        boolean[] out = new boolean[Filter.NUM_TYPE];
        for (int i = 0; i < Filter.NUM_TYPE - type.id + 1; i++) {
            out[i] = true;
        }
        return out;
    }
}
