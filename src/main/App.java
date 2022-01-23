package main;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import main.ui.resource.AppColor;
import main.ui.resource.AppText;
import main.util.IO;
import main.util.Version3;

public class App {

    public static final String NAME_KR = "소녀전선 칩셋 조합기";
    public static final String NAME_EN = "Girls' Frontline HOC Chip Calculator";
    public static final Version3 VERSION = new Version3(8, 1, 1);
    private static final String RESOURCE_PATH = "/resources/";

    public Color orange() {
        return AppColor.Three.orange(setting.colorPreset);
    }

    public Color green() {
        return AppColor.Three.green(setting.colorPreset);
    }

    public Color blue() {
        return AppColor.Three.blue(setting.colorPreset);
    }

    public Color[] colors() {
        return AppColor.Index.colors(setting.colorPreset);
    }
    // </editor-fold>

    private static final Logger LOGGER = Logger.getLogger("gfchipcalc");

    public final MainFrame mf;
    public final Setting setting = IO.loadSettings();
    public final Filter filter = new Filter();
    private final AppText text = new AppText();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Test.test();
        App app = new App();
        app.start();
    }

    private App() {
        try {
            String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            if (!systemLookAndFeel.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
                UIManager.setLookAndFeel(systemLookAndFeel);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex1) {
                log(ex1);
            }
        }
        mf = new MainFrame(this);
    }

    private void start() {
        mf.setVisible(true);
        mf.afterLoad();
    }

    public String getText(String key) {
        return text.getText(setting.locale, key);
    }

    public String getText(String key, String... replaces) {
        return text.getText(setting.locale, key, replaces);
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
                Logger.getLogger("GFChipCalc").log(Level.SEVERE, null, ex1);
            }
        }
        LOGGER.log(Level.SEVERE, null, ex);
    }

    public static URL getResource(String path) {
        return App.class.getResource(RESOURCE_PATH + path);
    }

    public static InputStream getResourceAsStream(String path) {
        return App.class.getResourceAsStream(RESOURCE_PATH + path);
    }
}
