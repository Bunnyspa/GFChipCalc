package main.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import main.App;
import main.puzzle.Board;
import main.puzzle.BoardTemplate;
import main.puzzle.Chip;
import main.puzzle.Puzzle;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.puzzle.Tag;
import main.puzzle.assembly.CalcExtraSetting;
import main.puzzle.assembly.CalcSetting;
import main.puzzle.assembly.Progress;
import main.puzzle.assembly.ProgressFile;
import main.setting.BoardSetting;
import main.setting.Setting;
import main.ui.resource.GFLResources;
import main.ui.resource.GFLTexts;

/**
 *
 * @author Bunnyspa
 */
public class IO {

    public static final String UTF8 = "UTF-8";

    public static final String EXT_INVENTORY = "gfci";
    public static final String EXT_COMBINATION = "gfcc";

    private static final String PATH_EX_LANG = "language";

    private static final String FILENAME_SETTINGS = "settings.dat";

    private static final String FILENAME_UPDATE = "GFChipCalc-Update.jar";

    private static final String URL_GITHUB_MAIN = "https://github.com/Bunnyspa/GFChipCalc/releases/latest";
    private static final String URL_GITHUB_UPDATE = "https://github.com/Bunnyspa/GFChipCalc-Update/releases/latest";
    private static final String URL_DOWNLOAD_UPDATE = URL_GITHUB_UPDATE + "/download/GFChipCalc-Update.jar";

    private static int pre420rotation(Shape shape) {
        switch (shape) {
            case _4_I:
            case _5A_I:
            case _5A_Z:
            case _5A_Zm:
            case _5B_F:
            case _6_A:
            case _6_C:
            case _6_D:
            case _6_I:
            case _6_R:
                return 1;
            case _4_T:
            case _5A_P:
            case _5A_Pm:
            case _5B_T:
            case _6_Y:
                return 2;
            case _4_Lm:
            case _5A_C:
            case _5B_W:
            case _5B_Fm:
            case _6_T:
                return 3;
            default:
                return 0;
        }
    }

