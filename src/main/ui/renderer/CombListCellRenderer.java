package main.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import main.App;
import main.puzzle.Board;
import main.puzzle.assembly.ChipFreq;
import main.util.Fn;

public class CombListCellRenderer extends DefaultListCellRenderer {

    private final App app;
    private final JList combChipFreqList;

    public CombListCellRenderer(App app, JList combChipFreqList) {
        super();
        this.app = app;
        this.combChipFreqList = combChipFreqList;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer cr = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        try {
            cr.setHorizontalAlignment(CENTER);
            Board b = (Board) value;
            cr.setText(Fn.fPercStr(b.getStatPerc()));

            boolean freqChipSelected = !combChipFreqList.isSelectionEmpty();
            boolean freqChipIncluded = false;
            if (!combChipFreqList.isSelectionEmpty()) {
                String freqID = ((ChipFreq) combChipFreqList.getSelectedValue()).chip.getID();
                freqChipIncluded = b.getChipIDs().stream().anyMatch((id) -> id.equals(freqID));
            }

            DefaultListModel combList = (DefaultListModel) list.getModel();
            Board bMax = (Board) combList.firstElement();
            Board bMin = (Board) combList.lastElement();
            cr.setForeground(freqChipSelected
                    ? (isSelected ? Color.GRAY : Color.BLACK)
                    : (isSelected ? Color.WHITE : Color.BLACK));
            cr.setBackground(freqChipIncluded ? Color.LIGHT_GRAY
                    : freqChipSelected ? Color.WHITE
                            : Fn.percColor(app.orange(), app.green(), app.blue(), b.getStatPerc(), bMin.getStatPerc(), bMax.getStatPerc()));
        } catch (Exception ex) {
        }
        return cr;
    }
}
