package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import main.App;
import main.puzzle.Chip;
import main.puzzle.Tag;

public class TagTableCellRenderer extends DefaultTableCellRenderer {

    private final App app;
    private final boolean checkBox;

    public TagTableCellRenderer(App app, boolean checkBox) {
        this.app = app;
        this.checkBox = checkBox;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer cr = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Tag tag = (Tag) value;
        cr.setText(tag.getName());
        cr.setForeground(tag.getColor());
        if (!checkBox) {
            List<Chip> chips = app.mf.inv_getFilteredChips();
            if (chips.stream().allMatch((t) ->  t.containsTag(tag))) {
                cr.setBackground(app.green());
            }else{
                cr.setBackground(Color.WHITE);
            }
        }
        return cr;
    }

}
