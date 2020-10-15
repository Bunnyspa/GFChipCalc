package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import main.App;
import main.puzzle.assembly.ChipFreq;
import main.ui.resource.AppImage;
import main.util.Fn;
import main.util.Ref;

/**
 *
 * @author Bunnyspa
 */
public class ChipFreqListCellRenderer extends DefaultListCellRenderer {

    private final App app;
    private final Ref<Boolean> blink;

    public ChipFreqListCellRenderer(App app, Ref<Boolean> blink) {
        super();
        this.app = app;
        this.blink = blink;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer cr = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        cr.setHorizontalAlignment(CENTER);
        cr.setText("");

        ChipFreq cf = (ChipFreq) value;
        if (value != null) {
            cr.setIcon(AppImage.Chip.get(app, cf.chip));
            cr.setBackground(isSelected && blink.v ? Color.LIGHT_GRAY : Fn.percColor(app.orange(), app.green(), app.blue(), cf.freq, 0.0, 1.0));
        } else {
            cr.setIcon(null);
            cr.setBackground(Color.WHITE);
        }
        return cr;
    }
}
