package main.ui.renderer;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import main.App;
import main.puzzle.Board;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class CombListCellRenderer extends DefaultListCellRenderer {

    private final App app;

    public CombListCellRenderer(App app) {
        super();
        this.app = app;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer cr = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        try {
            cr.setHorizontalAlignment(CENTER);
            Board b = (Board) value;
            cr.setText(Fn.fPercStr(b.getStatPerc()));
            DefaultListModel combList = (DefaultListModel) list.getModel();
            Board lb = (Board) combList.lastElement();
            cr.setBackground(Fn.percColor(app.orange(), app.green(), app.blue(), b.getStatPerc(), lb.getStatPerc(), 1.0f));
        } catch (Exception ex) {
        }
        return cr;
    }
}
