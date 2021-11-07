package main.setting;

import java.util.ArrayList;
import java.util.List;
import main.App;
import main.data.Unit;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.ui.resource.AppText;
import main.util.DoubleKeyHashMap;

public class StatPresetMap {

    private final DoubleKeyHashMap<Unit, Integer, List<StatPreset>> data = new DoubleKeyHashMap<>();
  

    public boolean containsKey(Unit unit, int star) {
        return data.containsKey(unit, star);
    }

    private List<StatPreset> get(Unit unit, int star) {
        if (containsKey(unit, star)) {
            return data.get(unit, star);
        }
        return null;
    }

    public StatPreset get(Unit unit, int star, int index) {
        if (containsKey(unit, star)) {
            return data.get(unit, star).get(index);
        }
        return null;
    }

//    public int size(Unit unit, int star) {
//        if (containsKey(unit, star)) {
//            return data.get(unit, star).size();
//        }
//        return 0;
//    }

    public List<String> getStrings(App app, Unit unit, int star) {
        List<StatPreset> bps = get(unit, star);
        List<String> out = new ArrayList<>(bps.size());
        for (int i = 0; i < bps.size(); i++) {
            Stat pt = bps.get(i).pt;
            String item;
            if (pt == null) {
                item = app.getText(AppText.CSET_PRESET_OPTION, String.valueOf(i + 1));
            } else {
                item = pt.toStringSlash();
            }
            Shape.Type type = data.get(unit, star).get(i).typeMin;

            item += " (" + type.toString() + (type == Shape.Type._6 ? "" : "-6") + ")";

            out.add(item);
        }

        return out;
    }

//    public boolean[] getTypeFilter(Unit unit, int star, int index) {
//        if (!containsKey(unit, star)) {
//            return new boolean[Filter.NUM_TYPE];
//        }
//
//        Shape.Type type = data.get(unit, star).get(index).typeMin;
//
//        boolean[] out = new boolean[Filter.NUM_TYPE];
//        for (int i = 0; i < Filter.NUM_TYPE - type.id + 1; i++) {
//            out[i] = true;
//        }
//        return out;
//    }
}
