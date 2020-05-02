package main.resource;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import main.App;
import main.puzzle.preset.PuzzlePreset;
import main.util.IO;

/**
 *
 * @author Bunnyspa
 */
public class Resources {

    private static final String RESOURCE_PATH = "/resources/";

    public static final Image FAVICON = getImage("favicon.png");
    public static final ImageIcon BANNER = getIcon("banner.png");

    public static final ImageIcon DONATION = getIcon("donation.png");
    public static final ImageIcon PAYPALQR = getIcon("paypalqr.png");
    public static final ImageIcon PAYPAL = getIcon("paypal.png");

    public static final Image CHIP_MARKED = getImage("chip_marked.png");
    public static final Image CHIP_ROTATED = getImage("chip_rotated.png");
    public static final Image CHIP_EQUIPPED = getImage("chip_equipped.png");

    public static final Icon UI_INFO = UIManager.getIcon("OptionPane.informationIcon");
    public static final Icon UI_WARNING = UIManager.getIcon("OptionPane.warningIcon");

    public static final ImageIcon MP448 = getIcon("MP448.png");

    public static final ImageIcon FONT = getIcon("font.png");
    public static final ImageIcon QUESTION = getIcon("question.png");

    public static final ImageIcon PICTURE = getIcon("picture.png");
    public static final ImageIcon PHONE = getIcon("phone.png");

    public static final ImageIcon ASCNEDING = getIcon("ascending.png");
    public static final ImageIcon DESCENDING = getIcon("descending.png");

    public static final ImageIcon ROTATE_LEFT = getIcon("rotate_left.png");
    public static final ImageIcon ROTATE_RIGHT = getIcon("rotate_right.png");

    public static final ImageIcon PANEL_OPEN = getIcon("panel_open.png");
    public static final ImageIcon PANEL_CLOSE = getIcon("panel_close.png");
    public static final ImageIcon ADD = getIcon("add.png");

    public static final ImageIcon DMG = getIcon("dmg.png");
    public static final ImageIcon BRK = getIcon("brk.png");
    public static final ImageIcon HIT = getIcon("hit.png");
    public static final ImageIcon RLD = getIcon("rld.png");
    public static final ImageIcon[] STATS = new ImageIcon[]{DMG, BRK, HIT, RLD};

    public static final ImageIcon SAVE = getIcon("save.png");
    public static final ImageIcon NEW = getIcon("new.png");
    public static final ImageIcon SAVEAS = getIcon("saveas.png");
    public static final ImageIcon OPEN = getIcon("open.png");

    public static final ImageIcon DISPLAY_PT = getIcon("display_pt.png");
    public static final ImageIcon DISPLAY_STAT = getIcon("display_stat.png");

    public static final ImageIcon FILTER = getIcon("filter.png");
    public static final ImageIcon FILTER_APPLY = getIcon("filter_apply.png");

    public static final ImageIcon DELETE = getIcon("delete.png");

    public static final ImageIcon SETTING = getIcon("setting.png");
    public static final ImageIcon SETTING_PRESET = getIcon("setting_preset.png");
    public static final ImageIcon SETTING_PT = getIcon("setting_pt.png");
    public static final ImageIcon SETTING_STAT = getIcon("setting_stat.png");

    public static final ImageIcon COMB_START = getIcon("combine_start.png");
    public static final ImageIcon COMB_PAUSE = getIcon("combine_pause.png");
    public static final ImageIcon COMB_STOP = getIcon("combine_stop.png");

    public static final ImageIcon LOADING = getIcon("loading.gif");
    public static final ImageIcon PAUSED = getIcon("paused.png");

    public static final ImageIcon TICKET = getIcon("ticket.png");

    public static final ImageIcon CHECKED = getIcon("checked.png");
    public static final ImageIcon UNCHECKED = getIcon("unchecked.png");

    public static final ImageIcon HELP_PROXY = getIcon("help/proxy.jpg");

    private static final BufferedImage IP_0 = getImage("imgproc/0.png");
    private static final BufferedImage IP_1 = getImage("imgproc/1.png");
    private static final BufferedImage IP_2 = getImage("imgproc/2.png");
    private static final BufferedImage IP_3 = getImage("imgproc/3.png");
    private static final BufferedImage IP_4 = getImage("imgproc/4.png");
    private static final BufferedImage IP_5 = getImage("imgproc/5.png");
    private static final BufferedImage IP_6 = getImage("imgproc/6.png");
    private static final BufferedImage IP_7 = getImage("imgproc/7.png");
    private static final BufferedImage IP_8 = getImage("imgproc/8.png");
    private static final BufferedImage IP_9 = getImage("imgproc/9.png");
    public static final BufferedImage[] IP_DIGITS = new BufferedImage[]{
        IP_0, IP_1, IP_2, IP_3, IP_4,
        IP_5, IP_6, IP_7, IP_8, IP_9
    };

    public static final BufferedImage IP_DMG = getImage("imgproc/dmg.png");
    public static final BufferedImage IP_BRK = getImage("imgproc/brk.png");
    public static final BufferedImage IP_HIT = getImage("imgproc/hit.png");
    public static final BufferedImage IP_RLD = getImage("imgproc/rld.png");

    private static ImageIcon getIcon(String s) {
        return new ImageIcon(App.class.getResource(RESOURCE_PATH + s));
    }

    private static BufferedImage getImage(String s) {
        try {
            return ImageIO.read(App.class.getResource(RESOURCE_PATH + s));
        } catch (IOException ex) {
            App.log(ex);
        }
        return null;
    }

    public static ImageIcon getScaledIcon(Icon icon, int width, int height) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public static final Font FONT_DIGIT = getFont("mohave/Mohave-Light.otf");

    public static Font getDefaultFont() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    private static Font getFont(String s) {
        try {
            InputStream is = App.class.getResourceAsStream(RESOURCE_PATH + "font/" + s);
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14.0f);
        } catch (FontFormatException | IOException ex) {
        }
        return null;
    }

    public static List<PuzzlePreset> loadPresets(String name, int star, String type, boolean isPartial) {
        List<PuzzlePreset> out = new ArrayList<>();
        String fileName = name + "_" + type + "_" + IO.data(isPartial) + "_" + star;
        URL url = App.class.getResource(RESOURCE_PATH + "preset/" + fileName + ".dat");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return IO.loadPresets_location(name, star, br);
        } catch (Exception ex) {
        }
        return out;
    }

    public static Map<Locale, Properties> readInternalProp() {
        Map<Locale, Properties> out = new HashMap<>();

        for (Locale locale : Language.LOCALES) {
            URL url = App.class.getResource(RESOURCE_PATH + "language/" + locale.toLanguageTag() + ".properties");
            try (Reader r = new InputStreamReader(url.openStream(), IO.UTF8)) {
                Properties props = new Properties();
                props.load(r);
                out.put(locale, props);
            } catch (Exception ex) {
            }
        }

        return out;
    }
}
