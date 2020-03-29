package main.setting;

import java.util.ArrayList;
import java.util.List;
import main.App;
import main.puzzle.Chip;
import main.puzzle.FStat;
import main.resource.Language;
import main.util.StrIntMap;

/**
 *
 * @author Bunnyspa
 */
public class StatPresetMap {

    private final StrIntMap<List<StatPreset>> data = new StrIntMap<>();

    public void put(String name, int star, StatPreset bp) {
        init(name, star);
        data.get(name, star).add(bp);
    }

    public void put(String name, int star, String typeMin, FStat stat, FStat pt, FStat ptMin, FStat ptMax) {
        StatPreset bp = new StatPreset(stat, pt, ptMin, ptMax, typeMin);
        put(name, star, bp);
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
        return data.get(name, star).size();
    }

    public List<String> getStrings(App app, String name, int star) {
        List<StatPreset> bps = get(name, star);
        List<String> out = new ArrayList<>(bps.size());
        for (int i = 0; i < bps.size(); i++) {
            FStat pt = bps.get(i).pt;
            String item;
            if (pt == null) {
                item = app.getText(Language.CSET_PRESET_OPTION, String.valueOf(i + 1));
            } else {
                item = pt.toStringSlash();
            }
            String type = data.get(name, star).get(i).typeMin;

            item += " (" + type + (Chip.TYPE_6.equals(type) ? "" : "-6") + ")";

            out.add(item);
        }

        return out;
    }

    public boolean[] getTypeFilter(String name, int star, int index) {
        if (!containsKey(name, star)) {
            return new boolean[Filter.NUM_TYPE];
        }

        String type = data.get(name, star).get(index).typeMin;

        boolean[] out = new boolean[Filter.NUM_TYPE];
        int i = 0;
        while (!Chip.TYPES[i].equals(type)) {
            out[i] = true;
            i++;
        }
        out[i] = true;
        return out;
    }
}
