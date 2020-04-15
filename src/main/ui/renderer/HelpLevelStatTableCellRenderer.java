package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import main.App;
import main.ui.help.HelpChipDialog;
import main.util.Fn;
import main.util.Ref;

/**
 *
 * @author Bunnyspa
 */
public class HelpLevelStatTableCellRenderer extends DefaultTableCellRenderer {

    private final App app;

    private final int rowHeaderNum, colHeaderNum;

    private final Ref<Boolean> toggleType;

    public HelpLevelStatTableCellRenderer(App app, int rowHeaderNum, int colHeaderNum, Ref<Boolean> toggleType) {
        super();
        this.app = app;
        this.rowHeaderNum = rowHeaderNum;
        this.colHeaderNum = colHeaderNum;
        this.toggleType = toggleType;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer cr = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cr.setHorizontalAlignment(CENTER);

        if (row < colHeaderNum || (column < rowHeaderNum)) {
            cr.setBackground(SystemColor.control);
        } else if (toggleType.v == HelpChipDialog.STAT) {
            cr.setBackground(Color.WHITE);
        } else if (Integer.valueOf((String) value) == 0) {
            cr.setBackground(app.green());
        } else {
            cr.setBackground(Fn.percColor(app.orange(), Color.WHITE, Integer.valueOf((String) value), -10, 0));
        }
        return cr;
    }
}