    public static List<Chip> loadInventory(String fileName) {
        List<Chip> chips = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            Iterator<String> bri = br.lines().iterator();
            if (bri.hasNext()) {
                String s = bri.next();
                Version3 v = new Version3(s);
                if (v.isCurrent(5, 3, 0)) {
                    Set<Tag> tags = new HashSet<>();
                    bri.forEachRemaining((l) -> chips.add(parseChip(v, l, tags)));
                } else if (v.isCurrent(4, 0, 0)) {
                    // 4.0.0+
                    while (bri.hasNext()) {
                        Chip c = new Chip(v, bri.next().split(";"), Chip.INVENTORY);
                        // 4.0.0 - 4.1.x
                        if (!v.isCurrent(4, 2, 0)) {
                            c.initRotate(pre420rotation(c.getShape()));
                        }
                        chips.add(c);
                    }
                } else {
                    // 1.0.0 - 3.0.0
                    chips.add(new Chip(new Version3(), s.split(","), Chip.INVENTORY));
                    while (bri.hasNext()) {
                        chips.add(new Chip(new Version3(), bri.next().split(","), Chip.INVENTORY));
                    }
                }
            }
        } catch (Exception ex) {
            App.log(ex);
        }
        return chips;
    }

    public static void saveInventory(String fileName, List<Chip> chips) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(App.VERSION.toData());
            bw.newLine();
            for (Chip c : chips) {
                bw.write(c.toData());
                bw.newLine();
            }
        } catch (Exception ex) {
            App.log(ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Combination (Pre 5.3.0)">
    private static List<Board> loadCombination(String s, Iterator<String> bri) {
        List<Board> boards = new ArrayList<>();
        Version3 v = new Version3(s);
        if (v.isCurrent(4, 0, 0)) {
            if (bri.hasNext()) {
                String[] info = bri.next().split(",");
                String name = info[0];
                int star = Integer.valueOf(info[1]);

                Stat maxStat = Board.getMaxStat(name, star);
                if (v.isCurrent(4, 2, 0) && info.length > 2) {
                    maxStat = new Stat(
                            Integer.valueOf(info[2]),
                            Integer.valueOf(info[3]),
                            Integer.valueOf(info[4]),
                            Integer.valueOf(info[5])
                    );
                } else if (info.length > 6 && !"1".equals(info[6])) {
                    maxStat = new Stat(
                            Integer.valueOf(info[2]),
                            Integer.valueOf(info[3]),
                            Integer.valueOf(info[4]),
                            Integer.valueOf(info[5])
                    );
                }

                while (bri.hasNext()) {
                    // Chips
                    int nChip = Integer.valueOf(bri.next());
                    List<Chip> chips = new ArrayList<>(nChip);
                    List<Shape> shapes = new ArrayList<>(nChip);
                    for (int i = 0; i < nChip; i++) {
                        String[] chipInfo = bri.next().split(";");
                        Chip c = new Chip(v, chipInfo, Chip.COMBINATION);
                        chips.add(c);
                        if (!v.isCurrent(4, 2, 0)) {
                            int r = c.getInitRotation();
                            r += pre420rotation(c.getShape());
                            c.setInitRotation(r);
                        }
                        shapes.add(c.getShape());
                    }
                    chips.forEach((c) -> c.setMaxLevel());

                    // Rotations
                    String[] rotStrs = bri.next().split(",");
                    for (int i = 0; i < nChip; i++) {
                        int r = Integer.valueOf(rotStrs[i]);
                        if (!v.isCurrent(4, 2, 0)) {
                            r += pre420rotation(shapes.get(i));
                        }
                        chips.get(i).setRotation(r);
                    }
                    // Locations
                    String[] locStrs = bri.next().split(",");
                    List<Point> locations = new ArrayList<>(locStrs.length);
                    for (String locStr : locStrs) {
                        locations.add(parsePoint(locStr));
                    }

                    // Generate board
                    Board b = new Board(name, star, maxStat, chips, locations);
                    boards.add(b);
                }
            }
        } else {
            String[] info = s.split(",");
            String name = info[0];
            int star = Integer.valueOf(info[1]);

            Stat max = Board.getMaxStat(name, star);
            if (info.length > 6 && !"1".equals(info[6])) {
                max = new Stat(
                        Integer.valueOf(info[2]),
                        Integer.valueOf(info[3]),
                        Integer.valueOf(info[4]),
                        Integer.valueOf(info[5])
                );
            }

            while (bri.hasNext()) {
                // Chips
                int nChip = Integer.valueOf(bri.next());
                List<Chip> chips = new ArrayList<>();
                for (int i = 0; i < nChip; i++) {
                    Chip c = new Chip(new Version3(), bri.next().split(","), Chip.COMBINATION);
                    c.rotate(pre420rotation(c.getShape()));
                    chips.add(c);
                }
                chips.forEach((c) -> c.setMaxLevel());

                // Matrix
                PuzzleMatrix<Integer> matrix = new PuzzleMatrix<>(Board.HEIGHT, Board.WIDTH, Board.UNUSED);
                for (int row = 0; row < Board.HEIGHT; row++) {
                    String rowStrs = bri.next();
                    for (int col = 0; col < Board.WIDTH; col++) {
                        String rowChar = rowStrs.substring(col, col + 1);
                        if (!"-".equals(rowChar)) {
                            matrix.set(row, col, Integer.valueOf(rowChar));
                        }
                    }
                }
                List<Point> chipLocs = Board.toLocation(matrix);
                if (chips.size() == chipLocs.size()) {
                    Board b = new Board(name, star, max, chips, chipLocs);
                    boards.add(b);
                }
            }
        }
        return boards;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Progress">
    public static ProgressFile loadProgressFile(String fileName, List<Chip> invChips) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            Iterator<String> bri = br.lines().iterator();
            if (bri.hasNext()) {
                String s = bri.next();
                Version3 v = new Version3(s);
                if (v.isCurrent(5, 3, 0)) {
                    return parseProgressFile(v, bri, invChips);
                } else {
                    List<Board> boards = loadCombination(s, bri);
                    String name = "";
                    int star = 0;
                    Stat stat = new Stat();
                    if (!boards.isEmpty()) {
                        Board b = boards.get(0);
                        name = b.getName();
                        star = b.getStar();
                        stat = b.getCustomMaxStat();
                    }
                    Set<Chip> chipSet = new HashSet<>();
                    boards.forEach((b) -> b.forEachChip((c) -> chipSet.add(c)));
                    loadProgress_adjustInits(chipSet, invChips);
                    return new ProgressFile(
                            new CalcSetting(name, star, false, false, false, stat, null),
                            new CalcExtraSetting(CalcExtraSetting.CALCMODE_FINISHED, 0, false, 0, 0, 0, 0, new ArrayList<>(chipSet)),
                            new Progress(0, -1, 1, 1, boards)
                    );
                }
            }
        } catch (Exception ex) {
            App.log(ex);
        }
        return null;
    }

    public static void saveProgressFile(String fileName, ProgressFile pf) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(App.VERSION.toData());
            bw.newLine();
            bw.write(pf.toData());
        } catch (Exception ex) {
            App.log(ex);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Setting">
    public static Setting loadSettings() {
        List<String> lines = new ArrayList<>();
        List<String> sgLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME_SETTINGS))) {
            Iterator<String> bri = br.lines().iterator();
            String section = Setting.SECTION_GENERAL;
            while (bri.hasNext()) {
                String s = bri.next().trim();
                if (s.startsWith("[")) {
                    section = s.substring(1, s.indexOf(']'));
                } else if (section.contains(Setting.SECTION_GENERAL)) {
                    lines.add(s);
                } else if (section.contains(Setting.SECTION_BOARD)) {
                    sgLines.add(s);
                }
            }
        } catch (FileNotFoundException ex) {
            return new Setting();
        } catch (Exception ex) {
            App.log(ex);
        }
        return new Setting(lines, sgLines);
    }

    public static void saveSettings(Setting settings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME_SETTINGS))) {
            String s = settings.toData();
            bw.write(s);
        } catch (Exception ex) {
            App.log(ex);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Preset">
    public static List<BoardTemplate> loadBoardTemplates(String name, int star, boolean isPartial) {
        List<BoardTemplate> out = new ArrayList<>();

        String fileName = "template_" + toFileName(name) + "_" + star + (isPartial ? "_p" : "") + ".dat";
        URL url = App.class.getResource(GFLResources.RESOURCE_PATH + "template/" + fileName);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            Iterator<String> bri = br.lines().iterator();
            while (bri.hasNext()) {
                String line = bri.next();
                out.add(loadBoardTemplate(name, star, line));
            }
        } catch (Exception ex) {
        }
        return out;
    }

    private static BoardTemplate loadBoardTemplate(String name, int star, String line) {
        String[] split = line.split(";");
        String[] names = split[0].split(",");
        String[] rotations = split[1].split(",");
        String[] locations = split[2].split(",");

        List<Puzzle> puzzles = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            puzzles.add(new Puzzle(
                    Shape.byId(Integer.parseInt(names[i])),
                    Integer.parseInt(rotations[i]),
                    parsePoint(locations[i])
            ));
        }

        if (split.length > 3) {
            // Symmetry
            boolean symmetry = parseBoolean(split[3]);

            BoardTemplate pp = new BoardTemplate(name, star, puzzles, symmetry);
            return pp;
        } else {
            BoardTemplate pp = new BoardTemplate(name, star, puzzles);
            return pp;
        }
    }

    public static String toFileName(String boardName) {
        return boardName.replace("-", "").replace(" ", "").toLowerCase();
    }
    // </editor-fold>

    private static String getNameWOExt(String s) {
        int lastIndex = s.lastIndexOf('.');
        return s.substring(0, lastIndex);
    }

    // <editor-fold defaultstate="collapsed" desc="Locales and Properties">
    public static List<Locale> getInternalLocales() {
        List<Locale> locales = new ArrayList<>();
        locales.addAll(Arrays.asList(GFLTexts.LOCALES));
        return locales;
    }

    public static List<Locale> getExternalLocales() {
        List<Locale> locales = new ArrayList<>();
        File folder = new File(PATH_EX_LANG);
        if (!folder.exists() || !folder.isDirectory()) {
            return locales;
        }
        for (File file : folder.listFiles()) {
            String name = getNameWOExt(file.getName());
            locales.add(Locale.forLanguageTag(name.replace("_", "-")));
        }
        return locales;
    }

    public static List<Locale> getLocales() {
        List<Locale> locales = new ArrayList<>(getInternalLocales());
        getExternalLocales().forEach((locale) -> {
            if (!locales.contains(locale)) {
                locales.add(locale);
            }
        });
        return locales;
    }

    public static void exportProps(App app, Component component) {
        File folder = new File(PATH_EX_LANG);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                JOptionPane.showMessageDialog(component, app.getText(GFLTexts.DISPLAY_EXPORT_FAIL_BODY), app.getText(GFLTexts.DISPLAY_EXPORT_FAIL_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        }

        try {
            for (Locale locale : getInternalLocales()) {
                String filePath = PATH_EX_LANG + "/" + locale.toLanguageTag() + ".properties";
                String fileContent = GFLTexts.getFileContent(locale);
                write(filePath, fileContent);
            }
            JOptionPane.showMessageDialog(component, app.getText(GFLTexts.DISPLAY_EXPORT_DONE_BODY, PATH_EX_LANG), app.getText(GFLTexts.DISPLAY_EXPORT_DONE_TITLE), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(component, GFLTexts.DISPLAY_EXPORT_FAIL_BODY, GFLTexts.DISPLAY_EXPORT_FAIL_TITLE, JOptionPane.ERROR);
        }
    }

    public static Properties getProp(Locale locale) {
        String lang = locale.getLanguage() + "-" + locale.getCountry();
        if (getExternalLocales().contains(locale)) {
            return getProp(PATH_EX_LANG + "/" + lang + ".properties");
        }
        return new Properties();
    }

    private static Properties getProp(String filePath) {
        try (Reader r = new InputStreamReader(new FileInputStream(filePath), UTF8)) {
            Properties props = new Properties();
            props.load(r);
            return props;
        } catch (Exception ex) {
            return new Properties();
        }
    }

    public static void sortProp(String order, String target, String save) {
        try {
            List<String> l = read(order);
            Properties props = getProp(target);
            List<String> values = new ArrayList<>();
            l.forEach((s) -> {
                if (props.containsKey(s)) {
                    values.add(s + "\t" + props.getProperty(s));
                } else {
                    values.add(s + "\t");
                }
            });
            write(save, String.join(System.lineSeparator(), values));
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }
    // </editor-fold>

    public static void write(String filePath, String fileContent) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), UTF8))) {
            w.write(fileContent);
        }
    }

    public static List<String> read(String filePath) throws IOException {
        List<String> l = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), UTF8))) {
            Stream<String> s = r.lines();
            s.forEach((t) -> {
                l.add(t.trim());
            });
        }
        return l;
    }

    public static void checkNewVersion(App app) {
        String mainLatest = getVersion(URL_GITHUB_MAIN, App.VERSION.toData());
        if (!App.VERSION.isCurrent(mainLatest)) {
            int retval = JOptionPane.showConfirmDialog(app.mf,
                    app.getText(GFLTexts.NEWVER_CONFIRM_BODY, mainLatest),
                    app.getText(GFLTexts.NEWVER_CONFIRM_TITLE),
                    JOptionPane.YES_NO_OPTION);
            if (retval == JOptionPane.YES_OPTION) {
                if (!runUpdate(app)) {
                    openWeb(app, app.mf, URL_GITHUB_MAIN);
                }
            }
        }
    }

    private static String getVersion(String url, String defaultVersion) {
        try {
            String latest;
            URLConnection con = new URL(url).openConnection();
            con.connect();
            try (InputStream is = con.getInputStream()) {
                String redirected = con.getURL().toString();
                latest = redirected.substring(redirected.lastIndexOf("/") + 2);
            }
            return latest;
        } catch (IOException ex) {
            return defaultVersion;
        }
    }

    private static boolean runUpdate(App app) {
        String updateLatest = getVersion(URL_GITHUB_UPDATE, app.setting.updateVersion.toData());
        String path = new File("").getAbsolutePath();
        try {
            String exePath = path + "\\" + FILENAME_UPDATE;
            File exeFile = new File(exePath);
            if (!app.setting.updateVersion.isCurrent(updateLatest) || !exeFile.exists()) {
                downloadUpdate();
                app.setting.updateVersion = new Version2(updateLatest);
                app.mf.settingFile_save();
            }
            if (exeFile.exists()) {
                ProcessBuilder process = new ProcessBuilder("java", "-jar", exePath);
                process.directory(new File(path + "\\"));
                process.start();
                System.exit(0);
            }
        } catch (IOException ex) {
        }
        return false;
    }

    private static void downloadUpdate() {
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(URL_DOWNLOAD_UPDATE).openStream());
                FileOutputStream fileOS = new FileOutputStream(FILENAME_UPDATE)) {
            byte data[] = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
        } catch (Exception ex) {
            Logger.getLogger(IO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void openWeb(App app, Component c, String link) {
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(c, app.getText(GFLTexts.NEWVER_ERROR_BODY), app.getText(GFLTexts.NEWVER_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Parsing">
    //========== Boolean ==========//
    public static String data(boolean b) {
        return b ? "1" : "0";
    }

    public static boolean parseBoolean(String s) {
        return "1".equals(s) || "true".equalsIgnoreCase(s);
    }

    //========== String ==========//
    public static String data(Stream<String> s, String delim) {
        List<String> l = new ArrayList<>();
        s.forEach((e) -> l.add(e));
        return String.join(delim, l);
    }

    private static List<String> parseStringList(String s) {
        return Arrays.asList(s.split(","));
    }

    //========== UUID ==========//
    public static String data(UUID u) {
        return u.toString();
    }

    //========== Point ==========//
    public static String data(Point p) {
        return p.x + "." + p.y;
    }

    public static Point parsePoint(String s) {
        String[] split = s.split("\\.");
        return new Point(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
    }

    //========== Tag ==========//
    public static Tag parseTag(String s) {
        if (s.length() > 6) {
            Color color = Color.decode("#" + s.substring(0, 6));
            String name = s.substring(6);
            return new Tag(color, name);
        } else {
            return new Tag();
        }
    }

    //========== Stat ==========//
    public static Stat parseStat(String s) {
        String[] d = s.split(",");
        int dmg = d.length > 0 ? Integer.valueOf(d[0]) : 0;
        int brk = d.length > 1 ? Integer.valueOf(d[1]) : 0;
        int hit = d.length > 2 ? Integer.valueOf(d[2]) : 0;
        int rld = d.length > 3 ? Integer.valueOf(d[3]) : 0;
        return new Stat(dmg, brk, hit, rld);
    }

    //========== Board Setting ==========//
    public static BoardSetting parseBS(List<String> data, boolean advancedSetting) {
        BoardSetting out = new BoardSetting();
        data.stream().map((line) -> line.split(";")).forEachOrdered((parts) -> {
            String name = parts[0];
            if (Arrays.asList(Board.NAMES).contains(name)) {
                int star = Integer.valueOf(parts[1]);
                if (advancedSetting) {
                    int mode = Integer.valueOf(parts[2]);
                    out.setMode(name, star, mode);
                }
                Stat stat = parseStat(parts[3]);
                Stat pt = parseStat(parts[4]);
                out.setStat(name, star, stat);
                out.setPt(name, star, pt);
                if (parts.length > 5) {
                    out.setPresetIndex(name, star, Integer.valueOf(parts[5]));
                }
            }
        });
        return out;
    }

    //========== Chip ==========//
    public static Chip parseChip(Version3 v, String s, Set<Tag> tagPool) {
        Iterator<String> it = Arrays.asList(s.split(";")).iterator();
        String id = it.next();
        Shape shape = v.isCurrent(7, 0, 0) ? Shape.byId(Integer.valueOf(it.next())) : Shape.byName(it.next());
        int star = Integer.valueOf(it.next());
        int color = Integer.valueOf(it.next());

        Stat pt = parseStat(it.next());

        int initLevel = Integer.valueOf(it.next());
        int initRotation = Integer.valueOf(it.next());

        boolean isMarked = parseBoolean(it.next());

        Set<Tag> tags = new HashSet<>();
        if (it.hasNext()) {
            parseStringList(it.next()).forEach((tagStr) -> {
                Tag tag = parseTag(tagStr);
                boolean added = false;
                for (Tag t : tagPool) {
                    if (tag.equals(t)) {
                        tags.add(t);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    tags.add(tag);
                    tagPool.add(tag);
                }
            });
        }

        return new Chip(id, shape, star, color, pt, initLevel, initRotation, isMarked, tags);
    }

    //========== Progress ==========//
    public static ProgressFile parseProgressFile(Version3 v, Iterator<String> it, List<Chip> invChips) {
        int calcMode = Integer.valueOf(it.next());
        String name = it.next();
        int star = Integer.valueOf(it.next());

        if (!v.isCurrent(6, 5, 3) && calcMode == CalcExtraSetting.CALCMODE_FINISHED) {
            Stat stat = parseStat(it.next());

            int nComb = -1;
            if (v.isCurrent(6, 4, 0)) {
                nComb = Integer.valueOf(it.next());
            }

            List<Chip> chips = parseProgress_chips(v, it, invChips);
            chips.forEach((c) -> c.setMaxLevel());

            List<Board> boards = parseProgress_boards(name, star, stat, chips, it);

            return new ProgressFile(
                    new CalcSetting(name, star, true, true, false, stat, new Stat(0)),
                    new CalcExtraSetting(calcMode, 0, true, 0, 0, 0, 0, chips),
                    new Progress(0, nComb, 1, 1, boards)
            );
        }

        boolean maxLevel = parseBoolean(it.next());
        boolean matchColor = parseBoolean(it.next());
        boolean rotation = parseBoolean(it.next());

        boolean symmetry = false;
        if (v.isCurrent(6, 9, 0)) {
            symmetry = parseBoolean(it.next());
        }

        int markMin = Integer.valueOf(it.next());
        int markMax = Integer.valueOf(it.next());
        int markType = Integer.valueOf(it.next());
        int sortType = Integer.valueOf(it.next());

        Stat stat = parseStat(it.next());
        Stat pt = parseStat(it.next());

        int nComb = Integer.valueOf(it.next());
        int progress = Integer.valueOf(it.next());
        int progMax = Integer.valueOf(it.next());
        int tag = Integer.valueOf(it.next());

        List<Chip> chips = parseProgress_chips(v, it, invChips);
        if (maxLevel) {
            chips.forEach((c) -> c.setMaxLevel());
        }

        List<Board> boards = parseProgress_boards(name, star, stat, chips, it);

        return new ProgressFile(
                new CalcSetting(name, star, maxLevel, rotation, symmetry, stat, pt),
                new CalcExtraSetting(calcMode, tag, matchColor, markMin, markMax, markType, sortType, chips),
                new Progress(sortType, nComb, progress, progMax, boards)
        );
    }

    private static List<Chip> parseProgress_chips(Version3 v, Iterator<String> it, List<Chip> invChips) {
        int nChip = Integer.valueOf(it.next());
        List<Chip> chips = new ArrayList<>();
        Set<Tag> tags = new HashSet<>();
        for (int i = 0; i < nChip; i++) {
            chips.add(parseChip(v, it.next(), tags));
        }
        loadProgress_adjustInits(chips, invChips);
        return chips;
    }

    private static void loadProgress_adjustInits(Collection<Chip> chips, List<Chip> invChips) {
        chips.forEach((c) -> {
            for (int i = 0; i < invChips.size(); i++) {
                Chip ic = invChips.get(i);
                if (c.equals(ic)) {
                    c.setInitRotation(ic.getInitRotation());
                    c.setInitLevel(ic.getInitLevel());
                    break;
                }
            }
        });
    }

    private static List<Board> parseProgress_boards(String name, int star, Stat stat, List<Chip> chips, Iterator<String> it) {
        List<Board> boards = new ArrayList<>();
        while (it.hasNext()) {
            int n = Integer.valueOf(it.next());
            List<Chip> bChips = new ArrayList<>();
            List<Point> bLocs = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                String[] split = it.next().split(",");
                int i = Integer.valueOf(split[0]);
                int r = Integer.valueOf(split[1]);
                Point l = parsePoint(split[2]);

                Chip c = new Chip(chips.get(i));
                c.setRotation(r);

                bChips.add(c);
                bLocs.add(l);
            }
            Board board = new Board(name, star, stat, bChips, bLocs);
            boards.add(board);
        }
        return boards;
    }
    // </editor-fold>
}
