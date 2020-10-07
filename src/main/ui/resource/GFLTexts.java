package main.ui.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import main.App;
import main.puzzle.Chip;
import main.puzzle.Shape;
import main.util.IO;

/**
 *
 * @author Bunnyspa
 */
public class GFLTexts {

    public static final Map<Integer, String> TEXT_MAP_COLOR = new HashMap<Integer, String>() // <editor-fold defaultstate="collapsed">
    {
        {
            put(Chip.COLOR_ORANGE, GFLTexts.CHIP_COLOR_ORANGE);
            put(Chip.COLOR_BLUE, GFLTexts.CHIP_COLOR_BLUE);
        }
    }; // </editor-fold>

    public static String text_type(App app, Shape.Type type) {
        switch (type.id) {
            case 6:
                return app.getText(UNIT_CELLTYPE, "5", "B");
            case 5:
                return app.getText(UNIT_CELLTYPE, "5", "A");
            case 7:
            case 4:
            case 3:
            case 2:
            case 1:
                return app.getText(UNIT_CELL, type.id);
            default:
                return "";
        }
    }

    public static final String TEXT_STAR_FULL = "★";
    public static final String TEXT_STAR_EMPTY = "☆";
    
    // <editor-fold defaultstate="collapsed" desc="Resources">
    private Properties prop;
    private String propTag = "";

    public static final Locale KO_KR = Locale.forLanguageTag("ko-KR");
    public static final Locale EN_US = Locale.forLanguageTag("en-US");
    public static final Locale JA_JP = Locale.forLanguageTag("ja-JP");
    public static final Locale[] LOCALES = {KO_KR, EN_US, JA_JP};

    private static final Map<Locale, Properties> LANGMAP = GFLResources.readInternalProp();

    // <editor-fold defaultstate="collapsed" desc="Map">
    // ACTION
    public static final String ACTION_OK = "ACTION_OK";
    public static final String ACTION_CANCEL = "ACTION_CANCEL";
    public static final String ACTION_APPLY = "ACTION_APPLY";
    public static final String ACTION_CLOSE = "ACTION_CLOSE";
    public static final String ACTION_ADD = "ACTION_ADD";
    public static final String ACTION_DEL = "ACTION_DEL";
    // UNIT
    public static final String UNIT_PT = "UNIT_PT";
    public static final String UNIT_COUNT = "UNIT_COUNT";
    public static final String UNIT_STAR = "UNIT_STAR";
    public static final String UNIT_STAR_SHORT = "UNIT_STAR_SHORT";
    public static final String UNIT_CELL = "UNIT_CELL";
    public static final String UNIT_CELLTYPE = "UNIT_CELLTYPE";
    public static final String UNIT_LEVEL = "UNIT_LEVEL";
    // NEWVER
    public static final String NEWVER_CONFIRM_TITLE = "NEWVER_CONFIRM_TITLE";
    public static final String NEWVER_CONFIRM_BODY = "NEWVER_CONFIRM_BODY";
    public static final String NEWVER_ERROR_TITLE = "NEWVER_ERROR_TITLE";
    public static final String NEWVER_ERROR_BODY = "NEWVER_ERROR_BODY";
    // TIP
    public static final String TIP_DISPLAY = "TIP_DISPLAY";
    public static final String TIP_HELP = "TIP_HELP";
    public static final String TIP_IMAGE = "TIP_IMAGE";
    public static final String TIP_PROXY = "TIP_PROXY";

    public static final String TIP_POOL = "TIP_POOL";

    public static final String TIP_POOL_ROTATE_LEFT = "TIP_POOL_ROTATE_LEFT";
    public static final String TIP_POOL_ROTATE_RIGHT = "TIP_POOL_ROTATE_RIGHT";
    public static final String TIP_POOL_SORT_ORDER = "TIP_POOL_SORT_ORDER";
    public static final String TIP_POOL_STAR = "TIP_POOL_STAR";
    public static final String TIP_POOL_COLOR = "TIP_POOL_COLOR";

