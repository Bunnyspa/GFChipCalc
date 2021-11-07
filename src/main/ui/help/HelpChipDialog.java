package main.ui.help;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import main.App;
import main.data.Unit;
import main.puzzle.Chip;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.ui.renderer.ChipListCellRenderer;
import main.ui.renderer.HelpLevelStatTableCellRenderer;
import main.ui.renderer.HelpTableCellRenderer;
import main.ui.resource.AppImage;
import main.ui.resource.AppText;
import main.util.Fn;
import main.util.Rational;
import main.util.Ref;

public class HelpChipDialog extends JDialog {

    private static final double CHIP_SIZE_FACTOR = 0.7;
    private static final int GAP = 5;

    public static final boolean STAT = false;
    public static final boolean LOSS = true;

    private final App app;
    private final DefaultListModel<Chip> alm = new DefaultListModel<>();
    private final DefaultListModel<Chip> blm = new DefaultListModel<>();
    private final DefaultTableModel resonanceTM = new HelpTableModel();
    private final DefaultTableModel percTM = new HelpTableModel();
    private final DefaultTableModel multTM = new HelpTableModel();

    private final DefaultTableModel lossTM = new HelpTableModel();
    private final Ref<Boolean> toggleType = new Ref<>(STAT);

    private final Timer aTimer = new Timer(1000, (e) -> rotateChips());

    private static final boolean SECTION = false;
    // private static final boolean CUMULATIVE = true;
    private boolean resonanceType;

    private class HelpTableModel extends DefaultTableModel {

        @Override
        public Class<?> getColumnClass(int column) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    public HelpChipDialog(App app) {
        this.app = app;
        initComponents();
        init();
    }

    private void init() {
        setTitle(app.getText(AppText.HELP_CHIP));

        aTabbedPane.addTab(app.getText(AppText.HELP_CHIP_INFO_POINT_TITLE), genTextPanel(app.getText(AppText.HELP_CHIP_INFO_POINT_BODY), multiplierPanel));
        aTabbedPane.addTab(app.getText(AppText.HELP_CHIP_INFO_EFFICIENCY_TITLE), genTextPanel(app.getText(AppText.HELP_CHIP_INFO_EFFICIENCY_BODY), efficiencyPanel, chipPanel));
        aTabbedPane.addTab(app.getText(AppText.HELP_CHIP_INFO_COLOR_TITLE), genTextPanel(app.getText(AppText.HELP_CHIP_INFO_COLOR_BODY), bonusPanel));
        aTabbedPane.addTab(app.getText(AppText.HELP_CHIP_INFO_CALC_TITLE), genTextPanel(app.getText(AppText.HELP_CHIP_INFO_CALC_BODY), calcPanel));

        fiveAPanel.setBorder(new TitledBorder(app.getText(AppText.UNIT_CELLTYPE, "5", "A")));
        fiveBPanel.setBorder(new TitledBorder(app.getText(AppText.UNIT_CELLTYPE, "5", "B")));

        fiveAList.setModel(alm);
        fiveAList.setCellRenderer(new ChipListCellRenderer(app, CHIP_SIZE_FACTOR));
        for (Shape shape : Shape.getShapes(Shape.Type._5A)) {
            alm.addElement(new Chip(shape));
        }

        fiveBList.setModel(blm);
        fiveBList.setCellRenderer(new ChipListCellRenderer(app, CHIP_SIZE_FACTOR));
        for (Shape shape : Shape.getShapes(Shape.Type._5B)) {
            blm.addElement(new Chip(shape));
        }

        int chipWidth = (int) (AppImage.Chip.width(false) * CHIP_SIZE_FACTOR);
        int chipHeight = (int) (AppImage.Chip.height(false) * CHIP_SIZE_FACTOR);

        fiveAList.setFixedCellWidth(chipWidth);
        fiveAList.setFixedCellHeight(chipHeight);
        fiveAScrollPane.setPreferredSize(new Dimension(chipWidth * 3 + GAP * 2, chipHeight * 3 + GAP * 2));

        fiveBList.setFixedCellWidth(chipWidth);
        fiveBList.setFixedCellHeight(chipHeight);
        fiveBScrollPane.setPreferredSize(new Dimension(chipWidth * 3 + GAP * 2, chipHeight * 3 + GAP * 2));

        for (Unit unit : Unit.values()) {
            resonanceBoardComboBox.addItem(unit);
        }

        closeButton.setText(app.getText(AppText.ACTION_CLOSE));

        initTables();
        addListeners();

        aTimer.start();

        this.setPreferredSize(app.mf.getPreferredDialogSize());
        pack();
    }

