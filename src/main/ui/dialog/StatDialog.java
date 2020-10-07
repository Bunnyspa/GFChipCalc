package main.ui.dialog;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import main.App;
import main.puzzle.Board;
import main.puzzle.Stat;
import main.ui.resource.GFLGraphics;
import main.ui.resource.GFLTexts;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class StatDialog extends JDialog {

    private final App app;
    private final Board board;
    private final String name;
    private final int star;

    public static void open(App app, Board board) {
        StatDialog dialog = new StatDialog(app, board);
        Fn.open(app.mf, dialog);
    }

    private StatDialog(App app, Board board) {
        this.app = app;
        this.board = board;
        name = board.getName();
        star = board.getStar();
        initComponents();
        init();
    }

    private void init() {
        initText();

        versionComboBox.setEnabled(star == 5);
        if (star < 5) {
            versionComboBox.addItem(Board.getStarHTML_star(star));
        } else {
            for (int v = 10; v >= 0; v--) {
                versionComboBox.addItem(Board.getStarHTML_version(v));
            }
        }

        addListeners();
        setPreferredSize(app.mf.getPreferredDialogSize());
        pack();
        update();
    }

    private void initText() {
        setTitle(app.getText(GFLTexts.STAT_TITLE));

        textVersionLabel.setText(app.getText(GFLTexts.STAT_VERSION));
        totalLabel.setText(Fn.toHTML(app.getText(GFLTexts.STAT_TOTAL)
                + " = " + Fn.htmlColor(app.getText(GFLTexts.STAT_HOC), GFLGraphics.COLOR_LEVEL)
                + " + " + app.getText(GFLTexts.STAT_CHIP)
                + " + " + Fn.htmlColor(app.getText(GFLTexts.STAT_RESONANCE), GFLGraphics.COLORS_CHIP.get(board.getColor()))
                + " + " + Fn.htmlColor(app.getText(GFLTexts.STAT_VERSION), GFLGraphics.COLOR_STAR_RED)));

        Stat s = board.getStat();
        Stat cm = board.getCustomMaxStat();
        Stat om = board.getOrigMaxStat();

        dmgPanel.setBorder(new TitledBorder(app.getText(GFLTexts.CHIP_STAT_DMG_LONG) + " " + s.dmg + " / " + cm.dmg + (cm.dmg == om.dmg ? "" : " (" + om.dmg + ")")));
        brkPanel.setBorder(new TitledBorder(app.getText(GFLTexts.CHIP_STAT_BRK_LONG) + " " + s.brk + " / " + cm.brk + (cm.brk == om.brk ? "" : " (" + om.brk + ")")));
        hitPanel.setBorder(new TitledBorder(app.getText(GFLTexts.CHIP_STAT_HIT_LONG) + " " + s.hit + " / " + cm.hit + (cm.hit == om.hit ? "" : " (" + om.hit + ")")));
        rldPanel.setBorder(new TitledBorder(app.getText(GFLTexts.CHIP_STAT_RLD_LONG) + " " + s.rld + " / " + cm.rld + (cm.rld == om.rld ? "" : " (" + om.rld + ")")));

        closeButton.setText(app.getText(GFLTexts.ACTION_CLOSE));
    }

    private void update() {
        int version = versionComboBox.isEnabled() ? 10 - versionComboBox.getSelectedIndex() : 0;

        JLabel[] keyLabel = {keyDmgLabel, keyBrkLabel, keyHitLabel, keyRldLabel};
        JLabel[] valueLabel = {valueDmgLabel, valueBrkLabel, valueHitLabel, valueRldLabel};

        int[] hocStat = Board.getHOCStat(name).toArray();
        int[] chipStat = board.getStat().limit(board.getOrigMaxStat()).toArray();
        int[] resStat = board.getResonance().toArray();
        int[] verStat = Board.getVersionStat(name, version).toArray();

        int[] totalStat = new int[4];
        for (int i = 0; i < 4; i++) {
            totalStat[i] = hocStat[i] + chipStat[i] + resStat[i] + verStat[i];
        }

        String totalKey = app.getText(GFLTexts.STAT_TOTAL);

        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            keys.clear();
            values.clear();

            keys.add(totalKey);
            values.add(totalStat[i]
                    + " = " + Fn.htmlColor(hocStat[i], GFLGraphics.COLOR_LEVEL)
                    + " + " + chipStat[i]
                    + " + " + Fn.htmlColor(resStat[i], GFLGraphics.COLORS_CHIP.get(board.getColor()))
                    + " + " + Fn.htmlColor(verStat[i], GFLGraphics.COLOR_STAR_RED)
            );

            switch (i) {
                case Stat.BRK:
                    // Old Stat
                    int oldBrk = board.getOldStat().limit(board.getOrigMaxStat()).brk;
                    int oldTotalBrk = hocStat[1] + oldBrk + resStat[1] + verStat[1];

                    String oldTotalKey = app.getText(GFLTexts.STAT_TOTAL_OLD);

                    keys.add(oldTotalKey);
                    values.add(oldTotalBrk
                            + " = " + Fn.htmlColor(hocStat[i], GFLGraphics.COLOR_LEVEL)
                            + " + " + oldBrk
                            + " + " + Fn.htmlColor(resStat[i], GFLGraphics.COLORS_CHIP.get(board.getColor()))
                            + " + " + Fn.htmlColor(verStat[i], GFLGraphics.COLOR_STAR_RED)
                    );

                    break;
                case Stat.RLD:
                    // Fire Rate
                    int firerate = rldToFirerate(totalStat[3]);
                    keys.add(app.getText(GFLTexts.STAT_RLD_FIRERATE));
                    values.add(String.valueOf(firerate));

                    // Frame
                    int frame = firerateToFrame(firerate);
                    double sec = frame / 30.0f;
                    keys.add(app.getText(GFLTexts.STAT_RLD_DELAY));
                    values.add(app.getText(GFLTexts.STAT_RLD_DELAY_FRAME, String.valueOf(frame)) + " = " + app.getText(GFLTexts.STAT_RLD_DELAY_SECOND, Fn.fStr(sec, 4)));

                    break;
                default:
            }
            setData(keyLabel[i], valueLabel[i], keys, values);
        }
    }

    private void addListeners() {
        versionComboBox.addActionListener((e) -> update());
        Fn.addEscDisposeListener(this);
    }

    private static void setData(JLabel keyLabel, JLabel valueLabel, List<String> keys, List<String> values) {
        if (keys.isEmpty()) {
            keyLabel.setText("");
            valueLabel.setText("");
        } else {
            List<String> keysMod = new ArrayList<>();
            keys.forEach((k) -> keysMod.add(k + "&nbsp;:&nbsp;"));
            keyLabel.setText(Fn.toHTML("<div align=right>" + String.join("<br>", keysMod) + "</div>"));
            valueLabel.setText(Fn.toHTML(String.join("<br>", values)));
        }
    }

    private static int rldToFirerate(int rld) {
        return Fn.ceil(rld + 300, 30);
    }

    private static int firerateToFrame(int firerate) {
        return Fn.floor(1500, firerate);
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
        dmgPanel = new javax.swing.JPanel();
        keyDmgLabel = new javax.swing.JLabel();
        valueDmgLabel = new javax.swing.JLabel();
        brkPanel = new javax.swing.JPanel();
        keyBrkLabel = new javax.swing.JLabel();
        valueBrkLabel = new javax.swing.JLabel();
        hitPanel = new javax.swing.JPanel();
        keyHitLabel = new javax.swing.JLabel();
        valueHitLabel = new javax.swing.JLabel();
        rldPanel = new javax.swing.JPanel();
        keyRldLabel = new javax.swing.JLabel();
        valueRldLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        textVersionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox<>();
        totalLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("도움말");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        closeButton.setText("닫기");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        dmgPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("dmg"));
        dmgPanel.setLayout(new java.awt.BorderLayout());

        keyDmgLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        keyDmgLabel.setText("key");
        keyDmgLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        dmgPanel.add(keyDmgLabel, java.awt.BorderLayout.LINE_START);

        valueDmgLabel.setText("value");
        valueDmgLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        dmgPanel.add(valueDmgLabel, java.awt.BorderLayout.CENTER);

        brkPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("brk"));
        brkPanel.setLayout(new java.awt.BorderLayout());

        keyBrkLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        keyBrkLabel.setText("key");
        keyBrkLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        brkPanel.add(keyBrkLabel, java.awt.BorderLayout.LINE_START);

        valueBrkLabel.setText("value");
        valueBrkLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        brkPanel.add(valueBrkLabel, java.awt.BorderLayout.CENTER);

        hitPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("hit"));
        hitPanel.setLayout(new java.awt.BorderLayout());

        keyHitLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        keyHitLabel.setText("key");
        keyHitLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        hitPanel.add(keyHitLabel, java.awt.BorderLayout.LINE_START);

        valueHitLabel.setText("value");
        valueHitLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        hitPanel.add(valueHitLabel, java.awt.BorderLayout.CENTER);

        rldPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("rld"));
        rldPanel.setLayout(new java.awt.BorderLayout());

        keyRldLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        keyRldLabel.setText("key");
        keyRldLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        rldPanel.add(keyRldLabel, java.awt.BorderLayout.LINE_START);

        valueRldLabel.setText("value");
        valueRldLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        rldPanel.add(valueRldLabel, java.awt.BorderLayout.CENTER);

        textVersionLabel.setText("version");

        versionComboBox.setPreferredSize(new java.awt.Dimension(100, 21));

        totalLabel.setText("jLabel1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(textVersionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(textVersionLabel)
                .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(totalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addComponent(rldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(hitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(brkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dmgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dmgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(brkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel brkPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel dmgPanel;
    private javax.swing.JPanel hitPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel keyBrkLabel;
    private javax.swing.JLabel keyDmgLabel;
    private javax.swing.JLabel keyHitLabel;
    private javax.swing.JLabel keyRldLabel;
    private javax.swing.JPanel rldPanel;
    private javax.swing.JLabel textVersionLabel;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JLabel valueBrkLabel;
    private javax.swing.JLabel valueDmgLabel;
    private javax.swing.JLabel valueHitLabel;
    private javax.swing.JLabel valueRldLabel;
    private javax.swing.JComboBox<String> versionComboBox;
    // End of variables declaration//GEN-END:variables
}
