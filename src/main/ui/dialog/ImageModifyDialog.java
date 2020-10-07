package main.ui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Enumeration;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.ui.renderer.ChipListCellRenderer;
import main.ui.resource.GFLGraphics;
import main.ui.resource.GFLResources;
import main.ui.resource.GFLTexts;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class ImageModifyDialog extends JDialog {

    private final App app;
    private final DefaultListModel<Chip> poolLM = new DefaultListModel<>();

    private Shape shape = Shape.DEFAULT;
    private int star = 5;
    private int color = Chip.COLOR_ORANGE;
    private int[] pt = new int[4];
    private int level = 0;
    private int rotation = 0;

    private boolean cancelled = true;

    public static Chip modify(App app, Chip c) {
        ImageModifyDialog d = new ImageModifyDialog(app, c);
        d.setVisible(true);
        if (d.cancelled) {
            return null;
        }
        return new Chip(d.shape, d.star, d.color, new Stat(d.pt), d.level, d.rotation);
    }

    private ImageModifyDialog(App app, Chip c) {
        this.app = app;
        initComponents();
        init();
        readChip(c);
    }

    private void init() {
        initPool();

        poolRotLButton.setIcon(GFLResources.ROTATE_LEFT);
        poolRotRButton.setIcon(GFLResources.ROTATE_RIGHT);
        dmgTextLabel.setIcon(GFLResources.DMG);
        brkTextLabel.setIcon(GFLResources.BRK);
        hitTextLabel.setIcon(GFLResources.HIT);
        rldTextLabel.setIcon(GFLResources.RLD);

        String[] piStarCBList = new String[]{
            Board.getStarHTML_star(5),
            Board.getStarHTML_star(4),
            Board.getStarHTML_star(3),
            Board.getStarHTML_star(2)
        };
        poolStarComboBox.setModel(new DefaultComboBoxModel<>(piStarCBList));
        levelSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.LEVEL_MAX, 1));

        initText();
        addListeners();

        setPreferredSize(new Dimension(getPreferredSize().width, app.mf.getPreferredDialogSize().height));
        pack();
    }

    private void initPool() {
        poolList.setModel(poolLM);// Renderer
        poolList.setCellRenderer(new ChipListCellRenderer(app));
        // Rows
        for (Shape.Type type : Shape.Type.values()) {
            for (Shape s : Shape.getShapes(type)) {
                poolLM.addElement(new Chip(s));
            }
        }
        poolList.addListSelectionListener((e) -> selectShape());
    }

    private void initText() {
        setTitle(app.getText(app.getText(GFLTexts.IMAGE_TITLE)));
        statPanel.setBorder(new TitledBorder(app.getText(GFLTexts.CSET_GROUP_STAT)));
        dmgTextLabel.setText(app.getText(GFLTexts.CHIP_STAT_DMG));
        brkTextLabel.setText(app.getText(GFLTexts.CHIP_STAT_BRK));
        hitTextLabel.setText(app.getText(GFLTexts.CHIP_STAT_HIT));
        rldTextLabel.setText(app.getText(GFLTexts.CHIP_STAT_RLD));

        levelLabel.setText(app.getText(GFLTexts.CHIP_LEVEL));
        okButton.setText(app.getText(GFLTexts.ACTION_OK));
        cancelButton.setText(app.getText(GFLTexts.ACTION_CANCEL));
    }

    private void addListeners() {
        poolRotLButton.addActionListener((e) -> rotate(Chip.COUNTERCLOCKWISE));
        poolRotRButton.addActionListener((e) -> rotate(Chip.CLOCKWISE));
        poolStarComboBox.addActionListener((e) -> setStar());
        poolColorButton.addActionListener((e) -> cycleColor());
        dmgComboBox.addItemListener((e) -> setPt());
        brkComboBox.addItemListener((e) -> setPt());
        hitComboBox.addItemListener((e) -> setPt());
        rldComboBox.addItemListener((e) -> setPt());
        levelSpinner.addChangeListener((e) -> setLevel());
        Fn.addEscDisposeListener(this);
    }

    private void readChip(Chip chip) {
        // Read data
        if (chip != null) {
            shape = chip.getShape();
            star = chip.getStar();
            color = chip.getColor();
            pt = chip.getPt().toArray();
            level = chip.getLevel();
            rotation = chip.getRotation();
        }
        // Select shape
        for (int i = 0; i < poolLM.size(); i++) {
            Chip c = poolLM.get(i);
            if (c.getShape() == shape) {
                poolList.setSelectedIndex(i);
                break;
            }
        }
        // Rotate
        for (int i = 0; i < rotation; i++) {
            rotatePool(Chip.CLOCKWISE);
        }
        // Level
        levelSpinner.setValue(level);
        // Color
        setColorText();

        updateAndVerify();
    }

    private boolean updating = false;

    private void updateAndVerify() {
        updating = true;

        // Update stat values
        dmgComboBox.removeAllItems();
        brkComboBox.removeAllItems();
        hitComboBox.removeAllItems();
        rldComboBox.removeAllItems();
        for (int i = 0; i <= Chip.getMaxPt(shape.getSize()); i++) {
            dmgComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_DMG, shape.getType(), star, level, i)));
            brkComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_BRK, shape.getType(), star, level, i)));
            hitComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_HIT, shape.getType(), star, level, i)));
            rldComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_RLD, shape.getType(), star, level, i)));
        }
        pt[0] = Fn.limit(pt[0], 0, dmgComboBox.getItemCount() - 1);
        pt[1] = Fn.limit(pt[1], 0, brkComboBox.getItemCount() - 1);
        pt[2] = Fn.limit(pt[2], 0, hitComboBox.getItemCount() - 1);
        pt[3] = Fn.limit(pt[3], 0, rldComboBox.getItemCount() - 1);
        dmgComboBox.setSelectedIndex(pt[0]);
        brkComboBox.setSelectedIndex(pt[1]);
        hitComboBox.setSelectedIndex(pt[2]);
        rldComboBox.setSelectedIndex(pt[3]);
        dmgPtLabel.setText(String.valueOf(pt[0]));
        brkPtLabel.setText(String.valueOf(pt[1]));
        hitPtLabel.setText(String.valueOf(pt[2]));
        rldPtLabel.setText(String.valueOf(pt[3]));

        // Verification
        Chip chip = new Chip(shape, star, color, new Stat(pt), level, rotation);
        boolean valid = ImageDialog.isValid(chip);

        dmgComboBox.setForeground(valid ? Color.BLACK : Color.RED);
        brkComboBox.setForeground(valid ? Color.BLACK : Color.RED);
        hitComboBox.setForeground(valid ? Color.BLACK : Color.RED);
        rldComboBox.setForeground(valid ? Color.BLACK : Color.RED);
        dmgPtLabel.setForeground(valid ? Color.BLACK : Color.RED);
        brkPtLabel.setForeground(valid ? Color.BLACK : Color.RED);
        hitPtLabel.setForeground(valid ? Color.BLACK : Color.RED);
        rldPtLabel.setForeground(valid ? Color.BLACK : Color.RED);

        okButton.setEnabled(valid);

        updating = false;
    }

    private void selectShape() {
        shape = poolList.getSelectedValue().getShape();
        updateAndVerify();
    }

    private void rotate(boolean direction) {
        rotatePool(direction);
        rotation = poolList.getSelectedValue().getRotation();
        updateAndVerify();
    }

    private void rotatePool(boolean direction) {
        for (Enumeration<Chip> elements = poolLM.elements(); elements.hasMoreElements();) {
            Chip c = elements.nextElement();
            c.initRotate(direction);
        }
        poolList.repaint();
    }

    private void setStar() {
        star = 5 - poolStarComboBox.getSelectedIndex();
        updateAndVerify();
    }

    private void setLevel() {
        level = (int) levelSpinner.getValue();
        updateAndVerify();
    }

    private void setPt() {
        if (!updating) {
            pt[0] = dmgComboBox.getSelectedIndex();
            pt[1] = brkComboBox.getSelectedIndex();
            pt[2] = hitComboBox.getSelectedIndex();
            pt[3] = rldComboBox.getSelectedIndex();
            updateAndVerify();
        }
    }

    private void cycleColor() {
        setColor((color + 1) % GFLTexts.TEXT_MAP_COLOR.size());
    }

    private void setColor(int c) {
        color = c;
        setColorText();
    }

    private void setColorText() {
        poolColorButton.setText(app.getText(GFLTexts.TEXT_MAP_COLOR.get(color)));
        poolColorButton.setForeground(GFLGraphics.COLORS_CHIP.get(color));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        chipScrollPane = new javax.swing.JScrollPane();
        poolList = new javax.swing.JList<>();
        poolRotLButton = new javax.swing.JButton();
        poolRotRButton = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        poolColorButton = new javax.swing.JButton();
        poolStarComboBox = new javax.swing.JComboBox<>();
        statPanel = new javax.swing.JPanel();
        dmgPanel = new javax.swing.JPanel();
        dmgTextLabel = new javax.swing.JLabel();
        dmgComboBox = new javax.swing.JComboBox<>();
        dmgPtLabel = new javax.swing.JLabel();
        brkPanel = new javax.swing.JPanel();
        brkTextLabel = new javax.swing.JLabel();
        brkComboBox = new javax.swing.JComboBox<>();
        brkPtLabel = new javax.swing.JLabel();
        hitPanel = new javax.swing.JPanel();
        hitTextLabel = new javax.swing.JLabel();
        hitComboBox = new javax.swing.JComboBox<>();
        hitPtLabel = new javax.swing.JLabel();
        rldPanel = new javax.swing.JPanel();
        rldTextLabel = new javax.swing.JLabel();
        rldComboBox = new javax.swing.JComboBox<>();
        rldPtLabel = new javax.swing.JLabel();
        levelSpinner = new javax.swing.JSpinner();
        levelLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("도움말");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        okButton.setText("ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        chipScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chipScrollPane.setPreferredSize(new java.awt.Dimension(250, 250));

        poolList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        poolList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        poolList.setVisibleRowCount(-1);
        chipScrollPane.setViewportView(poolList);

        poolRotLButton.setMinimumSize(new java.awt.Dimension(50, 50));
        poolRotLButton.setPreferredSize(new java.awt.Dimension(50, 50));

        poolRotRButton.setMinimumSize(new java.awt.Dimension(50, 50));
        poolRotRButton.setPreferredSize(new java.awt.Dimension(50, 50));

        poolColorButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        poolColorButton.setPreferredSize(new java.awt.Dimension(100, 22));

        poolStarComboBox.setPreferredSize(new java.awt.Dimension(100, 22));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolStarComboBox, 0, 150, Short.MAX_VALUE)
            .addComponent(poolColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(poolStarComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(poolColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("stat"));
        statPanel.setFocusable(false);

        dmgPanel.setLayout(new java.awt.BorderLayout(5, 0));

        dmgTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        dmgTextLabel.setText("살상");
        dmgTextLabel.setFocusable(false);
        dmgTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        dmgPanel.add(dmgTextLabel, java.awt.BorderLayout.LINE_START);

        dmgComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        dmgPanel.add(dmgComboBox, java.awt.BorderLayout.CENTER);

        dmgPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dmgPtLabel.setText("-");
        dmgPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        dmgPtLabel.setFocusable(false);
        dmgPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        dmgPanel.add(dmgPtLabel, java.awt.BorderLayout.LINE_END);

        brkPanel.setLayout(new java.awt.BorderLayout(5, 0));

        brkTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        brkTextLabel.setText("파쇄");
        brkTextLabel.setFocusable(false);
        brkTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        brkPanel.add(brkTextLabel, java.awt.BorderLayout.LINE_START);

        brkComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        brkPanel.add(brkComboBox, java.awt.BorderLayout.CENTER);

        brkPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        brkPtLabel.setText("-");
        brkPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        brkPtLabel.setFocusable(false);
        brkPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        brkPanel.add(brkPtLabel, java.awt.BorderLayout.LINE_END);

        hitPanel.setLayout(new java.awt.BorderLayout(5, 0));

        hitTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        hitTextLabel.setText("정밀");
        hitTextLabel.setFocusable(false);
        hitTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        hitPanel.add(hitTextLabel, java.awt.BorderLayout.LINE_START);

        hitComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        hitPanel.add(hitComboBox, java.awt.BorderLayout.CENTER);

        hitPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        hitPtLabel.setText("-");
        hitPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        hitPtLabel.setFocusable(false);
        hitPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        hitPanel.add(hitPtLabel, java.awt.BorderLayout.LINE_END);

        rldPanel.setLayout(new java.awt.BorderLayout(5, 0));

        rldTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        rldTextLabel.setText("장전");
        rldTextLabel.setFocusable(false);
        rldTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        rldPanel.add(rldTextLabel, java.awt.BorderLayout.LINE_START);

        rldComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        rldPanel.add(rldComboBox, java.awt.BorderLayout.CENTER);

        rldPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rldPtLabel.setText("-");
        rldPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        rldPtLabel.setFocusable(false);
        rldPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        rldPanel.add(rldPtLabel, java.awt.BorderLayout.LINE_END);

        javax.swing.GroupLayout statPanelLayout = new javax.swing.GroupLayout(statPanel);
        statPanel.setLayout(statPanelLayout);
        statPanelLayout.setHorizontalGroup(
            statPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dmgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(brkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(hitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        statPanelLayout.setVerticalGroup(
            statPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statPanelLayout.createSequentialGroup()
                .addComponent(dmgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(brkPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hitPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        levelSpinner.setPreferredSize(new java.awt.Dimension(100, 22));

        levelLabel.setText("level");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(poolRotLButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(poolRotRButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(chipScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(levelLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(levelSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                    .addComponent(statPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(levelLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(chipScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton))
                    .addComponent(poolRotRButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(poolRotLButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        cancelled = false;
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> brkComboBox;
    private javax.swing.JPanel brkPanel;
    private javax.swing.JLabel brkPtLabel;
    private javax.swing.JLabel brkTextLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane chipScrollPane;
    private javax.swing.JComboBox<String> dmgComboBox;
    private javax.swing.JPanel dmgPanel;
    private javax.swing.JLabel dmgPtLabel;
    private javax.swing.JLabel dmgTextLabel;
    private javax.swing.JComboBox<String> hitComboBox;
    private javax.swing.JPanel hitPanel;
    private javax.swing.JLabel hitPtLabel;
    private javax.swing.JLabel hitTextLabel;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JLabel levelLabel;
    private javax.swing.JSpinner levelSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JButton poolColorButton;
    private javax.swing.JList<Chip> poolList;
    private javax.swing.JButton poolRotLButton;
    private javax.swing.JButton poolRotRButton;
    private javax.swing.JComboBox<String> poolStarComboBox;
    private javax.swing.JComboBox<String> rldComboBox;
    private javax.swing.JPanel rldPanel;
    private javax.swing.JLabel rldPtLabel;
    private javax.swing.JLabel rldTextLabel;
    private javax.swing.JPanel statPanel;
    // End of variables declaration//GEN-END:variables
}