    public static final String TIP_POOLWINDOW = "TIP_POOLWINDOW";
    public static final String TIP_ADD = "TIP_ADD";

    public static final String TIP_INV = "TIP_INV";
    public static final String TIP_INV_NEW = "TIP_INV_NEW";
    public static final String TIP_INV_OPEN = "TIP_INV_OPEN";
    public static final String TIP_INV_SAVE = "TIP_INV_SAVE";
    public static final String TIP_INV_SAVEAS = "TIP_INV_SAVEAS";

    public static final String TIP_INV_SORT_ORDER = "TIP_INV_SORT_ORDER";
    public static final String TIP_INV_SORT_TYPE = "TIP_INV_SORT_TYPE";
    public static final String TIP_INV_FILTER = "TIP_INV_FILTER";
    public static final String TIP_INV_STAT = "TIP_INV_STAT";

    public static final String TIP_INV_APPLY = "TIP_INV_APPLY";
    public static final String TIP_INV_STAR = "TIP_INV_STAR";
    public static final String TIP_INV_COLOR = "TIP_INV_COLOR";
    public static final String TIP_INV_ENHANCEMENT = "TIP_INV_ENHANCEMENT";
    public static final String TIP_INV_ROTATE_LEFT = "TIP_INV_ROTATE_LEFT";
    public static final String TIP_INV_ROTATE_RIGHT = "TIP_INV_ROTATE_RIGHT";
    public static final String TIP_INV_DELETE = "TIP_INV_DELETE";
    public static final String TIP_INV_MARK = "TIP_INV_MARK";
    public static final String TIP_INV_TAG = "TIP_INV_TAG";

    public static final String TIP_BOARD_NAME = "TIP_BOARD_NAME";
    public static final String TIP_BOARD_STAR = "TIP_BOARD_STAR";

    public static final String TIP_RESEARCH_OLD = "TIP_RESEARCH_OLD";

    public static final String TIP_COMB_LIST = "TIP_COMB_LIST";
    public static final String TIP_COMB_CHIPLIST = "TIP_COMB_CHIPLIST";
    public static final String TIP_COMB_FREQLIST = "TIP_COMB_FREQLIST";

    public static final String TIP_COMB_SETTING = "TIP_COMB_SETTING";
    public static final String TIP_COMB_SHOWPROGIMAGE = "TIP_COMB_SHOWPROGIMAGE";
    public static final String TIP_COMB_START = "TIP_COMB_START";
    public static final String TIP_COMB_STAT = "TIP_COMB_STAT";
    public static final String TIP_COMB_OPEN = "TIP_COMB_OPEN";
    public static final String TIP_COMB_SAVE = "TIP_COMB_SAVE";
    public static final String TIP_COMB_MARK = "TIP_COMB_MARK";
    public static final String TIP_COMB_TAG = "TIP_COMB_TAG";
    // HELP
    public static final String HELP_TITLE = "HELP_TITLE";

    public static final String HELP_CHIP = "HELP_CHIP";

    public static final String HELP_CHIP_INFO_POINT_TITLE = "HELP_CHIP_INFO_POINT_TITLE";
    public static final String HELP_CHIP_INFO_POINT_BODY = "HELP_CHIP_INFO_POINT_BODY";

    public static final String HELP_CHIP_INFO_EFFICIENCY_TITLE = "HELP_CHIP_INFO_EFFICIENCY_TITLE";
    public static final String HELP_CHIP_INFO_EFFICIENCY_BODY = "HELP_CHIP_INFO_EFFICIENCY_BODY";

    public static final String HELP_CHIP_INFO_COLOR_TITLE = "HELP_CHIP_INFO_COLOR_TITLE";
    public static final String HELP_CHIP_INFO_COLOR_BODY = "HELP_CHIP_INFO_COLOR_BODY";

