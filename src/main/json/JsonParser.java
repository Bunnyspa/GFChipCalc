package main.json;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.Stat;
import main.puzzle.Tag;

/**
 *
 * @author Bunnyspa
 */
public class JsonParser {

    private static final Map<Integer, String> MAP_GRID = new HashMap<Integer, String>() // <editor-fold defaultstate="collapsed">
    {
        {
            int i = 39;
            for (String type : Chip.TYPES) {
                for (String name : Chip.getNames(type)) {
                    put(i, name);
                    i--;
                }
            }
        }
    }; // </editor-fold>

    private static final String SIGNKEY = "sign";
    private static final String CHIPKEY_SQUAD = "squad_with_user_info";
    private static final String CHIPKEY_CHIP = "chip_with_user_info";

    public static List<Chip> readFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String data = br.readLine();
            List<Chip> chips = parseChip(data);
            return chips;
        } catch (IOException ex) {
            App.log(ex);
            return new ArrayList<>();
        }
    }

    public static String parseSign(String data) {
        ObjectJson o = Json.getObjectJson(Json.parse(data));
        return Json.getText(o.getValue(SIGNKEY));
    }

    public static List<Chip> parseChip(String data) {
        // Init
        Map<Integer, Tag> squadMap = new HashMap<>();
        List<Chip> chips = new ArrayList();
        try {
            // Parse
            ObjectJson o = Json.getObjectJson(Json.parse(data));

            // Squad
            Json squadsJ = o.getValue(CHIPKEY_SQUAD);
            if (squadsJ.getType() == Json.OBJECT) {
                List<String> squadJKeys = Json.getObjectKeys(squadsJ);
                parseChip_squad(squadMap, squadJKeys.stream().map((jKey) -> Json.getObjectJson(Json.getObjectValue(squadsJ, jKey))));
            } else if (squadsJ.getType() == Json.ARRAY) {
                List<Json> squadJs = Json.getList(squadsJ);
                parseChip_squad(squadMap, squadJs.stream().map((squadJ) -> Json.getObjectJson(squadJ)));
            }

            // Chip
            Json chipsJ = o.getValue(CHIPKEY_CHIP);
            if (chipsJ.getType() == Json.OBJECT) {
                List<String> chipJKeys = Json.getObjectKeys(chipsJ);
                parseChip_chip(squadMap, chips, chipJKeys.stream().map((jKey) -> Json.getObjectJson(Json.getObjectValue(chipsJ, jKey))));
            } else if (chipsJ.getType() == Json.ARRAY) {
                List<Json> chipJs = Json.getList(chipsJ);
                parseChip_chip(squadMap, chips, chipJs.stream().map((chipJ) -> Json.getObjectJson(chipJ)));
            }

            Collections.reverse(chips);
        } catch (Exception ex) {
        }
        return chips;
    }

    private static void parseChip_squad(Map<Integer, Tag> squadMap, Stream<ObjectJson> stream) {
        stream.forEach((squadJ) -> {
            int squadID = Integer.valueOf(Json.getText(squadJ.getValue("id")));
            int squadIndex = Integer.valueOf(Json.getText(squadJ.getValue("squad_id")));
            squadMap.put(squadID, boardTag(squadIndex - 1));
        });
    }

    private static void parseChip_chip(Map<Integer, Tag> squadMap, List<Chip> chips, Stream<ObjectJson> stream) {
        stream.forEach((chipJ) -> {
            // Raw
            String id = ((TextJson) chipJ.getValue("id")).getText();
            int gridData = Integer.valueOf(((TextJson) chipJ.getValue("grid_id")).getText());
            String name = MAP_GRID.get(gridData);
            int dmg = Integer.valueOf(Json.getText(chipJ.getValue("assist_damage")));
            int brk = Integer.valueOf(Json.getText(chipJ.getValue("assist_def_break")));
            int hit = Integer.valueOf(Json.getText(chipJ.getValue("assist_hit")));
            int rld = Integer.valueOf(Json.getText(chipJ.getValue("assist_reload")));
            int star = Integer.valueOf(Json.getText(chipJ.getValue("chip_id")).substring(0, 1));
            int level = Integer.valueOf(Json.getText(chipJ.getValue("chip_level")));
            int color = Integer.valueOf(Json.getText(chipJ.getValue("color_id"))) - 1;
            int rotation = Integer.valueOf(Json.getText(chipJ.getValue("shape_info")).substring(0, 1));
            int squadID = Integer.valueOf(Json.getText(chipJ.getValue("squad_with_user_id")));

            Stat pt = new Stat(dmg, brk, hit, rld);
            Chip chip = new Chip(id, name, star, color, pt, level, rotation);
            if (squadMap.containsKey(squadID)) {
                Tag tag = squadMap.get(squadID);
                chip.setTag(tag, true);
            } else if (squadID != 0) {
                Tag tag = boardTag(squadID - 10001);
                chip.setTag(tag, true);
            }
            chips.add(chip);
        });
    }

    private static Tag boardTag(int index) {
        return new Tag(Color.GRAY, Board.NAMES[index]);
    }
}
