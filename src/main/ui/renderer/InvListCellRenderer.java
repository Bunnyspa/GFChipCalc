package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.Timer;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;

/**
 *
 * @author Bunnyspa
 */
public class InvListCellRenderer extends DefaultListCellRenderer {

    private final App app;
    private final JList combList;
    private final JList combChipList;
    private boolean blink;
    private final Timer invListBlinkTimer;

    public InvListCellRenderer(App app, JList invList, JList combList, JList combChipList) {
        super();
        this.app = app;
        this.combList = combList;
        this.combChipList = combChipList;
        invListBlinkTimer = new Timer(500, (e) -> {
            blink = !blink;
            if (invList != null) {
                invList.repaint();
            }
        });
        invListBlinkTimer.start();
    }

    public void stopTimer() {
        invListBlinkTimer.stop();
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
        if (!combChipList.isSelectionEmpty()) {
            Chip chip = (Chip) combChipList.getSelectedValue();
            if (c.equals(chip)) {
                resultSelected = true;
            }
        }

        cr.setIcon(c.getImage(app));
        boolean selBlink = isSelected && blink;
        if (resultSelected) {
            cr.setBackground(selBlink ? app.orange() : app.green());
        } else if (combSelected) {
            cr.setBackground(selBlink ? app.orange() : app.blue());
        } else if (!c.isPtValid()) {
            cr.setBackground(selBlink ? app.orange() : Color.PINK);
        } else {
            cr.setBackground(isSelected ? app.orange() : Color.WHITE);
        }

        //cr.setToolTipText(c.getID().toString()); //DEBUG
        return cr;
    }
}