    public static final String HELP_CHIP_INFO_CALC_TITLE = "HELP_CHIP_INFO_CALC_TITLE";
    public static final String HELP_CHIP_INFO_CALC_BODY = "HELP_CHIP_INFO_CALC_BODY";

    public static final String HELP_CHIP_COL_CELL = "HELP_CHIP_COL_CELL";
    public static final String HELP_CHIP_COL_LEVEL = "HELP_CHIP_COL_LEVEL";

    public static final String HELP_CHIP_COLOR_SECTION = "HELP_CHIP_COLOR_SECTION";
    public static final String HELP_CHIP_COLOR_CUMULATIVE = "HELP_CHIP_COLOR_CUMULATIVE";

    public static final String HELP_CHIP_CALC_DESC = "HELP_CHIP_CALC_DESC";
    public static final String HELP_CHIP_CALC_STAT = "HELP_CHIP_CALC_STAT";
    public static final String HELP_CHIP_CALC_LOSS = "HELP_CHIP_CALC_LOSS";

    public static final String HELP_APP_IMPORT = "HELP_APP_IMPORT";
    public static final String HELP_APP_IMPORT_PROXY = "HELP_APP_IMPORT_PROXY";
    public static final String HELP_APP_IMPORT_IMAGESCAN = "HELP_APP_IMPORT_IMAGESCAN";
    public static final String HELP_APP_IMPORT_OPEN = "HELP_APP_IMPORT_OPEN";

    public static final String HELP_APP_OPTIMIZE = "HELP_APP_OPTIMIZE";
    public static final String HELP_APP_OPTIMIZE_FILTER = "HELP_APP_OPTIMIZE_FILTER";
    public static final String HELP_APP_OPTIMIZE_SETTING = "HELP_APP_OPTIMIZE_SETTING";
    public static final String HELP_APP_OPTIMIZE_MARK = "HELP_APP_OPTIMIZE_MARK";

    public static final String HELP_PROGRAM = "HELP_PROGRAM";

    public static final String HELP_PROXY = "HELP_PROXY";
    public static final String HELP_PROXY_DESC = "HELP_PROXY_DESC";

    public static final String HELP_CHANGELOG = "HELP_CHANGELOG";
    public static final String HELP_ABOUT = "HELP_ABOUT";
    // DISPLAY
    public static final String DISPLAY_TITLE = "DISPLAY_TITLE";

    public static final String DISPLAY_LANGUAGE_TITLE = "DISPLAY_LANGUAGE_TITLE";
    public static final String DISPLAY_LANGUAGE_PREVIEW = "DISPLAY_LANGUAGE_PREVIEW";
    public static final String DISPLAY_HOWTO_ADDLANGUAGE = "DISPLAY_HOWTO_ADDLANGUAGE";
    public static final String DISPLAY_EXPORT = "DISPLAY_EXPORT";
    public static final String DISPLAY_EXPORT_DONE_TITLE = "DISPLAY_EXPORT_DONE_TITLE";
    public static final String DISPLAY_EXPORT_DONE_BODY = "DISPLAY_EXPORT_DONE_BODY";
    public static final String DISPLAY_EXPORT_FAIL_TITLE = "DISPLAY_EXPORT_FAIL_TITLE";
    public static final String DISPLAY_EXPORT_FAIL_BODY = "DISPLAY_EXPORT_FAIL_BODY";

