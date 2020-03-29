package main.ui.dialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import main.App;
import main.puzzle.Board;
import main.puzzle.FStat;
import main.puzzle.Stat;
import main.resource.Language;
import main.resource.Resources;
import main.setting.BoardSetting;
import main.setting.Filter;
import main.setting.Setting;
import main.setting.StatPresetMap;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class CalcSettingDialog extends JDialog {

    private final App app;
    private int mode, presetIndex;
    private Stat stat, pt;
    private boolean radioLoading;
    private int markType;

    private final String name;
    private final int star;

    private static final int MARK_MIN = 0;
    private static final int MARK_MAX = 64;

    public static CalcSettingDialog getInstance(App app) {
        return new CalcSettingDialog(app);
    }

    private CalcSettingDialog(App app) {
        initComponents();
        this.app = app;
        name = app.mf.getBoardName();
        star = app.mf.getBoardStar();
        init();
        addListeners();
    }

    private void init() {
        Setting setting = app.setting;

        dmgTextLabel.setIcon(Resources.DMG);
        brkTextLabel.setIcon(Resources.BRK);
        hitTextLabel.setIcon(Resources.HIT);
        rldTextLabel.setIcon(Resources.RLD);

        setTitle(app.getText(Language.CSET_TITLE));
        okButton.setText(app.getText(Language.ACTION_OK));
        cancelButton.setText(app.getText(Language.ACTION_CANCEL));

        // Group
        statPanel.setBorder(new TitledBorder(app.getText(Language.CSET_GROUP_STAT)));
        markPanel.setBorder(new TitledBorder(app.getText(Language.CSET_GROUP_MARK)));
        sortPanel.setBorder(new TitledBorder(app.getText(Language.CSET_GROUP_SORT)));
        miscPanel.setBorder(new TitledBorder(app.getText(Language.CSET_GROUP_MISC)));

        // Stat
        maxNormalRadioButton.setText(app.getText(Language.CSET_DEFAULT_STAT));
        maxStatRadioButton.setText(app.getText(Language.CSET_STAT));
        maxPtRadioButton.setText(app.getText(Language.CSET_PT));
        maxPresetRadioButton.setText(app.getText(Language.CSET_PRESET));

        dmgTextLabel.setText(app.getText(Language.CHIP_STAT_DMG));
        brkTextLabel.setText(app.getText(Language.CHIP_STAT_BRK));
        hitTextLabel.setText(app.getText(Language.CHIP_STAT_HIT));
        rldTextLabel.setText(app.getText(Language.CHIP_STAT_RLD));

        // Mark
        markDescLabel.setText(app.getText(Language.CSET_MARK_DESC));

        // Sort
        sortTicketRadioButton.setText(app.getText(Language.CSET_SORT_TICKET));
        sortXPRadioButton.setText(app.getText(Language.CSET_SORT_XP));

        // Misc.
        maxLevelCheckBox.setText(app.getText(Language.CSET_MAXLEVEL_DESC));
        colorCheckBox.setText(app.getText(Language.CSET_COLOR_DESC));
        rotationCheckBox.setText(app.getText(Language.CSET_ROTATION_DESC));

        //// Load settings //// 
        mode = setting.board.getMode(name, star);
        stat = setting.board.getStat(name, star);
        pt = setting.board.getPt(name, star);
        presetIndex = setting.board.getPresetIndex(name, star);
        if (star != 5 || !BoardSetting.PRESET.containsKey(name, star)) {
            maxPresetRadioButton.setEnabled(false);
            maxPresetComboBox.setVisible(false);
        }

        pack();

        // Stat
        switch (mode) {
            case BoardSetting.MAX_STAT:
                maxStatRadioButton.setSelected(true);
                break;
            case BoardSetting.MAX_PT:
                maxPtRadioButton.setSelected(true);
                break;
            case BoardSetting.MAX_PRESET:
                maxPresetRadioButton.setSelected(true);
                break;
            default:
                maxNormalRadioButton.setSelected(true);
        }
        maxRadioEvent();

        // Mark
        markMinSpinner.setModel(new SpinnerNumberModel(
                Fn.limit(setting.boardMarkMin, MARK_MIN, MARK_MAX),
                MARK_MIN, MARK_MAX, 1)
        );
        markMaxSpinner.setModel(new SpinnerNumberModel(
                Fn.limit(setting.boardMarkMax, MARK_MIN, MARK_MAX),
                MARK_MIN, MARK_MAX, 1)
        );
        setMarkType(setting.boardMarkType);

        // Sort
        switch (setting.boardSortType) {
            case Setting.BOARD_SORTTYPE_XP:
                sortXPRadioButton.setSelected(true);
                break;
            default:
                sortTicketRadioButton.setSelected(true);
        }

        // Misc.
        maxLevelCheckBox.setSelected(setting.chipLevelMax);
        colorCheckBox.setSelected(setting.chipMatchColor);
        rotationCheckBox.setSelected(setting.chipAllowRotation);
    }

    private void addListeners() {
        maxNormalRadioButton.addItemListener((e) -> maxRadioEvent());
        maxStatRadioButton.addItemListener((e) -> maxRadioEvent());
        maxPtRadioButton.addItemListener((e) -> maxRadioEvent());
        maxPresetRadioButton.addItemListener((e) -> maxRadioEvent());

        maxPresetComboBox.addActionListener((e) -> maxComboBoxEvent());

        maxDmgSpinner.addChangeListener((e) -> maxSpinnerEvent());
        maxBrkSpinner.addChangeListener((e) -> maxSpinnerEvent());
        maxHitSpinner.addChangeListener((e) -> maxSpinnerEvent());
        maxRldSpinner.addChangeListener((e) -> maxSpinnerEvent());

        markMinSpinner.addChangeListener((e) -> markSpinnerEvent(false));
        markMaxSpinner.addChangeListener((e) -> markSpinnerEvent(true));

        Fn.addEscDisposeListener(this);
    }

    private void maxRadioEvent() {
        radioLoading = true;
        if (maxPtRadioButton.isSelected()) {
            mode = BoardSetting.MAX_PT;
        } else if (maxStatRadioButton.isSelected()) {
            mode = BoardSetting.MAX_STAT;
        } else if (maxPresetRadioButton.isSelected()) {
            mode = BoardSetting.MAX_PRESET;
        } else {
            mode = BoardSetting.MAX_DEFAULT;
        }

        boolean editable = mode == BoardSetting.MAX_STAT || mode == BoardSetting.MAX_PT;
        maxDmgSpinner.setEnabled(editable);
        maxBrkSpinner.setEnabled(editable);
        maxHitSpinner.setEnabled(editable);
        maxRldSpinner.setEnabled(editable);

        maxPresetComboBox.setVisible(mode == BoardSetting.MAX_PRESET);
        maxPresetComboBox.removeAllItems();

        switch (mode) {
            case BoardSetting.MAX_STAT:
                maxDmgSpinner.setValue(stat.dmg);
                maxBrkSpinner.setValue(stat.brk);
                maxHitSpinner.setValue(stat.hit);
                maxRldSpinner.setValue(stat.rld);
                break;
            case BoardSetting.MAX_PT:
                maxDmgSpinner.setValue(pt.dmg);
                maxBrkSpinner.setValue(pt.brk);
                maxHitSpinner.setValue(pt.hit);
                maxRldSpinner.setValue(pt.rld);
                break;
            case BoardSetting.MAX_PRESET:
                BoardSetting.PRESET.getStrings(app, name, star).forEach((item) -> {
                    maxPresetComboBox.addItem(item);
                });
                maxPresetComboBox.setSelectedIndex(presetIndex < maxPresetComboBox.getItemCount() ? presetIndex : 0);
                break;
            default:
                Stat maxStat = Board.getMaxStat(getBoardName(), getBoardStar());
                maxDmgSpinner.setValue(maxStat.dmg);
                maxBrkSpinner.setValue(maxStat.brk);
                maxHitSpinner.setValue(maxStat.hit);
                maxRldSpinner.setValue(maxStat.rld);
                break;
        }
        radioLoading = false;
        maxComboBoxEvent();
        maxSpinnerEvent();
    }

    private void maxComboBoxEvent() {
        if (!radioLoading) {
            int i = maxPresetComboBox.getSelectedIndex();
            if (0 <= i) {
                presetIndex = i;
                FStat presetStat = BoardSetting.PRESET.get(getBoardName(), star, i).stat;
                maxDmgSpinner.setValue(presetStat.dmg);
                maxBrkSpinner.setValue(presetStat.brk);
                maxHitSpinner.setValue(presetStat.hit);
                maxRldSpinner.setValue(presetStat.rld);
            }
        }
    }

    private void maxSpinnerEvent() {
        if (!radioLoading) {
            Stat spinnerStat = new Stat(
                    (int) maxDmgSpinner.getValue(),
                    (int) maxBrkSpinner.getValue(),
                    (int) maxHitSpinner.getValue(),
                    (int) maxRldSpinner.getValue()
            );
            switch (mode) {
                case BoardSetting.MAX_STAT:
                    stat = spinnerStat;
                    break;
                case BoardSetting.MAX_PT:
                    pt = spinnerStat;
                    break;
                default:
                    break;
            }

            int total = Board.getCellCount(getBoardName(), getBoardStar());
            if (maxPtRadioButton.isSelected()) {
                ptSumLabel.setText(app.getText(Language.UNIT_PT, String.valueOf(pt.sum() + "/" + total)));
            } else {
                ptSumLabel.setText(app.getText(Language.UNIT_PT, String.valueOf(total)));
            }
        }
    }

    private void markSpinnerEvent(boolean isMax) {
        int min = (int) markMinSpinner.getValue();
        int max = (int) markMaxSpinner.getValue();
        if (min > max) {
            if (isMax) {
                markMinSpinner.setValue(max);
            } else {
                markMaxSpinner.setValue(min);
            }
        }
    }

    private void setMarkType(int t) {
        this.markType = t % Setting.NUM_BOARD_MARKTYPE;
        switch (markType) {
            case Setting.BOARD_MARKTYPE_CHIP:
                markTypeButton.setText(app.getText(Language.CSET_MARK_CHIP));
                break;
            default:
                markTypeButton.setText(app.getText(Language.CSET_MARK_CELL));
        }
    }

    private String getBoardName() {
        return app.mf.getBoardName();
    }

    private int getBoardStar() {
        return app.mf.getBoardStar();
    }

    private void setFilter(boolean[] stars, boolean[] colors, boolean[] types, Stat ptMin, Stat ptMax) {
        app.filter.setColors(colors);
        app.filter.setStars(stars);
        app.filter.setTypes(types);
        app.filter.ptMin = ptMin;
        app.filter.ptMax = ptMax;

        app.mf.display_applyFilterSort();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        maxButtonGroup = new javax.swing.ButtonGroup();
        sortButtonGroup = new javax.swing.ButtonGroup();
        miscPanel = new javax.swing.JPanel();
        maxLevelCheckBox = new javax.swing.JCheckBox();
        colorCheckBox = new javax.swing.JCheckBox();
        rotationCheckBox = new javax.swing.JCheckBox();
        statPanel = new javax.swing.JPanel();
        maxPanel = new javax.swing.JPanel();
        dmgTextLabel = new javax.swing.JLabel();
        brkTextLabel = new javax.swing.JLabel();
        hitTextLabel = new javax.swing.JLabel();
        rldTextLabel = new javax.swing.JLabel();
        maxDmgSpinner = new javax.swing.JSpinner();
        maxBrkSpinner = new javax.swing.JSpinner();
        maxHitSpinner = new javax.swing.JSpinner();
        maxRldSpinner = new javax.swing.JSpinner();
        ptSumLabel = new javax.swing.JLabel();
        maxPresetComboBox = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        maxNormalRadioButton = new javax.swing.JRadioButton();
        maxPresetRadioButton = new javax.swing.JRadioButton();
        maxPtRadioButton = new javax.swing.JRadioButton();
        maxStatRadioButton = new javax.swing.JRadioButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        markPanel = new javax.swing.JPanel();
        markTypeButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        markMaxSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        markMinSpinner = new javax.swing.JSpinner();
        markDescLabel = new javax.swing.JLabel();
        sortPanel = new javax.swing.JPanel();
        sortTicketRadioButton = new javax.swing.JRadioButton();
        sortXPRadioButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("조합 설정");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        miscPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("기본"));

        maxLevelCheckBox.setSelected(true);
        maxLevelCheckBox.setText("level");

        colorCheckBox.setSelected(true);
        colorCheckBox.setText("color");

        rotationCheckBox.setSelected(true);
        rotationCheckBox.setText("rotation");

        javax.swing.GroupLayout miscPanelLayout = new javax.swing.GroupLayout(miscPanel);
        miscPanel.setLayout(miscPanelLayout);
        miscPanelLayout.setHorizontalGroup(
            miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(miscPanelLayout.createSequentialGroup()
                .addGroup(miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxLevelCheckBox)
                    .addComponent(colorCheckBox)
                    .addComponent(rotationCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        miscPanelLayout.setVerticalGroup(
            miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(miscPanelLayout.createSequentialGroup()
                .addComponent(maxLevelCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotationCheckBox)
                .addGap(0, 0, 0))
        );

        statPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("최대치 설정"));

        dmgTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        dmgTextLabel.setText("D");
        dmgTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        brkTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        brkTextLabel.setText("B");
        brkTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        hitTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        hitTextLabel.setText("H");
        hitTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        rldTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        rldTextLabel.setText("R");
        rldTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        maxDmgSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        maxDmgSpinner.setEnabled(false);
        maxDmgSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        maxBrkSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        maxBrkSpinner.setEnabled(false);
        maxBrkSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        maxHitSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        maxHitSpinner.setEnabled(false);
        maxHitSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        maxRldSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        maxRldSpinner.setEnabled(false);
        maxRldSpinner.setPreferredSize(new java.awt.Dimension(50, 22));

        javax.swing.GroupLayout maxPanelLayout = new javax.swing.GroupLayout(maxPanel);
        maxPanel.setLayout(maxPanelLayout);
        maxPanelLayout.setHorizontalGroup(
            maxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxPanelLayout.createSequentialGroup()
                .addComponent(dmgTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxDmgSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(maxPanelLayout.createSequentialGroup()
                .addComponent(brkTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxBrkSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(maxPanelLayout.createSequentialGroup()
                .addComponent(hitTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxHitSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(maxPanelLayout.createSequentialGroup()
                .addComponent(rldTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxRldSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        maxPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {brkTextLabel, dmgTextLabel, hitTextLabel, rldTextLabel});

        maxPanelLayout.setVerticalGroup(
            maxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, maxPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(maxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dmgTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxDmgSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addGroup(maxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(brkTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxBrkSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addGroup(maxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hitTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxHitSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addGroup(maxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rldTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxRldSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        ptSumLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ptSumLabel.setText("pt: 123");

        maxPresetComboBox.setPreferredSize(new java.awt.Dimension(100, 21));

        maxButtonGroup.add(maxNormalRadioButton);
        maxNormalRadioButton.setSelected(true);
        maxNormalRadioButton.setText("default");

        maxButtonGroup.add(maxPresetRadioButton);
        maxPresetRadioButton.setText("preset");

        maxButtonGroup.add(maxPtRadioButton);
        maxPtRadioButton.setText("pt");

        maxButtonGroup.add(maxStatRadioButton);
        maxStatRadioButton.setText("stat");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(maxNormalRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPresetRadioButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(maxStatRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPtRadioButton)))
                .addGap(24, 24, 24))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxNormalRadioButton)
                    .addComponent(maxPresetRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxStatRadioButton)
                    .addComponent(maxPtRadioButton)))
        );

        javax.swing.GroupLayout statPanelLayout = new javax.swing.GroupLayout(statPanel);
        statPanel.setLayout(statPanelLayout);
        statPanelLayout.setHorizontalGroup(
            statPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ptSumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(maxPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(maxPresetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        statPanelLayout.setVerticalGroup(
            statPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statPanelLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxPresetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(maxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ptSumLabel))
        );

        cancelButton.setText("cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        markPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("mark"));

        markTypeButton.setText("type");
        markTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markTypeButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("-");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(markMinSpinner)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markMaxSpinner))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(markMinSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1)
                .addComponent(markMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        markDescLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        markDescLabel.setText("desc");

        javax.swing.GroupLayout markPanelLayout = new javax.swing.GroupLayout(markPanel);
        markPanel.setLayout(markPanelLayout);
        markPanelLayout.setHorizontalGroup(
            markPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(markPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markTypeButton))
            .addComponent(markDescLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        markPanelLayout.setVerticalGroup(
            markPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(markPanelLayout.createSequentialGroup()
                .addGroup(markPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(markTypeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markDescLabel))
        );

        sortPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("sort"));

        sortButtonGroup.add(sortTicketRadioButton);
        sortTicketRadioButton.setText("ticket");

        sortButtonGroup.add(sortXPRadioButton);
        sortXPRadioButton.setText("xp");

        javax.swing.GroupLayout sortPanelLayout = new javax.swing.GroupLayout(sortPanel);
        sortPanel.setLayout(sortPanelLayout);
        sortPanelLayout.setHorizontalGroup(
            sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sortPanelLayout.createSequentialGroup()
                .addComponent(sortTicketRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sortXPRadioButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        sortPanelLayout.setVerticalGroup(
            sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(sortTicketRadioButton)
                .addComponent(sortXPRadioButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(miscPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(markPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sortPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(markPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(miscPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelButton)
                            .addComponent(okButton))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        Setting setting = app.setting;

        setting.chipLevelMax = maxLevelCheckBox.isSelected();
        setting.chipMatchColor = colorCheckBox.isSelected();
        setting.chipAllowRotation = rotationCheckBox.isSelected();

        setting.board.setMode(name, star, mode);
        setting.board.setPt(name, star, pt);
        setting.board.setStat(name, star, stat);
        setting.board.setPresetIndex(name, star, presetIndex);

        setting.boardMarkType = markType;
        setting.boardMarkMin = (int) markMinSpinner.getValue();
        setting.boardMarkMax = (int) markMaxSpinner.getValue();

        if (sortXPRadioButton.isSelected()) {
            setting.boardSortType = Setting.BOARD_SORTTYPE_XP;
        } else {
            setting.boardSortType = Setting.BOARD_SORTTYPE_TICKET;
        }

        app.mf.setting_resetDisplay(); 
        app.mf.settingFile_save();

        // Preset - Apply Filter
        if (mode == BoardSetting.MAX_PRESET) {
            boolean[] stars = new boolean[]{true, false, false, false};

            boolean[] colors = new boolean[Filter.NUM_COLOR];
            int c = Board.getColor(name);
            if (c < colors.length) {
                colors[c] = true;
            }

            StatPresetMap presetMap = BoardSetting.PRESET;
            boolean[] types = presetMap.getTypeFilter(name, star, presetIndex);

            Stat ptMin = presetMap.get(name, star, presetIndex).ptMin.toStat();
            Stat ptMax = presetMap.get(name, star, presetIndex).ptMax.toStat();

            if (!app.filter.equals(stars, types, ptMin, ptMax)) {
                int retval = JOptionPane.showConfirmDialog(this,
                        app.getText(Language.CSET_CONFIRM_FILTER_BODY), app.getText(Language.CSET_CONFIRM_FILTER_TITLE),
                        JOptionPane.YES_NO_OPTION);
                if (retval == JOptionPane.YES_OPTION) {
                    setFilter(stars, colors, types, ptMin, ptMax);
                }
            }
        }

        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void markTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markTypeButtonActionPerformed
        setMarkType(markType + 1);
    }//GEN-LAST:event_markTypeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel brkTextLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JLabel dmgTextLabel;
    private javax.swing.JLabel hitTextLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel markDescLabel;
    private javax.swing.JSpinner markMaxSpinner;
    private javax.swing.JSpinner markMinSpinner;
    private javax.swing.JPanel markPanel;
    private javax.swing.JButton markTypeButton;
    private javax.swing.JSpinner maxBrkSpinner;
    private javax.swing.ButtonGroup maxButtonGroup;
    private javax.swing.JSpinner maxDmgSpinner;
    private javax.swing.JSpinner maxHitSpinner;
    private javax.swing.JCheckBox maxLevelCheckBox;
    private javax.swing.JRadioButton maxNormalRadioButton;
    private javax.swing.JPanel maxPanel;
    private javax.swing.JComboBox<String> maxPresetComboBox;
    private javax.swing.JRadioButton maxPresetRadioButton;
    private javax.swing.JRadioButton maxPtRadioButton;
    private javax.swing.JSpinner maxRldSpinner;
    private javax.swing.JRadioButton maxStatRadioButton;
    private javax.swing.JPanel miscPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel ptSumLabel;
    private javax.swing.JLabel rldTextLabel;
    private javax.swing.JCheckBox rotationCheckBox;
    private javax.swing.ButtonGroup sortButtonGroup;
    private javax.swing.JPanel sortPanel;
    private javax.swing.JRadioButton sortTicketRadioButton;
    private javax.swing.JRadioButton sortXPRadioButton;
    private javax.swing.JPanel statPanel;
    // End of variables declaration//GEN-END:variables
}
