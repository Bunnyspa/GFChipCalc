package main.setting;

import java.util.ArrayList;
import java.util.List;
import main.data.Unit;
import main.puzzle.Board;
import main.puzzle.Stat;
import main.util.DoubleKeyHashMap;

public class BoardSetting {

    public static final int MAX_DEFAULT = 0;
    public static final int MAX_STAT = 1;
    public static final int MAX_PT = 2;
    public static final int MAX_PRESET = 3;

    private final DoubleKeyHashMap<Unit, Integer, Stat> statMap, ptMap;
    private final DoubleKeyHashMap<Unit, Integer, Integer> modeMap, presetIndexMap;

    public BoardSetting() {
        this.statMap = new DoubleKeyHashMap<>();
        this.ptMap = new DoubleKeyHashMap<>();
        this.modeMap = new DoubleKeyHashMap<>();
        this.presetIndexMap = new DoubleKeyHashMap<>();

        for (Unit unit : Unit.values()) {
            for (int star = 1; star <= 5; star++) {
                statMap.put(unit, star, Board.getMaxStat(unit, star));
                ptMap.put(unit, star, Board.getMaxPt(unit, star));
                modeMap.put(unit, star, star == 5 ? MAX_PRESET : MAX_DEFAULT);
                presetIndexMap.put(unit, star, 0);
            }
        }
    }

    public String toData() {
        List<String> lines = new ArrayList<>();
        for (Unit unit : Unit.values()) {
            for (int star = 1; star <= 5; star++) {
                String ss = String.join(";",
                        unit.getName(),
                        String.valueOf(star),
                        String.valueOf(getStatMode(unit, star)),
                        getStat(unit, star).toData(),
                        getPt(unit, star).toData(),
                        String.valueOf(getPresetIndex(unit, star))
                );
                lines.add(ss);
            }
        }
        return String.join(System.lineSeparator(), lines);
    }

    public void setStat(Unit unit, int star, Stat stat) {
        statMap.put(unit, star, stat);
    }

    public void setPt(Unit unit, int star, Stat pt) {
        ptMap.put(unit, star, pt);
    }

    public void setMode(Unit unit, int star, int mode) {
        modeMap.put(unit, star, mode);
    }

    public void setPresetIndex(Unit unit, int star, int index) {
        presetIndexMap.put(unit, star, index);
    }

    public Stat getStat(Unit unit, int star) {
        if (statMap.containsKey(unit, star)) {
            return statMap.get(unit, star);
        }
        return new Stat();
    }

    public Stat getPt(Unit unit, int star) {
        if (ptMap.containsKey(unit, star)) {
            return ptMap.get(unit, star);
        }
        return new Stat();
    }

    public int getStatMode(Unit unit, int star) {
        if (modeMap.containsKey(unit, star)) {
            return modeMap.get(unit, star);
        }
        return MAX_DEFAULT;
    }

    public int getPresetIndex(Unit unit, int star) {
        if (presetIndexMap.containsKey(unit, star)) {
            return presetIndexMap.get(unit, star);
        }
        return -1;
    }
}