    public static final String DISPLAY_COLORFONT_TITLE = "DISPLAY_COLORFONT_TITLE";
    public static final String DISPLAY_COLOR_NORMAL = "DISPLAY_COLOR_NORMAL";
    public static final String DISPLAY_COLOR_COLORBLIND = "DISPLAY_COLOR_COLORBLIND";
    public static final String DISPLAY_FONT_RESET = "DISPLAY_FONT_RESET";
    // IMAGE
    public static final String IMAGE_TITLE = "IMAGE_TITLE";
    public static final String IMAGE_OPEN = "IMAGE_OPEN";
    public static final String IMAGE_OPEN_EXT = "IMAGE_OPEN_EXT";
    public static final String IMAGE_OVERLAPPED_TITLE = "IMAGE_OVERLAPPED_TITLE";
    public static final String IMAGE_OVERLAPPED_BODY = "IMAGE_OVERLAPPED_BODY";
    // PROXY
    public static final String PROXY_TITLE = "PROXY_TITLE";
    public static final String PROXY_WARNING = "PROXY_WARNING";
    public static final String PROXY_STAGE1_INST = "PROXY_STAGE1_INST";
    public static final String PROXY_STAGE1_INFO = "PROXY_STAGE1_INFO";
    public static final String PROXY_STAGE2_INST = "PROXY_STAGE2_INST";
    public static final String PROXY_STAGE2_INFO = "PROXY_STAGE2_INFO";
    public static final String PROXY_ERROR_INST = "PROXY_ERROR_INST";
    public static final String PROXY_ERROR_INFO = "PROXY_ERROR_INFO";
    // SORT
    public static final String SORT_CUSTOM = "SORT_CUSTOM";
    public static final String SORT_CELL = "SORT_CELL";
    public static final String SORT_ENHANCEMENT = "SORT_ENHANCEMENT";
    public static final String SORT_STAR = "SORT_STAR";
    public static final String SORT_PT = "SORT_PT";
    // FILTER
    public static final String FILTER_TITLE = "FILTER_TITLE";
    public static final String FILTER_GROUP_STAR = "FILTER_GROUP_STAR";
    public static final String FILTER_GROUP_COLOR = "FILTER_GROUP_COLOR";
    public static final String FILTER_GROUP_CELL = "FILTER_GROUP_CELL";
    public static final String FILTER_GROUP_MARK = "FILTER_GROUP_MARK";
    public static final String FILTER_GROUP_PT = "FILTER_GROUP_PT";
    public static final String FILTER_GROUP_ENHANCEMENT = "FILTER_GROUP_ENHANCEMENT";
    public static final String FILTER_GROUP_TAG_INCLUDE = "FILTER_GROUP_TAG_INCLUDE";
    public static final String FILTER_GROUP_TAG_EXCLUDE = "FILTER_GROUP_TAG_EXCLUDE";
    public static final String FILTER_PRESET = "FILTER_PRESET";
    public static final String FILTER_RESET = "FILTER_RESET";
    public static final String FILTER_TAG_RESET = "FILTER_TAG_RESET";
    // APPLY
    public static final String APPLY_TITLE = "APPLY_TITLE";
    public static final String APPLY_MARK_ALL = "APPLY_MARK_ALL";
    public static final String APPLY_MARK_NONE = "APPLY_MARK_NONE";
    public static final String APPLY_CONFIRM_DESC = "APPLY_CONFIRM_DESC";
    public static final String APPLY_TAG_DESC = "APPLY_TAG_DESC";
    //CHIP
    public static final String CHIP_COLOR_ORANGE = "CHIP_COLOR_ORANGE";
    public static final String CHIP_COLOR_BLUE = "CHIP_COLOR_BLUE";

    public static final String CHIP_STAT_DMG = "CHIP_STAT_DMG";
    public static final String CHIP_STAT_BRK = "CHIP_STAT_BRK";
    public static final String CHIP_STAT_HIT = "CHIP_STAT_HIT";
    public static final String CHIP_STAT_RLD = "CHIP_STAT_RLD";
    public static final String CHIP_STAT_DMG_LONG = "CHIP_STAT_DMG_LONG";
    public static final String CHIP_STAT_BRK_LONG = "CHIP_STAT_BRK_LONG";
    public static final String CHIP_STAT_HIT_LONG = "CHIP_STAT_HIT_LONG";
    public static final String CHIP_STAT_RLD_LONG = "CHIP_STAT_RLD_LONG";