    private void initTables() {
        resonanceTable.setModel(resonanceTM);
        resonanceTable.setDefaultRenderer(String.class, new HelpTableCellRenderer(1, 1));
        String[] resonanceCols = {
            app.getText(AppText.HELP_CHIP_COL_CELL),
            app.getText(AppText.CHIP_STAT_DMG_LONG),
            app.getText(AppText.CHIP_STAT_BRK_LONG),
            app.getText(AppText.CHIP_STAT_HIT_LONG),
            app.getText(AppText.CHIP_STAT_RLD_LONG)
        };
        for (String col : resonanceCols) {
            resonanceTM.addColumn(col);
        }
        resonanceTable.getTableHeader().setUI(null);

        resonanceTM.addRow(resonanceCols);
        int resonanceRowCount = Unit.BGM71.GetResonanceStats().size();
        resonanceTablePanel.setPreferredSize(new Dimension(200, resonanceTable.getRowHeight() * (resonanceRowCount + 1) + GAP));

        percTable.setModel(percTM);
        percTable.setDefaultRenderer(String.class, new HelpTableCellRenderer(1, 1));
        String[] percCols = {
            app.getText(AppText.HELP_CHIP_COL_CELL),
            app.getText(AppText.UNIT_STAR_SHORT, "5"),
            app.getText(AppText.UNIT_STAR_SHORT, "4"),
            app.getText(AppText.UNIT_STAR_SHORT, "3"),
            app.getText(AppText.UNIT_STAR_SHORT, "2")
        };
        for (String col : percCols) {
            percTM.addColumn(col);
        }
        percTable.getTableHeader().setUI(null);

        percTM.addRow(percCols);
        for (Shape.Type row : Shape.Type.values()) {
            if (row == Shape.Type.NONE) {
                continue;
            }
            percTM.addRow(new Object[]{
                row.toString(),
                Fn.iPercStr(Chip.getTypeMult(row, 5).getDouble()),
                Fn.iPercStr(Chip.getTypeMult(row, 4).getDouble()),
                Fn.iPercStr(Chip.getTypeMult(row, 3).getDouble()),
                Fn.iPercStr(Chip.getTypeMult(row, 2).getDouble())
            });
        }
        percTablePanel.setPreferredSize(new Dimension(200, percTable.getRowHeight() * percTable.getRowCount() + GAP));

        multTable.setModel(multTM);
        multTable.setDefaultRenderer(String.class, new HelpTableCellRenderer(0, 1));
        String[] multCols = {
            app.getText(AppText.CHIP_STAT_DMG_LONG),
            app.getText(AppText.CHIP_STAT_BRK_LONG),
            app.getText(AppText.CHIP_STAT_HIT_LONG),
            app.getText(AppText.CHIP_STAT_RLD_LONG)
        };
        for (String col : multCols) {
            multTM.addColumn(col);
        }
        multTable.getTableHeader().setUI(null);

        multTM.addRow(multCols);
        multTM.addRow(new Object[]{Chip.RATE_DMG.getDouble(), Chip.RATE_BRK.getDouble(), Chip.RATE_HIT.getDouble(), Chip.RATE_RLD.getDouble()});
        multTablePanel.setPreferredSize(new Dimension(200, multTable.getRowHeight() * multTable.getRowCount() + GAP));

        setResonanceType(SECTION);

        lossLabel.setText(app.getText(AppText.HELP_CHIP_CALC_DESC));

        lossTable.setModel(lossTM);
        lossTable.setDefaultRenderer(String.class, new HelpLevelStatTableCellRenderer(app, 1, 2, toggleType));
        String[] header1 = new String[Chip.LEVEL_MAX + 2];
        header1[0] = app.getText(AppText.HELP_CHIP_COL_LEVEL);
        for (int level = 0; level <= Chip.LEVEL_MAX; level++) {
            header1[level + 1] = String.valueOf(level);
        }

        String[] header2 = new String[Chip.LEVEL_MAX + 2];
        header2[0] = "<html>&times;</html>";
        for (int level = 0; level <= Chip.LEVEL_MAX; level++) {
            header2[level + 1] = String.valueOf(Chip.getLevelMult(level).getDouble());
        }

        for (String header : header1) {
            lossTM.addColumn(header);
        }
        lossTable.getTableHeader().setUI(null);

        lossTM.addRow(header1);
        lossTM.addRow(header2);

        lossComboBox.addItem(app.getText(AppText.CHIP_STAT_DMG_LONG));
        lossComboBox.addItem(app.getText(AppText.CHIP_STAT_BRK_LONG));
        lossComboBox.addItem(app.getText(AppText.CHIP_STAT_HIT_LONG));
        lossComboBox.addItem(app.getText(AppText.CHIP_STAT_RLD_LONG));

        setLossType(LOSS);

        statScrollPane.setPreferredSize(new Dimension(statScrollPane.getPreferredSize().width, lossTable.getRowHeight() * 7 + GAP));
        updateStatTable();
    }

