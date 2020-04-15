package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Bunnyspa
 */
public class HelpTableCellRenderer extends DefaultTableCellRenderer {

    private final int rowHeaderNum, colHeaderNum;

    public HelpTableCellRenderer(int rowHeaderNum, int colHeaderNum) {
        this.rowHeaderNum = rowHeaderNum;
        this.colHeaderNum = colHeaderNum;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer cr = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cr.setHorizontalAlignment(CENTER);

        if (row < colHeaderNum || (column < rowHeaderNum)) {
            cr.setBackground(SystemColor.control);
        } else {
            cr.setBackground(Color.WHITE);
        }

        return cr;
    }
}
