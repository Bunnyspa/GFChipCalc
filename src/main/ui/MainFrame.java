package main.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.App;
import main.data.Unit;
import main.http.ResearchConnection;
import main.json.JsonParser;
import main.puzzle.Board;
import main.puzzle.BoardTemplate;
import main.puzzle.Chip;
import main.puzzle.Shape;
import main.puzzle.Stat;
import main.puzzle.Tag;
import main.puzzle.assembly.Assembler;
import main.puzzle.assembly.AssemblyResult;
import main.puzzle.assembly.CalcExtraSetting;
import main.puzzle.assembly.CalcSetting;
import main.puzzle.assembly.ChipFreq;
import main.puzzle.assembly.Progress;
import main.puzzle.assembly.ProgressFile;
import main.setting.BoardSetting;
import main.setting.Setting;
import main.ui.dialog.AppSettingDialog;
import main.ui.dialog.ApplyDialog;
import main.ui.dialog.CalcSettingDialog;
import main.ui.dialog.DonationDialog;
import main.ui.dialog.FilterDialog;
import main.ui.dialog.ImageDialog;
import main.ui.dialog.JsonFilterDialog;
import main.ui.dialog.ProxyDialog;
import main.ui.dialog.StatDialog;
import main.ui.dialog.TagDialog;
import main.ui.help.HelpDialog;
import main.ui.renderer.ChipFreqListCellRenderer;
import main.ui.renderer.ChipListCellRenderer;
import main.ui.renderer.CombListCellRenderer;
import main.ui.renderer.InvListCellRenderer;
import main.ui.resource.AppColor;
import main.ui.resource.AppFont;
import main.ui.resource.AppImage;
import main.ui.resource.AppText;
import main.ui.shortcut.ShortcutKeyAdapter;
import main.ui.tip.TipMouseListener;
import main.ui.transfer.InvListTransferHandler;
import main.util.Fn;
import main.util.IO;
import main.util.Ref;

public class MainFrame extends JFrame {

    /* STATIC */
    // UI
    private static final int BORDERSIZE = 3;

    // Chip Stat
    private static final int FOCUSED_NONE = -1;
    private static final int FOCUSED_DMG = 0;
    // private static final int FOCUSED_BRK = 1;
    // private static final int FOCUSED_HIT = 2;
    // private static final int FOCUSED_RLD = 3;
    private static final int INPUT_BUFFER_SIZE = 3;

    // Sort
    private static final int SORT_NONE = 0;
    private static final int SORT_SIZE = 1;
    private static final int SORT_LEVEL = 2;
    private static final int SORT_STAR = 3;
    private static final int SORT_DMG = 4;
    private static final int SORT_BRK = 5;
    private static final int SORT_HIT = 6;
    private static final int SORT_RLD = 7;

    // Calculator
    private static final int SIZE_DONETIME = 100;

    // Setting
    private static final boolean ASCENDING = Setting.ASCENDING;
    private static final boolean DESCENDING = Setting.DESCENDING;
    private static final int DISPLAY_STAT = Setting.DISPLAY_STAT;

    /* VARIABLES */
    private final App app;

    // UI
    private Dimension initSize;
    private Border onBorder;
    private final Border offBorder = new LineBorder(this.getBackground(), BORDERSIZE);
    private final TipMouseListener tml;

    // Chip
    private final List<Chip> invChips = new ArrayList<>();

    // File
    private final JFileChooser iofc = new JFileChooser(new File(".")); // Inventory File Chooser
    private final JFileChooser isfc = new JFileChooser(new File(".")); // Inventory File Chooser
    private final JFileChooser cfc = new JFileChooser(new File(".")); // Combination File Chooser
    private String invFile_path = "";

    // List
    private final DefaultListModel<Chip> poolLM = new DefaultListModel<>(),
            invLM = new DefaultListModel<>(),
            combChipLM = new DefaultListModel<>();
    private final DefaultListModel<Board> combLM = new DefaultListModel<>();
    private final DefaultListModel<ChipFreq> combFreqLM = new DefaultListModel<>();

    private int invListMouseDragIndex;
    private final Ref<Boolean> blink = new Ref<>(false);
    private final Timer blinkTimer;

    // Chip Stat 
    private boolean invStat_loading;
    private Unit.Color invStat_color = null;
    private int focusedStat = FOCUSED_NONE;
    private final List<Integer> statInputBuffer = new ArrayList<>(INPUT_BUFFER_SIZE + 1);

    // Sort
    private boolean inv_order = DESCENDING;

    // Calculator
    private final Assembler assembler = new Assembler(new Assembler.Intermediate() {
        @Override
        public void stop() {
            process_stop();
        }

        @Override
        public void update(int nDone) {
            process_prog(nDone);
        }

        @Override
        public void set(int nDone, int nTotal) {
            process_setProgBar(nDone, nTotal);
        }

        @Override
        public void show(BoardTemplate template) {
            process_showImage(template);
        }
    });
    private long time, pauseTime;
    private long prevDoneTime;
    private final List<Long> doneTimes = new LinkedList<>();
    private final Timer calcTimer = new Timer(100, (e) -> calcTimer());

    // Setting
    private boolean settingFile_loading = false;

    // Array
    private final List<JComboBox<String>> invComboBoxes = new ArrayList<>(4);
    private final List<JPanel> invStatPanels = new ArrayList<>(4);

    // <editor-fold defaultstate="collapsed" desc="Constructor Methods">
    public MainFrame(App app) {
        this.app = app;
        initComponents();
        blinkTimer = new Timer(500, (e) -> {
            blink.v = !blink.v;
            invList.repaint();
            combFreqList.repaint();
        });
        tml = new TipMouseListener(tipLabel);
        init();
    }

