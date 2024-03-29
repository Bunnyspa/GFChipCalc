package main.ui.dialog;

import java.awt.Dimension;
import java.util.List;
import javax.swing.JDialog;
import main.App;
import main.puzzle.Chip;
import main.puzzle.Tag;
import main.ui.component.TagPanel;
import main.ui.resource.AppText;
import main.util.Fn;

public class TagDialog extends JDialog {

    private final App app;
    private final TagPanel tp;
    private final List<Chip> chips;

    public static TagDialog getInstance(App app, List<Chip> chips) {
        return new TagDialog(app, chips);
    }

    private TagDialog(App app, List<Chip> chips) {
        initComponents();
        this.app = app;
        this.chips = chips;
        this.tp = new TagPanel(app, this, (t) -> allChipContainsTag(chips, t), true);
        init();
    }

    private void init() {
        String tag = app.getText(AppText.TAG_TITLE);
        String count = app.getText(AppText.UNIT_COUNT, String.valueOf(chips.size()));
        this.setTitle(chips.size() == 1 ? tag : tag + " - " + count);
        closeButton.setText(app.getText(AppText.ACTION_CLOSE));
        descLabel.setText(app.getText(AppText.TAG_DESC));

        tagPanel.add(tp);

        addListeners();

        Dimension d = getPreferredSize();
        d.height = app.mf.getPreferredDialogSize().height;
        setPreferredSize(d);
        pack();
    }

    private static boolean allChipContainsTag(List<Chip> chips, Tag t) {
        return chips.stream().allMatch((chip) -> (chip.containsTag(t)));
    }

    private void addListeners() {
        Fn.addEscDisposeListener(this);
        tp.addTableModelListener((e) -> applyTag());
    }

    private void applyTag() {
        int i = tp.getSelectedRow();
        if (0 <= i) {
            chips.forEach((c) -> c.setTag(tp.getTag(i), tp.isChecked(i)));
            app.mf.invStat_enableSave();
            app.mf.invStat_loadStats();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        descLabel = new javax.swing.JLabel();
        tagPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("태그");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        closeButton.setText("닫기");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        descLabel.setText("더이상 사용하지 않는 태그는 자동으로 삭제됩니다.");

        tagPanel.setPreferredSize(new java.awt.Dimension(250, 250));
        tagPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tagPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeButton)
                    .addComponent(descLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {closeButton, descLabel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel descLabel;
    private javax.swing.JPanel tagPanel;
    // End of variables declaration//GEN-END:variables

}