    public static final String CHIP_COLOR = "CHIP_COLOR";
    public static final String CHIP_STAR = "CHIP_STAR";
    public static final String CHIP_TAG = "CHIP_TAG";
    public static final String CHIP_MARK = "CHIP_MARK";
    public static final String CHIP_PT = "CHIP_PT";
    public static final String CHIP_TICKET = "CHIP_TICKET";
    public static final String CHIP_XP = "CHIP_XP";
    public static final String CHIP_LEVEL = "CHIP_LEVEL";
    // LEGEND
    public static final String LEGEND_EQUIPPED = "LEGEND_EQUIPPED";
    public static final String LEGEND_ROTATED = "LEGEND_ROTATED";
    // STAT
    public static final String STAT_TITLE = "STAT_TITLE";
    public static final String STAT_TOTAL = "STAT_TOTAL";
    public static final String STAT_TOTAL_OLD = "STAT_TOTAL_OLD";
    public static final String STAT_HOC = "STAT_HOC";
    public static final String STAT_CHIP = "STAT_CHIP";
    public static final String STAT_RESONANCE = "STAT_RESONANCE";
    public static final String STAT_VERSION = "STAT_VERSION";

    public static final String STAT_RLD_FIRERATE = "STAT_RLD_FIRERATE";
    public static final String STAT_RLD_DELAY = "STAT_RLD_DELAY";
    public static final String STAT_RLD_DELAY_FRAME = "STAT_RLD_DELAY_FRAME";
    public static final String STAT_RLD_DELAY_SECOND = "STAT_RLD_DELAY_SECOND";
    // FILTER
    public static final String FILTER_ENABLED = "FILTER_ENABLED";
    public static final String FILTER_DISABLED = "FILTER_DISABLED";
    // TAG
    public static final String TAG_NONE = "TAG_NONE";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_DESC = "TAG_DESC";
    // CSET
    public static final String CSET_TITLE = "CSET_TITLE";

    public static final String CSET_ADVANCED_MODE = "CSET_ADVANCED_MODE";

    public static final String CSET_GROUP_STAT = "CSET_GROUP_STAT";
    public static final String CSET_GROUP_MARK = "CSET_GROUP_MARK";
    public static final String CSET_GROUP_SORT = "CSET_GROUP_SORT";
    public static final String CSET_GROUP_MISC = "CSET_GROUP_MISC";

    public static final String CSET_DEFAULT_STAT = "CSET_DEFAULT_STAT";
    public static final String CSET_STAT = "CSET_STAT";
    public static final String CSET_PT = "CSET_PT";
    public static final String CSET_PRESET = "CSET_PRESET";
    public static final String CSET_PRESET_OPTION = "CSET_PRESET_OPTION";

    public static final String CSET_MARK_CELL = "CSET_MARK_CELL";
    public static final String CSET_MARK_CHIP = "CSET_MARK_CHIP";
    public static final String CSET_MARK_DESC = "CSET_MARK_DESC";

    public static final String CSET_SORT_TICKET = "CSET_SORT_TICKET";
    public static final String CSET_SORT_XP = "CSET_SORT_XP";

    public static final String CSET_MAXLEVEL_DESC = "CSET_MAXLEVEL_DESC";
    public static final String CSET_COLOR_DESC = "CSET_COLOR_DESC";
    public static final String CSET_ROTATION_DESC = "CSET_ROTATION_DESC";
    public static final String CSET_SYMMETRY_DESC = "CSET_SYMMETRY_DESC";

