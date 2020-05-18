package main.ui.dialog;

import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import main.App;
import main.puzzle.Board;
import main.puzzle.Chip;
import main.puzzle.Stat;
import main.resource.Language;
import main.resource.Resources;
import main.setting.BoardSetting;
import main.setting.Filter;
import main.setting.StatPresetMap;
import main.ui.component.TagPanel;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class FilterDialog extends JDialog {

    private final App app;
    private final TagPanel tip, txp;
    private final JToggleButton[] starTBs;
    private final JToggleButton[] colorTBs;
    private final JToggleButton[] typeTBs;
    private final JToggleButton[] markedTBs;
    private final JSpinner[] ptMinSpinners;
    private final JSpinner[] ptMaxSpinners;

    public static FilterDialog getInstance(App app) {
        return new FilterDialog(app);
    }

    private FilterDialog(App app) {
        initComponents();

        this.app = app;
        this.tip = new TagPanel(app, this, (t1) -> app.filter.includedTags.stream().anyMatch((t2) -> t1.equals(t2)), false);
        this.txp = new TagPanel(app, this, (t1) -> app.filter.excludedTags.stream().anyMatch((t2) -> t1.equals(t2)), false);

        starTBs = new JToggleButton[]{star5TB, star4TB, star3TB, star2TB};
        colorTBs = new JToggleButton[]{colorOrangeTB, colorBlueTB};
        typeTBs = new JToggleButton[]{size6TB, size5BTB, size5ATB, size4TB, size3TB, size2TB, size1TB};
        markedTBs = new JToggleButton[]{markedTrueTB, markedFalseTB};
        ptMinSpinners = new JSpinner[]{ptMinDmgSpinner, ptMinBrkSpinner, ptMinHitSpinner, ptMinRldSpinner};
        ptMaxSpinners = new JSpinner[]{ptMaxDmgSpinner, ptMaxBrkSpinner, ptMaxHitSpinner, ptMaxRldSpinner};
        init();
    }

    private void init() {
        setTitle(app.getText(Language.FILTER_TITLE));
        okButton.setText(app.getText(Language.ACTION_OK));
        cancelButton.setText(app.getText(Language.ACTION_CANCEL));
        presetButton.setText(app.getText(Language.FILTER_PRESET));
        resetButton.setText(app.getText(Language.FILTER_RESET));
        tagResetButton.setText(app.getText(Language.FILTER_TAG_RESET));

        markedTrueTB.setIcon(Resources.CHECKED);
        markedFalseTB.setIcon(Resources.UNCHECKED);
        dmgTextLabel.setIcon(Resources.DMG);
        brkTextLabel.setIcon(Resources.BRK);
        hitTextLabel.setIcon(Resources.HIT);
        rldTextLabel.setIcon(Resources.RLD);

        starPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_STAR)));
        for (int i = 0; i < Filter.NUM_STAR; i++) {
            String starStr = String.valueOf(5 - i);
            starTBs[i].setText(app.getText(Language.UNIT_STAR, starStr));
            starTBs[i].setSelected(app.filter.getStar(i));
        }

        colorPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_COLOR)));
        colorOrangeTB.setText(app.getText(Language.CHIP_COLOR_ORANGE));
        colorBlueTB.setText(app.getText(Language.CHIP_COLOR_BLUE));
        for (int i = 0; i < Filter.NUM_COLOR; i++) {
            colorTBs[i].setSelected(app.filter.getColor(i));
        }

        cellPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_CELL)));
        String[] typeStrs = {
            app.getText(Language.UNIT_CELL, Chip.TYPE_6),
            app.getText(Language.UNIT_CELLTYPE, "5", "B"),
            app.getText(Language.UNIT_CELLTYPE, "5", "A"),
            app.getText(Language.UNIT_CELL, Chip.TYPE_4),
            app.getText(Language.UNIT_CELL, Chip.TYPE_3),
            app.getText(Language.UNIT_CELL, Chip.TYPE_2),
            app.getText(Language.UNIT_CELL, Chip.TYPE_1)
        };
        for (int i = 0; i < Filter.NUM_TYPE; i++) {
            typeTBs[i].setText(typeStrs[i]);
            typeTBs[i].setSelected(app.filter.getType(i));
        }

        markPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_MARK)));
        for (int i = 0; i < Filter.NUM_MARK; i++) {
            markedTBs[i].setSelected(app.filter.getMark(i));
        }

        ptPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_PT)));

        dmgTextLabel.setText(app.getText(Language.CHIP_STAT_DMG));
        brkTextLabel.setText(app.getText(Language.CHIP_STAT_BRK));
        hitTextLabel.setText(app.getText(Language.CHIP_STAT_HIT));
        rldTextLabel.setText(app.getText(Language.CHIP_STAT_RLD));
        ptMinDmgSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.PT_MAX, 1));
        ptMinBrkSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.PT_MAX, 1));
        ptMinHitSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.PT_MAX, 1));
        ptMinRldSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.PT_MAX, 1));
        Stat ptMin = app.filter.ptMin;
        ptMinDmgSpinner.setValue(ptMin.dmg);
        ptMinBrkSpinner.setValue(ptMin.brk);
        ptMinHitSpinner.setValue(ptMin.hit);
        ptMinRldSpinner.setValue(ptMin.rld);

        ptMaxDmgSpinner.setModel(new SpinnerNumberModel(Chip.PT_MAX, 0, Chip.PT_MAX, 1));
        ptMaxBrkSpinner.setModel(new SpinnerNumberModel(Chip.PT_MAX, 0, Chip.PT_MAX, 1));
        ptMaxHitSpinner.setModel(new SpinnerNumberModel(Chip.PT_MAX, 0, Chip.PT_MAX, 1));
        ptMaxRldSpinner.setModel(new SpinnerNumberModel(Chip.PT_MAX, 0, Chip.PT_MAX, 1));
        Stat ptMax = app.filter.ptMax;
        ptMaxDmgSpinner.setValue(ptMax.dmg);
        ptMaxBrkSpinner.setValue(ptMax.brk);
        ptMaxHitSpinner.setValue(ptMax.hit);
        ptMaxRldSpinner.setValue(ptMax.rld);

        enhancementPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_ENHANCEMENT)));
        levelMinSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.LEVEL_MAX, 1));
        levelMaxSpinner.setModel(new SpinnerNumberModel(0, 0, Chip.LEVEL_MAX, 1));
        levelMinSpinner.setValue(app.filter.levelMin);
        levelMaxSpinner.setValue(app.filter.levelMax);

        tagIncludedPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_TAG_INCLUDE)));
        tagExcludedPanel.setBorder(new TitledBorder(app.getText(Language.FILTER_GROUP_TAG_EXCLUDE)));
        tagIncludedPanel.add(tip);
        tagExcludedPanel.add(txp);

        String name = app.mf.getBoardName();
        int star = app.mf.getBoardStar();
        if (star != 5 || app.setting.board.getMode(name, star) != BoardSetting.MAX_PRESET) {
            presetButton.setVisible(false);
        }

        addListeners();

        Dimension d = getPreferredSize();
        d.height = app.mf.getPreferredDialogSize().height;
        setPreferredSize(d);
        pack();
    }

    private void addListeners() {
        levelMinSpinner.addChangeListener((e) -> {
            if (getLevelMinSpinnerValue() > getLevelMaxSpinnerValue()) {
                levelMaxSpinner.setValue(getLevelMinSpinnerValue());
            }
        });
        levelMaxSpinner.addChangeListener((e) -> {
            if (getLevelMinSpinnerValue() > getLevelMaxSpinnerValue()) {
                levelMinSpinner.setValue(getLevelMaxSpinnerValue());
            }
        });

        for (JSpinner ptMinSpinner : ptMinSpinners) {
            ptMinSpinner.addChangeListener((e) -> fixPtMaxSpinners());
        }

        for (JSpinner ptMaxSpinner : ptMaxSpinners) {
            ptMaxSpinner.addChangeListener((e) -> fixPtMinSpinners());
        }

        tip.addTableModelListener((e) -> {
            for (int i = 0; i < tip.getCount(); i++) {
                if (tip.isChecked(i) && txp.isChecked(i)) {
                    txp.setChecked(i, false);
                }
            }
        });

        txp.addTableModelListener((e) -> {
            for (int i = 0; i < tip.getCount(); i++) {
                if (tip.isChecked(i) && txp.isChecked(i)) {
                    tip.setChecked(i, false);
                }
            }
        });

        Fn.addEscDisposeListener(this);
    }

    private void fixPtMinSpinners() {
        for (int i = 0; i < ptMinSpinners.length; i++) {
            JSpinner ptMinSpinner = ptMinSpinners[i];
            JSpinner ptMaxSpinner = ptMaxSpinners[i];
            if ((int) ptMinSpinner.getValue() > (int) ptMaxSpinner.getValue()) {
                ptMinSpinner.setValue((int) ptMaxSpinner.getValue());
            }
        }
    }

    private void fixPtMaxSpinners() {
        for (int i = 0; i < ptMaxSpinners.length; i++) {
            JSpinner ptMinSpinner = ptMinSpinners[i];
            JSpinner ptMaxSpinner = ptMaxSpinners[i];
            if ((int) ptMinSpinner.getValue() > (int) ptMaxSpinner.getValue()) {
                ptMaxSpinner.setValue((int) ptMinSpinner.getValue());
            }
        }
    }

    private int getLevelMinSpinnerValue() {
        return (int) levelMinSpinner.getValue();
    }

    private int getLevelMaxSpinnerValue() {
        return (int) levelMaxSpinner.getValue();
    }

    private void applyPreset() {
        String name = app.mf.getBoardName();
        int star = app.mf.getBoardStar();
        int presetIndex = app.setting.board.getPresetIndex(name, star);

        StatPresetMap presetMap = BoardSetting.PRESET;

        for (JToggleButton starTB : starTBs) {
            starTB.setSelected(starTB == star5TB);
        }

        colorOrangeTB.setSelected(Board.getColor(name) == Chip.COLOR_ORANGE);
        colorBlueTB.setSelected(Board.getColor(name) == Chip.COLOR_BLUE);

        boolean[] typeArray = presetMap.getTypeFilter(name, 5, presetIndex);
        for (int i = 0; i < typeTBs.length; i++) {
            typeTBs[i].setSelected(typeArray[i]);
        }

        int[] minPtArray = presetMap.get(name, 5, presetIndex).ptMin.toArray();
        for (int i = 0; i < ptMinSpinners.length; i++) {
            ptMinSpinners[i].setValue(minPtArray[i]);
        }

        int[] maxPtArray = presetMap.get(name, 5, presetIndex).ptMax.toArray();
        for (int i = 0; i < ptMaxSpinners.length; i++) {
            ptMaxSpinners[i].setValue(maxPtArray[i]);
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

        starPanel = new javax.swing.JPanel();
        star5TB = new javax.swing.JToggleButton();
        star4TB = new javax.swing.JToggleButton();
        star3TB = new javax.swing.JToggleButton();
        star2TB = new javax.swing.JToggleButton();
        colorPanel = new javax.swing.JPanel();
        colorOrangeTB = new javax.swing.JToggleButton();
        colorBlueTB = new javax.swing.JToggleButton();
        cellPanel = new javax.swing.JPanel();
        size6TB = new javax.swing.JToggleButton();
        size5BTB = new javax.swing.JToggleButton();
        size5ATB = new javax.swing.JToggleButton();
        size4TB = new javax.swing.JToggleButton();
        size3TB = new javax.swing.JToggleButton();
        size2TB = new javax.swing.JToggleButton();
        size1TB = new javax.swing.JToggleButton();
        markPanel = new javax.swing.JPanel();
        markedTrueTB = new javax.swing.JToggleButton();
        markedFalseTB = new javax.swing.JToggleButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        enhancementPanel = new javax.swing.JPanel();
        levelMinSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        levelMaxSpinner = new javax.swing.JSpinner();
        ptPanel = new javax.swing.JPanel();
        dmgTextLabel = new javax.swing.JLabel();
        brkTextLabel = new javax.swing.JLabel();
        hitTextLabel = new javax.swing.JLabel();
        rldTextLabel = new javax.swing.JLabel();
        ptMaxDmgSpinner = new javax.swing.JSpinner();
        ptMaxBrkSpinner = new javax.swing.JSpinner();
        ptMaxHitSpinner = new javax.swing.JSpinner();
        ptMaxRldSpinner = new javax.swing.JSpinner();
        ptMinDmgSpinner = new javax.swing.JSpinner();
        ptMinBrkSpinner = new javax.swing.JSpinner();
        ptMinHitSpinner = new javax.swing.JSpinner();
        ptMinRldSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        tagIncludedPanel = new javax.swing.JPanel();
        tagExcludedPanel = new javax.swing.JPanel();
        tagResetButton = new javax.swing.JButton();
        presetButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("인벤토리 필터");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        starPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("레어도"));

        star5TB.setText("5성");
        star5TB.setFocusPainted(false);
        star5TB.setFocusable(false);
        star5TB.setRolloverEnabled(false);

        star4TB.setText("4성");
        star4TB.setFocusPainted(false);
        star4TB.setFocusable(false);
        star4TB.setRolloverEnabled(false);

        star3TB.setText("3성");
        star3TB.setFocusPainted(false);
        star3TB.setFocusable(false);
        star3TB.setRolloverEnabled(false);

        star2TB.setText("2성");
        star2TB.setFocusPainted(false);
        star2TB.setFocusable(false);
        star2TB.setRolloverEnabled(false);

        javax.swing.GroupLayout starPanelLayout = new javax.swing.GroupLayout(starPanel);
        starPanel.setLayout(starPanelLayout);
        starPanelLayout.setHorizontalGroup(
            starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(starPanelLayout.createSequentialGroup()
                .addComponent(star5TB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(star4TB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(star3TB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(star2TB))
        );

        starPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {star2TB, star3TB, star4TB, star5TB});

        starPanelLayout.setVerticalGroup(
            starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(star5TB)
                .addComponent(star4TB)
                .addComponent(star3TB)
                .addComponent(star2TB))
        );

        starPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {star2TB, star3TB, star4TB, star5TB});

        colorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("color"));

        colorOrangeTB.setText("주황");
        colorOrangeTB.setFocusPainted(false);
        colorOrangeTB.setFocusable(false);
        colorOrangeTB.setRolloverEnabled(false);

        colorBlueTB.setText("파랑");
        colorBlueTB.setFocusPainted(false);
        colorBlueTB.setFocusable(false);
        colorBlueTB.setRolloverEnabled(false);

        javax.swing.GroupLayout colorPanelLayout = new javax.swing.GroupLayout(colorPanel);
        colorPanel.setLayout(colorPanelLayout);
        colorPanelLayout.setHorizontalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createSequentialGroup()
                .addComponent(colorOrangeTB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorBlueTB))
        );

        colorPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {colorBlueTB, colorOrangeTB});

        colorPanelLayout.setVerticalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(colorOrangeTB)
                .addComponent(colorBlueTB))
        );

        colorPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {colorBlueTB, colorOrangeTB});

        cellPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("칸 수"));

        size6TB.setText("6칸");
        size6TB.setFocusPainted(false);
        size6TB.setFocusable(false);
        size6TB.setRolloverEnabled(false);

        size5BTB.setText("5칸 B형");
        size5BTB.setFocusPainted(false);
        size5BTB.setFocusable(false);
        size5BTB.setRolloverEnabled(false);

        size5ATB.setText("5칸 A형");
        size5ATB.setFocusPainted(false);
        size5ATB.setFocusable(false);
        size5ATB.setRolloverEnabled(false);

        size4TB.setText("4칸");
        size4TB.setFocusPainted(false);
        size4TB.setFocusable(false);
        size4TB.setRolloverEnabled(false);

        size3TB.setText("3칸");
        size3TB.setFocusPainted(false);
        size3TB.setFocusable(false);
        size3TB.setRolloverEnabled(false);

        size2TB.setText("2칸");
        size2TB.setFocusPainted(false);
        size2TB.setFocusable(false);
        size2TB.setRolloverEnabled(false);

        size1TB.setText("1칸");
        size1TB.setFocusPainted(false);
        size1TB.setFocusable(false);
        size1TB.setRolloverEnabled(false);

        javax.swing.GroupLayout cellPanelLayout = new javax.swing.GroupLayout(cellPanel);
        cellPanel.setLayout(cellPanelLayout);
        cellPanelLayout.setHorizontalGroup(
            cellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cellPanelLayout.createSequentialGroup()
                .addGroup(cellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(size6TB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(size4TB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cellPanelLayout.createSequentialGroup()
                        .addComponent(size3TB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(size2TB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(size1TB))
                    .addGroup(cellPanelLayout.createSequentialGroup()
                        .addComponent(size5BTB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(size5ATB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        cellPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {size1TB, size2TB, size3TB, size4TB, size6TB});

        cellPanelLayout.setVerticalGroup(
            cellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cellPanelLayout.createSequentialGroup()
                .addGroup(cellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(size6TB)
                    .addComponent(size5BTB)
                    .addComponent(size5ATB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(size4TB)
                    .addComponent(size3TB)
                    .addComponent(size2TB)
                    .addComponent(size1TB)))
        );

        cellPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {size1TB, size2TB, size3TB, size4TB, size5ATB, size5BTB, size6TB});

        markPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("마킹 여부"));

        markedTrueTB.setFocusPainted(false);
        markedTrueTB.setFocusable(false);
        markedTrueTB.setMargin(new java.awt.Insets(2, 2, 2, 2));
        markedTrueTB.setRolloverEnabled(false);

        markedFalseTB.setFocusPainted(false);
        markedFalseTB.setFocusable(false);
        markedFalseTB.setMargin(new java.awt.Insets(2, 2, 2, 2));
        markedFalseTB.setRolloverEnabled(false);

        javax.swing.GroupLayout markPanelLayout = new javax.swing.GroupLayout(markPanel);
        markPanel.setLayout(markPanelLayout);
        markPanelLayout.setHorizontalGroup(
            markPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(markPanelLayout.createSequentialGroup()
                .addComponent(markedTrueTB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markedFalseTB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        markPanelLayout.setVerticalGroup(
            markPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(markPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(markedTrueTB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(markedFalseTB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        resetButton.setText("reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        enhancementPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("강화 레벨 범위"));

        levelMinSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 20, 1));
        levelMinSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        jLabel1.setText("-");

        levelMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(20, 0, 20, 1));
        levelMaxSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        javax.swing.GroupLayout enhancementPanelLayout = new javax.swing.GroupLayout(enhancementPanel);
        enhancementPanel.setLayout(enhancementPanelLayout);
        enhancementPanelLayout.setHorizontalGroup(
            enhancementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(enhancementPanelLayout.createSequentialGroup()
                .addComponent(levelMinSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(levelMaxSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        enhancementPanelLayout.setVerticalGroup(
            enhancementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(enhancementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(levelMinSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addComponent(levelMaxSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ptPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("포인트 범위"));

        dmgTextLabel.setText("살상");
        dmgTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        brkTextLabel.setText("파쇄");
        brkTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        hitTextLabel.setText("정밀");
        hitTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        rldTextLabel.setText("장전");
        rldTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        ptMaxDmgSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMaxBrkSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMaxHitSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMaxRldSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMinDmgSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMinBrkSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMinHitSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        ptMinRldSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        jLabel2.setText("-");

        jLabel3.setText("-");

        jLabel5.setText("-");

        jLabel7.setText("-");

        javax.swing.GroupLayout ptPanelLayout = new javax.swing.GroupLayout(ptPanel);
        ptPanel.setLayout(ptPanelLayout);
        ptPanelLayout.setHorizontalGroup(
            ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptPanelLayout.createSequentialGroup()
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dmgTextLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(brkTextLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(hitTextLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rldTextLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ptMinDmgSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptMinBrkSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptMinHitSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptMinRldSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ptMaxDmgSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptMaxBrkSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptMaxHitSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptMaxRldSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        ptPanelLayout.setVerticalGroup(
            ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptPanelLayout.createSequentialGroup()
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(dmgTextLabel)
                    .addComponent(jLabel2)
                    .addComponent(ptMaxDmgSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ptMinDmgSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(brkTextLabel)
                    .addComponent(ptMinBrkSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(ptMaxBrkSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hitTextLabel)
                    .addComponent(ptMinHitSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(ptMaxHitSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ptPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rldTextLabel)
                    .addComponent(ptMinRldSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(ptMaxRldSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        tagIncludedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("tag included"));
        tagIncludedPanel.setPreferredSize(new java.awt.Dimension(200, 200));
        tagIncludedPanel.setLayout(new java.awt.BorderLayout());

        tagExcludedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("tag excluded"));
        tagExcludedPanel.setPreferredSize(new java.awt.Dimension(200, 200));
        tagExcludedPanel.setLayout(new java.awt.BorderLayout());

        tagResetButton.setText("tag reset");
        tagResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagResetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tagResetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(tagIncludedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagExcludedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tagIncludedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagExcludedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagResetButton))
        );

        presetButton.setText("preset");
        presetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(cellPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(starPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ptPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(enhancementPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(markPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(presetButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(resetButton)
                    .addComponent(presetButton))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(starPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cellPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(markPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enhancementPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ptPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        for (int i = 0; i < Filter.NUM_STAR; i++) {
            app.filter.setStar(i, starTBs[i].isSelected());
        }
        for (int i = 0; i < Filter.NUM_COLOR; i++) {
            app.filter.setColor(i, colorTBs[i].isSelected());
        }
        for (int i = 0; i < Filter.NUM_TYPE; i++) {
            app.filter.setType(i, typeTBs[i].isSelected());
        }
        for (int i = 0; i < Filter.NUM_MARK; i++) {
            app.filter.setMark(i, markedTBs[i].isSelected());
        }
        app.filter.levelMin = getLevelMinSpinnerValue();
        app.filter.levelMax = getLevelMaxSpinnerValue();

        Stat ptMin = app.filter.ptMin;
        ptMin.dmg = (int) ptMinDmgSpinner.getValue();
        ptMin.brk = (int) ptMinBrkSpinner.getValue();
        ptMin.hit = (int) ptMinHitSpinner.getValue();
        ptMin.rld = (int) ptMinRldSpinner.getValue();

        Stat ptMax = app.filter.ptMax;
        ptMax.dmg = (int) ptMaxDmgSpinner.getValue();
        ptMax.brk = (int) ptMaxBrkSpinner.getValue();
        ptMax.hit = (int) ptMaxHitSpinner.getValue();
        ptMax.rld = (int) ptMaxRldSpinner.getValue();

        app.filter.includedTags.clear();
        for (int i = 0; i < tip.getCount(); i++) {
            if (tip.isChecked(i)) {
                app.filter.includedTags.add(tip.getTag(i));
            }
        }

        app.filter.excludedTags.clear();
        for (int i = 0; i < tip.getCount(); i++) {
            if (txp.isChecked(i)) {
                app.filter.excludedTags.add(txp.getTag(i));
            }
        }

        app.mf.display_applyFilterSort();
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        app.filter.reset();
        app.mf.display_applyFilterSort();
        this.dispose();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void tagResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagResetButtonActionPerformed
        for (int i = 0; i < tip.getCount(); i++) {
            tip.setChecked(i, false);
            txp.setChecked(i, false);
        }
    }//GEN-LAST:event_tagResetButtonActionPerformed

    private void presetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetButtonActionPerformed
        applyPreset();
    }//GEN-LAST:event_presetButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel brkTextLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cellPanel;
    private javax.swing.JToggleButton colorBlueTB;
    private javax.swing.JToggleButton colorOrangeTB;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JLabel dmgTextLabel;
    private javax.swing.JPanel enhancementPanel;
    private javax.swing.JLabel hitTextLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSpinner levelMaxSpinner;
    private javax.swing.JSpinner levelMinSpinner;
    private javax.swing.JPanel markPanel;
    private javax.swing.JToggleButton markedFalseTB;
    private javax.swing.JToggleButton markedTrueTB;
    private javax.swing.JButton okButton;
    private javax.swing.JButton presetButton;
    private javax.swing.JSpinner ptMaxBrkSpinner;
    private javax.swing.JSpinner ptMaxDmgSpinner;
    private javax.swing.JSpinner ptMaxHitSpinner;
    private javax.swing.JSpinner ptMaxRldSpinner;
    private javax.swing.JSpinner ptMinBrkSpinner;
    private javax.swing.JSpinner ptMinDmgSpinner;
    private javax.swing.JSpinner ptMinHitSpinner;
    private javax.swing.JSpinner ptMinRldSpinner;
    private javax.swing.JPanel ptPanel;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel rldTextLabel;
    private javax.swing.JToggleButton size1TB;
    private javax.swing.JToggleButton size2TB;
    private javax.swing.JToggleButton size3TB;
    private javax.swing.JToggleButton size4TB;
    private javax.swing.JToggleButton size5ATB;
    private javax.swing.JToggleButton size5BTB;
    private javax.swing.JToggleButton size6TB;
    private javax.swing.JToggleButton star2TB;
    private javax.swing.JToggleButton star3TB;
    private javax.swing.JToggleButton star4TB;
    private javax.swing.JToggleButton star5TB;
    private javax.swing.JPanel starPanel;
    private javax.swing.JPanel tagExcludedPanel;
    private javax.swing.JPanel tagIncludedPanel;
    private javax.swing.JButton tagResetButton;
    // End of variables declaration//GEN-END:variables
}
