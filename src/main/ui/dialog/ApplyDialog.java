package main.ui.dialog;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import main.App;
import main.puzzle.Chip;
import main.puzzle.Tag;
import main.ui.component.TagPanel;
import main.ui.resource.AppImage;
import main.ui.resource.AppText;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class ApplyDialog extends JDialog {

    private final App app;
    private final TagPanel tp;

    public static ApplyDialog getInstance(App app) {
        return new ApplyDialog(app);
    }

    private ApplyDialog(App app) {
        initComponents();
        this.app = app;
        this.tp = new TagPanel(app, this, null, true);
        init();
    }

    private void init() {
        setTitle(app.getText(AppText.APPLY_TITLE));
        tagPanel.add(tp);

        colorOrangeButton.setText(app.getText(AppText.CHIP_COLOR_ORANGE));
        colorBlueButton.setText(app.getText(AppText.CHIP_COLOR_BLUE));
        levelMinButton.setText(app.getText(AppText.UNIT_LEVEL, "0"));
        levelMaxButton.setText(app.getText(AppText.UNIT_LEVEL, Chip.LEVEL_MAX));
        markAllButton.setText(app.getText(AppText.APPLY_MARK_ALL));
        markNoneButton.setText(app.getText(AppText.APPLY_MARK_NONE));
        tagDescLabel.setText(app.getText(AppText.APPLY_TAG_DESC));
        cancelButton.setText(app.getText(AppText.ACTION_CANCEL));

        rotateLeftButton.setIcon(AppImage.ROTATE_LEFT);
        rotateRightButton.setIcon(AppImage.ROTATE_RIGHT);
        
        addListeners();

        Dimension d = getPreferredSize();
        d.height = app.mf.getPreferredDialogSize().height;
        setPreferredSize(d);
        pack();
    }

    private void addListeners() {
        Fn.addEscDisposeListener(this);

        tp.addTableMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() >= 2 && tp.getSelectedRow() > -1) {
                    applyTag();
                }
            }
        });
    }

    private void applyTag() {
        Tag tag = tp.getTag(tp.getSelectedRow());
        List<Chip> chips = app.mf.inv_getFilteredChips();
        if (chips.stream().allMatch((chip) -> chip.containsTag(tag))) {
            // remove
            confirmAndApply((t) -> t.setTag(tag, false));
            if (app.mf.inv_getAllTags().stream().noneMatch((t) -> t.equals(tag))) {
                app.filter.includedTags.removeIf((t) -> t.equals(tag));
                app.filter.excludedTags.removeIf((t) -> t.equals(tag));
                app.mf.display_applyFilterSort();
            }
        } else {
            // add
            confirmAndApply((t) -> t.setTag(tag, true));
        }

    }

    private void confirmAndApply(Consumer<? super Chip> action) {
        int val = JOptionPane.showConfirmDialog(this, app.getText(AppText.APPLY_CONFIRM_DESC), app.getText(AppText.APPLY_TITLE), JOptionPane.YES_NO_OPTION);
        if (val == JOptionPane.YES_OPTION) {
            app.mf.invStat_applyAll(action);
            closeDialog();
        }
    }

    private void closeDialog() {
        this.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        rotateLeftButton = new javax.swing.JButton();
        rotateRightButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        colorOrangeButton = new javax.swing.JButton();
        colorBlueButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        levelMinButton = new javax.swing.JButton();
        levelMaxButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        markAllButton = new javax.swing.JButton();
        markNoneButton = new javax.swing.JButton();
        tagPanel = new javax.swing.JPanel();
        tagDescLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("모두 적용");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        cancelButton.setText("적용 취소");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        rotateLeftButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        rotateLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateLeftButtonActionPerformed(evt);
            }
        });

        rotateRightButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        rotateRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateRightButtonActionPerformed(evt);
            }
        });

        colorOrangeButton.setText("주황");
        colorOrangeButton.setPreferredSize(new java.awt.Dimension(100, 22));
        colorOrangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorOrangeButtonActionPerformed(evt);
            }
        });

        colorBlueButton.setText("파랑");
        colorBlueButton.setPreferredSize(new java.awt.Dimension(100, 22));
        colorBlueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorBlueButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(colorOrangeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorBlueButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(colorOrangeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
            .addComponent(colorBlueButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        levelMinButton.setText("0강");
        levelMinButton.setPreferredSize(new java.awt.Dimension(100, 22));
        levelMinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                levelMinButtonActionPerformed(evt);
            }
        });

        levelMaxButton.setText("20강");
        levelMaxButton.setPreferredSize(new java.awt.Dimension(100, 22));
        levelMaxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                levelMaxButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(levelMinButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(levelMaxButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(levelMaxButton, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
            .addComponent(levelMinButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        markAllButton.setText("마킹");
        markAllButton.setPreferredSize(new java.awt.Dimension(100, 22));
        markAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markAllButtonActionPerformed(evt);
            }
        });

        markNoneButton.setText("마킹 해제");
        markNoneButton.setPreferredSize(new java.awt.Dimension(100, 22));
        markNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markNoneButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(markAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markNoneButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(markAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
            .addComponent(markNoneButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tagPanel.setPreferredSize(new java.awt.Dimension(250, 250));
        tagPanel.setLayout(new java.awt.BorderLayout());

        tagDescLabel.setText("<html>\n태그를 더블클릭시 모든 (필터된) 칩에 추가합니다.<br>\n모든 칩이 가진 태그는 배경색이 있으며 더블클릭시 제거합니다.\n</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rotateLeftButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(rotateRightButton))
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tagPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tagDescLabel))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rotateLeftButton)
                            .addComponent(rotateRightButton))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tagDescLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tagPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void colorOrangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorOrangeButtonActionPerformed
        confirmAndApply((t) -> t.setColor(Chip.COLOR_ORANGE));
    }//GEN-LAST:event_colorOrangeButtonActionPerformed

    private void colorBlueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorBlueButtonActionPerformed
        confirmAndApply((t) -> t.setColor(Chip.COLOR_BLUE));
    }//GEN-LAST:event_colorBlueButtonActionPerformed

    private void levelMinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelMinButtonActionPerformed
        confirmAndApply((t) -> t.setMinInitLevel());
    }//GEN-LAST:event_levelMinButtonActionPerformed

    private void levelMaxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelMaxButtonActionPerformed
        confirmAndApply((t) -> t.setMaxInitLevel());
    }//GEN-LAST:event_levelMaxButtonActionPerformed

    private void markAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markAllButtonActionPerformed
        confirmAndApply((t) -> t.setMarked(true));
    }//GEN-LAST:event_markAllButtonActionPerformed

    private void markNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markNoneButtonActionPerformed
        confirmAndApply((t) -> t.setMarked(false));
    }//GEN-LAST:event_markNoneButtonActionPerformed

    private void rotateLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateLeftButtonActionPerformed
        confirmAndApply((t) -> t.initRotate(Chip.COUNTERCLOCKWISE));
    }//GEN-LAST:event_rotateLeftButtonActionPerformed

    private void rotateRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateRightButtonActionPerformed
        confirmAndApply((t) -> t.initRotate(Chip.CLOCKWISE));
    }//GEN-LAST:event_rotateRightButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton colorBlueButton;
    private javax.swing.JButton colorOrangeButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton levelMaxButton;
    private javax.swing.JButton levelMinButton;
    private javax.swing.JButton markAllButton;
    private javax.swing.JButton markNoneButton;
    private javax.swing.JButton rotateLeftButton;
    private javax.swing.JButton rotateRightButton;
    private javax.swing.JLabel tagDescLabel;
    private javax.swing.JPanel tagPanel;
    // End of variables declaration//GEN-END:variables
}