    public static final String CSET_CONFIRM_FILTER_TITLE = "CSET_CONFIRM_FILTER_TITLE";
    public static final String CSET_CONFIRM_FILTER_BODY = "CSET_CONFIRM_FILTER_BODY";
    // RESEARCH
    public static final String RESEARCH_TITLE = "RESEARCH_TITLE";
    public static final String RESEARCH_WTF = "RESEARCH_WTF";
    public static final String RESEARCH_THREAD = "RESEARCH_THREAD";
    public static final String RESEARCH_START = "RESEARCH_START";
    public static final String RESEARCH_STOP = "RESEARCH_STOP";
    public static final String RESEARCH_READY = "RESEARCH_READY";
    public static final String RESEARCH_EMPTY = "RESEARCH_EMPTY";
    public static final String RESEARCH_WAITING = "RESEARCH_WAITING";
    public static final String RESEARCH_WORKING = "RESEARCH_WORKING";
    // WARNING
    public static final String WARNING_HOCMAX = "WARNING_HOCMAX";
    public static final String WARNING_HOCMAX_DESC = "WARNING_HOCMAX_DESC";
    public static final String WARNING_TIME = "WARNING_TIME";
    public static final String WARNING_TIME_DESC = "WARNING_TIME_DESC";
    // COMB
    public static final String COMB_REMAINING = "COMB_REMAINING";
    public static final String COMB_DESC = "COMB_DESC";
    public static final String COMB_NONEFOUND = "COMB_NONEFOUND";

    public static final String COMB_TAB_RESULT = "COMB_TAB_RESULT";
    public static final String COMB_TAB_FREQ = "COMB_TAB_FREQ";

    public static final String COMB_MARK_CONTINUE_TITLE = "COMB_MARK_CONTINUE_TITLE";
    public static final String COMB_MARK_CONTINUE_BODY = "COMB_MARK_CONTINUE_BODY";
    public static final String COMB_DNE_TITLE = "COMB_DNE_TITLE";
    public static final String COMB_DNE_BODY = "COMB_DNE_BODY";

    public static final String COMB_OPTION_M2_0 = "COMB_OPTION_M2_0";
    public static final String COMB_OPTION_M2_1 = "COMB_OPTION_M2_1";
    public static final String COMB_OPTION_M2_2 = "COMB_OPTION_M2_2";
    public static final String COMB_OPTION_M2_DESC = "COMB_OPTION_M2_DESC";
    public static final String COMB_OPTION_DEFAULT_0 = "COMB_OPTION_DEFAULT_0";
    public static final String COMB_OPTION_DEFAULT_1 = "COMB_OPTION_DEFAULT_1";
    public static final String COMB_OPTION_DEFAULT_DESC = "COMB_OPTION_DEFAULT_DESC";
    public static final String COMB_OPTION_FILTER_DESC = "COMB_OPTION_FILTER_DESC";
    public static final String COMB_OPTION_TITLE = "COMB_OPTION_TITLE";
    public static final String COMB_ERROR_STAT_TITLE = "COMB_ERROR_STAT_TITLE";
    public static final String COMB_ERROR_STAT_BODY = "COMB_ERROR_STAT_BODY";
    // FILE
    public static final String FILE_EXT_INV_OPEN = "FILE_EXT_INV_OPEN";
    public static final String FILE_EXT_INV_SAVE = "FILE_EXT_INV_SAVE";
    public static final String FILE_EXT_COMB = "FILE_EXT_COMB";

    public static final String FILE_SAVE_TITLE = "FILE_SAVE_TITLE";
    public static final String FILE_SAVE_BODY = "FILE_SAVE_BODY";
    public static final String FILE_OVERWRITE_TITLE = "FILE_OVERWRITE_TITLE";
    public static final String FILE_OVERWRITE_BODY = "FILE_OVERWRITE_BODY";
    // JSON
    public static final String JSON_TITLE = "JSON_TITLE";
    public static final String JSON_FILTER_STAR = "JSON_FILTER_STAR";
    public static final String JSON_FILTER_SIZE = "JSON_FILTER_SIZE";
    public static final String JSON_SHAPETAG = "JSON_SHAPETAG"; //REMOVE
    public static final String JSON_MARK = "JSON_MARK";
    // </editor-fold>

