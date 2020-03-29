package main.puzzle.preset;

import java.util.List;
import main.util.StrIntMap;

/**
 *
 * @author Bunnyspa
 */
public class PuzzlePresetMap {

    private final StrIntMap<List<PuzzlePreset>> data = new StrIntMap<>();
    private final StrIntMap<String> minTypeMap = new StrIntMap<>();

    public void put(String name, int star, List<PuzzlePreset> presets, String minType) {
        if (!presets.isEmpty()) {
            data.put(name, star, presets);
            minTypeMap.put(name, star, minType);
        }
    }

    public List<PuzzlePreset> get(String name, int star) {
        if (containsKey(name, star)) {
            return data.get(name, star);
        }
        return null;
    }

    public String getMinType(String name, int star) {
        if (containsMinTypeKey(name, star)) {
            return minTypeMap.get(name, star);
        }
        return "";
    }

    public boolean containsKey(String name, int star) {
        return data.containsKey(name, star);
    }

    private boolean containsMinTypeKey(String name, int star) {
        return minTypeMap.containsKey(name, star);
    }
}
