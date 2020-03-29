package main.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
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
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.FStat;
import main.puzzle.PuzzleMatrix;
import main.puzzle.Stat;
import main.puzzle.Tag;
import main.puzzle.assembly.Progress;
import main.puzzle.preset.PuzzlePreset;
import main.resource.Language;
import main.setting.BoardSetting;
import main.setting.Setting;

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

    private static final String URL_GITHUB_LATEST = "https://github.com/Bunnyspa/GFChipCalc/releases/latest";

    private static int pre420rotation(String name) {
        if (name.matches("4I|5[IZF]|5Zm|6[ACDIR]")) {
            return 1;
        }
        if (name.matches("4T|5[PT]|5Pm|6Y")) {
            return 2;
        }
        if (name.matches("4Lm|5[CW]|5Fm|6T")) {
            return 3;
        }
        return 0;
    }

    public static List<Chip> loadInventory(String fileName) {
        List<Chip> chips = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            Iterator<String> bri = br.lines().iterator();
            if (bri.hasNext()) {
                String s = bri.next();
                Version v = new Version(s);
                if (v.isCurrent(5, 3, 0)) {
                    Set<Tag> tags = new HashSet<>();
                    bri.forEachRemaining((l) -> chips.add(parseChip(l, tags)));
                } else if (v.isCurrent(4, 0, 0)) {
                    // 4.0.0+
                    while (bri.hasNext()) {
                        Chip c = new Chip(v, bri.next().split(";"), Chip.INVENTORY);
                        // 4.0.0 - 4.1.x
                        if (!v.isCurrent(4, 2, 0)) {
                            c.initRotate(pre420rotation(c.getName()));
                        }
                        chips.add(c);
                    }
                } else {
                    // 1.0.0 - 3.0.0
                    chips.add(new Chip(new Version(), s.split(","), Chip.INVENTORY));
                    while (bri.hasNext()) {
                        chips.add(new Chip(new Version(), bri.next().split(","), Chip.INVENTORY));
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
        Version v = new Version(s);
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
                    List<String> names = new ArrayList<>(nChip);
                    for (int i = 0; i < nChip; i++) {
                        String[] chipInfo = bri.next().split(";");
                        Chip c = new Chip(v, chipInfo, Chip.COMBINATION);
                        chips.add(c);
                        if (!v.isCurrent(4, 2, 0)) {
                            int r = c.getInitRotation();
                            r += pre420rotation(c.getName());
                            c.setInitRotation(r);
                        }
                        names.add(c.getName());
                    }
                    chips.forEach((c) -> c.setMaxLevel());

                    // Rotations
                    String[] rotStrs = bri.next().split(",");
                    for (int i = 0; i < nChip; i++) {
                        int r = Integer.valueOf(rotStrs[i]);
                        if (!v.isCurrent(4, 2, 0)) {
                            r += pre420rotation(names.get(i));
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
                    Chip c = new Chip(new Version(), bri.next().split(","), Chip.COMBINATION);
                    c.rotate(pre420rotation(c.getName()));
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
    public static Progress loadProgress(String fileName, List<Chip> invChips) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            Iterator<String> bri = br.lines().iterator();
            if (bri.hasNext()) {
                String s = bri.next();
                Version v = new Version(s);
                if (v.isCurrent(5, 3, 0)) {
                    return parseProgress(v, bri, invChips);
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
                    return new Progress(Progress.FINISHED, name, star,
                            false, false, false,
                            0, 0, 0, 0,
                            new FStat(stat), null,
                            -1, 1, 1,
                            new ArrayList<>(chipSet), boards, 0);
                }
            }
        } catch (Exception ex) {
            App.log(ex);
        }
        return null;
    }

    public static void saveProgress(String fileName, Progress prog) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(App.VERSION.toData());
            bw.newLine();
            bw.write(prog.toData());
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
    private static List<PuzzlePreset> loadExternalPresets_location(String name, int star, String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader("private/preset/" + fileName))) {
            return loadPresets_location(name, star, br);
        } catch (Exception ex) {
        }
        return new ArrayList<>();
    }

    private static List<PuzzlePreset> loadExternalPresets_placement(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader("private/preset/" + fileName))) {
            return loadPresets_placement(br);
        } catch (Exception ex) {
        }
        return new ArrayList<>();
    }

    public static List<PuzzlePreset> loadPresets_location(String name, int star, BufferedReader br) {
        List<PuzzlePreset> out = new ArrayList<>();
        Iterator<String> bri = br.lines().iterator();
        while (bri.hasNext()) {
            String[] line = bri.next().split(";");
            // Names
            List<String> chipNames = Arrays.asList(line[0].split(","));
            // Rotations
            List<Integer> rotations = new ArrayList<>(chipNames.size());
            Stream.of(line[1].split(",")).forEach((r) -> rotations.add(Integer.valueOf(r)));
            // Locations
            List<Point> locations = new ArrayList<>(chipNames.size());
            for (String pStr : line[2].split(",")) {
                locations.add(parsePoint(pStr));
            }
            PuzzlePreset pp = new PuzzlePreset(name, star, chipNames, rotations, locations);
            out.add(pp);
        }
        return out;
    }

    private static List<PuzzlePreset> loadPresets_placement(BufferedReader br) {
        List<PuzzlePreset> out = new ArrayList<>();
        Iterator<String> bri = br.lines().iterator();
        while (bri.hasNext()) {
            Integer[][] data = new Integer[Board.HEIGHT][Board.WIDTH];
            for (int r = 0; r < Board.HEIGHT; r++) {
                String line = bri.next();
                for (int c = 0; c < Board.WIDTH; c++) {
                    String ch = line.substring(c, c + 1);
                    int d = Board.UNUSED;
                    if (!" ".equals(ch) && !"X".equals(ch)) {
                        d = Integer.parseInt(ch);
                    }
                    data[r][c] = d;
                }
            }
            if (bri.hasNext()) {
                bri.next();
            }
            PuzzleMatrix<Integer> pm = new PuzzleMatrix<>(data);
            PuzzlePreset pp = new PuzzlePreset(pm);
            out.add(pp);
        }
        return out;
    }

    public static void convToLocation(String fileName) { // DEV
        List<PuzzlePreset> presets = loadExternalPresets_placement(fileName);
        List<String> presetLines = new ArrayList<>();
        presets.forEach((p) -> presetLines.add(p.toData()));
        sortPresets(presetLines);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("private/preset/location_" + fileName))) {
            String prev = "";
            for (String p : presetLines) {
                String now = p.split(";")[0];
                if (now.equals(prev)) {
                    System.out.println(now);
                }
                prev = now;
                bw.write(p);
                bw.newLine();
            }
        } catch (Exception ex) {
            App.log(ex);
        }
    }

    public static void convToPlacement(String fileName, String name, int star, int nCol) {
        List<PuzzlePreset> presets = loadExternalPresets_location(name, star, fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("private/preset/placement_" + fileName))) {
            List<String[]> lg = new ArrayList<>(nCol);
            int i = 0;
            for (PuzzlePreset p : presets) {
                PuzzleMatrix<Integer> matrix = p.getMatrix();
                String[] lines = matrix.toString().replace(" ", "\t").split(System.lineSeparator());
                lg.add(lines);
                i++;
                if (i == nCol) {
                    convToPlacement_write(bw, lg, i);
                    //
                    i = 0;
                    lg.clear();
                }
            }
            if (0 < i) {
                convToPlacement_write(bw, lg, i);
            }
        } catch (Exception ex) {
            App.log(ex);
        }
    }

    private static void convToPlacement_write(BufferedWriter bw, List<String[]> lg, int nCol) throws IOException {
        for (int r = 0; r < Board.HEIGHT; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < nCol; c++) {
                sb.append(lg.get(c)[r]).append("\t");
            }
            bw.write(sb.toString().trim());
            bw.newLine();
        }
        bw.newLine();
    }

    public static void sortPresets() {
        File folder_sorted = new File("private/preset_sorted");
        if (!folder_sorted.exists()) {
            folder_sorted.mkdir();
        }

        File folder = new File("private/preset");
        File[] listOfFiles = folder.listFiles();
        String[] fileNames = Stream.of(listOfFiles).map((f) -> f.getName()).toArray(String[]::new);
        for (String fileName : fileNames) {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("private/preset/" + fileName))) {
                br.lines().forEach((line) -> lines.add(line));
            } catch (IOException ex) {
            }

            sortPresets(lines);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter("private/preset_sorted/" + fileName))) {
                for (String l : lines) {
                    bw.write(l + System.lineSeparator());
                }
            } catch (Exception e) {
            }
        }
    }

    private static void sortPresets(List<String> lines) {
        // Sort line
        for (int i = 0; i < lines.size(); i++) {
            // Load
            String line = lines.get(i);
            String[] split = line.split(";");
            String[] names = split[0].split(",");
            String[] rotations = split[1].split(",");
            String[] locations = split[2].split(",");
            List<NRL> nrls = new ArrayList<>();
            for (int j = 0; j < names.length; j++) {
                nrls.add(new NRL(names[j], rotations[j], locations[j]));
            }
            // Sort
            nrls.sort((o1, o2) -> Chip.compareName(o1.name, o2.name));
            List<String> newNames = new ArrayList<>();
            List<String> newRotations = new ArrayList<>();
            List<String> newLocations = new ArrayList<>();
            nrls.forEach((nrl) -> {
                newNames.add(nrl.name);
                newRotations.add(nrl.rotation);
                newLocations.add(nrl.location);
            });
            // Set
            lines.set(i,
                    String.join(",", newNames) + ";"
                    + String.join(",", newRotations) + ";"
                    + String.join(",", newLocations)
            );
        }
        // Sort lines
        lines.sort((o1, o2) -> {
            String[] c1 = o1.split(";")[0].split(",");
            String[] c2 = o2.split(";")[0].split(",");
            int i = 0;
            while (i < Math.min(c1.length, c2.length)) {
                int comp = Chip.compareName(c1[i], c2[i]);
                if (comp != 0) {
                    return comp;
                }
                i++;
            }
            return 0;
        });
    }

    private static class NRL {

        final String name;
        final String rotation;
        final String location;

        public NRL(String name, String rotation, String location) {
            this.name = name;
            this.rotation = rotation;
            this.location = location;
        }
    }
    // </editor-fold>

    private static String getNameWOExt(String s) {
        int lastIndex = s.lastIndexOf('.');
        return s.substring(0, lastIndex);
    }

    // <editor-fold defaultstate="collapsed" desc="Locales and Properties">
    public static List<Locale> getInternalLocales() {
        List<Locale> locales = new ArrayList<>();
        locales.addAll(Arrays.asList(Language.LOCALES));
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
                JOptionPane.showMessageDialog(component, app.getText(Language.DISPLAY_EXPORT_FAIL_BODY), app.getText(Language.DISPLAY_EXPORT_FAIL_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        }

        try {
            for (Locale locale : getInternalLocales()) {
                String filePath = PATH_EX_LANG + "/" + locale.getLanguage() + "_" + locale.getCountry() + ".properties";
                String fileContent = Language.getFileContent(locale);
                write(filePath, fileContent);
            }
            JOptionPane.showMessageDialog(component, app.getText(Language.DISPLAY_EXPORT_DONE_BODY, PATH_EX_LANG), app.getText(Language.DISPLAY_EXPORT_DONE_TITLE), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(component, Language.DISPLAY_EXPORT_FAIL_BODY, Language.DISPLAY_EXPORT_FAIL_TITLE, JOptionPane.ERROR);
        }
    }

    public static Properties getProp(Locale locale) {
        String lang = locale.getLanguage() + "_" + locale.getCountry();
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

    private static void write(String filePath, String fileContent) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), UTF8))) {
            w.write(fileContent);
        }
    }

    private static List<String> read(String filePath) throws IOException {
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
        try {
            URLConnection con = new URL(URL_GITHUB_LATEST).openConnection();
            con.connect();
            String latest = App.VERSION.toData();
            try (InputStream is = con.getInputStream()) {
                String redirected = con.getURL().toString();
                latest = redirected.substring(redirected.lastIndexOf("/") + 2);
            }

            if (!App.VERSION.isCurrent(latest)) {
                int retval = JOptionPane.showConfirmDialog(app.mf,
                        app.getText(Language.NEWVER_CONFIRM_BODY, latest),
                        app.getText(Language.NEWVER_CONFIRM_TITLE),
                        JOptionPane.YES_NO_OPTION);
                if (retval == JOptionPane.YES_OPTION) {
                    if (!runProgram()) {
                        openWeb(app, app.mf, URL_GITHUB_LATEST);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private static boolean runProgram() {
        String path = new File("").getAbsolutePath();
        try {
            String exePath = path + "\\GFChipCalc-Update.jar";
            if (new File(exePath).exists()) {
                ProcessBuilder process = new ProcessBuilder("java", "-jar", exePath);
                process.directory(new File(path + "\\"));
                process.start();
                System.exit(0);
            }
        } catch (IOException ex) {
        }
        return false;
    }

    public static void openWeb(App app, Component c, String link) {
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(c, app.getText(Language.NEWVER_ERROR_BODY), app.getText(Language.NEWVER_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
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
        Stat stat = new Stat();
        stat.dmg = d.length > 0 ? Integer.valueOf(d[0]) : 0;
        stat.brk = d.length > 1 ? Integer.valueOf(d[1]) : 0;
        stat.hit = d.length > 2 ? Integer.valueOf(d[2]) : 0;
        stat.rld = d.length > 3 ? Integer.valueOf(d[3]) : 0;
        return stat;
    }

    public static FStat parseFStat(String s) {
        return new FStat(parseStat(s));
    }

    //========== Board Setting ==========//
    public static BoardSetting parseBS(List<String> data) {
        BoardSetting out = new BoardSetting();
        data.stream().map((line) -> line.split(";")).forEachOrdered((parts) -> {
            String name = parts[0];
            if (Arrays.asList(Board.NAMES).contains(name)) {
                int star = Integer.valueOf(parts[1]);
                int mode = Integer.valueOf(parts[2]);
                Stat stat = parseStat(parts[3]);
                Stat pt = parseStat(parts[4]);
                out.setMode(name, star, mode);
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
    public static Chip parseChip(String s, Set<Tag> tagPool) {
        Iterator<String> it = Arrays.asList(s.split(";")).iterator();
        String id = it.next();
        String name = it.next();
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

        return new Chip(id, name, star, color, pt, initLevel, initRotation, isMarked, tags);
    }

    //========== Progress ==========//
    public static Progress parseProgress(Version v, Iterator<String> it, List<Chip> invChips) {
        int status = Integer.valueOf(it.next());
        String name = it.next();
        int star = Integer.valueOf(it.next());

        if (!v.isCurrent(6, 5, 3) && status == Progress.FINISHED) {
            FStat stat = parseFStat(it.next());

            int nComb = -1;
            if (v.isCurrent(6, 4, 0)) {
                nComb = Integer.valueOf(it.next());
            }

            List<Chip> chips = parseProgress_chips(it, invChips);
            chips.forEach((c) -> c.setMaxLevel());

            List<Board> boards = parseProgress_boards(name, star, stat, chips, it);

            return new Progress(status, name, star,
                    true, true, true,
                    0, 0, 0, 0,
                    stat, new FStat(0),
                    nComb, 1, 1,
                    chips, boards, 0);
        }

        boolean maxLevel = parseBoolean(it.next());
        boolean matchColor = parseBoolean(it.next());
        boolean allowRotation = parseBoolean(it.next());

        int markMin = Integer.valueOf(it.next());
        int markMax = Integer.valueOf(it.next());
        int markType = Integer.valueOf(it.next());
        int sortType = Integer.valueOf(it.next());

        FStat stat = parseFStat(it.next());
        FStat pt = parseFStat(it.next());

        int nComb = Integer.valueOf(it.next());
        int progress = Integer.valueOf(it.next());
        int progMax = Integer.valueOf(it.next());
        int tag = Integer.valueOf(it.next());

        List<Chip> chips = parseProgress_chips(it, invChips);
        if (maxLevel) {
            chips.forEach((c) -> c.setMaxLevel());
        }

        List<Board> boards = parseProgress_boards(name, star, stat, chips, it);

        return new Progress(status, name, star,
                maxLevel, matchColor, allowRotation,
                markMin, markMax, markType, sortType,
                stat, pt,
                nComb, progress, progMax, chips, boards, tag
        );
    }

    private static List<Chip> parseProgress_chips(Iterator<String> it, List<Chip> invChips) {
        int nChip = Integer.valueOf(it.next());
        List<Chip> chips = new ArrayList<>();
        Set<Tag> tags = new HashSet<>();
        for (int i = 0; i < nChip; i++) {
            chips.add(parseChip(it.next(), tags));
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

    private static List<Board> parseProgress_boards(String name, int star, FStat stat, List<Chip> chips, Iterator<String> it) {
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
            Board board = new Board(name, star, stat.toStat(), bChips, bLocs);
            boards.add(board);
        }
        return boards;
    }
    // </editor-fold>
}