    private void addListeners() {
        resonanceBoardComboBox.addActionListener((e) -> updateResonance());
        lossComboBox.addActionListener((e) -> updateStatTable());
        Fn.addEscDisposeListener(this);
    }

    private static JPanel genTextPanel(String text, JPanel pageEndPanel) {
        return genTextPanel(text, pageEndPanel, null);
    }

    private static JPanel genTextPanel(String text, JPanel pageEndPanel, JPanel lineEndPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(text);
        label.setVerticalAlignment(JLabel.TOP);

        panel.add(label, BorderLayout.CENTER);

        if (pageEndPanel != null) {
            panel.add(pageEndPanel, BorderLayout.PAGE_END);
        }

        if (lineEndPanel != null) {
            panel.add(lineEndPanel, BorderLayout.LINE_END);
        }

        panel.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));

        return panel;
    }

    private void rotateChips() {
        for (Enumeration<Chip> elements = alm.elements(); elements.hasMoreElements();) {
            Chip c = elements.nextElement();
            c.initRotate(Chip.CLOCKWISE);
        }
        fiveAList.repaint();
        for (Enumeration<Chip> elements = blm.elements(); elements.hasMoreElements();) {
            Chip c = elements.nextElement();
            c.initRotate(Chip.CLOCKWISE);
        }
        fiveBList.repaint();
    }

    private void setResonanceType(boolean type) {
        resonanceType = type;
        if (type == SECTION) {
            resonanceButton.setText(app.getText(AppText.HELP_CHIP_COLOR_SECTION));
        } else {
            resonanceButton.setText(app.getText(AppText.HELP_CHIP_COLOR_CUMULATIVE));
        }
        updateResonance();
    }

    private void setLossType(boolean type) {
        toggleType.v = type;
        if (toggleType.v == STAT) {
            lossButton.setText(app.getText(AppText.HELP_CHIP_CALC_STAT));
        } else {
            lossButton.setText(app.getText(AppText.HELP_CHIP_CALC_LOSS));
        }
        updateStatTable();
    }

    private void updateResonance() {
        Unit unit = resonanceBoardComboBox.getItemAt(resonanceBoardComboBox.getSelectedIndex());

        Unit.Color color = unit.getColor();
        boardLabel.setText(app.getText(AppText.CHIP_COLOR) + ": " + app.getText(AppText.TEXT_MAP_COLOR.get(color)));
        Set<Integer> steps = unit.GetResonanceStats().keySet();

        List<Integer> resonanceSteps = new ArrayList<>(steps);
        resonanceSteps.sort(Collections.reverseOrder());

        resonanceTM.setRowCount(1);
        if (resonanceType == SECTION) {
            resonanceSteps.forEach((step) -> {
                Stat stat = unit.GetResonanceStats().get(step);
                resonanceTM.addRow(new Integer[]{step, stat.dmg, stat.brk, stat.hit, stat.rld});
            });
        } else {
            for (Integer step : resonanceSteps) {
                List<Stat> stats = new ArrayList<>();
                for (Integer key : steps) {
                    if (key <= step) {
                        stats.add(unit.GetResonanceStats().get(key));
                    }
                }
                Stat s = new Stat(stats);
                resonanceTM.addRow(new Integer[]{step, s.dmg, s.brk, s.hit, s.rld});
            }
        }
    }

    private void updateStatTable() {
        lossTM.setRowCount(2);
        for (int pt = 1; pt <= 5; pt++) {
            String[] rows = new String[Chip.LEVEL_MAX + 2];
            rows[0] = pt + "pt";
            for (int level = 0; level <= Chip.LEVEL_MAX; level++) {
                if (toggleType.v == STAT) {
                    rows[level + 1] = String.valueOf(Chip.getMaxEffStat(getRate(), pt, level));
                } else {
                    int one = Chip.getMaxEffStat(getRate(), 1, level);
                    int current = Chip.getMaxEffStat(getRate(), pt, level);
                    int value = current - one * pt;
                    rows[level + 1] = String.valueOf(value);
                }
            }
            lossTM.addRow(rows);
        }
    }

    private Rational getRate() {
        switch (lossComboBox.getSelectedIndex()) {
            case 0:
                return Chip.RATE_DMG;
            case 1:
                return Chip.RATE_BRK;
            case 2:
                return Chip.RATE_HIT;
            case 3:
                return Chip.RATE_RLD;
            default:
                throw new AssertionError();
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

        efficiencyPanel = new javax.swing.JPanel();
        percTablePanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        percTable = new javax.swing.JTable();
        calcPanel = new javax.swing.JPanel();
        lossLabel = new javax.swing.JLabel();
        lossButton = new javax.swing.JButton();
        lossComboBox = new javax.swing.JComboBox<>();
        statScrollPane = new javax.swing.JScrollPane();
        lossTable = new javax.swing.JTable();
        bonusPanel = new javax.swing.JPanel();
        resonanceBoardComboBox = new javax.swing.JComboBox<>();
        boardLabel = new javax.swing.JLabel();
        resonanceTablePanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        resonanceTable = new javax.swing.JTable();
        resonanceButton = new javax.swing.JButton();
        multiplierPanel = new javax.swing.JPanel();
        multTablePanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        multTable = new javax.swing.JTable();
        chipPanel = new javax.swing.JPanel();
        fiveAPanel = new javax.swing.JPanel();
        fiveAScrollPane = new javax.swing.JScrollPane();
        fiveAList = new javax.swing.JList<>();
        fiveBPanel = new javax.swing.JPanel();
        fiveBScrollPane = new javax.swing.JScrollPane();
        fiveBList = new javax.swing.JList<>();
        closeButton = new javax.swing.JButton();
        aTabbedPane = new javax.swing.JTabbedPane();

        jScrollPane3.setPreferredSize(new java.awt.Dimension(100, 100));

        percTable.setFocusable(false);
        percTable.setRowSelectionAllowed(false);
        jScrollPane3.setViewportView(percTable);

        javax.swing.GroupLayout percTablePanelLayout = new javax.swing.GroupLayout(percTablePanel);
        percTablePanel.setLayout(percTablePanelLayout);
        percTablePanelLayout.setHorizontalGroup(
            percTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        percTablePanelLayout.setVerticalGroup(
            percTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout efficiencyPanelLayout = new javax.swing.GroupLayout(efficiencyPanel);
        efficiencyPanel.setLayout(efficiencyPanelLayout);
        efficiencyPanelLayout.setHorizontalGroup(
            efficiencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(percTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        efficiencyPanelLayout.setVerticalGroup(
            efficiencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(percTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        lossLabel.setText("<html><b>손실 계산</b>: (P포인트일 때의 스텟) - P &times; (1포인트일 때의 스텟)</html>");

        lossButton.setText("스텟");
        lossButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lossButtonActionPerformed(evt);
            }
        });

        statScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        lossTable.setFocusable(false);
        lossTable.setRowSelectionAllowed(false);
        statScrollPane.setViewportView(lossTable);

        javax.swing.GroupLayout calcPanelLayout = new javax.swing.GroupLayout(calcPanel);
        calcPanel.setLayout(calcPanelLayout);
        calcPanelLayout.setHorizontalGroup(
            calcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calcPanelLayout.createSequentialGroup()
                .addComponent(lossLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lossButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lossComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(statScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        calcPanelLayout.setVerticalGroup(
            calcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calcPanelLayout.createSequentialGroup()
                .addGroup(calcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lossComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lossButton)
                    .addComponent(lossLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        resonanceBoardComboBox.setPreferredSize(new java.awt.Dimension(100, 21));

        boardLabel.setText("jLabel1");

        jScrollPane4.setPreferredSize(new java.awt.Dimension(100, 100));

        resonanceTable.setFocusable(false);
        resonanceTable.setRowSelectionAllowed(false);
        jScrollPane4.setViewportView(resonanceTable);

        javax.swing.GroupLayout resonanceTablePanelLayout = new javax.swing.GroupLayout(resonanceTablePanel);
        resonanceTablePanel.setLayout(resonanceTablePanelLayout);
        resonanceTablePanelLayout.setHorizontalGroup(
            resonanceTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        resonanceTablePanelLayout.setVerticalGroup(
            resonanceTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        resonanceButton.setText("구간");
        resonanceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resonanceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bonusPanelLayout = new javax.swing.GroupLayout(bonusPanel);
        bonusPanel.setLayout(bonusPanelLayout);
        bonusPanelLayout.setHorizontalGroup(
            bonusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resonanceTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(resonanceBoardComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(bonusPanelLayout.createSequentialGroup()
                .addComponent(boardLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resonanceButton))
        );
        bonusPanelLayout.setVerticalGroup(
            bonusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bonusPanelLayout.createSequentialGroup()
                .addComponent(resonanceBoardComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bonusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resonanceButton)
                    .addComponent(boardLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resonanceTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane5.setPreferredSize(new java.awt.Dimension(100, 100));

        multTable.setFocusable(false);
        multTable.setRowSelectionAllowed(false);
        jScrollPane5.setViewportView(multTable);

        javax.swing.GroupLayout multTablePanelLayout = new javax.swing.GroupLayout(multTablePanel);
        multTablePanel.setLayout(multTablePanelLayout);
        multTablePanelLayout.setHorizontalGroup(
            multTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        multTablePanelLayout.setVerticalGroup(
            multTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout multiplierPanelLayout = new javax.swing.GroupLayout(multiplierPanel);
        multiplierPanel.setLayout(multiplierPanelLayout);
        multiplierPanelLayout.setHorizontalGroup(
            multiplierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(multTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        multiplierPanelLayout.setVerticalGroup(
            multiplierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(multTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        fiveAPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("5칸 A형 칩"));

        fiveAScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fiveAScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        fiveAScrollPane.setPreferredSize(new java.awt.Dimension(130, 130));

        fiveAList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fiveAList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        fiveAList.setVisibleRowCount(-1);
        fiveAScrollPane.setViewportView(fiveAList);

        javax.swing.GroupLayout fiveAPanelLayout = new javax.swing.GroupLayout(fiveAPanel);
        fiveAPanel.setLayout(fiveAPanelLayout);
        fiveAPanelLayout.setHorizontalGroup(
            fiveAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fiveAScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        fiveAPanelLayout.setVerticalGroup(
            fiveAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fiveAScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        fiveBPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("5칸 B형 칩"));

        fiveBScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fiveBScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        fiveBScrollPane.setPreferredSize(new java.awt.Dimension(130, 130));

        fiveBList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fiveBList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        fiveBList.setVisibleRowCount(-1);
        fiveBScrollPane.setViewportView(fiveBList);

        javax.swing.GroupLayout fiveBPanelLayout = new javax.swing.GroupLayout(fiveBPanel);
        fiveBPanel.setLayout(fiveBPanelLayout);
        fiveBPanelLayout.setHorizontalGroup(
            fiveBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fiveBScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        fiveBPanelLayout.setVerticalGroup(
            fiveBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fiveBScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout chipPanelLayout = new javax.swing.GroupLayout(chipPanel);
        chipPanel.setLayout(chipPanelLayout);
        chipPanelLayout.setHorizontalGroup(
            chipPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fiveAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(fiveBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        chipPanelLayout.setVerticalGroup(
            chipPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chipPanelLayout.createSequentialGroup()
                .addComponent(fiveAPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fiveBPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("칩셋 가이드");
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setType(java.awt.Window.Type.UTILITY);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        closeButton.setText("닫기");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(aTabbedPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void resonanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resonanceButtonActionPerformed
        setResonanceType(!resonanceType);
    }//GEN-LAST:event_resonanceButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        aTimer.stop();
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void lossButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lossButtonActionPerformed
        setLossType(!toggleType.v);
    }//GEN-LAST:event_lossButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane aTabbedPane;
    private javax.swing.JLabel boardLabel;
    private javax.swing.JPanel bonusPanel;
    private javax.swing.JPanel calcPanel;
    private javax.swing.JPanel chipPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel efficiencyPanel;
    private javax.swing.JList<Chip> fiveAList;
    private javax.swing.JPanel fiveAPanel;
    private javax.swing.JScrollPane fiveAScrollPane;
    private javax.swing.JList<Chip> fiveBList;
    private javax.swing.JPanel fiveBPanel;
    private javax.swing.JScrollPane fiveBScrollPane;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton lossButton;
    private javax.swing.JComboBox<String> lossComboBox;
    private javax.swing.JLabel lossLabel;
    private javax.swing.JTable lossTable;
    private javax.swing.JTable multTable;
    private javax.swing.JPanel multTablePanel;
    private javax.swing.JPanel multiplierPanel;
    private javax.swing.JTable percTable;
    private javax.swing.JPanel percTablePanel;
    private javax.swing.JComboBox<Unit> resonanceBoardComboBox;
    private javax.swing.JButton resonanceButton;
    private javax.swing.JTable resonanceTable;
    private javax.swing.JPanel resonanceTablePanel;
    private javax.swing.JScrollPane statScrollPane;
    // End of variables declaration//GEN-END:variables
}
