package main;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import main.setting.Filter;
import main.setting.Setting;
import main.ui.MainFrame;
import main.ui.resource.GFLTexts;
import main.util.IO;
import main.util.Version3;

/**
 *
 * @author Bunnyspa
 */
public class App {

    public static final String NAME_KR = "소녀전선 칩셋 조합기";
    public static final String NAME_EN = "Girls' Frontline HOC Chip Calculator";
    public static final Version3 VERSION = new Version3(7, 2, 2);

    // <editor-fold defaultstate="collapsed" desc="Colors">
    private static final Color[] COLORS_DEFAULT = { // <editor-fold defaultstate="collapsed">
        new Color(15079755),
        new Color(3978315),
        new Color(16769305),
        new Color(4416472),
        new Color(16089649),
        new Color(9510580),
        new Color(4649200),
        new Color(15741670),
        new Color(12383756),
        new Color(16432830),
        new Color(32896),
        new Color(15122175),
        new Color(10117924),
        new Color(16775880),
        new Color(8388608),
        new Color(11206595),
        new Color(8421376),
        new Color(16767153),
        new Color(117),
        new Color(8421504)
    }; // </editor-fold>
    private static final Color ORANGE = new Color(14928556);
    private static final Color GREEN = new Color(12901541);
    private static final Color BLUE = new Color(10335956);

    private static final Color[] COLORS_CB = { // <editor-fold defaultstate="collapsed">
        new Color(15113984),
        new Color(5682409),
        new Color(40563),
        new Color(15787074),
        new Color(29362),
        new Color(13983232),
        new Color(13400487)
    }; // </editor-fold>
    private static final Color ORANGE_CB = COLORS_CB[0];
    private static final Color GREEN_CB = COLORS_CB[2];
    private static final Color BLUE_CB = COLORS_CB[1];

    public Color orange(int alt) {
        switch (alt % Setting.NUM_COLOR) {
            case 1:
                return ORANGE_CB;
            default:
                return ORANGE;
        }
    }

    public Color green(int alt) {
        switch (alt % Setting.NUM_COLOR) {
            case 1:
                return GREEN_CB;
            default:
                return GREEN;
        }
    }

    public Color blue(int alt) {
        switch (alt % Setting.NUM_COLOR) {
            case 1:
                return BLUE_CB;
            default:
                return BLUE;
        }
    }

    public Color[] colors(int alt) {
        switch (alt % Setting.NUM_COLOR) {
            case 1:
                return COLORS_CB;
            default:
                return COLORS_DEFAULT;
        }
    }

    public Color orange() {
        return orange(setting.colorPreset);
    }

    public Color green() {
        return green(setting.colorPreset);
    }

    public Color blue() {
        return blue(setting.colorPreset);
    }

    public Color[] colors() {
        return colors(setting.colorPreset);
    }
    // </editor-fold>

    private static final Logger LOGGER = Logger.getLogger("gfchipcalc");

    public final MainFrame mf;
    public final Setting setting = IO.loadSettings();
    public final Filter filter = new Filter();
    private final GFLTexts language = new GFLTexts();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Test.test();
        App app = new App();
    }

    private App() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex1) {
                log(ex1);
            }
        }
        mf = new MainFrame(this);
        mf.setVisible(true);
        mf.afterLoad();
    }

    public String getText(String key) {
        return language.getText(setting.locale, key);
    }

    public String getText(String key, String... replaces) {
        return language.getText(setting.locale, key, replaces);
    }

    public String getText(String key, int... replaces) {
        String[] repStrs = new String[replaces.length];
        for (int i = 0; i < replaces.length; i++) {
            repStrs[i] = String.valueOf(replaces[i]);
        }
        return getText(key, repStrs);
    }

    public static void log(Exception ex) {
        if (LOGGER.getHandlers().length == 0) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss");
                FileHandler fh = new FileHandler("Error_" + formatter.format(new Date()) + ".log");
                fh.setFormatter(new SimpleFormatter());
                LOGGER.addHandler(fh);
            } catch (IOException | SecurityException ex1) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        LOGGER.log(Level.SEVERE, null, ex);
    }
}