    private void init() {
        initImages();
        initTables();

        invComboBoxes.add(invDmgComboBox);
        invComboBoxes.add(invBrkComboBox);
        invComboBoxes.add(invHitComboBox);
        invComboBoxes.add(invRldComboBox);

        invStatPanels.add(invDmgPanel);
        invStatPanels.add(invBrkPanel);
        invStatPanels.add(invHitPanel);
        invStatPanels.add(invRldPanel);

        combTabbedPane.add(app.getText(AppText.COMB_TAB_RESULT), combResultPanel);
        combTabbedPane.add(app.getText(AppText.COMB_TAB_FREQ), combFreqPanel);

        settingFile_load();

        for (Unit unit : Unit.values()) {
            unitComboBox.addItem(unit);
        }

        ((JLabel) invSortTypeComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        poolListPanel.setBorder(offBorder);
        invListPanel.setBorder(offBorder);
        combListPanel.setBorder(offBorder);
        invStatPanels.forEach((t) -> t.setBorder(offBorder));

        invLevelSlider.setMaximum(Chip.LEVEL_MAX);
        combStopButton.setVisible(false);
        researchButton.setVisible(false);
        timeWarningButton.setVisible(false);

        blinkTimer.start();

        addListeners();
        setting_resetBoard();

        packAndSetInitSize();
    }

    public void afterLoad() {
        new Thread(() -> {
            // Check app version
            IO.checkNewVersion(app);

            // Check research
            String version = ResearchConnection.getVersion();
            if (version != null && !version.isEmpty()) {
                if (!App.VERSION.isCurrent(version)) {
                    researchButton.setEnabled(false);
                }
                researchButton.setVisible(true);
            }
        }).start();
    }

    private void initImages() {
        this.setIconImage(AppImage.FAVICON);

        donationButton.setIcon(AppImage.DONATION);

        helpButton.setIcon(AppImage.QUESTION);
        displaySettingButton.setIcon(AppImage.FONT);
        poolRotLButton.setIcon(AppImage.ROTATE_LEFT);
        poolRotRButton.setIcon(AppImage.ROTATE_RIGHT);
        poolSortButton.setIcon(AppImage.DESCENDING);

        imageButton.setIcon(AppImage.PICTURE);
        proxyButton.setIcon(AppImage.PHONE);

        poolWindowButton.setIcon(AppImage.PANEL_CLOSE);
        addButton.setIcon(AppImage.ADD);

        invNewButton.setIcon(AppImage.NEW);
        invOpenButton.setIcon(AppImage.OPEN);
        invSaveButton.setIcon(AppImage.SAVE);
        invSaveAsButton.setIcon(AppImage.SAVEAS);

        invSortOrderButton.setIcon(AppImage.DESCENDING);
        filterButton.setIcon(AppImage.FILTER);
        displayTypeButton.setIcon(AppImage.DISPLAY_STAT);

        invRotLButton.setIcon(AppImage.ROTATE_LEFT);
        invRotRButton.setIcon(AppImage.ROTATE_RIGHT);
        invDelButton.setIcon(AppImage.DELETE);
        invDmgTextLabel.setIcon(AppImage.DMG);
        invBrkTextLabel.setIcon(AppImage.BRK);
        invHitTextLabel.setIcon(AppImage.HIT);
        invRldTextLabel.setIcon(AppImage.RLD);

        combWarningButton.setIcon(AppImage.getScaledIcon(AppImage.UI_WARNING, 16, 16));
        timeWarningButton.setIcon(AppImage.getScaledIcon(AppImage.UI_WARNING, 16, 16));

        settingButton.setIcon(AppImage.SETTING);
        combStopButton.setIcon(AppImage.COMB_STOP);
        combStartPauseButton.setIcon(AppImage.COMB_START);

        combDmgTextLabel.setIcon(AppImage.DMG);
        combBrkTextLabel.setIcon(AppImage.BRK);
        combHitTextLabel.setIcon(AppImage.HIT);
        combRldTextLabel.setIcon(AppImage.RLD);

        combSaveButton.setIcon(AppImage.SAVE);
        combOpenButton.setIcon(AppImage.OPEN);
        ticketTextLabel.setIcon(AppImage.TICKET);

        legendEquippedLabel.setIcon(new ImageIcon(AppImage.CHIP_EQUIPPED));
        legendRotatedLabel.setIcon(new ImageIcon(AppImage.CHIP_ROTATED));
    }

    private void initTables() {
        /* POOL */
        // Model
        poolList.setModel(poolLM);
        // Renderer
        poolList.setCellRenderer(new ChipListCellRenderer(app));
        // Rows
        for (Shape.Type type : Shape.Type.values()) {
            for (Shape s : Shape.getShapes(type)) {
                poolLM.addElement(new Chip(s));
            }
        }

        /* INVENTORY */
        invList.setFixedCellHeight(AppImage.Chip.height(true) + 3);
        invList.setFixedCellWidth(AppImage.Chip.width(true) + 3);
        Dimension invD = invListPanel.getSize();
        invD.width = invList.getFixedCellWidth() * 4 + BORDERSIZE * 2 + 10 + invListScrollPane.getVerticalScrollBar().getPreferredSize().width;
        invListPanel.setPreferredSize(invD);

        // Model
        invList.setModel(invLM);
        // Renderer
        invList.setCellRenderer(new InvListCellRenderer(app, invList, combList, combTabbedPane, combChipList, combFreqList, blink));

        // Transfer Handler
        invList.setTransferHandler(new InvListTransferHandler(this));
        invList.getActionMap().getParent().remove("cut");
        invList.getActionMap().getParent().remove("copy");
        invList.getActionMap().getParent().remove("paste");

        /* COMBINATION */
        // Model
        combList.setModel(combLM);
        // Renderer
        combList.setCellRenderer(new CombListCellRenderer(app, combFreqList));

        /* RESULT */
        int height = AppImage.Chip.height(true) + 3;
        int width = AppImage.Chip.width(true) + 3;

        combChipList.setFixedCellHeight(height);
        combChipList.setFixedCellWidth(width);
        Dimension ccD = combChipListPanel.getSize();
        ccD.width = width + 10 + combChipListScrollPane.getVerticalScrollBar().getPreferredSize().width;
        combChipListPanel.setPreferredSize(ccD);

        combFreqList.setFixedCellHeight(height);
        combFreqList.setFixedCellWidth(width);
        Dimension ccfD = combFreqListPanel.getSize();
        ccfD.width = width + 10 + combFreqListScrollPane.getVerticalScrollBar().getPreferredSize().width;
        combFreqListPanel.setPreferredSize(ccfD);

        // Model
        combChipList.setModel(combChipLM);
        combFreqList.setModel(combFreqLM);
        // Renderer
        combChipList.setCellRenderer(new ChipListCellRenderer(app));
        combFreqList.setCellRenderer(new ChipFreqListCellRenderer(app, blink));

    }
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Listener Methods">
    private void addListeners() {
        // Tip
        Fn.getAllComponents(this).forEach((t) -> t.addMouseListener(tml));

        // Shortcuts
        for (KeyListener kl : invList.getKeyListeners()) {
            invList.removeKeyListener(kl);
        }

        Fn.getAllComponents(this).stream()
                .filter((c) -> c.isFocusable())
                .forEach((c) -> c.addKeyListener(initSKA_focusable()));

        ShortcutKeyAdapter piKA = initSKA_pi();
        poolList.addKeyListener(piKA);
        invList.addKeyListener(piKA);

        poolList.addKeyListener(initSKA_pool());
        invList.addKeyListener(initSKA_inv());
        combList.addKeyListener(initSKA_comb());

        // Pool Top
        displaySettingButton.addActionListener((e) -> openDialog(AppSettingDialog.getInstance(app)));
        helpButton.addActionListener((e) -> openDialog(HelpDialog.getInstance(app)));
        donationButton.addActionListener((e) -> openDialog(DonationDialog.getInstance(app)));
        poolWindowButton.addActionListener((e) -> setPoolPanelVisible(!poolPanel.isVisible()));

        imageButton.addActionListener((e) -> invFile_openImageDialog());
        proxyButton.addActionListener((e) -> invFile_openProxyDialog());

        // Pool Mid
        poolList.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                addButton.setEnabled(!poolList.isSelectionEmpty());
            }
        });
        poolList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (!poolList.isSelectionEmpty() && 2 <= evt.getClickCount()) {
                    pool_addToInv();
                }
            }
        });
        poolList.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                poolListPanel.setBorder(onBorder);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                poolListPanel.setBorder(offBorder);
            }
        });

        // Pool Bot
        poolRotLButton.addActionListener((e) -> pool_rotate(Chip.COUNTERCLOCKWISE));
        poolRotRButton.addActionListener((e) -> pool_rotate(Chip.CLOCKWISE));
        poolSortButton.addActionListener((e) -> pool_toggleOrder());
        poolStarComboBox.addActionListener((e) -> pool_starChanged());
        poolColorButton.addActionListener((e) -> pool_cycleColor());

        // Pool Right
        addButton.addActionListener((e) -> pool_addToInv());

        // Inv Top
        invNewButton.addActionListener((e) -> invFile_new());
        invOpenButton.addActionListener((e) -> invFile_open());
        invSaveButton.addActionListener((e) -> invFile_save());
        invSaveAsButton.addActionListener((e) -> invFile_saveAs());

        invSortOrderButton.addActionListener((e) -> display_toggleOrder());
        invSortTypeComboBox.addActionListener((e) -> display_applyFilterSort());
        filterButton.addActionListener((e) -> openDialog(FilterDialog.getInstance(app)));
        displayTypeButton.addActionListener((e) -> display_toggleType());

        // Inv Mid
        invList.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                if (invList.getSelectedIndices().length == 1) {
                    invList.ensureIndexIsVisible(invList.getSelectedIndex());
                }
                invStat_loadStats();
            }
        });
        invList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                invListMouseDragIndex = invList.getSelectedIndex();
            }
        });
        invList.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                invListPanel.setBorder(onBorder);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                invListPanel.setBorder(offBorder);
            }
        });

        // Inv Bot
        invApplyButton.addActionListener((e) -> openDialog(ApplyDialog.getInstance(app)));

        invLevelSlider.addChangeListener((e) -> {
            invStat_setStats();
            invStat_refreshStatComboBoxes();
        });
        invStarComboBox.addActionListener((e) -> {
            invStat_setStats();
            invStat_refreshStatComboBoxes();
        });
        invColorButton.addActionListener((e) -> invStat_cycleColor());

        invComboBoxes.forEach((t) -> t.addItemListener((e) -> invStat_setStats()));

        invRotLButton.addActionListener((e) -> invStat_rotate(Chip.COUNTERCLOCKWISE));
        invRotRButton.addActionListener((e) -> invStat_rotate(Chip.CLOCKWISE));
        invDelButton.addActionListener((e) -> invStat_delete());
        invMarkCheckBox.addItemListener((e) -> invStat_setStats());

        invTagButton.addActionListener((e) -> invStat_openTagDialog());

        // Comb Left
        combList.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                comb_loadCombination();
            }
        });
        combList.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                combListPanel.setBorder(onBorder);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                combListPanel.setBorder(offBorder);
            }
        });

        // Comb Top
        unitComboBox.addActionListener((e) -> setting_resetBoard());
        unitStarComboBox.addActionListener((e) -> setting_resetBoard());
        settingButton.addActionListener((e) -> openDialog(CalcSettingDialog.getInstance(app)));
        combWarningButton.addActionListener((e) -> Fn.popup(combWarningButton, app.getText(AppText.WARNING_HOCMAX), app.getText(AppText.WARNING_HOCMAX_DESC)));
        timeWarningButton.addActionListener((e) -> Fn.popup(timeWarningButton, app.getText(AppText.WARNING_TIME), app.getText(AppText.WARNING_TIME_DESC)));

        showProgImageCheckBox.addItemListener((e) -> comb_setShowProgImage());
        combStartPauseButton.addActionListener((e) -> process_toggleStartPause());
        combStopButton.addActionListener((e) -> process_stop());

        // Comb Bot
        researchButton.addActionListener((e) -> openFrame(ResearchFrame.getInstance(app)));
        statButton.addActionListener((e) -> comb_openStatDialog());
        combOpenButton.addActionListener((e) -> progFile_open());
        combSaveButton.addActionListener((e) -> progFile_saveAs());

        // Comb Right
        combTabbedPane.addChangeListener((e) -> {
            if (combTabbedPane.getSelectedIndex() != 1) {
                combFreqList.clearSelection();
            }
        });

        combChipList.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                comb_ensureInvListIndexIsVisible_combChipList();
                invList.repaint();
            }
        });
        combMarkButton.addActionListener((e) -> comb_result_mark());
        combTagButton.addActionListener((e) -> comb_result_openTagDialog());

        combFreqList.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                comb_ensureInvListIndexIsVisible_combChipFreqList();
                comb_updateFreqLabel();
                invList.repaint();
                combList.repaint();
            }
        });
        combFreqMarkButton.addActionListener((e) -> comb_freq_mark());
        combFreqTagButton.addActionListener((e) -> comb_freq_openTagDialog());
    }

    private ShortcutKeyAdapter initSKA_focusable() {
        ShortcutKeyAdapter ska = new ShortcutKeyAdapter();

        ska.addShortcut_c(KeyEvent.VK_F, () -> openDialog(FilterDialog.getInstance(app)));
        ska.addShortcut_c(KeyEvent.VK_D, () -> display_toggleType());
        ska.addShortcut_c(KeyEvent.VK_E, () -> openDialog(CalcSettingDialog.getInstance(app)));

        ska.addShortcut(KeyEvent.VK_F1, () -> openDialog(HelpDialog.getInstance(app)));
        ska.addShortcut(KeyEvent.VK_F5, () -> process_toggleStartPause());
        ska.addShortcut(KeyEvent.VK_F6, () -> process_stop());

        return ska;
    }

    private ShortcutKeyAdapter initSKA_pi() {
        ShortcutKeyAdapter ska = new ShortcutKeyAdapter();

        ska.addShortcut_c(KeyEvent.VK_N, () -> invFile_new());
        ska.addShortcut_c(KeyEvent.VK_O, () -> invFile_open());
        ska.addShortcut_c(KeyEvent.VK_S, () -> invFile_save());

        ska.addShortcut_cs(KeyEvent.VK_S, () -> invFile_saveAs());

        ska.addShortcut(KeyEvent.VK_ENTER, () -> invStat_focusNextStat());
        for (int i = KeyEvent.VK_0; i <= KeyEvent.VK_9; i++) {
            int number = i - KeyEvent.VK_0;
            ska.addShortcut(i, () -> invStat_readInput(number));
        }
        for (int i = KeyEvent.VK_NUMPAD0; i <= KeyEvent.VK_NUMPAD9; i++) {
            int number = i - KeyEvent.VK_NUMPAD0;
            ska.addShortcut(i, () -> invStat_readInput(number));
        }

        return ska;
    }

    private ShortcutKeyAdapter initSKA_pool() {
        ShortcutKeyAdapter ska = new ShortcutKeyAdapter();

        ska.addShortcut(KeyEvent.VK_COMMA, () -> pool_rotate(Chip.COUNTERCLOCKWISE));
        ska.addShortcut(KeyEvent.VK_OPEN_BRACKET, () -> pool_rotate(Chip.COUNTERCLOCKWISE));
        ska.addShortcut(KeyEvent.VK_PERIOD, () -> pool_rotate(Chip.CLOCKWISE));
        ska.addShortcut(KeyEvent.VK_CLOSE_BRACKET, () -> pool_rotate(Chip.CLOCKWISE));
        ska.addShortcut(KeyEvent.VK_C, () -> pool_cycleColor());
        ska.addShortcut(KeyEvent.VK_R, () -> pool_toggleOrder());
        ska.addShortcut(KeyEvent.VK_SPACE, () -> pool_addToInv());

        return ska;
    }

    private ShortcutKeyAdapter initSKA_inv() {
        ShortcutKeyAdapter ska = new ShortcutKeyAdapter();

        ska.addShortcut(KeyEvent.VK_COMMA, () -> invStat_rotate(Chip.COUNTERCLOCKWISE));
        ska.addShortcut(KeyEvent.VK_OPEN_BRACKET, () -> invStat_rotate(Chip.COUNTERCLOCKWISE));
        ska.addShortcut(KeyEvent.VK_PERIOD, () -> invStat_rotate(Chip.CLOCKWISE));
        ska.addShortcut(KeyEvent.VK_CLOSE_BRACKET, () -> invStat_rotate(Chip.CLOCKWISE));
        ska.addShortcut(KeyEvent.VK_A, () -> openDialog(ApplyDialog.getInstance(app)));
        ska.addShortcut(KeyEvent.VK_C, () -> invStat_cycleColor());
        ska.addShortcut(KeyEvent.VK_M, () -> invStat_toggleMarked());
        ska.addShortcut(KeyEvent.VK_T, () -> invStat_openTagDialog());
        ska.addShortcut(KeyEvent.VK_R, () -> display_toggleOrder());
        ska.addShortcut(KeyEvent.VK_DELETE, () -> invStat_delete());
        ska.addShortcut(KeyEvent.VK_MINUS, () -> invStat_decLevel());
        ska.addShortcut(KeyEvent.VK_SUBTRACT, () -> invStat_decLevel());
        ska.addShortcut(KeyEvent.VK_EQUALS, () -> invStat_incLevel());
        ska.addShortcut(KeyEvent.VK_ADD, () -> invStat_incLevel());

        ska.addShortcut_c(KeyEvent.VK_A, () -> {
        });

        return ska;
    }

    private ShortcutKeyAdapter initSKA_comb() {
        ShortcutKeyAdapter ska = new ShortcutKeyAdapter();

        ska.addShortcut_c(KeyEvent.VK_O, () -> progFile_open());
        ska.addShortcut_c(KeyEvent.VK_S, () -> progFile_saveAs());

        ska.addShortcut(KeyEvent.VK_C, () -> comb_nextBoardName());
        ska.addShortcut(KeyEvent.VK_M, () -> {
            if (combTabbedPane.getSelectedIndex() == 0) {
                comb_result_mark();
            } else {
                comb_freq_mark();
            }
        });
        ska.addShortcut(KeyEvent.VK_T, () -> {
            if (combTabbedPane.getSelectedIndex() == 0) {
                comb_result_openTagDialog();
            } else {
                comb_freq_openTagDialog();
            }
        });

        return ska;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Util Methods">
    private void openDialog(JDialog dialog) {
        Fn.open(this, dialog);
    }

    private void openFrame(JFrame frame) {
        Fn.open(this, frame);
        this.setVisible(false);
    }

    public Dimension getPreferredDialogSize() {
        Dimension dim = new Dimension();
        dim.width = piButtonPanel.getWidth() + invPanel.getWidth() + combLeftPanel.getWidth() + combRightPanel.getWidth();
        dim.height = getHeight();
        return dim;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Refresh Methods">
    public void refreshDisplay() {
        refreshLang();
        refreshFont();
        refreshColor();
    }

    private void refreshLang() {
        String[] piStarCBList = new String[]{
            Board.getStarHTML_star(5),
            Board.getStarHTML_star(4),
            Board.getStarHTML_star(3),
            Board.getStarHTML_star(2)
        };
        poolStarComboBox.setModel(new DefaultComboBoxModel<>(piStarCBList));
        invStarComboBox.setModel(new DefaultComboBoxModel<>(piStarCBList));

        String[] bStarCBList = new String[]{
            Board.getStarHTML_star(5),
            Board.getStarHTML_star(4),
            Board.getStarHTML_star(3),
            Board.getStarHTML_star(2),
            Board.getStarHTML_star(1)
        };
        unitStarComboBox.setModel(new DefaultComboBoxModel<>(bStarCBList));

        invDmgTextLabel.setText(app.getText(AppText.CHIP_STAT_DMG));
        invBrkTextLabel.setText(app.getText(AppText.CHIP_STAT_BRK));
        invHitTextLabel.setText(app.getText(AppText.CHIP_STAT_HIT));
        invRldTextLabel.setText(app.getText(AppText.CHIP_STAT_RLD));
        combDmgTextLabel.setText(app.getText(AppText.CHIP_STAT_DMG));
        combBrkTextLabel.setText(app.getText(AppText.CHIP_STAT_BRK));
        combHitTextLabel.setText(app.getText(AppText.CHIP_STAT_HIT));
        combRldTextLabel.setText(app.getText(AppText.CHIP_STAT_RLD));

        invApplyButton.setText(app.getText(AppText.APPLY_TITLE));
        enhancementTextLabel.setText(app.getText(AppText.CHIP_LEVEL));
        invMarkCheckBox.setText(app.getText(AppText.CHIP_MARK));

        researchButton.setText(app.getText(AppText.RESEARCH_TITLE));
        statButton.setText(app.getText(AppText.STAT_TITLE));

        combTabbedPane.setTitleAt(0, app.getText(AppText.COMB_TAB_RESULT));
        combTabbedPane.setTitleAt(1, app.getText(AppText.COMB_TAB_FREQ));

        ticketTextLabel.setText(app.getText(AppText.CHIP_TICKET));
        xpTextLabel.setText(app.getText(AppText.CHIP_XP));
        combMarkButton.setText(app.getText(AppText.CHIP_MARK));
        combTagButton.setText(app.getText(AppText.CHIP_TAG));
        combFreqMarkButton.setText(app.getText(AppText.CHIP_MARK));
        combFreqTagButton.setText(app.getText(AppText.CHIP_TAG));

        legendEquippedLabel.setText(app.getText(AppText.LEGEND_EQUIPPED));
        legendRotatedLabel.setText(app.getText(AppText.LEGEND_ROTATED));

        iofc.resetChoosableFileFilters();
        isfc.resetChoosableFileFilters();
        cfc.resetChoosableFileFilters();
        iofc.setFileFilter(new FileNameExtensionFilter(app.getText(AppText.FILE_EXT_INV_OPEN, IO.EXT_INVENTORY), IO.EXT_INVENTORY, "json"));
        isfc.setFileFilter(new FileNameExtensionFilter(app.getText(AppText.FILE_EXT_INV_SAVE, IO.EXT_INVENTORY), IO.EXT_INVENTORY));
        cfc.setFileFilter(new FileNameExtensionFilter(app.getText(AppText.FILE_EXT_COMB, IO.EXT_COMBINATION), IO.EXT_COMBINATION));

        invSortTypeComboBox.removeAllItems();
        invSortTypeComboBox.addItem(app.getText(AppText.SORT_CUSTOM));
        invSortTypeComboBox.addItem(app.getText(AppText.SORT_CELL));
        invSortTypeComboBox.addItem(app.getText(AppText.SORT_ENHANCEMENT));
        invSortTypeComboBox.addItem(app.getText(AppText.SORT_STAR));
        invSortTypeComboBox.addItem(app.getText(AppText.CHIP_STAT_DMG_LONG));
        invSortTypeComboBox.addItem(app.getText(AppText.CHIP_STAT_BRK_LONG));
        invSortTypeComboBox.addItem(app.getText(AppText.CHIP_STAT_HIT_LONG));
        invSortTypeComboBox.addItem(app.getText(AppText.CHIP_STAT_RLD_LONG));

        pool_setColorText();
        invStat_setColorText();
        display_refreshInvListCountText();
        process_setCombLabelText();
        refreshTips();

        boolean isKorean = app.setting.locale.equals(Locale.KOREA) || app.setting.locale.equals(Locale.KOREAN);
        setTitle(isKorean ? App.NAME_KR : App.NAME_EN);
    }

    private void refreshTips() {
        tml.clearTips();

        addTip(displaySettingButton, app.getText(AppText.TIP_DISPLAY));
        addTip(helpButton, app.getText(AppText.TIP_HELP));
        addTip(imageButton, app.getText(AppText.TIP_IMAGE));
        addTip(proxyButton, app.getText(AppText.TIP_PROXY));

        addTip(poolList, app.getText(AppText.TIP_POOL));

        addTip(poolRotLButton, app.getText(AppText.TIP_POOL_ROTATE_LEFT));
        addTip(poolRotRButton, app.getText(AppText.TIP_POOL_ROTATE_RIGHT));
        addTip(poolSortButton, app.getText(AppText.TIP_POOL_SORT_ORDER));
        addTip(poolStarComboBox, app.getText(AppText.TIP_POOL_STAR));
        addTip(poolColorButton, app.getText(AppText.TIP_POOL_COLOR));

        addTip(poolWindowButton, app.getText(AppText.TIP_POOLWINDOW));
        addTip(addButton, app.getText(AppText.TIP_ADD));

        addTip(invList, app.getText(AppText.TIP_INV));

        addTip(invNewButton, app.getText(AppText.TIP_INV_NEW));
        addTip(invOpenButton, app.getText(AppText.TIP_INV_OPEN));
        addTip(invSaveButton, app.getText(AppText.TIP_INV_SAVE));
        addTip(invSaveAsButton, app.getText(AppText.TIP_INV_SAVEAS));

        addTip(invSortOrderButton, app.getText(AppText.TIP_INV_SORT_ORDER));
        addTip(invSortTypeComboBox, app.getText(AppText.TIP_INV_SORT_TYPE));
        addTip(filterButton, app.getText(AppText.TIP_INV_FILTER));
        addTip(displayTypeButton, app.getText(AppText.TIP_INV_STAT));

        addTip(invApplyButton, app.getText(AppText.TIP_INV_APPLY));
        addTip(invStarComboBox, app.getText(AppText.TIP_INV_STAR));
        addTip(invColorButton, app.getText(AppText.TIP_INV_COLOR));
        addTip(invLevelSlider, app.getText(AppText.TIP_INV_ENHANCEMENT));
        addTip(invRotLButton, app.getText(AppText.TIP_INV_ROTATE_LEFT));
        addTip(invRotRButton, app.getText(AppText.TIP_INV_ROTATE_RIGHT));
        addTip(invDelButton, app.getText(AppText.TIP_INV_DELETE));
        addTip(invMarkCheckBox, app.getText(AppText.TIP_INV_MARK));
        addTip(invTagButton, app.getText(AppText.TIP_INV_TAG));

        addTip(invDmgTextLabel, app.getText(AppText.CHIP_STAT_DMG_LONG));
        addTip(invBrkTextLabel, app.getText(AppText.CHIP_STAT_BRK_LONG));
        addTip(invHitTextLabel, app.getText(AppText.CHIP_STAT_HIT_LONG));
        addTip(invRldTextLabel, app.getText(AppText.CHIP_STAT_RLD_LONG));

        addTip(unitComboBox, app.getText(AppText.TIP_BOARD_NAME));
        addTip(unitStarComboBox, app.getText(AppText.TIP_BOARD_STAR));

        addTip(combWarningButton, app.getText(AppText.WARNING_HOCMAX));
        addTip(timeWarningButton, app.getText(AppText.WARNING_TIME));

        if (!researchButton.isEnabled()) {
            addTip(researchButton, app.getText(AppText.TIP_RESEARCH_OLD));
        }

        addTip(combList, app.getText(AppText.TIP_COMB_LIST));
        addTip(combChipList, app.getText(AppText.TIP_COMB_CHIPLIST));
        addTip(combFreqList, app.getText(AppText.TIP_COMB_FREQLIST));

        addTip(combDmgTextLabel, app.getText(AppText.CHIP_STAT_DMG_LONG));
        addTip(combBrkTextLabel, app.getText(AppText.CHIP_STAT_BRK_LONG));
        addTip(combHitTextLabel, app.getText(AppText.CHIP_STAT_HIT_LONG));
        addTip(combRldTextLabel, app.getText(AppText.CHIP_STAT_RLD_LONG));

        addTip(settingButton, app.getText(AppText.TIP_COMB_SETTING));
        addTip(showProgImageCheckBox, app.getText(AppText.TIP_COMB_SHOWPROGIMAGE));
        addTip(combStartPauseButton, app.getText(AppText.TIP_COMB_START));
        addTip(statButton, app.getText(AppText.TIP_COMB_STAT));
        addTip(combOpenButton, app.getText(AppText.TIP_COMB_OPEN));
        addTip(combSaveButton, app.getText(AppText.TIP_COMB_SAVE));
        addTip(combMarkButton, app.getText(AppText.TIP_COMB_MARK));
        addTip(combTagButton, app.getText(AppText.TIP_COMB_TAG));
        addTip(combFreqMarkButton, app.getText(AppText.TIP_COMB_MARK));
        addTip(combFreqTagButton, app.getText(AppText.TIP_COMB_TAG));
    }

    private void addTip(JComponent c, String s) {
        tml.setTip(c, s);
        c.setToolTipText(s);
    }

    private void refreshFont() {
        Font defaultFont = AppFont.getDefault().deriveFont((float) app.setting.fontSize);
        // Font
        invTagButton.setText("");
        Fn.setUIFont(defaultFont);
        Fn.getAllComponents(this).forEach((c) -> c.setFont(defaultFont));

        // Size
        combWarningButton.setPreferredSize(new Dimension(combWarningButton.getHeight(), combWarningButton.getHeight()));
        timeWarningButton.setPreferredSize(new Dimension(timeWarningButton.getHeight(), timeWarningButton.getHeight()));
        combImageLabel.setPreferredSize(new Dimension(combImageLabel.getWidth(), combImageLabel.getWidth()));

        int height = Fn.getHeight(defaultFont);
        int levelWidth = 0;
        for (int i = 0; i <= 20; i++) {
            levelWidth = Math.max(levelWidth, Fn.getWidth(String.valueOf(i), defaultFont));
        }
        invLevelLabel.setPreferredSize(new Dimension(levelWidth + 10, height));

        int textWidth = Fn.max(Fn.getWidth(app.getText(AppText.CHIP_STAT_DMG), defaultFont),
                Fn.getWidth(app.getText(AppText.CHIP_STAT_BRK), defaultFont),
                Fn.getWidth(app.getText(AppText.CHIP_STAT_HIT), defaultFont),
                Fn.getWidth(app.getText(AppText.CHIP_STAT_RLD), defaultFont)
        );
        Dimension textDim = new Dimension(textWidth + 30, height);
        invDmgTextLabel.setPreferredSize(textDim);
        invBrkTextLabel.setPreferredSize(textDim);
        invHitTextLabel.setPreferredSize(textDim);
        invRldTextLabel.setPreferredSize(textDim);

        int statWidth = 0;
        for (int i = 0; i <= Chip.PT_MAX; i++) {
            statWidth = Math.max(statWidth, Fn.getWidth(String.valueOf(i), defaultFont));
        }

        Dimension ptDim = new Dimension(statWidth + 10, height);
        invDmgPtLabel.setPreferredSize(ptDim);
        invBrkPtLabel.setPreferredSize(ptDim);
        invHitPtLabel.setPreferredSize(ptDim);
        invRldPtLabel.setPreferredSize(ptDim);

        int colorWidth = 0;
        for (String color : AppText.TEXT_MAP_COLOR.values()) {
            colorWidth = Math.max(colorWidth, Fn.getWidth(app.getText(color), defaultFont));
        }
        invColorButton.setPreferredSize(new Dimension(colorWidth + 10, height));

        int prefCombWidth = combStatPanel.getPreferredSize().width;
        combImagePanel.setPreferredSize(new Dimension(prefCombWidth, prefCombWidth));

        // Save
        packAndSetInitSize();
        invStat_setTagButtonText();
    }

    private void refreshColor() {
        onBorder = new LineBorder(app.blue(), BORDERSIZE);
        comb_loadCombination();
    }

    public void packAndSetInitSize() {
        pack();
        initSize = getSize();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Pool Methods">
    private void pool_rotate(boolean direction) {
        for (Enumeration<Chip> elements = poolLM.elements(); elements.hasMoreElements();) {
            Chip c = elements.nextElement();
            c.initRotate(direction);
        }
        poolList.repaint();
    }

    private void pool_setOrder(boolean b) {
        if (!poolLM.isEmpty()) {
            app.setting.poolOrder = b;
            Chip c = (Chip) poolLM.firstElement();
            if (b == ASCENDING) {
                poolSortButton.setIcon(AppImage.ASCNEDING);
                if (c.getSize() != 1) {
                    pool_reverseList();
                }
            } else {
                poolSortButton.setIcon(AppImage.DESCENDING);
                if (c.getSize() != 6) {
                    pool_reverseList();
                }
            }
            settingFile_save();
        }
    }

    private void pool_toggleOrder() {
        pool_setOrder(!app.setting.poolOrder);
    }

    private void pool_reverseList() {
        int sel = poolList.getSelectedIndex();
        List<Chip> cs = Collections.list(poolLM.elements());
        Collections.reverse(cs);
        poolLM.clear();
        cs.forEach((c) -> poolLM.addElement(c));
        if (sel > -1) {
            sel = cs.size() - sel - 1;
            poolList.setSelectedIndex(sel);
            poolList.ensureIndexIsVisible(sel);
        }
    }

    private void pool_setColor(Unit.Color color) {
        app.setting.poolColor = color;
        pool_setColorText();
        settingFile_save();
    }

    private void pool_setColorText() {
        poolColorButton.setText(app.getText(AppText.TEXT_MAP_COLOR.get(app.setting.poolColor)));
        poolColorButton.setForeground(AppColor.CHIPS.get(app.setting.poolColor));
    }

    private void pool_cycleColor() {
        pool_setColor(Unit.Color.byId((app.setting.poolColor.id + 1) % Unit.Color.values().length));
    }

    private void pool_starChanged() {
        app.setting.poolStar = 5 - poolStarComboBox.getSelectedIndex();
        settingFile_save();
    }

    private void setPoolPanelVisible(boolean b) {
        if (b) {
            poolPanel.setVisible(true);
            poolWindowButton.setIcon(AppImage.PANEL_CLOSE);
        } else {
            poolPanel.setVisible(false);
            poolList.clearSelection();
            poolWindowButton.setIcon(AppImage.PANEL_OPEN);
        }
        if (getSize().equals(initSize)) {
            packAndSetInitSize();
        }
        settingFile_save();
    }

    private void pool_addToInv() {
        if (!poolList.isSelectionEmpty()) {
            Chip poolChip = poolList.getSelectedValue();
            Chip c = new Chip(poolChip, 5 - poolStarComboBox.getSelectedIndex(), app.setting.poolColor);
            if (invList.getSelectedIndices().length == 1) {
                int i = invList.getSelectedIndex() + 1;
                inv_chipsAdd(i, c);
                invList.setSelectedIndex(i);
            } else {
                inv_chipsAdd(c);
            }
            invStat_enableSave();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inventory Chip Methods">
    private void inv_chipsAdd(int i, Chip c) {
        invChips.add(i, c);
        invLM.add(i, c);
        c.setDisplayType(app.setting.displayType);
    }

    private void inv_chipsAdd(Chip c) {
        invChips.add(c);
        invLM.addElement(c);
        c.setDisplayType(app.setting.displayType);
    }

    public void inv_chipsLoad(Collection<Chip> cs) {
        inv_chipsClear();
        invChips.addAll(cs);
        invChips.forEach((c) -> c.setDisplayType(app.setting.displayType));
        display_applyFilterSort();
    }

    private void inv_chipsClear() {
        invChips.clear();
        invLM.clear();
    }

    private void inv_chipsRemove(int i) {
        invChips.remove((Chip) invLM.get(i));
        invLM.removeElementAt(i);
    }

    private void inv_chipsRefresh() {
        invChips.clear();
        for (Enumeration<Chip> elements = invLM.elements(); elements.hasMoreElements();) {
            Chip c = elements.nextElement();
            invChips.add(c);
        }
    }

    public List<Chip> inv_getFilteredChips() {
        List<Chip> chips = new ArrayList<>();
        for (int i = 0; i < invLM.size(); i++) {
            chips.add((Chip) invLM.getElementAt(i));
        }
        return chips;
    }

    public List<Tag> inv_getAllTags() {
        return Tag.getTags(invChips);
    }

    public void invListTransferHandler_ExportDone() {
        inv_chipsRefresh();
        if (invListMouseDragIndex != invList.getSelectedIndex()) {
            invStat_enableSave();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inventory Stat Methods">
    public void invStat_loadStats() {
        if (!invStat_loading) {
            invStat_loading = true;
            boolean singleSelected = invList.getSelectedIndices().length == 1;
            boolean multipleSelected = invList.getSelectedIndices().length >= 1;

            invComboBoxes.forEach((t) -> t.setEnabled(singleSelected));
            invStarComboBox.setEnabled(singleSelected);
            invLevelSlider.setEnabled(singleSelected);
            invColorButton.setEnabled(singleSelected);
            invMarkCheckBox.setEnabled(singleSelected);
            invStat_resetFocus(singleSelected);

            invDelButton.setEnabled(multipleSelected);
            invRotLButton.setEnabled(multipleSelected);
            invRotRButton.setEnabled(multipleSelected);

            invTagButton.setEnabled(multipleSelected);

            invStarComboBox.setSelectedIndex(singleSelected ? 5 - invList.getSelectedValue().getStar() : 0);
            invLevelSlider.setValue(singleSelected ? invList.getSelectedValue().getLevel() : 0);
            invStat_setColor(singleSelected ? invList.getSelectedValue().getColor() : null);
            invMarkCheckBox.setSelected(singleSelected ? invList.getSelectedValue().isMarked() : false);

            invStat_setTagButtonText();

            invStat_loading = false;
        }
        invStat_refreshStatComboBoxes();
        invStat_refreshLabels();
    }

    private void invStat_setTagButtonText() {
        String tagButtonText = app.getText(AppText.TAG_NONE);
        if (invList.getSelectedIndices().length >= 1) {
            Set<Tag> tags = new HashSet<>();
            invChips.forEach((c) -> tags.addAll(c.getTags()));
            for (int selectedIndex : invList.getSelectedIndices()) {
                Chip c = invLM.get(selectedIndex);
                tags.retainAll(c.getTags());
            }

            String widthStr = "";
            List<String> tagStrs = new ArrayList<>();
            boolean ellipsis = false;
            for (Tag t : tags) {
                int width = invTagButton.getWidth() - 10;
                String next = t.getName();

                while (!next.isEmpty() && Fn.getWidth(widthStr + next + " ...", invTagButton.getFont()) >= width) {
                    ellipsis = true;
                    next = next.substring(0, next.length() - 1);
                }
                if (next.isEmpty()) {
                    break;
                }
                tagStrs.add(Fn.htmlColor(next, t.getColor()));
                widthStr += t.getName() + ", ";
            }

            String text = String.join(", ", tagStrs) + (ellipsis ? " ..." : "");
            if (!text.isEmpty()) {
                tagButtonText = Fn.toHTML(text);
            }
        }
        invTagButton.setText(tagButtonText);
    }

    private void invStat_setStats() {
        if (!invStat_loading) {
            if (invList.getSelectedIndices().length == 1) {
                Chip c = invList.getSelectedValue();
                c.setPt(
                        invDmgComboBox.getSelectedIndex(),
                        invBrkComboBox.getSelectedIndex(),
                        invHitComboBox.getSelectedIndex(),
                        invRldComboBox.getSelectedIndex()
                );
                c.setStar(5 - invStarComboBox.getSelectedIndex());
                c.setInitLevel(invLevelSlider.getValue());
                c.setColor(invStat_color);
                c.setMarked(invMarkCheckBox.isSelected());
                invStat_enableSave();
                comb_updateMark();
            }
            invList.repaint();
            invStat_refreshLabels();
        }
    }

    public void invStat_enableSave() {
        invSaveButton.setEnabled(true);
    }

    private void invStat_refreshLabels() {
        boolean singleSelected = invList.getSelectedIndices().length == 1;

        if (singleSelected) {
            invDmgPtLabel.setText(String.valueOf(invDmgComboBox.getSelectedIndex()));
            invBrkPtLabel.setText(String.valueOf(invBrkComboBox.getSelectedIndex()));
            invHitPtLabel.setText(String.valueOf(invHitComboBox.getSelectedIndex()));
            invRldPtLabel.setText(String.valueOf(invRldComboBox.getSelectedIndex()));
            invLevelLabel.setText(String.valueOf(invLevelSlider.getValue()));
        } else {
            invDmgPtLabel.setText("");
            invBrkPtLabel.setText("");
            invHitPtLabel.setText("");
            invRldPtLabel.setText("");
            invLevelLabel.setText("");
        }

        if (singleSelected && !invList.getSelectedValue().isPtValid()) {
            invDmgPtLabel.setForeground(Color.RED);
            invBrkPtLabel.setForeground(Color.RED);
            invHitPtLabel.setForeground(Color.RED);
            invRldPtLabel.setForeground(Color.RED);
        } else {
            invDmgPtLabel.setForeground(Color.BLACK);
            invBrkPtLabel.setForeground(Color.BLACK);
            invHitPtLabel.setForeground(Color.BLACK);
            invRldPtLabel.setForeground(Color.BLACK);
        }
    }

    private void invStat_refreshStatComboBoxes() {
        if (!invStat_loading) {
            invStat_loading = true;
            invComboBoxes.forEach((t) -> t.removeAllItems());

            if (invList.getSelectedIndices().length == 1) {
                Chip c = invList.getSelectedValue();
                for (int i = 0; i <= c.getMaxPt(); i++) {
                    invDmgComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_DMG, c, i)));
                    invBrkComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_BRK, c, i)));
                    invHitComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_HIT, c, i)));
                    invRldComboBox.addItem(String.valueOf(Chip.getStat(Chip.RATE_RLD, c, i)));
                }
                invDmgComboBox.setSelectedIndex(c.getPt().dmg);
                invBrkComboBox.setSelectedIndex(c.getPt().brk);
                invHitComboBox.setSelectedIndex(c.getPt().hit);
                invRldComboBox.setSelectedIndex(c.getPt().rld);
            }
            invStat_loading = false;
        }
    }

    private void invStat_setColor(Unit.Color color) {
        invStat_color = color;
        invStat_setColorText();
        if (invList.getSelectedIndices().length == 1) {
            invStat_setStats();
        }
    }

    private void invStat_setColorText() {
        if (invStat_color == null) {
            invColorButton.setText(" ");
        } else {
            invColorButton.setText(app.getText(AppText.TEXT_MAP_COLOR.get(invStat_color)));
            invColorButton.setForeground(AppColor.CHIPS.get(invStat_color));
        }
    }

    private void invStat_cycleColor() {
        if (invList.getSelectedIndices().length == 1) {
            invStat_setColor(Unit.Color.byId((invStat_color.id + 1) % Unit.Color.values().length));
        }
    }

    private void invStat_setLevel(int i) {
        if (invList.getSelectedIndices().length == 1) {
            invLevelSlider.setValue(Fn.limit(i, 0, Chip.LEVEL_MAX));
        }
    }

    private void invStat_decLevel() {
        invStat_setLevel(invLevelSlider.getValue() - 1);
    }

    private void invStat_incLevel() {
        invStat_setLevel(invLevelSlider.getValue() + 1);
    }

    private void invStat_toggleMarked() {
        if (invList.getSelectedIndices().length == 1) {
            invMarkCheckBox.setSelected(!invMarkCheckBox.isSelected());
        }
    }

    private void invStat_openTagDialog() {
        if (invList.getSelectedIndices().length >= 1) {
            List<Chip> chips = new ArrayList<>(invList.getSelectedIndices().length);
            for (int selectedIndex : invList.getSelectedIndices()) {
                Chip c = invLM.get(selectedIndex);
                chips.add(c);
            }
            openDialog(TagDialog.getInstance(app, chips));
        }
    }

    private void invStat_focusStat(int type) {
        focusedStat = type;
        for (int i = 0; i < 4; i++) {
            invStatPanels.get(i).setBorder(type == i ? onBorder : offBorder);
        }
        statInputBuffer.clear();
    }

    private void invStat_resetFocus(boolean focused) {
        invStat_focusStat(focused ? FOCUSED_DMG : FOCUSED_NONE);
    }

    private void invStat_focusNextStat() {
        if (invList.getSelectedIndices().length == 1) {
            invStat_focusStat((focusedStat + 1) % 4);
        }
    }

    private void invStat_readInput(int number) {
        if (invList.getSelectedIndices().length == 1) {
            statInputBuffer.add(number);
            if (statInputBuffer.size() > INPUT_BUFFER_SIZE) {
                statInputBuffer.remove(0);
            }
            String[] inputs = new String[statInputBuffer.size()];
            for (int i = 0; i < inputs.length; i++) {
                if (statInputBuffer.size() >= i + 1) {
                    String t = "";
                    for (int j = i; j < statInputBuffer.size(); j++) {
                        t += String.valueOf(statInputBuffer.get(j));
                    }
                    inputs[i] = t;
                }
            }

            JComboBox<String> combobox = invComboBoxes.get(focusedStat);
            int nItems = combobox.getItemCount();
            if (app.setting.displayType == DISPLAY_STAT) {
                for (int i = nItems - 1; i >= 0; i--) {
                    String candidate = combobox.getItemAt(i);
                    for (String input : inputs) {
                        if (candidate.equals(input)) {
                            combobox.setSelectedIndex(i);
                            return;
                        }
                    }
                }
            } else {
                int pt = Integer.valueOf(inputs[inputs.length - 1]);
                if (pt < nItems) {
                    combobox.setSelectedIndex(pt);
                }
            }
        }

    }

    public void invStat_applyAll(Consumer<? super Chip> action) {
        List<Chip> cs = inv_getFilteredChips();
        if (!cs.isEmpty()) {
            cs.forEach(action);
            invList.repaint();
            invStat_loadStats();
            invStat_enableSave();
        }
    }

    private void invStat_rotate(boolean direction) {
        if (invList.getSelectedIndices().length >= 1) {
            for (int selectedIndex : invList.getSelectedIndices()) {
                Chip c = invLM.get(selectedIndex);
                c.initRotate(direction);
            }

            invList.repaint();
            invStat_enableSave();
        }
    }

    private void invStat_delete() {
        if (invList.getSelectedIndices().length >= 1) {
            int[] indices = invList.getSelectedIndices();
            List<Integer> indexList = new ArrayList<>(indices.length);
            for (int i : indices) {
                indexList.add(i);
            }
            Collections.reverse(indexList);

            indexList.forEach((selectedIndex) -> {
                inv_chipsRemove(selectedIndex);
            });
            display_refreshInvListCountText();
            invStat_enableSave();
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inventory Display Methods">
    private void display_setOrder(boolean order) {
        if (invSortOrderButton.isEnabled()) {
            inv_order = order;
            if (order == ASCENDING) {
                invSortOrderButton.setIcon(AppImage.ASCNEDING);
            } else {
                invSortOrderButton.setIcon(AppImage.DESCENDING);
            }
            display_applyFilterSort();
        }
    }

    private void display_toggleOrder() {
        display_setOrder(!inv_order);
    }

    private boolean display_anyTrueFilter() {
        return app.filter.anySCTMTrue()
                || app.filter.levelMin != 0 || app.filter.levelMax != Chip.LEVEL_MAX
                || !app.filter.ptMin.equals(new Stat())
                || !app.filter.ptMax.equals(new Stat(Chip.PT_MAX))
                || !app.filter.includedTags.isEmpty()
                || !app.filter.excludedTags.isEmpty();
    }

    public void display_applyFilterSort() {
        invLM.removeAllElements();
        List<Chip> temp = new ArrayList<>();

        //// Filter
        invChips.forEach((c) -> {
            boolean pass = true;
            // Star
            if (app.filter.anyStarTrue()) {
                int i = 5 - c.getStar();
                pass = app.filter.getStar(i);
            }
            // Color
            if (pass && app.filter.anyColorTrue()) {
                Unit.Color color = c.getColor();
                pass = app.filter.getColor(color);
            }
            // Size
            if (pass && app.filter.anyTypeTrue()) {
                int i = 6 - c.getSize();
                if (c.getSize() < 5 || c.getType() == Shape.Type._5A) {
                    i++;
                }
                pass = app.filter.getType(i);
            }
            // Marked
            if (pass && app.filter.anyMarkTrue()) {
                int i = c.isMarked() ? 0 : 1;
                pass = app.filter.getMark(i);
            }
            // Level
            if (pass) {
                pass = app.filter.levelMin <= c.getLevel() && c.getLevel() <= app.filter.levelMax;
            }
            // PT
            if (pass) {
                Stat cPt = c.getPt();
                pass = cPt.allGeq(app.filter.ptMin) && cPt.allLeq(app.filter.ptMax);
            }
            // Tag
            if (pass && !app.filter.includedTags.isEmpty()) {
                pass = app.filter.includedTags.stream().allMatch((fTag) -> c.getTags().stream().anyMatch((cTag) -> fTag.equals(cTag)));
            }
            if (pass && !app.filter.excludedTags.isEmpty()) {
                pass = app.filter.excludedTags.stream().noneMatch((fTag) -> c.getTags().stream().anyMatch((cTag) -> fTag.equals(cTag)));
            }

            // Final
            if (pass) {
                temp.add(c);
            }
        });

        //// Sort
        switch (invSortTypeComboBox.getSelectedIndex()) {
            case SORT_SIZE:
                temp.sort((c1, c2) -> Chip.compare(c1, c2));
                break;
            case SORT_LEVEL:
                temp.sort((c1, c2) -> Chip.compareLevel(c1, c2));
                break;
            case SORT_STAR:
                temp.sort((c1, c2) -> Chip.compareStar(c1, c2));
                break;
            case SORT_DMG:
                temp.sort((c1, c2)
                        -> app.setting.displayType == Setting.DISPLAY_STAT
                                ? c1.getStat().dmg - c2.getStat().dmg
                                : c1.getPt().dmg - c2.getPt().dmg);
                break;
            case SORT_BRK:
                temp.sort((c1, c2)
                        -> app.setting.displayType == Setting.DISPLAY_STAT
                                ? c1.getStat().brk - c2.getStat().brk
                                : c1.getPt().brk - c2.getPt().brk);
                break;
            case SORT_HIT:
                temp.sort((c1, c2)
                        -> app.setting.displayType == Setting.DISPLAY_STAT
                                ? c1.getStat().hit - c2.getStat().hit
                                : c1.getPt().hit - c2.getPt().hit);
                break;
            case SORT_RLD:
                temp.sort((c1, c2)
                        -> app.setting.displayType == Setting.DISPLAY_STAT
                                ? c1.getStat().rld - c2.getStat().rld
                                : c1.getPt().rld - c2.getPt().rld);
                break;
            default:
        }

        if (invSortTypeComboBox.getSelectedIndex() != SORT_NONE && inv_order == DESCENDING) {
            Collections.reverse(temp);
        }

        // Fill
        temp.forEach((c) -> {
            invLM.addElement(c);
        });

        boolean anyTrueAll = display_anyTrueFilter();

        // UI
        invSortOrderButton.setEnabled(invSortTypeComboBox.getSelectedIndex() != SORT_NONE);
        boolean chipEnabled = invSortTypeComboBox.getSelectedIndex() == SORT_NONE && !anyTrueAll;
        poolList.setEnabled(chipEnabled);
        if (!chipEnabled) {
            poolList.clearSelection();
        }
        addButton.setEnabled(chipEnabled && poolList.getSelectedIndex() != -1);
        invList.setDragEnabled(chipEnabled);
        filterButton.setIcon(anyTrueAll ? AppImage.FILTER_APPLY : AppImage.FILTER);
        display_refreshInvListCountText();
    }

    private void display_refreshInvListCountText() {
        filterChipCountLabel.setText(display_anyTrueFilter()
                ? app.getText(AppText.FILTER_ENABLED, String.valueOf(invLM.size()), String.valueOf(invChips.size()))
                : app.getText(AppText.FILTER_DISABLED, String.valueOf(invChips.size())
                )
        );
    }

    private void display_setType(int type) {
        int iMod = type % Setting.NUM_DISPLAY;
        app.setting.displayType = iMod;
        if (iMod == DISPLAY_STAT) {
            displayTypeButton.setIcon(AppImage.DISPLAY_STAT);
        } else {
            displayTypeButton.setIcon(AppImage.DISPLAY_PT);
        }
        invChips.forEach((t) -> t.setDisplayType(iMod));
        display_applyFilterSort();
        invList.repaint();
        for (int i = 0; i < combLM.size(); i++) {
            Board board = (Board) combLM.get(i);
            board.forEachChip((t) -> t.setDisplayType(iMod));
        }
        combChipList.repaint();
        for (Enumeration<ChipFreq> cfEnum = combFreqLM.elements(); cfEnum.hasMoreElements();) {
            cfEnum.nextElement().chip.setDisplayType(iMod);
        }
        combFreqList.repaint();
        settingFile_save();
    }

    private void display_toggleType() {
        display_setType(app.setting.displayType + 1);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Setting Methods">
    public final Unit getUnit() {
        return unitComboBox.getItemAt(unitComboBox.getSelectedIndex());
    }

    public final int getUnitStar() {
        return 5 - unitStarComboBox.getSelectedIndex();
    }

    public void setting_resetDisplay() {
        Icon settingIcon;
        switch (app.setting.board.getStatMode(getUnit(), getUnitStar())) {
            case BoardSetting.MAX_STAT:
                settingIcon = AppImage.SETTING_STAT;
                break;
            case BoardSetting.MAX_PT:
                settingIcon = AppImage.SETTING_PT;
                break;
            case BoardSetting.MAX_PRESET:
                settingIcon = AppImage.SETTING_PRESET;
                break;
            default:
                settingIcon = AppImage.SETTING;
        }
        settingButton.setIcon(settingIcon);
        BoardSetting board = app.setting.board;
        boolean maxWarning = getUnitStar() == 5
                && board.getStatMode(getUnit(), getUnitStar()) != BoardSetting.MAX_PRESET
                && !getUnit().hasDefaultPreset();
        combWarningButton.setVisible(maxWarning);
    }

    private void setting_resetBoard() {
        setting_resetDisplay();
        boardImageLabel.setIcon(AppImage.Board.get(app, boardImageLabel.getWidth(), getUnit(), getUnitStar()));
        boardImageLabel.repaint();
    }

    public boolean setting_isPresetFilter() {
        Unit unit = getUnit();
        int star = getUnitStar();
        int presetIndex = app.setting.board.getPresetIndex(unit, star);
        boolean[] stars = new boolean[]{true, false, false, false};
        boolean[] types = unit.getPresetTypeFilter(presetIndex);
        Stat ptMin = unit.getPresetFilterPtMin(presetIndex);
        Stat ptMax = unit.getPresetFilterPtMax(presetIndex);
        return app.filter.equals(stars, types, ptMin, ptMax);
    }

    public void setting_applyPresetFilter() {
        Unit unit = getUnit();
        int star = getUnitStar();
        int presetIndex = app.setting.board.getPresetIndex(unit, star);
        boolean[] stars = new boolean[]{true, false, false, false};

        boolean[] colors = new boolean[Unit.Color.values().length];
        Unit.Color c = unit.getColor();
        colors[c.id] = true;
        boolean[] types = unit.getPresetTypeFilter(presetIndex);
        Stat ptMin = unit.getPresetFilterPtMin(presetIndex);
        Stat ptMax = unit.getPresetFilterPtMax(presetIndex);

        app.filter.setColors(colors);
        app.filter.setStars(stars);
        app.filter.setTypes(types);
        app.filter.ptMin = ptMin;
        app.filter.ptMax = ptMax;

        display_applyFilterSort();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Combination Methods">
    private void comb_setBoardName(int i) {
        if (unitComboBox.isEnabled()) {
            unitComboBox.setSelectedIndex(Fn.limit(i, 0, unitComboBox.getItemCount()));
        }
    }

    private void comb_nextBoardName() {
        comb_setBoardName((unitComboBox.getSelectedIndex() + 1) % unitComboBox.getItemCount());
    }

    private void comb_setShowProgImage() {
        app.setting.showProgImage = showProgImageCheckBox.isSelected();
        if (!app.setting.showProgImage) {
            boardImageLabel.setIcon(AppImage.Board.get(app, boardImageLabel.getWidth(), getUnit(), getUnitStar()));
            boardImageLabel.repaint();
        }
    }

    public void comb_loadCombination() {
        combChipLM.clear();
        boolean selected = !combList.isSelectionEmpty();
        statButton.setEnabled(selected);
        combMarkButton.setEnabled(selected);
        combTagButton.setEnabled(selected);
        if (selected) {
            Board board = combList.getSelectedValue();
            int size = Math.min(combImageLabel.getHeight(), combImageLabel.getWidth()) - 1;
            combImageLabel.setIcon(AppImage.Board.get(app, size, board));
            combImageLabel.setText("");

            board.forEachChip((c) -> {
                c.setDisplayType(app.setting.displayType);
                combChipLM.addElement(c);
            });
            comb_updateMark();

            Stat stat = board.getStat();
            Stat cMax = board.getCustomMaxStat();
            Stat oMax = board.getOrigMaxStat();
            Stat resonance = board.getResonance();
            Stat pt = board.getPt();

            combDmgStatLabel.setText(stat.dmg + " / " + cMax.dmg + (cMax.dmg == oMax.dmg ? "" : " (" + oMax.dmg + ")"));
            combBrkStatLabel.setText(stat.brk + " / " + cMax.brk + (cMax.brk == oMax.brk ? "" : " (" + oMax.brk + ")"));
            combHitStatLabel.setText(stat.hit + " / " + cMax.hit + (cMax.hit == oMax.hit ? "" : " (" + oMax.hit + ")"));
            combRldStatLabel.setText(stat.rld + " / " + cMax.rld + (cMax.rld == oMax.rld ? "" : " (" + oMax.rld + ")"));
            combDmgStatLabel.setForeground(stat.dmg >= cMax.dmg ? Color.RED : Color.BLACK);
            combBrkStatLabel.setForeground(stat.brk >= cMax.brk ? Color.RED : Color.BLACK);
            combHitStatLabel.setForeground(stat.hit >= cMax.hit ? Color.RED : Color.BLACK);
            combRldStatLabel.setForeground(stat.rld >= cMax.rld ? Color.RED : Color.BLACK);

            combDmgPercLabel.setText(Fn.fPercStr(board.getStatPerc(Stat.DMG))
                    + (cMax.dmg == oMax.dmg
                            ? ""
                            : " (" + Fn.iPercStr(Board.getStatPerc(Stat.DMG, stat, oMax)) + ")"));
            combBrkPercLabel.setText(Fn.fPercStr(board.getStatPerc(Stat.BRK))
                    + (cMax.brk == oMax.brk
                            ? ""
                            : " (" + Fn.iPercStr(Board.getStatPerc(Stat.BRK, stat, oMax)) + ")"));
            combHitPercLabel.setText(Fn.fPercStr(board.getStatPerc(Stat.HIT))
                    + (cMax.hit == oMax.hit
                            ? ""
                            : " (" + Fn.iPercStr(Board.getStatPerc(Stat.HIT, stat, oMax)) + ")"));
            combRldPercLabel.setText(Fn.fPercStr(board.getStatPerc(Stat.RLD))
                    + (cMax.rld == oMax.rld
                            ? ""
                            : " (" + Fn.iPercStr(Board.getStatPerc(Stat.RLD, stat, oMax)) + ")"));

            combDmgPtLabel.setText(app.getText(AppText.UNIT_PT, String.valueOf(pt.dmg)));
            combBrkPtLabel.setText(app.getText(AppText.UNIT_PT, String.valueOf(pt.brk)));
            combHitPtLabel.setText(app.getText(AppText.UNIT_PT, String.valueOf(pt.hit)));
            combRldPtLabel.setText(app.getText(AppText.UNIT_PT, String.valueOf(pt.rld)));

            combDmgPtLabel.setForeground(Color.BLACK);
            combBrkPtLabel.setForeground(Color.BLACK);
            combHitPtLabel.setForeground(Color.BLACK);
            combRldPtLabel.setForeground(Color.BLACK);

            combDmgResonanceStatLabel.setText("+" + resonance.dmg);
            combBrkResonanceStatLabel.setText("+" + resonance.brk);
            combHitResonanceStatLabel.setText("+" + resonance.hit);
            combRldResonanceStatLabel.setText("+" + resonance.rld);

            Color color = AppColor.CHIPS.get(board.getColor());
            combDmgResonanceStatLabel.setForeground(color);
            combBrkResonanceStatLabel.setForeground(color);
            combHitResonanceStatLabel.setForeground(color);
            combRldResonanceStatLabel.setForeground(color);

            ticketLabel.setText(String.valueOf(board.getTicketCount()));
            xpLabel.setText(Fn.thousandComma(board.getXP()));
        } else {
            combImageLabel.setIcon(null);
            combImageLabel.setText(app.getText(AppText.COMB_DESC));

            combDmgStatLabel.setForeground(Color.BLACK);
            combDmgStatLabel.setText("");
            combBrkStatLabel.setText("");
            combHitStatLabel.setText("");
            combRldStatLabel.setText("");

            combDmgPercLabel.setForeground(Color.BLACK);
            combDmgPercLabel.setText("");
            combBrkPercLabel.setText("");
            combHitPercLabel.setText("");
            combRldPercLabel.setText("");

            combDmgPtLabel.setForeground(Color.BLACK);
            combDmgPtLabel.setText("");
            combBrkPtLabel.setText("");
            combHitPtLabel.setText("");
            combRldPtLabel.setText("");

            combDmgResonanceStatLabel.setForeground(Color.BLACK);
            combDmgResonanceStatLabel.setText("");
            combBrkResonanceStatLabel.setText("");
            combHitResonanceStatLabel.setText("");
            combRldResonanceStatLabel.setText("");

            ticketLabel.setText("-");
            xpLabel.setText("-");
        }
        invList.repaint();
    }

    private void comb_updateMark() {
        for (Enumeration<Chip> combChips = combChipLM.elements(); combChips.hasMoreElements();) {
            Chip c = combChips.nextElement();
            for (Chip invChip : invChips) {
                if (invChip.equals(c)) {
                    c.setMarked(invChip.isMarked());
                    break;
                }
            }
        }
        combChipList.repaint();

        for (Enumeration<ChipFreq> combCFs = combFreqLM.elements(); combCFs.hasMoreElements();) {
            Chip c = combCFs.nextElement().chip;
            for (Chip invChip : invChips) {
                if (invChip.equals(c)) {
                    c.setMarked(invChip.isMarked());
                    break;
                }
            }
        }
        combFreqList.repaint();
    }

    private List<Chip> comb_result_getChipsFromInv() {
        List<Chip> out = new ArrayList<>();
        for (Enumeration<Chip> chipEnum = combChipLM.elements(); chipEnum.hasMoreElements();) {
            Chip c = chipEnum.nextElement();
            for (Chip invChip : invChips) {
                if (invChip.equals(c)) {
                    out.add(invChip);
                    break;
                }
            }
        }
        return out;
    }

    private List<Chip> comb_freq_getChipsFromInv() {
        List<Chip> out = new ArrayList<>();
        for (Enumeration<ChipFreq> cfEnum = combFreqLM.elements(); cfEnum.hasMoreElements();) {
            Chip c = cfEnum.nextElement().chip;
            for (Chip invChip : invChips) {
                if (invChip.equals(c)) {
                    out.add(invChip);
                    break;
                }
            }
        }
        return out;
    }

    private void comb_openStatDialog() {
        if (!combList.isSelectionEmpty()) {
            Board board = combList.getSelectedValue();
            StatDialog.open(app, board);
        }
    }

    private void comb_result_mark() {
        if (!combList.isSelectionEmpty()) {
            List<Chip> chipList = comb_result_getChipsFromInv();
            // Continue
            int retval = JOptionPane.showConfirmDialog(this,
                    app.getText(AppText.COMB_MARK_CONTINUE_BODY), app.getText(AppText.COMB_MARK_CONTINUE_TITLE),
                    JOptionPane.YES_NO_OPTION);
            // If some chips are missing in the inventory
            if (retval == JOptionPane.YES_OPTION && combChipLM.size() != chipList.size()) {
                retval = JOptionPane.showConfirmDialog(this,
                        app.getText(AppText.COMB_DNE_BODY), app.getText(AppText.COMB_DNE_TITLE),
                        JOptionPane.YES_NO_OPTION);
            }
            // Mark
            if (retval == JOptionPane.YES_OPTION) {
                chipList.forEach((c) -> c.setMarked(true));
                invList.repaint();
                comb_updateMark();
                invStat_enableSave();
            }
        }
    }

    private void comb_freq_mark() {
        if (!combFreqLM.isEmpty()) {
            List<Chip> chipList = comb_freq_getChipsFromInv();
            // Continue
            int retval = JOptionPane.showConfirmDialog(this,
                    app.getText(AppText.COMB_MARK_CONTINUE_BODY), app.getText(AppText.COMB_MARK_CONTINUE_TITLE),
                    JOptionPane.YES_NO_OPTION);
            // Some chips are missing in the inventory
            if (retval == JOptionPane.YES_OPTION && combFreqLM.size() != chipList.size()) {
                retval = JOptionPane.showConfirmDialog(this,
                        app.getText(AppText.COMB_DNE_BODY), app.getText(AppText.COMB_DNE_TITLE),
                        JOptionPane.YES_NO_OPTION);
            }
            // Mark
            if (retval == JOptionPane.YES_OPTION) {
                chipList.forEach((c) -> c.setMarked(true));
                invList.repaint();
                comb_updateMark();
                invStat_enableSave();
            }
        }
    }

    private void comb_result_openTagDialog() {
        if (!combList.isSelectionEmpty()) {
            List<Chip> chipList = comb_result_getChipsFromInv();
            int retval = JOptionPane.YES_OPTION;
            if (combChipLM.size() != chipList.size()) {
                retval = JOptionPane.showConfirmDialog(this,
                        app.getText(AppText.COMB_DNE_BODY), app.getText(AppText.COMB_DNE_TITLE),
                        JOptionPane.YES_NO_OPTION);
            }
            if (retval == JOptionPane.YES_OPTION) {
                openDialog(TagDialog.getInstance(app, chipList));
            }
        }
    }

    private void comb_freq_openTagDialog() {
        if (!combFreqLM.isEmpty()) {
            List<Chip> chipList = comb_freq_getChipsFromInv();
            int retval = JOptionPane.YES_OPTION;
            if (combFreqLM.size() != chipList.size()) {
                retval = JOptionPane.showConfirmDialog(this,
                        app.getText(AppText.COMB_DNE_BODY), app.getText(AppText.COMB_DNE_TITLE),
                        JOptionPane.YES_NO_OPTION);
            }
            if (retval == JOptionPane.YES_OPTION) {
                openDialog(TagDialog.getInstance(app, chipList));
            }
        }
    }

    private void comb_ensureInvListIndexIsVisible_combChipList() {
        if (!combChipList.isSelectionEmpty()) {
            Chip selected = combChipList.getSelectedValue();
            for (int i = 0; i < invLM.size(); i++) {
                Chip invChip = (Chip) invLM.get(i);
                if (selected.equals(invChip)) {
                    invList.ensureIndexIsVisible(i);
                    break;
                }
            }
        }
    }

    private void comb_ensureInvListIndexIsVisible_combChipFreqList() {
        if (!combFreqList.isSelectionEmpty()) {
            ChipFreq selected = combFreqList.getSelectedValue();
            for (int i = 0; i < invLM.size(); i++) {
                Chip invChip = (Chip) invLM.get(i);
                if (selected.chip.equals(invChip)) {
                    invList.ensureIndexIsVisible(i);
                    break;
                }
            }
        }
    }

    private void comb_updateFreqLabel() {
        if (!combFreqList.isSelectionEmpty()) {
            ChipFreq selected = combFreqList.getSelectedValue();
            combFreqLabel.setText(Fn.fPercStr(selected.freq) + " (" + app.getText(AppText.UNIT_COUNT, selected.count) + ")");
        } else {
            combFreqLabel.setText("-");
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Process Methods">
    private void process_toggleStartPause() {
        switch (assembler.getStatus()) {
            case STOPPED:
                process_start();
                break;
            case RUNNING:
                process_pause();
                break;
            case PAUSED:
                process_resume();
                break;
            default:
                throw new AssertionError();
        }
    }

    private CalcSetting calcSetting;
    private CalcExtraSetting calcExtraSetting;
    private Progress progress;

    private void process_start() {
        // Check for the validity of all inventory chips
        for (Enumeration<Chip> elements = invLM.elements(); elements.hasMoreElements();) {
            Chip chip = elements.nextElement();
            if (!chip.isPtValid()) {
                JOptionPane.showMessageDialog(this,
                        app.getText(AppText.COMB_ERROR_STAT_BODY),
                        app.getText(AppText.COMB_ERROR_STAT_TITLE),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Unit unit = getUnit();
        int unitStar = getUnitStar();

        // init
        boolean start = true;
        int calcMode = CalcExtraSetting.CALCMODE_DICTIONARY;
        boolean alt = false;
        Shape.Type minType = assembler.getMinType(unit, unitStar, false);

        if (app.setting.advancedSetting) {
            // Partial option
            if (assembler.hasPartial(unit, unitStar)) {
                // Query
                String[] options = {
                    app.getText(AppText.COMB_OPTION_M2_0),
                    app.getText(AppText.COMB_OPTION_M2_1),
                    app.getText(AppText.COMB_OPTION_M2_2),
                    app.getText(AppText.ACTION_CANCEL)
                };
                int response = JOptionPane.showOptionDialog(this,
                        app.getText(AppText.COMB_OPTION_M2_DESC, options[0], options[1]),
                        app.getText(AppText.COMB_OPTION_TITLE),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]
                );
                // Response
                start = response != JOptionPane.CLOSED_OPTION && response <= 2;
                calcMode = response <= 1 ? CalcExtraSetting.CALCMODE_DICTIONARY : CalcExtraSetting.CALCMODE_DXZ;
                alt = response == 0;
            } //
            // Full option
            else {
                // Check if any chip size is smaller than dictionary chip size
                Enumeration<Chip> elements = invLM.elements();
                while (elements.hasMoreElements() && calcMode == CalcExtraSetting.CALCMODE_DICTIONARY) {
                    Chip c = elements.nextElement();
                    if (!c.typeGeq(minType)) {
                        calcMode = CalcExtraSetting.CALCMODE_DXZ;
                        break;
                    }
                }
                // Query
                if (calcMode == CalcExtraSetting.CALCMODE_DXZ) {
                    String combOption0Text = AppText.text_type(app, minType);
                    String[] options = {
                        app.getText(AppText.COMB_OPTION_DEFAULT_0, combOption0Text),
                        app.getText(AppText.COMB_OPTION_DEFAULT_1),
                        app.getText(AppText.ACTION_CANCEL)
                    };
                    int response = JOptionPane.showOptionDialog(this,
                            app.getText(AppText.COMB_OPTION_DEFAULT_DESC, options[0], combOption0Text),
                            app.getText(AppText.COMB_OPTION_TITLE),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]
                    );
                    // Response
                    start = response != JOptionPane.CLOSED_OPTION && response <= 1;
                    calcMode = response == 0 ? CalcExtraSetting.CALCMODE_DICTIONARY : CalcExtraSetting.CALCMODE_DXZ;
                }
            }
        } else if (getUnitStar() == 5 && !setting_isPresetFilter()) {
            int retval = JOptionPane.showOptionDialog(this,
                    app.getText(AppText.COMB_OPTION_FILTER_DESC),
                    app.getText(AppText.COMB_OPTION_TITLE),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            start = retval != JOptionPane.CLOSED_OPTION && retval != JOptionPane.CANCEL_OPTION;
            if (retval == JOptionPane.YES_OPTION) {
                setting_applyPresetFilter();
            }
        }

        // If preset DNE
        if (!assembler.btExists(unit, unitStar, alt)) {
            calcMode = CalcExtraSetting.CALCMODE_DXZ;
        }

        if (start) {
            // Filter and deep-copy chips
            List<Chip> candidates = new ArrayList<>();
            for (Enumeration<Chip> elements = invLM.elements(); elements.hasMoreElements();) {
                Chip chip = elements.nextElement();
                boolean colorMatch = !app.setting.colorMatch || unit.getColor() == chip.getColor();
                boolean sizeMatch = calcMode == CalcExtraSetting.CALCMODE_DXZ || chip.typeGeq(minType);
                boolean markMatchNeg = 0 < app.setting.boardMarkMax || !chip.isMarked();
                boolean markMatchPos = app.setting.boardMarkMin < Board.getCellCount(unit, unitStar) || chip.isMarked();
                if (colorMatch && sizeMatch && markMatchNeg && markMatchPos) {
                    candidates.add(new Chip(chip));
                }
            }

            if (app.setting.maxLevel) {
                candidates.forEach((c) -> c.setMaxLevel());
            }

            BoardSetting bs = app.setting.board;
            Stat stat, pt;

            switch (bs.getStatMode(unit, unitStar)) {
                case BoardSetting.MAX_PRESET:
                    int presetIndex = bs.getPresetIndex(unit, unitStar);
                    stat = unit.getPresetStat(presetIndex);
                    pt = unit.getPresetPt(presetIndex);
                    break;
                case BoardSetting.MAX_STAT:
                    stat = bs.getStat(unit, unitStar);
                    pt = bs.getPt(unit, unitStar);
                    break;
                default:
                    stat = Board.getMaxStat(unit, unitStar);
                    pt = Board.getMaxPt(unit, unitStar);
            }

            calcSetting = new CalcSetting(unit, unitStar, app.setting.maxLevel, app.setting.rotation, app.setting.symmetry, stat, pt);
            calcExtraSetting = new CalcExtraSetting(calcMode, alt ? 1 : 0,
                    app.setting.colorMatch,
                    app.setting.boardMarkMin, app.setting.boardMarkMax,
                    app.setting.boardMarkType, app.setting.boardSortType, candidates);
            progress = new Progress(app.setting.boardSortType);
            process_init();
            process_resume();
        }
    }

    private void process_init() {
        assembler.set(calcSetting, calcExtraSetting, progress);

        time = System.currentTimeMillis();
        pauseTime = 0;

        process_updateProgress(true);

        combStopButton.setVisible(true);

    }

    private void process_setUI(Assembler.Status status) {
        switch (status) {
            case RUNNING:
                loadingLabel.setIcon(AppImage.LOADING);
                break;
            case PAUSED:
                loadingLabel.setIcon(AppImage.PAUSED);
                break;
            case STOPPED:
                loadingLabel.setIcon(null);
                setting_resetBoard();
                break;
            default:
                throw new AssertionError();
        }

        if (status != Assembler.Status.RUNNING) {
            prevDoneTime = 0;
            doneTimes.clear();
        }

        combStartPauseButton.setIcon(status == Assembler.Status.RUNNING ? AppImage.COMB_PAUSE : AppImage.COMB_START);
        combStopButton.setVisible(status != Assembler.Status.STOPPED);

        unitComboBox.setEnabled(status == Assembler.Status.STOPPED);
        unitStarComboBox.setEnabled(status == Assembler.Status.STOPPED);
        settingButton.setEnabled(status == Assembler.Status.STOPPED);
        researchButton.setEnabled(status == Assembler.Status.STOPPED);
        combOpenButton.setEnabled(status == Assembler.Status.STOPPED);
        combSaveButton.setEnabled(status != Assembler.Status.RUNNING && progress != null && progress.getBoardSize() > 0);

        process_updateProgress(status != Assembler.Status.RUNNING);
    }

    private void calcTimer() {
        if (assembler.getStatus() == Assembler.Status.RUNNING) {
            process_updateProgress(false);
        }
    }

    private void process_pause() {
        pauseTime = System.currentTimeMillis();
        calcTimer.stop();

        process_setUI(Assembler.Status.PAUSED);
        assembler.pause();
    }

    private void process_resume() {
        if (0 < pauseTime) {
            time += System.currentTimeMillis() - pauseTime;
        }
        pauseTime = 0;
        calcTimer.start();

        process_setUI(Assembler.Status.RUNNING);
        assembler.resume();
    }

    private void process_stop() {
        if (0 < pauseTime) {
            time += System.currentTimeMillis() - pauseTime;
        }
        pauseTime = 0;
        calcTimer.stop();

        if (progress != null) {
            calcExtraSetting.calcMode = CalcExtraSetting.CALCMODE_FINISHED;
        }
        process_setUI(Assembler.Status.STOPPED);
        assembler.stop();
    }

    private void process_updateProgress(boolean forceUpdate) {
        process_setCombLabelText();
        process_setElapsedTime();
        process_refreshCombListModel(forceUpdate);
    }

    private void process_setCombLabelText() {
        if (progress != null && calcExtraSetting.calcMode == CalcExtraSetting.CALCMODE_FINISHED && 0 == progress.nComb) {
            combLabel.setText(app.getText(AppText.COMB_NONEFOUND));
        } else if (progress != null && 0 <= progress.nComb) {
            combLabel.setText(Fn.thousandComma(progress.nComb));
        } else {
            combLabel.setText("");
        }
    }

    private void process_setElapsedTime() {
        StringBuilder sb = new StringBuilder();

        long sec = (System.currentTimeMillis() - time) / 1000;
        sb.append(Fn.getTime(sec));

        boolean warn = false;
        if (!doneTimes.isEmpty()) {
            long avg = doneTimes.stream().mapToLong((v) -> v).sum() / doneTimes.size();
            long remaining = avg * (progress.nTotal - progress.nDone) / 1000;
            warn = 60 * 60 < remaining;
            sb.append(" (").append(app.getText(AppText.COMB_REMAINING, Fn.getTime(remaining))).append(")");
        }

        timeWarningButton.setVisible(app.setting.advancedSetting && warn);
        timeLabel.setText(sb.toString());
    }

    private void process_refreshCombListModel(boolean forceUpdate) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (forceUpdate || assembler.boardsUpdated()) {
                    Board selectedBoard = null;
                    String selectedChipID = null;
                    if (!combList.isSelectionEmpty()) {
                        selectedBoard = combList.getSelectedValue();
                    }
                    if (!combFreqList.isSelectionEmpty()) {
                        selectedChipID = combFreqList.getSelectedValue().chip.getID();
                    }

                    AssemblyResult ar = assembler.getResult();

                    boolean exist = !ar.freqs.isEmpty();

                    combLM.clear();
                    ar.boards.forEach((b) -> combLM.addElement(b));

                    combFreqLM.clear();
                    ar.freqs.forEach((cf) -> combFreqLM.addElement(cf));

                    combFreqMarkButton.setEnabled(exist);
                    combFreqTagButton.setEnabled(exist);

                    combList.setSelectedValue(selectedBoard, true);

                    if (selectedChipID != null) {
                        int i = 0;
                        int size = combFreqLM.size();
                        boolean found = false;
                        while (!found && i < size) {
                            if (selectedChipID.equals(combFreqLM.get(i).chip.getID())) {
                                combFreqList.setSelectedIndex(i);
                                combFreqList.ensureIndexIsVisible(i);
                                combFreqList.repaint();
                                found = true;
                            }
                            i++;
                        }
                    }
                }
            } catch (Exception ex) {
            }
        });
    }

    // From Combinator
    private void process_setProgBar(int n, int max) {
        combProgressBar.setMaximum(max);
        combProgressBar.setValue(n);
    }

    private void process_showImage(BoardTemplate template) {
        SwingUtilities.invokeLater(() -> {
            if (app.setting.showProgImage && assembler.getStatus() == Assembler.Status.RUNNING) {
                boardImageLabel.setIcon(AppImage.Board.get(app, boardImageLabel.getWidth(), template.getMatrix()));
                boardImageLabel.repaint();
            }
        });
    }

    private void process_prog(int prog) {
        SwingUtilities.invokeLater(() -> {
            long doneTime = System.currentTimeMillis();
            if (prevDoneTime != 0) {
                long t = doneTime - prevDoneTime;
                if (doneTimes.size() == SIZE_DONETIME) {
                    doneTimes.remove(0);
                }
                doneTimes.add(t);
            }
            prevDoneTime = doneTime;
            combProgressBar.setValue(prog);
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Setting File Methods">
    private void settingFile_load() {
        if (!settingFile_loading) {
            settingFile_loading = true;

            refreshDisplay();

            pool_setOrder(app.setting.poolOrder);
            poolStarComboBox.setSelectedIndex(5 - app.setting.poolStar);

            setPoolPanelVisible(app.setting.poolPanelVisible);
            display_setType(app.setting.displayType);

            showProgImageCheckBox.setSelected(app.setting.showProgImage);

            settingFile_loading = false;
        }
    }

    public void settingFile_save() {
        if (!settingFile_loading) {
            IO.saveSettings(app.setting);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inventory File Methods">
    private boolean invFile_confirmSave() {
        if (invSaveButton.isEnabled()) {
            int retval = JOptionPane.showConfirmDialog(this,
                    app.getText(AppText.FILE_SAVE_BODY), app.getText(AppText.FILE_SAVE_TITLE),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (retval == JOptionPane.CANCEL_OPTION) {
                return false;
            } else if (retval == JOptionPane.YES_OPTION) {
                invFile_save();
            }
        }
        return true;
    }

    private void invFile_new() {
        if (invFile_confirmSave()) {
            invFile_clear();
        }
    }

    public void invFile_clear() {
        invFile_path = "";
        fileTextArea.setText("");
        inv_chipsClear();
        invSaveButton.setEnabled(false);
    }

    private void invFile_open() {
        if (invFile_confirmSave()) {
            int retval = iofc.showOpenDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                invFile_path = iofc.getSelectedFile().getPath();
                fileTextArea.setText(iofc.getSelectedFile().getName());
                inv_chipsLoad(invFile_path.endsWith("." + IO.EXT_INVENTORY)
                        ? IO.loadInventory(invFile_path)
                        : JsonFilterDialog.filter(app, this, JsonParser.readFile(invFile_path))
                );
                invSaveButton.setEnabled(false);
            }
        }
    }

    private void invFile_save() {
        if (invSaveButton.isEnabled()) {
            if (invFile_path.isEmpty()) {
                invFile_saveAs();
            } else {
                IO.saveInventory(invFile_path, invChips);
                invSaveButton.setEnabled(false);
            }
        }
    }

    private void invFile_saveAs() {
        int retval = isfc.showSaveDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            String selectedPath = isfc.getSelectedFile().getPath();
            String fileName = isfc.getSelectedFile().getName();

            // Extension
            if (!selectedPath.endsWith("." + IO.EXT_INVENTORY)) {
                selectedPath += "." + IO.EXT_INVENTORY;
                fileName += "." + IO.EXT_INVENTORY;
            }

            // Overwrite
            boolean confirmed = true;
            if (isfc.getSelectedFile().exists()) {
                int option = JOptionPane.showConfirmDialog(this,
                        app.getText(AppText.FILE_OVERWRITE_BODY), app.getText(AppText.FILE_OVERWRITE_TITLE),
                        JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION) {
                    confirmed = false;
                }
            }

            // Save
            if (confirmed) {
                invFile_path = selectedPath;
                IO.saveInventory(invFile_path, invChips);
                fileTextArea.setText(fileName);
                invSaveButton.setEnabled(false);
            }
        }
    }

    private void invFile_openImageDialog() {
        ImageDialog.getData(app).forEach((c) -> inv_chipsAdd(c));
    }

    private void invFile_openProxyDialog() {
        if (invFile_confirmSave()) {
            List<Chip> chips = ProxyDialog.extract(app);
            if (chips != null) {
                invFile_clear();
                inv_chipsLoad(chips);
                invStat_enableSave();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Progress File Methods">
    private void progFile_open() {
        int retval = cfc.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            String path = cfc.getSelectedFile().getPath();
            ProgressFile pf = IO.loadProgressFile(path, invChips);
            calcSetting = pf.cs;
            calcExtraSetting = pf.ces;
            progress = pf.p;
            combSaveButton.setEnabled(false);

            unitComboBox.setSelectedItem(calcSetting.unit);
            unitStarComboBox.setSelectedIndex(5 - calcSetting.unitStar);

            if (calcExtraSetting.calcMode != CalcExtraSetting.CALCMODE_FINISHED) {
                Setting setting = app.setting;

                setting.maxLevel = calcSetting.maxLevel;
                setting.rotation = calcSetting.rotation;
                setting.colorMatch = calcExtraSetting.matchColor;

                setting.boardMarkMin = calcExtraSetting.markMin;
                setting.boardMarkMax = calcExtraSetting.markMax;
                setting.boardMarkType = calcExtraSetting.markType;
                setting.boardSortType = calcExtraSetting.sortType;
            }
            process_init();
            if (calcExtraSetting.calcMode != CalcExtraSetting.CALCMODE_FINISHED) {
                process_pause();
            }
        }
    }

    private void progFile_saveAs() {
        if (combSaveButton.isEnabled()) {
            int retval = cfc.showSaveDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                String path = cfc.getSelectedFile().getPath();

                // Extension
                if (!path.endsWith("." + IO.EXT_COMBINATION)) {
                    path += "." + IO.EXT_COMBINATION;
                }

                // Save
                IO.saveProgressFile(path, new ProgressFile(calcSetting, calcExtraSetting, progress));
                combSaveButton.setEnabled(false);
            }
        }
    }
    // </editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        combResultPanel = new javax.swing.JPanel();
        combChipListPanel = new javax.swing.JPanel();
        combChipListScrollPane = new javax.swing.JScrollPane();
        combChipList = new javax.swing.JList<>();
        combMarkButton = new javax.swing.JButton();
        combTagButton = new javax.swing.JButton();
        combImagePanel = new javax.swing.JPanel();
        combImageLabel = new javax.swing.JLabel();
        combStatPanel = new javax.swing.JPanel();
        statButton = new javax.swing.JButton();
        combDmgPanel = new javax.swing.JPanel();
        combDmgTextLabel = new javax.swing.JLabel();
        combDmgPercLabel = new javax.swing.JLabel();
        combDmgResonanceStatLabel = new javax.swing.JLabel();
        combDmgPtLabel = new javax.swing.JLabel();
        combDmgStatLabel = new javax.swing.JLabel();
        combBrkPanel = new javax.swing.JPanel();
        combBrkTextLabel = new javax.swing.JLabel();
        combBrkStatLabel = new javax.swing.JLabel();
        combBrkPtLabel = new javax.swing.JLabel();
        combBrkResonanceStatLabel = new javax.swing.JLabel();
        combBrkPercLabel = new javax.swing.JLabel();
        combHitPanel = new javax.swing.JPanel();
        combHitTextLabel = new javax.swing.JLabel();
        combHitStatLabel = new javax.swing.JLabel();
        combHitPtLabel = new javax.swing.JLabel();
        combHitResonanceStatLabel = new javax.swing.JLabel();
        combHitPercLabel = new javax.swing.JLabel();
        combRldPanel = new javax.swing.JPanel();
        combRldTextLabel = new javax.swing.JLabel();
        combRldStatLabel = new javax.swing.JLabel();
        combRldPtLabel = new javax.swing.JLabel();
        combRldResonanceStatLabel = new javax.swing.JLabel();
        combRldPercLabel = new javax.swing.JLabel();
        combInfoPanel = new javax.swing.JPanel();
        ticketLabel = new javax.swing.JLabel();
        ticketTextLabel = new javax.swing.JLabel();
        xpTextLabel = new javax.swing.JLabel();
        xpLabel = new javax.swing.JLabel();
        combFreqPanel = new javax.swing.JPanel();
        combFreqLabel = new javax.swing.JLabel();
        combFreqListPanel = new javax.swing.JPanel();
        combFreqListScrollPane = new javax.swing.JScrollPane();
        combFreqList = new javax.swing.JList<>();
        combFreqMarkButton = new javax.swing.JButton();
        combFreqTagButton = new javax.swing.JButton();
        poolPanel = new javax.swing.JPanel();
        poolTPanel = new javax.swing.JPanel();
        helpButton = new javax.swing.JButton();
        displaySettingButton = new javax.swing.JButton();
        donationButton = new javax.swing.JButton();
        poolBPanel = new javax.swing.JPanel();
        poolControlPanel = new javax.swing.JPanel();
        poolRotRButton = new javax.swing.JButton();
        poolRotLButton = new javax.swing.JButton();
        poolSortButton = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        poolColorButton = new javax.swing.JButton();
        poolStarComboBox = new javax.swing.JComboBox<>();
        poolListPanel = new javax.swing.JPanel();
        poolListScrollPane = new javax.swing.JScrollPane();
        poolList = new javax.swing.JList<>();
        poolReadPanel = new javax.swing.JPanel();
        imageButton = new javax.swing.JButton();
        proxyButton = new javax.swing.JButton();
        piButtonPanel = new javax.swing.JPanel();
        poolWindowButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        invPanel = new javax.swing.JPanel();
        invTPanel = new javax.swing.JPanel();
        invNewButton = new javax.swing.JButton();
        invOpenButton = new javax.swing.JButton();
        invSaveButton = new javax.swing.JButton();
        invSaveAsButton = new javax.swing.JButton();
        fileTAPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fileTextArea = new javax.swing.JTextArea();
        invBPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        filterChipCountLabel = new javax.swing.JLabel();
        invSortTypeComboBox = new javax.swing.JComboBox<>();
        invSortOrderButton = new javax.swing.JButton();
        filterButton = new javax.swing.JButton();
        displayTypeButton = new javax.swing.JButton();
        invStatPanel = new javax.swing.JPanel();
        invApplyButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        invStarComboBox = new javax.swing.JComboBox<>();
        invColorButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        invDmgPanel = new javax.swing.JPanel();
        invDmgTextLabel = new javax.swing.JLabel();
        invDmgComboBox = new javax.swing.JComboBox<>();
        invDmgPtLabel = new javax.swing.JLabel();
        invBrkPanel = new javax.swing.JPanel();
        invBrkTextLabel = new javax.swing.JLabel();
        invBrkComboBox = new javax.swing.JComboBox<>();
        invBrkPtLabel = new javax.swing.JLabel();
        invHitPanel = new javax.swing.JPanel();
        invHitTextLabel = new javax.swing.JLabel();
        invHitComboBox = new javax.swing.JComboBox<>();
        invHitPtLabel = new javax.swing.JLabel();
        invRldPanel = new javax.swing.JPanel();
        invRldTextLabel = new javax.swing.JLabel();
        invRldComboBox = new javax.swing.JComboBox<>();
        invRldPtLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        enhancementTextLabel = new javax.swing.JLabel();
        invLevelSlider = new javax.swing.JSlider();
        invLevelLabel = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        invRotLButton = new javax.swing.JButton();
        invRotRButton = new javax.swing.JButton();
        invDelButton = new javax.swing.JButton();
        invMarkCheckBox = new javax.swing.JCheckBox();
        invTagButton = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        invListPanel = new javax.swing.JPanel();
        invListScrollPane = new javax.swing.JScrollPane();
        invList = new javax.swing.JList<>();
        combLeftPanel = new javax.swing.JPanel();
        combLTPanel = new javax.swing.JPanel();
        settingButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        unitComboBox = new javax.swing.JComboBox<>();
        unitStarComboBox = new javax.swing.JComboBox<>();
        combLBPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        combLabel = new javax.swing.JLabel();
        combWarningButton = new javax.swing.JButton();
        combListPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        combList = new javax.swing.JList<>();
        researchButton = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        legendEquippedLabel = new javax.swing.JLabel();
        legendRotatedLabel = new javax.swing.JLabel();
        combRightPanel = new javax.swing.JPanel();
        combRTPanel = new javax.swing.JPanel();
        combStopButton = new javax.swing.JButton();
        loadingLabel = new javax.swing.JLabel();
        boardImageLabel = new javax.swing.JLabel();
        showProgImageCheckBox = new javax.swing.JCheckBox();
        combStartPauseButton = new javax.swing.JButton();
        combRBPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        combSaveButton = new javax.swing.JButton();
        combOpenButton = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        timeLabel = new javax.swing.JLabel();
        timeWarningButton = new javax.swing.JButton();
        combTabbedPane = new javax.swing.JTabbedPane();
        combProgressBar = new javax.swing.JProgressBar();
        tipLabel = new javax.swing.JLabel();

        combChipListPanel.setFocusable(false);

        combChipListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        combChipListScrollPane.setFocusable(false);
        combChipListScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        combChipList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        combChipList.setFocusable(false);
        combChipList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        combChipList.setVisibleRowCount(-1);
        combChipListScrollPane.setViewportView(combChipList);

        combMarkButton.setText("mark");
        combMarkButton.setEnabled(false);
        combMarkButton.setFocusable(false);
        combMarkButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        combTagButton.setText("tag");
        combTagButton.setEnabled(false);
        combTagButton.setFocusable(false);
        combTagButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        javax.swing.GroupLayout combChipListPanelLayout = new javax.swing.GroupLayout(combChipListPanel);
        combChipListPanel.setLayout(combChipListPanelLayout);
        combChipListPanelLayout.setHorizontalGroup(
            combChipListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combChipListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combMarkButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combChipListPanelLayout.setVerticalGroup(
            combChipListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combChipListPanelLayout.createSequentialGroup()
                .addComponent(combChipListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combMarkButton)
                .addGap(0, 0, 0)
                .addComponent(combTagButton))
        );

        combImagePanel.setFocusable(false);
        combImagePanel.setPreferredSize(new java.awt.Dimension(175, 175));

        combImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combImageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combImageLabel.setFocusable(false);

        javax.swing.GroupLayout combImagePanelLayout = new javax.swing.GroupLayout(combImagePanel);
        combImagePanel.setLayout(combImagePanelLayout);
        combImagePanelLayout.setHorizontalGroup(
            combImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combImagePanelLayout.setVerticalGroup(
            combImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        combStatPanel.setFocusable(false);

        statButton.setText("detail");
        statButton.setEnabled(false);
        statButton.setFocusable(false);

        combDmgTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combDmgTextLabel.setText("D");
        combDmgTextLabel.setFocusable(false);
        combDmgTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        combDmgTextLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        combDmgPercLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combDmgPercLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combDmgPercLabel.setFocusable(false);
        combDmgPercLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        combDmgResonanceStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combDmgResonanceStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combDmgResonanceStatLabel.setFocusable(false);
        combDmgResonanceStatLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combDmgPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combDmgPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combDmgPtLabel.setFocusable(false);
        combDmgPtLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combDmgStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combDmgStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combDmgStatLabel.setFocusable(false);
        combDmgStatLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        javax.swing.GroupLayout combDmgPanelLayout = new javax.swing.GroupLayout(combDmgPanel);
        combDmgPanel.setLayout(combDmgPanelLayout);
        combDmgPanelLayout.setHorizontalGroup(
            combDmgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, combDmgPanelLayout.createSequentialGroup()
                .addComponent(combDmgTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(combDmgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combDmgStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combDmgPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combDmgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combDmgResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combDmgPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        combDmgPanelLayout.setVerticalGroup(
            combDmgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combDmgPanelLayout.createSequentialGroup()
                .addGroup(combDmgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combDmgPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combDmgStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combDmgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combDmgPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combDmgResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(combDmgTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        combBrkTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combBrkTextLabel.setText("B");
        combBrkTextLabel.setFocusable(false);
        combBrkTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        combBrkTextLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        combBrkStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combBrkStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combBrkStatLabel.setFocusable(false);
        combBrkStatLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        combBrkPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combBrkPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combBrkPtLabel.setFocusable(false);
        combBrkPtLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combBrkResonanceStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combBrkResonanceStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combBrkResonanceStatLabel.setFocusable(false);
        combBrkResonanceStatLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combBrkPercLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combBrkPercLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combBrkPercLabel.setFocusable(false);
        combBrkPercLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        javax.swing.GroupLayout combBrkPanelLayout = new javax.swing.GroupLayout(combBrkPanel);
        combBrkPanel.setLayout(combBrkPanelLayout);
        combBrkPanelLayout.setHorizontalGroup(
            combBrkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combBrkPanelLayout.createSequentialGroup()
                .addComponent(combBrkTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(combBrkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combBrkStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combBrkPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combBrkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combBrkResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combBrkPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        combBrkPanelLayout.setVerticalGroup(
            combBrkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combBrkPanelLayout.createSequentialGroup()
                .addGroup(combBrkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combBrkStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combBrkPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combBrkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combBrkPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combBrkResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(combBrkTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        combHitTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combHitTextLabel.setText("H");
        combHitTextLabel.setFocusable(false);
        combHitTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        combHitTextLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        combHitStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combHitStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combHitStatLabel.setFocusable(false);
        combHitStatLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        combHitPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combHitPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combHitPtLabel.setFocusable(false);
        combHitPtLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combHitResonanceStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combHitResonanceStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combHitResonanceStatLabel.setFocusable(false);
        combHitResonanceStatLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combHitPercLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combHitPercLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combHitPercLabel.setFocusable(false);
        combHitPercLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        javax.swing.GroupLayout combHitPanelLayout = new javax.swing.GroupLayout(combHitPanel);
        combHitPanel.setLayout(combHitPanelLayout);
        combHitPanelLayout.setHorizontalGroup(
            combHitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combHitPanelLayout.createSequentialGroup()
                .addComponent(combHitTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(combHitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combHitStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combHitPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combHitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combHitResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combHitPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        combHitPanelLayout.setVerticalGroup(
            combHitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, combHitPanelLayout.createSequentialGroup()
                .addGroup(combHitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combHitStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combHitPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combHitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combHitPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combHitResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(combHitTextLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        combRldTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combRldTextLabel.setText("R");
        combRldTextLabel.setFocusable(false);
        combRldTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        combRldTextLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        combRldStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combRldStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combRldStatLabel.setFocusable(false);
        combRldStatLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        combRldPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combRldPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combRldPtLabel.setFocusable(false);
        combRldPtLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combRldResonanceStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combRldResonanceStatLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combRldResonanceStatLabel.setFocusable(false);
        combRldResonanceStatLabel.setPreferredSize(new java.awt.Dimension(50, 22));

        combRldPercLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combRldPercLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combRldPercLabel.setFocusable(false);
        combRldPercLabel.setPreferredSize(new java.awt.Dimension(110, 22));

        javax.swing.GroupLayout combRldPanelLayout = new javax.swing.GroupLayout(combRldPanel);
        combRldPanel.setLayout(combRldPanelLayout);
        combRldPanelLayout.setHorizontalGroup(
            combRldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combRldPanelLayout.createSequentialGroup()
                .addComponent(combRldTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(combRldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combRldStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combRldPercLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combRldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combRldResonanceStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combRldPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        combRldPanelLayout.setVerticalGroup(
            combRldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combRldPanelLayout.createSequentialGroup()
                .addGroup(combRldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combRldPtLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combRldStatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(combRldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combRldPercLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combRldResonanceStatLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(combRldTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout combStatPanelLayout = new javax.swing.GroupLayout(combStatPanel);
        combStatPanel.setLayout(combStatPanelLayout);
        combStatPanelLayout.setHorizontalGroup(
            combStatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combDmgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combBrkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combHitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combRldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(statButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combStatPanelLayout.setVerticalGroup(
            combStatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combStatPanelLayout.createSequentialGroup()
                .addComponent(combDmgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combBrkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combHitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combRldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statButton))
        );

        ticketLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ticketLabel.setText("-");
        ticketLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ticketLabel.setFocusable(false);
        ticketLabel.setPreferredSize(new java.awt.Dimension(75, 22));

        ticketTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        ticketTextLabel.setText("ticket");
        ticketTextLabel.setFocusable(false);
        ticketTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        xpTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        xpTextLabel.setText("enh");
        xpTextLabel.setFocusable(false);

        xpLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        xpLabel.setText("-");
        xpLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        xpLabel.setFocusable(false);
        xpLabel.setPreferredSize(new java.awt.Dimension(75, 22));

        javax.swing.GroupLayout combInfoPanelLayout = new javax.swing.GroupLayout(combInfoPanel);
        combInfoPanel.setLayout(combInfoPanelLayout);
        combInfoPanelLayout.setHorizontalGroup(
            combInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combInfoPanelLayout.createSequentialGroup()
                .addGroup(combInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ticketTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xpTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(combInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xpLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ticketLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        combInfoPanelLayout.setVerticalGroup(
            combInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combInfoPanelLayout.createSequentialGroup()
                .addGroup(combInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ticketTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ticketLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(combInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xpLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xpTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        combInfoPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {ticketLabel, xpLabel});

        javax.swing.GroupLayout combResultPanelLayout = new javax.swing.GroupLayout(combResultPanel);
        combResultPanel.setLayout(combResultPanelLayout);
        combResultPanelLayout.setHorizontalGroup(
            combResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combResultPanelLayout.createSequentialGroup()
                .addGroup(combResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(combStatPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combChipListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        combResultPanelLayout.setVerticalGroup(
            combResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combResultPanelLayout.createSequentialGroup()
                .addComponent(combImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combStatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(combChipListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        combFreqLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combFreqLabel.setText("-");
        combFreqLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combFreqLabel.setFocusable(false);
        combFreqLabel.setPreferredSize(new java.awt.Dimension(75, 22));

        combFreqListPanel.setFocusable(false);

        combFreqListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        combFreqListScrollPane.setFocusable(false);
        combFreqListScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        combFreqList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        combFreqList.setFocusable(false);
        combFreqList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        combFreqList.setVisibleRowCount(-1);
        combFreqListScrollPane.setViewportView(combFreqList);

        javax.swing.GroupLayout combFreqListPanelLayout = new javax.swing.GroupLayout(combFreqListPanel);
        combFreqListPanel.setLayout(combFreqListPanelLayout);
        combFreqListPanelLayout.setHorizontalGroup(
            combFreqListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combFreqListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combFreqListPanelLayout.setVerticalGroup(
            combFreqListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combFreqListScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
        );

        combFreqMarkButton.setText("mark");
        combFreqMarkButton.setEnabled(false);
        combFreqMarkButton.setFocusable(false);
        combFreqMarkButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        combFreqTagButton.setText("tag");
        combFreqTagButton.setEnabled(false);
        combFreqTagButton.setFocusable(false);
        combFreqTagButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        javax.swing.GroupLayout combFreqPanelLayout = new javax.swing.GroupLayout(combFreqPanel);
        combFreqPanel.setLayout(combFreqPanelLayout);
        combFreqPanelLayout.setHorizontalGroup(
            combFreqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combFreqListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combFreqLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combFreqTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combFreqMarkButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combFreqPanelLayout.setVerticalGroup(
            combFreqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combFreqPanelLayout.createSequentialGroup()
                .addComponent(combFreqLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combFreqListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combFreqMarkButton)
                .addGap(0, 0, 0)
                .addComponent(combFreqTagButton))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        poolPanel.setFocusable(false);

        poolTPanel.setFocusable(false);

        helpButton.setFocusable(false);
        helpButton.setMinimumSize(new java.awt.Dimension(50, 50));
        helpButton.setPreferredSize(new java.awt.Dimension(50, 50));

        displaySettingButton.setFocusable(false);
        displaySettingButton.setMinimumSize(new java.awt.Dimension(50, 50));
        displaySettingButton.setPreferredSize(new java.awt.Dimension(50, 50));

        donationButton.setText("<html>Your donation will<br>help me run this app!</html>");
        donationButton.setPreferredSize(new java.awt.Dimension(200, 41));

        javax.swing.GroupLayout poolTPanelLayout = new javax.swing.GroupLayout(poolTPanel);
        poolTPanel.setLayout(poolTPanelLayout);
        poolTPanelLayout.setHorizontalGroup(
            poolTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(poolTPanelLayout.createSequentialGroup()
                .addComponent(helpButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(displaySettingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(donationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        poolTPanelLayout.setVerticalGroup(
            poolTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(donationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(helpButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(displaySettingButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        poolBPanel.setFocusable(false);

        poolControlPanel.setFocusable(false);
        poolControlPanel.setPreferredSize(new java.awt.Dimension(274, 50));

        poolRotRButton.setFocusable(false);
        poolRotRButton.setMinimumSize(new java.awt.Dimension(50, 50));
        poolRotRButton.setPreferredSize(new java.awt.Dimension(50, 50));

        poolRotLButton.setFocusable(false);
        poolRotLButton.setMinimumSize(new java.awt.Dimension(50, 50));
        poolRotLButton.setPreferredSize(new java.awt.Dimension(50, 50));

        poolSortButton.setFocusable(false);
        poolSortButton.setMinimumSize(new java.awt.Dimension(50, 50));
        poolSortButton.setPreferredSize(new java.awt.Dimension(50, 50));

        poolColorButton.setFocusable(false);
        poolColorButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        poolColorButton.setPreferredSize(new java.awt.Dimension(100, 22));

        poolStarComboBox.setFocusable(false);
        poolStarComboBox.setPreferredSize(new java.awt.Dimension(100, 22));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolStarComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(poolColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(poolStarComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(poolColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout poolControlPanelLayout = new javax.swing.GroupLayout(poolControlPanel);
        poolControlPanel.setLayout(poolControlPanelLayout);
        poolControlPanelLayout.setHorizontalGroup(
            poolControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, poolControlPanelLayout.createSequentialGroup()
                .addComponent(poolRotLButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(poolRotRButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(poolSortButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        poolControlPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {poolRotLButton, poolRotRButton, poolSortButton});

        poolControlPanelLayout.setVerticalGroup(
            poolControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(poolSortButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(poolRotLButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(poolRotRButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        poolControlPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {poolRotLButton, poolRotRButton, poolSortButton});

        poolListPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        poolListPanel.setFocusable(false);

        poolListScrollPane.setFocusable(false);
        poolListScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        poolList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        poolList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        poolList.setVisibleRowCount(-1);
        poolListScrollPane.setViewportView(poolList);

        javax.swing.GroupLayout poolListPanelLayout = new javax.swing.GroupLayout(poolListPanel);
        poolListPanel.setLayout(poolListPanelLayout);
        poolListPanelLayout.setHorizontalGroup(
            poolListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        poolListPanelLayout.setVerticalGroup(
            poolListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolListScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        imageButton.setFocusable(false);
        imageButton.setMinimumSize(new java.awt.Dimension(50, 50));
        imageButton.setPreferredSize(new java.awt.Dimension(50, 50));

        proxyButton.setFocusable(false);
        proxyButton.setMinimumSize(new java.awt.Dimension(50, 50));
        proxyButton.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout poolReadPanelLayout = new javax.swing.GroupLayout(poolReadPanel);
        poolReadPanel.setLayout(poolReadPanelLayout);
        poolReadPanelLayout.setHorizontalGroup(
            poolReadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(poolReadPanelLayout.createSequentialGroup()
                .addComponent(imageButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(proxyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        poolReadPanelLayout.setVerticalGroup(
            poolReadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(imageButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(proxyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout poolBPanelLayout = new javax.swing.GroupLayout(poolBPanel);
        poolBPanel.setLayout(poolBPanelLayout);
        poolBPanelLayout.setHorizontalGroup(
            poolBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(poolControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
            .addComponent(poolReadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        poolBPanelLayout.setVerticalGroup(
            poolBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, poolBPanelLayout.createSequentialGroup()
                .addComponent(poolReadPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(poolListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(poolControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout poolPanelLayout = new javax.swing.GroupLayout(poolPanel);
        poolPanel.setLayout(poolPanelLayout);
        poolPanelLayout.setHorizontalGroup(
            poolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(poolTPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        poolPanelLayout.setVerticalGroup(
            poolPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(poolPanelLayout.createSequentialGroup()
                .addComponent(poolTPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(poolBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        piButtonPanel.setFocusable(false);

        poolWindowButton.setFocusable(false);
        poolWindowButton.setMinimumSize(new java.awt.Dimension(50, 50));
        poolWindowButton.setPreferredSize(new java.awt.Dimension(50, 50));

        addButton.setEnabled(false);
        addButton.setFocusable(false);
        addButton.setMinimumSize(new java.awt.Dimension(50, 50));
        addButton.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout piButtonPanelLayout = new javax.swing.GroupLayout(piButtonPanel);
        piButtonPanel.setLayout(piButtonPanelLayout);
        piButtonPanelLayout.setHorizontalGroup(
            piButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(poolWindowButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        piButtonPanelLayout.setVerticalGroup(
            piButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(piButtonPanelLayout.createSequentialGroup()
                .addComponent(poolWindowButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        invPanel.setFocusable(false);

        invTPanel.setFocusable(false);

        invNewButton.setFocusable(false);
        invNewButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invNewButton.setPreferredSize(new java.awt.Dimension(50, 50));

        invOpenButton.setFocusable(false);
        invOpenButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invOpenButton.setPreferredSize(new java.awt.Dimension(50, 50));

        invSaveButton.setEnabled(false);
        invSaveButton.setFocusable(false);
        invSaveButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invSaveButton.setPreferredSize(new java.awt.Dimension(50, 50));

        invSaveAsButton.setFocusable(false);
        invSaveAsButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invSaveAsButton.setPreferredSize(new java.awt.Dimension(50, 50));

        fileTAPanel.setFocusable(false);
        fileTAPanel.setPreferredSize(new java.awt.Dimension(50, 50));

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setToolTipText("");
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setFocusable(false);

        fileTextArea.setBackground(java.awt.SystemColor.control);
        fileTextArea.setColumns(20);
        fileTextArea.setLineWrap(true);
        fileTextArea.setRows(5);
        fileTextArea.setEnabled(false);
        fileTextArea.setFocusable(false);
        jScrollPane1.setViewportView(fileTextArea);

        javax.swing.GroupLayout fileTAPanelLayout = new javax.swing.GroupLayout(fileTAPanel);
        fileTAPanel.setLayout(fileTAPanelLayout);
        fileTAPanelLayout.setHorizontalGroup(
            fileTAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        fileTAPanelLayout.setVerticalGroup(
            fileTAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout invTPanelLayout = new javax.swing.GroupLayout(invTPanel);
        invTPanel.setLayout(invTPanelLayout);
        invTPanelLayout.setHorizontalGroup(
            invTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invTPanelLayout.createSequentialGroup()
                .addComponent(invNewButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(invOpenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(invSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(invSaveAsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileTAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
        );
        invTPanelLayout.setVerticalGroup(
            invTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invNewButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(invOpenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(invSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(invSaveAsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(fileTAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        invBPanel.setFocusable(false);

        jPanel9.setFocusable(false);

        jPanel12.setFocusable(false);

        filterChipCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        filterChipCountLabel.setFocusable(false);

        invSortTypeComboBox.setFocusable(false);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterChipCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invSortTypeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(invSortTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterChipCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        invSortOrderButton.setEnabled(false);
        invSortOrderButton.setFocusable(false);
        invSortOrderButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invSortOrderButton.setPreferredSize(new java.awt.Dimension(50, 50));

        filterButton.setFocusable(false);
        filterButton.setMinimumSize(new java.awt.Dimension(50, 50));
        filterButton.setPreferredSize(new java.awt.Dimension(50, 50));

        displayTypeButton.setFocusable(false);
        displayTypeButton.setMinimumSize(new java.awt.Dimension(50, 50));
        displayTypeButton.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(invSortOrderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(displayTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(displayTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(filterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(invSortOrderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        invStatPanel.setFocusable(false);

        invApplyButton.setText("apply all");
        invApplyButton.setFocusable(false);

        invStarComboBox.setEnabled(false);
        invStarComboBox.setFocusable(false);
        invStarComboBox.setPreferredSize(new java.awt.Dimension(100, 22));

        invColorButton.setEnabled(false);
        invColorButton.setFocusable(false);
        invColorButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        invColorButton.setPreferredSize(new java.awt.Dimension(75, 22));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(invStarComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(invStarComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(invColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jPanel2.setFocusable(false);

        invDmgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        invDmgPanel.setLayout(new java.awt.BorderLayout(5, 0));

        invDmgTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        invDmgTextLabel.setText("D");
        invDmgTextLabel.setFocusable(false);
        invDmgTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        invDmgPanel.add(invDmgTextLabel, java.awt.BorderLayout.LINE_START);

        invDmgComboBox.setEnabled(false);
        invDmgComboBox.setFocusable(false);
        invDmgComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        invDmgPanel.add(invDmgComboBox, java.awt.BorderLayout.CENTER);

        invDmgPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        invDmgPtLabel.setText("-");
        invDmgPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        invDmgPtLabel.setFocusable(false);
        invDmgPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        invDmgPanel.add(invDmgPtLabel, java.awt.BorderLayout.LINE_END);

        invBrkPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        invBrkPanel.setLayout(new java.awt.BorderLayout(5, 0));

        invBrkTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        invBrkTextLabel.setText("B");
        invBrkTextLabel.setFocusable(false);
        invBrkTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        invBrkPanel.add(invBrkTextLabel, java.awt.BorderLayout.LINE_START);

        invBrkComboBox.setEnabled(false);
        invBrkComboBox.setFocusable(false);
        invBrkComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        invBrkPanel.add(invBrkComboBox, java.awt.BorderLayout.CENTER);

        invBrkPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        invBrkPtLabel.setText("-");
        invBrkPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        invBrkPtLabel.setFocusable(false);
        invBrkPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        invBrkPanel.add(invBrkPtLabel, java.awt.BorderLayout.LINE_END);

        invHitPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        invHitPanel.setLayout(new java.awt.BorderLayout(5, 0));

        invHitTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        invHitTextLabel.setText("H");
        invHitTextLabel.setFocusable(false);
        invHitTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        invHitPanel.add(invHitTextLabel, java.awt.BorderLayout.LINE_START);

        invHitComboBox.setEnabled(false);
        invHitComboBox.setFocusable(false);
        invHitComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        invHitPanel.add(invHitComboBox, java.awt.BorderLayout.CENTER);

        invHitPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        invHitPtLabel.setText("-");
        invHitPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        invHitPtLabel.setFocusable(false);
        invHitPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        invHitPanel.add(invHitPtLabel, java.awt.BorderLayout.LINE_END);

        invRldPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        invRldPanel.setLayout(new java.awt.BorderLayout(5, 0));

        invRldTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        invRldTextLabel.setText("R");
        invRldTextLabel.setFocusable(false);
        invRldTextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        invRldPanel.add(invRldTextLabel, java.awt.BorderLayout.LINE_START);

        invRldComboBox.setEnabled(false);
        invRldComboBox.setFocusable(false);
        invRldComboBox.setPreferredSize(new java.awt.Dimension(50, 22));
        invRldPanel.add(invRldComboBox, java.awt.BorderLayout.CENTER);

        invRldPtLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        invRldPtLabel.setText("-");
        invRldPtLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        invRldPtLabel.setFocusable(false);
        invRldPtLabel.setPreferredSize(new java.awt.Dimension(22, 22));
        invRldPanel.add(invRldPtLabel, java.awt.BorderLayout.LINE_END);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invDmgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invBrkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invHitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invRldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(invDmgPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(invBrkPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(invHitPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 1, Short.MAX_VALUE)
                .addComponent(invRldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        enhancementTextLabel.setText("강화");
        enhancementTextLabel.setFocusable(false);

        invLevelSlider.setMajorTickSpacing(5);
        invLevelSlider.setMaximum(20);
        invLevelSlider.setMinorTickSpacing(1);
        invLevelSlider.setSnapToTicks(true);
        invLevelSlider.setValue(0);
        invLevelSlider.setEnabled(false);
        invLevelSlider.setFocusable(false);
        invLevelSlider.setPreferredSize(new java.awt.Dimension(100, 22));

        invLevelLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        invLevelLabel.setText("-");
        invLevelLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        invLevelLabel.setFocusable(false);
        invLevelLabel.setPreferredSize(new java.awt.Dimension(22, 22));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(enhancementTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invLevelSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invLevelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(enhancementTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(invLevelSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(invLevelLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        invRotLButton.setEnabled(false);
        invRotLButton.setFocusable(false);
        invRotLButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invRotLButton.setPreferredSize(new java.awt.Dimension(50, 50));

        invRotRButton.setEnabled(false);
        invRotRButton.setFocusable(false);
        invRotRButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invRotRButton.setPreferredSize(new java.awt.Dimension(50, 50));

        invDelButton.setEnabled(false);
        invDelButton.setFocusable(false);
        invDelButton.setMinimumSize(new java.awt.Dimension(50, 50));
        invDelButton.setPreferredSize(new java.awt.Dimension(50, 50));

        invMarkCheckBox.setText("mark");
        invMarkCheckBox.setBorder(null);
        invMarkCheckBox.setEnabled(false);
        invMarkCheckBox.setFocusable(false);
        invMarkCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        invMarkCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        invMarkCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(invRotLButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(invRotRButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(invDelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invMarkCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {invDelButton, invRotLButton, invRotRButton});

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(invDelButton, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addComponent(invMarkCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(invRotRButton, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addComponent(invRotLButton, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {invDelButton, invRotLButton, invRotRButton});

        invTagButton.setText("tag");
        invTagButton.setEnabled(false);
        invTagButton.setFocusable(false);
        invTagButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        javax.swing.GroupLayout invStatPanelLayout = new javax.swing.GroupLayout(invStatPanel);
        invStatPanel.setLayout(invStatPanelLayout);
        invStatPanelLayout.setHorizontalGroup(
            invStatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invStatPanelLayout.createSequentialGroup()
                .addGroup(invStatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(invApplyButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invTagButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        invStatPanelLayout.setVerticalGroup(
            invStatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invStatPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(invStatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(invStatPanelLayout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invTagButton)
                .addGap(0, 0, 0)
                .addComponent(invApplyButton))
        );

        invListPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        invListPanel.setFocusable(false);

        invListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        invListScrollPane.setFocusable(false);
        invListScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        invList.setDragEnabled(true);
        invList.setDropMode(javax.swing.DropMode.INSERT);
        invList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        invList.setVisibleRowCount(-1);
        invListScrollPane.setViewportView(invList);

        javax.swing.GroupLayout invListPanelLayout = new javax.swing.GroupLayout(invListPanel);
        invListPanel.setLayout(invListPanelLayout);
        invListPanelLayout.setHorizontalGroup(
            invListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        invListPanelLayout.setVerticalGroup(
            invListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(invListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout invBPanelLayout = new javax.swing.GroupLayout(invBPanel);
        invBPanel.setLayout(invBPanelLayout);
        invBPanelLayout.setHorizontalGroup(
            invBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invStatPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        invBPanelLayout.setVerticalGroup(
            invBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invBPanelLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invStatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout invPanelLayout = new javax.swing.GroupLayout(invPanel);
        invPanel.setLayout(invPanelLayout);
        invPanelLayout.setHorizontalGroup(
            invPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(invTPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        invPanelLayout.setVerticalGroup(
            invPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invPanelLayout.createSequentialGroup()
                .addComponent(invTPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        combLeftPanel.setFocusable(false);

        combLTPanel.setFocusable(false);

        settingButton.setFocusable(false);
        settingButton.setMinimumSize(new java.awt.Dimension(50, 50));
        settingButton.setPreferredSize(new java.awt.Dimension(50, 50));

        jPanel8.setFocusable(false);
        jPanel8.setPreferredSize(new java.awt.Dimension(100, 50));

        unitComboBox.setFocusable(false);
        unitComboBox.setPreferredSize(new java.awt.Dimension(100, 21));

        unitStarComboBox.setFocusable(false);
        unitStarComboBox.setPreferredSize(new java.awt.Dimension(100, 21));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(unitComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(unitStarComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(unitComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(unitStarComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout combLTPanelLayout = new javax.swing.GroupLayout(combLTPanel);
        combLTPanel.setLayout(combLTPanelLayout);
        combLTPanelLayout.setHorizontalGroup(
            combLTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combLTPanelLayout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(settingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        combLTPanelLayout.setVerticalGroup(
            combLTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(settingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        combLBPanel.setFocusable(false);

        jPanel4.setFocusable(false);
        jPanel4.setLayout(new java.awt.BorderLayout());

        combLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        combLabel.setText("0");
        combLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        combLabel.setFocusable(false);
        combLabel.setPreferredSize(new java.awt.Dimension(100, 22));
        jPanel4.add(combLabel, java.awt.BorderLayout.CENTER);

        combWarningButton.setFocusable(false);
        combWarningButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        combWarningButton.setPreferredSize(new java.awt.Dimension(21, 21));
        jPanel4.add(combWarningButton, java.awt.BorderLayout.WEST);

        combListPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        combListPanel.setFocusable(false);

        jScrollPane4.setFocusable(false);
        jScrollPane4.setPreferredSize(new java.awt.Dimension(100, 100));

        combList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        combList.setVisibleRowCount(-1);
        jScrollPane4.setViewportView(combList);

        javax.swing.GroupLayout combListPanelLayout = new javax.swing.GroupLayout(combListPanel);
        combListPanel.setLayout(combListPanelLayout);
        combListPanelLayout.setHorizontalGroup(
            combListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combListPanelLayout.setVerticalGroup(
            combListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        researchButton.setText("research");

        jPanel18.setLayout(new java.awt.BorderLayout());

        legendEquippedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        legendEquippedLabel.setText("legend equipped");
        jPanel18.add(legendEquippedLabel, java.awt.BorderLayout.NORTH);

        legendRotatedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        legendRotatedLabel.setText("legend rotated");
        jPanel18.add(legendRotatedLabel, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout combLBPanelLayout = new javax.swing.GroupLayout(combLBPanel);
        combLBPanel.setLayout(combLBPanelLayout);
        combLBPanelLayout.setHorizontalGroup(
            combLBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(researchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combLBPanelLayout.setVerticalGroup(
            combLBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, combLBPanelLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(researchButton))
        );

        javax.swing.GroupLayout combLeftPanelLayout = new javax.swing.GroupLayout(combLeftPanel);
        combLeftPanel.setLayout(combLeftPanelLayout);
        combLeftPanelLayout.setHorizontalGroup(
            combLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combLTPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combLBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combLeftPanelLayout.setVerticalGroup(
            combLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combLeftPanelLayout.createSequentialGroup()
                .addComponent(combLTPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combLBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        combRightPanel.setFocusable(false);

        combRTPanel.setFocusable(false);
        combRTPanel.setPreferredSize(new java.awt.Dimension(300, 50));

        combStopButton.setFocusable(false);
        combStopButton.setMinimumSize(new java.awt.Dimension(50, 50));
        combStopButton.setPreferredSize(new java.awt.Dimension(50, 50));

        loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadingLabel.setFocusable(false);
        loadingLabel.setPreferredSize(new java.awt.Dimension(50, 50));

        boardImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        boardImageLabel.setFocusable(false);
        boardImageLabel.setPreferredSize(new java.awt.Dimension(50, 50));

        showProgImageCheckBox.setFocusable(false);

        combStartPauseButton.setFocusable(false);
        combStartPauseButton.setMinimumSize(new java.awt.Dimension(50, 50));
        combStartPauseButton.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout combRTPanelLayout = new javax.swing.GroupLayout(combRTPanel);
        combRTPanel.setLayout(combRTPanelLayout);
        combRTPanelLayout.setHorizontalGroup(
            combRTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combRTPanelLayout.createSequentialGroup()
                .addComponent(showProgImageCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(boardImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(combStartPauseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(combStopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(loadingLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        combRTPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {boardImageLabel, loadingLabel});

        combRTPanelLayout.setVerticalGroup(
            combRTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combStopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(loadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(showProgImageCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(boardImageLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combStartPauseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        combRBPanel.setFocusable(false);

        jPanel3.setFocusable(false);

        combSaveButton.setEnabled(false);
        combSaveButton.setFocusable(false);
        combSaveButton.setMinimumSize(new java.awt.Dimension(50, 50));
        combSaveButton.setPreferredSize(new java.awt.Dimension(50, 50));

        combOpenButton.setFocusable(false);
        combOpenButton.setMinimumSize(new java.awt.Dimension(50, 50));
        combOpenButton.setPreferredSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(combOpenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(combSaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combOpenButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combSaveButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel17.setLayout(new java.awt.BorderLayout());

        timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeLabel.setText("0:00:00");
        timeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        timeLabel.setFocusable(false);
        timeLabel.setPreferredSize(new java.awt.Dimension(100, 22));
        jPanel17.add(timeLabel, java.awt.BorderLayout.CENTER);

        timeWarningButton.setPreferredSize(new java.awt.Dimension(21, 21));
        jPanel17.add(timeWarningButton, java.awt.BorderLayout.WEST);

        javax.swing.GroupLayout combRBPanelLayout = new javax.swing.GroupLayout(combRBPanel);
        combRBPanel.setLayout(combRBPanelLayout);
        combRBPanelLayout.setHorizontalGroup(
            combRBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combRBPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combTabbedPane)
        );
        combRBPanelLayout.setVerticalGroup(
            combRBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combRBPanelLayout.createSequentialGroup()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout combRightPanelLayout = new javax.swing.GroupLayout(combRightPanel);
        combRightPanel.setLayout(combRightPanelLayout);
        combRightPanelLayout.setHorizontalGroup(
            combRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(combRTPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(combRBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        combRightPanelLayout.setVerticalGroup(
            combRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(combRightPanelLayout.createSequentialGroup()
                .addComponent(combRTPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combRBPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        combProgressBar.setFocusable(false);

        tipLabel.setText(" ");
        tipLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(poolPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(piButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combLeftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combRightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(combProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tipLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(invPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combLeftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(combRightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(piButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(poolPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(tipLabel))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (invFile_confirmSave()) {
            process_stop();
            blinkTimer.stop();
            dispose();
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel boardImageLabel;
    private javax.swing.JPanel combBrkPanel;
    private javax.swing.JLabel combBrkPercLabel;
    private javax.swing.JLabel combBrkPtLabel;
    private javax.swing.JLabel combBrkResonanceStatLabel;
    private javax.swing.JLabel combBrkStatLabel;
    private javax.swing.JLabel combBrkTextLabel;
    private javax.swing.JList<Chip> combChipList;
    private javax.swing.JPanel combChipListPanel;
    private javax.swing.JScrollPane combChipListScrollPane;
    private javax.swing.JPanel combDmgPanel;
    private javax.swing.JLabel combDmgPercLabel;
    private javax.swing.JLabel combDmgPtLabel;
    private javax.swing.JLabel combDmgResonanceStatLabel;
    private javax.swing.JLabel combDmgStatLabel;
    private javax.swing.JLabel combDmgTextLabel;
    private javax.swing.JLabel combFreqLabel;
    private javax.swing.JList<ChipFreq> combFreqList;
    private javax.swing.JPanel combFreqListPanel;
    private javax.swing.JScrollPane combFreqListScrollPane;
    private javax.swing.JButton combFreqMarkButton;
    private javax.swing.JPanel combFreqPanel;
    private javax.swing.JButton combFreqTagButton;
    private javax.swing.JPanel combHitPanel;
    private javax.swing.JLabel combHitPercLabel;
    private javax.swing.JLabel combHitPtLabel;
    private javax.swing.JLabel combHitResonanceStatLabel;
    private javax.swing.JLabel combHitStatLabel;
    private javax.swing.JLabel combHitTextLabel;
    private javax.swing.JLabel combImageLabel;
    private javax.swing.JPanel combImagePanel;
    private javax.swing.JPanel combInfoPanel;
    private javax.swing.JPanel combLBPanel;
    private javax.swing.JPanel combLTPanel;
    private javax.swing.JLabel combLabel;
    private javax.swing.JPanel combLeftPanel;
    private javax.swing.JList<Board> combList;
    private javax.swing.JPanel combListPanel;
    private javax.swing.JButton combMarkButton;
    private javax.swing.JButton combOpenButton;
    private javax.swing.JProgressBar combProgressBar;
    private javax.swing.JPanel combRBPanel;
    private javax.swing.JPanel combRTPanel;
    private javax.swing.JPanel combResultPanel;
    private javax.swing.JPanel combRightPanel;
    private javax.swing.JPanel combRldPanel;
    private javax.swing.JLabel combRldPercLabel;
    private javax.swing.JLabel combRldPtLabel;
    private javax.swing.JLabel combRldResonanceStatLabel;
    private javax.swing.JLabel combRldStatLabel;
    private javax.swing.JLabel combRldTextLabel;
    private javax.swing.JButton combSaveButton;
    private javax.swing.JButton combStartPauseButton;
    private javax.swing.JPanel combStatPanel;
    private javax.swing.JButton combStopButton;
    private javax.swing.JTabbedPane combTabbedPane;
    private javax.swing.JButton combTagButton;
    private javax.swing.JButton combWarningButton;
    private javax.swing.JButton displaySettingButton;
    private javax.swing.JButton displayTypeButton;
    private javax.swing.JButton donationButton;
    private javax.swing.JLabel enhancementTextLabel;
    private javax.swing.JPanel fileTAPanel;
    private javax.swing.JTextArea fileTextArea;
    private javax.swing.JButton filterButton;
    private javax.swing.JLabel filterChipCountLabel;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton imageButton;
    private javax.swing.JButton invApplyButton;
    private javax.swing.JPanel invBPanel;
    private javax.swing.JComboBox<String> invBrkComboBox;
    private javax.swing.JPanel invBrkPanel;
    private javax.swing.JLabel invBrkPtLabel;
    private javax.swing.JLabel invBrkTextLabel;
    private javax.swing.JButton invColorButton;
    private javax.swing.JButton invDelButton;
    private javax.swing.JComboBox<String> invDmgComboBox;
    private javax.swing.JPanel invDmgPanel;
    private javax.swing.JLabel invDmgPtLabel;
    private javax.swing.JLabel invDmgTextLabel;
    private javax.swing.JComboBox<String> invHitComboBox;
    private javax.swing.JPanel invHitPanel;
    private javax.swing.JLabel invHitPtLabel;
    private javax.swing.JLabel invHitTextLabel;
    private javax.swing.JLabel invLevelLabel;
    private javax.swing.JSlider invLevelSlider;
    private javax.swing.JList<Chip> invList;
    private javax.swing.JPanel invListPanel;
    private javax.swing.JScrollPane invListScrollPane;
    private javax.swing.JCheckBox invMarkCheckBox;
    private javax.swing.JButton invNewButton;
    private javax.swing.JButton invOpenButton;
    private javax.swing.JPanel invPanel;
    private javax.swing.JComboBox<String> invRldComboBox;
    private javax.swing.JPanel invRldPanel;
    private javax.swing.JLabel invRldPtLabel;
    private javax.swing.JLabel invRldTextLabel;
    private javax.swing.JButton invRotLButton;
    private javax.swing.JButton invRotRButton;
    private javax.swing.JButton invSaveAsButton;
    private javax.swing.JButton invSaveButton;
    private javax.swing.JButton invSortOrderButton;
    private javax.swing.JComboBox<String> invSortTypeComboBox;
    private javax.swing.JComboBox<String> invStarComboBox;
    private javax.swing.JPanel invStatPanel;
    private javax.swing.JPanel invTPanel;
    private javax.swing.JButton invTagButton;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel legendEquippedLabel;
    private javax.swing.JLabel legendRotatedLabel;
    private javax.swing.JLabel loadingLabel;
    private javax.swing.JPanel piButtonPanel;
    private javax.swing.JPanel poolBPanel;
    private javax.swing.JButton poolColorButton;
    private javax.swing.JPanel poolControlPanel;
    private javax.swing.JList<Chip> poolList;
    private javax.swing.JPanel poolListPanel;
    private javax.swing.JScrollPane poolListScrollPane;
    private javax.swing.JPanel poolPanel;
    private javax.swing.JPanel poolReadPanel;
    private javax.swing.JButton poolRotLButton;
    private javax.swing.JButton poolRotRButton;
    private javax.swing.JButton poolSortButton;
    private javax.swing.JComboBox<String> poolStarComboBox;
    private javax.swing.JPanel poolTPanel;
    private javax.swing.JButton poolWindowButton;
    private javax.swing.JButton proxyButton;
    private javax.swing.JButton researchButton;
    private javax.swing.JButton settingButton;
    private javax.swing.JCheckBox showProgImageCheckBox;
    private javax.swing.JButton statButton;
    private javax.swing.JLabel ticketLabel;
    private javax.swing.JLabel ticketTextLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JButton timeWarningButton;
    private javax.swing.JLabel tipLabel;
    private javax.swing.JComboBox<Unit> unitComboBox;
    private javax.swing.JComboBox<String> unitStarComboBox;
    private javax.swing.JLabel xpLabel;
    private javax.swing.JLabel xpTextLabel;
    // End of variables declaration//GEN-END:variables
}