    private static final List<String> KEYS_HTML = Arrays.asList(
            DISPLAY_HOWTO_ADDLANGUAGE,
            HELP_CHIP_INFO_POINT_BODY, HELP_CHIP_INFO_EFFICIENCY_BODY, HELP_CHIP_INFO_COLOR_BODY, HELP_CHIP_INFO_CALC_BODY,
            HELP_CHIP_CALC_DESC,
            HELP_APP_IMPORT_PROXY, HELP_APP_IMPORT_IMAGESCAN, HELP_APP_IMPORT_OPEN,
            HELP_APP_OPTIMIZE, HELP_APP_OPTIMIZE_FILTER, HELP_APP_OPTIMIZE_SETTING, HELP_APP_OPTIMIZE_MARK,
            HELP_PROXY_DESC,
            PROXY_WARNING, PROXY_STAGE1_INFO, PROXY_STAGE2_INFO,
            APPLY_TAG_DESC,
            WARNING_HOCMAX_DESC, WARNING_TIME_DESC,
            COMB_OPTION_M2_DESC, COMB_OPTION_DEFAULT_DESC, COMB_OPTION_FILTER_DESC,
            RESEARCH_WTF
    );
    private static final List<String> KEYS_HTML_CENTER = Arrays.asList(
            COMB_DESC
    );
    private static final List<String> KEYS_PLURAL = Arrays.asList(
            UNIT_STAR, UNIT_CELL
    );

    private static final String DISPLAY_LANGUAGE_PREVIEW_DEFAULT = "ABC abc 1234567890 ★";

    public String getText(Locale locale, String key, String... replaces) {
        String value = getValue(locale, key);

        for (int i = 0; i < replaces.length; i++) {
            String replace = replaces[i];
            value = value.replace("{" + i + "}", replace);
        }

        try {
            if (EN_US.equals(locale) && KEYS_PLURAL.contains(key) && Integer.valueOf(replaces[0]) > 1) {
                value += "s";
            }
        } catch (Exception ex) {
        }

        if (KEYS_HTML_CENTER.contains(key)) {
            return "<html><center>" + value + "</center></html>";
        }

        if (KEYS_HTML.contains(key)) {
            return "<html>" + value + "</html>";
        }

        if (key.equals(DISPLAY_LANGUAGE_PREVIEW)) {
            return value + (value.isEmpty() ? "" : " ") + DISPLAY_LANGUAGE_PREVIEW_DEFAULT;
        }

        return value;
    }

    private String getValue(Locale locale, String key) {
        if (!propTag.equals(locale.toLanguageTag())) {
            prop = IO.getProp(locale);
            propTag = locale.toLanguageTag();
        }
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        }
        if (KO_KR.equals(locale) && LANGMAP.get(KO_KR).containsKey(key)) {
            return LANGMAP.get(KO_KR).getProperty(key);
        }
        if (JA_JP.equals(locale) && LANGMAP.get(JA_JP).containsKey(key)) {
            return LANGMAP.get(JA_JP).getProperty(key);
        }
        return LANGMAP.get(EN_US).getProperty(key);
    }

    public static String getFileContent(Locale locale) {
        Properties prop;
        if (LANGMAP.containsKey(locale)) {
            prop = LANGMAP.get(locale);
        } else {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(App.VERSION.toData()).append(System.lineSeparator());
        List<String> keyList = new ArrayList<>();
        prop.keySet().forEach((k) -> keyList.add(k.toString()));
        Collections.sort(keyList);
        String prevKeyPrefix = "";
        for (String key : keyList) {
            String keyPrefix = key.substring(0, key.indexOf('_'));
            if (!prevKeyPrefix.isEmpty() && !prevKeyPrefix.equals(keyPrefix)) {
                sb.append(System.lineSeparator());
            }
            String value = prop.getProperty(key);
            sb.append(key).append("=").append(value).append(System.lineSeparator());
            prevKeyPrefix = keyPrefix;
        }
        return sb.toString();
    }
    // </editor-fold>
}
