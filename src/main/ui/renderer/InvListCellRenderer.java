package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.assembly.ChipFreq;
import main.ui.resource.GFLGraphics;
import main.util.Ref;

/**
 *
 * @author Bunnyspa
 */
public class InvListCellRenderer extends DefaultListCellRenderer {

    private final App app;
    private final JTabbedPane combChipListTabbedPane;
    private final JList combList, combChipList, combChipFreqList;
    private final Ref<Boolean> blink;

    public InvListCellRenderer(App app, JList invList, JList combList, JTabbedPane combChipListTabbedPane, JList combChipList, JList combChipFreqList, Ref<Boolean> blink) {
        super();
        this.app = app;
        this.combChipListTabbedPane = combChipListTabbedPane;
        this.combList = combList;
        this.combChipList = combChipList;
        this.combChipFreqList = combChipFreqList;
        this.blink = blink;
    }

    public void refresh() {

    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer cr = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        cr.setHorizontalAlignment(CENTER);
        cr.setText("");

        Chip c = (Chip) value;

        boolean combSelected = false;
        if (!combList.isSelectionEmpty()) {
            for (String id : ((Board) combList.getSelectedValue()).getChipIDs()) {
                if (c.getID().equals(id)) {
                    combSelected = true;
                    break;
                }
            }
        }

        boolean resultSelected = false;
        if (combChipListTabbedPane.getSelectedIndex() == 0) {
            if (!combChipList.isSelectionEmpty()) {
                Chip chip = (Chip) combChipList.getSelectedValue();
                if (c.equals(chip)) {
                    resultSelected = true;
                }
            }
        } else {
            if (!combChipFreqList.isSelectionEmpty()) {
                ChipFreq cf = (ChipFreq) combChipFreqList.getSelectedValue();
                if (c.equals(cf.chip)) {
                    resultSelected = true;
                }
            }
        }

        cr.setIcon(GFLGraphics.chip(app, c));
        boolean selBlink = isSelected && blink.v;
        Color selColor = app.orange();
        if (resultSelected) {
            cr.setBackground(selBlink ? selColor : Color.LIGHT_GRAY);
        } else if (combSelected) {
            cr.setBackground(selBlink ? selColor : app.blue());
        } else if (!c.isPtValid()) {
            cr.setBackground(selBlink ? selColor : Color.PINK);
        } else {
            cr.setBackground(isSelected ? selColor : Color.WHITE);
        }

        cr.setToolTipText(c.getID().toString()); //DEBUG
        return cr;
    }
}
