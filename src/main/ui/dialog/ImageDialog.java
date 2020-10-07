package main.ui.dialog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.App;
import main.image.ImageProcessor;
import main.puzzle.Chip;
import main.puzzle.Shape;
import main.ui.resource.GFLGraphics;
import main.ui.resource.GFLTexts;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class ImageDialog extends JDialog {

    private final App app;
    private final JFileChooser fileChooser = new JFileChooser(new File("."));
    private JLabel imageLabel;
    private BufferedImage image;

    private boolean cancelled = true;

    private class RC {

        final Rectangle rect;
        Chip chip;

        public RC(Rectangle rect, Chip chip) {
            this.rect = rect;
            this.chip = chip;
        }

    }
    private final List<RC> rcs = new ArrayList<>();
    private final Map<Integer, JLabel> chipImagePoppedup = new HashMap<>();

    private int zoom = 100;
    private static final int ZOOM_MIN = 10;
    private static final int ZOOM_MAX = 1000;

    enum Interaction {
        DISABLED,
        ADD,
        DELETE
    }
    private Interaction interactionMode = Interaction.DISABLED;
    private boolean isMouseDown = false;
    private Point startP, currentP;

    public static List<Chip> getData(App app) {
        ImageDialog d = new ImageDialog(app);
        d.setVisible(true);
        List<Chip> chips = new ArrayList<>(d.rcs.size());
        if (!d.cancelled) {
            d.rcs.forEach((rc) -> chips.add(rc.chip));
        }
        return chips;
    }

    private ImageDialog(App app) {
        this.app = app;
        initComponents();
        init();
        openImage();
    }

    private void init() {
        setTitle(app.getText(GFLTexts.IMAGE_TITLE));
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                app.getText(GFLTexts.IMAGE_OPEN_EXT) + " (.png, .jpg, .gif, .bmp)",
                "png", "jpg", "gif", "bmp")
        );
        openButton.setText(app.getText(GFLTexts.IMAGE_OPEN));
        addToggleButton.setText(app.getText(GFLTexts.ACTION_ADD));
        deleteToggleButton.setText(app.getText(GFLTexts.ACTION_DEL));
        okButton.setText(app.getText(GFLTexts.ACTION_OK));
        cancelButton.setText(app.getText(GFLTexts.ACTION_CANCEL));

        initLabel();
        aScrollPane.setViewportView(imageLabel);
        zoomSpinner.setModel(new SpinnerNumberModel(100, ZOOM_MIN, ZOOM_MAX, 10));

        addListeners();

        this.setPreferredSize(app.mf.getPreferredDialogSize());
        pack();
    }

    private void initLabel() {
        imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke(new BasicStroke(3));

                    if (interactionMode == Interaction.ADD) {
                        if (isMouseDown) {
                            Rectangle r = getRectWithPts(startP, currentP);
                            g2.setColor(Color.WHITE);
                            g2.drawRect(r.x, r.y, r.width, r.height);
                        }
                    }

                    Set<Integer> invalidRectIndices = new HashSet<>();
                    for (int i = 0; i < rcs.size(); i++) {
                        Chip chip = rcs.get(i).chip;
                        if (!ImageDialog.isValid(chip)) {
                            invalidRectIndices.add(i);
                        }
                    }

                    for (int i = 0; i < rcs.size(); i++) {
                        Rectangle rect = rcs.get(i).rect;
                        g2.setColor(i == getSelectedRCIndex() ? Color.YELLOW
                                : invalidRectIndices.contains(i) ? Color.RED
                                : Color.GREEN);
                        g2.drawRect(rect.x * zoom / 100, rect.y * zoom / 100, rect.width * zoom / 100, rect.height * zoom / 100);
                    }
                }
            }
        };
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                startP = me.getPoint();
                isMouseDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                switch (interactionMode) {
                    case ADD:
                        Rectangle rect = getRectWithPts(startP, currentP);
                        addRect(unzoomRect(rect, zoom), true);
                        break;
                    case DELETE:
                        deleteSelectedChip();
                        break;
                    default:
                        modifySelectedChip();
                }
                setInteractionMode(Interaction.DISABLED);
                addToggleButton.setSelected(false);
                deleteToggleButton.setSelected(false);
                isMouseDown = false;
            }
        });
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                updatePoint(me);
            }

            @Override
            public void mouseMoved(MouseEvent me) {
                updatePoint(me);
                popupChipImage();
            }

            public void updatePoint(MouseEvent me) {
                Point p = me.getPoint();
                p.x = Fn.limit(p.x, 0, imageLabel.getWidth());
                p.y = Fn.limit(p.y, 0, imageLabel.getHeight());
                currentP = p;
                aScrollPane.repaint();
            }
        });
        imageLabel.addMouseWheelListener((e) -> {
            JScrollBar bar = aScrollPane.getVerticalScrollBar();
            if (e.isControlDown()) {
                if (e.getWheelRotation() < 0) {
                    zoomSpinner.setValue(Fn.limit(zoom + 10, ZOOM_MIN, ZOOM_MAX));
                } else {
                    zoomSpinner.setValue(Fn.limit(zoom - 10, ZOOM_MIN, ZOOM_MAX));
                }
            } else if (e.isShiftDown()) {
                bar = aScrollPane.getHorizontalScrollBar();
            }
            int val = bar.getValue();
            int inc = 100;
            if (e.getWheelRotation() < 0) {
                bar.setValue(bar.getValue() - inc);
            } else {
                bar.setValue(bar.getValue() + inc);
            }
        });
        imageLabel.setBackground(Color.GRAY);
        imageLabel.setVerticalAlignment(JLabel.TOP);
        //  imageLabel.setLayout(new FlowLayout());
    }

    private void addListeners() {
        Fn.addEscDisposeListener(this);
        zoomSpinner.addChangeListener((e) -> {
            zoom = (int) zoomSpinner.getValue();
            resizeImageWindow();
        });
    }

    private void resizeImageWindow() {
        imageLabel.setPreferredSize(new Dimension(image.getWidth() * zoom / 100, image.getHeight() * zoom / 100));
        Image i = image.getScaledInstance(image.getWidth() * zoom / 100, image.getHeight() * zoom / 100, Image.SCALE_DEFAULT);
        imageLabel.setIcon(new ImageIcon(i));
        refresh();
    }

    private void refresh() {
        aScrollPane.revalidate();
        aScrollPane.repaint();
    }

    private void setInteractionMode(Interaction interaction) {
        interactionMode = interaction;
        deleteToggleButton.setSelected(interactionMode == Interaction.DELETE);
        addToggleButton.setSelected(interactionMode == Interaction.ADD);
        imageLabel.setCursor(interactionMode == Interaction.DISABLED
                ? Cursor.getDefaultCursor()
                : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        imageLabel.repaint();
    }

    private boolean openImage() {
        int retval = fileChooser.showOpenDialog(app.mf);
        if (retval == JFileChooser.APPROVE_OPTION) {
            try {
                readImage(ImageIO.read(fileChooser.getSelectedFile()));
                resizeImageWindow();
            } catch (IOException ex) {
                App.log(ex);
            }
            return true;
        }
        return false;
    }

    private void readImage(BufferedImage image) {
        this.image = image;
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> scanProgressBar.setIndeterminate(true));
            List<Rectangle> candidates = ImageProcessor.detectChips(image);
            rcs.clear();
            SwingUtilities.invokeLater(() -> {
                scanProgressBar.setIndeterminate(false);
                scanProgressBar.setMaximum(candidates.size());
                scanProgressBar.setValue(0);
            });
            candidates.sort((o1, o2) -> {
                if (o2.y < o1.y + o1.height && o1.y < o2.y + o2.height) {
                    return Integer.compare(o1.x, o2.x);
                }
                return Integer.compare(o1.y, o2.y);
            });
            candidates.forEach((r) -> {
                addRect(r, false);
                SwingUtilities.invokeLater(() -> scanProgressBar.setValue(scanProgressBar.getValue() + 1));
            });
        }).start();
    }

    private void addRect(Rectangle rect, boolean addedByUser) {
        boolean overlapped = rcs.stream().anyMatch((rc) -> Fn.isOverlapped(rect, rc.rect));
        if (!overlapped) {
            Chip c = ImageProcessor.idChip(image, rect);
            rcs.add(new RC(rect, c));
            refresh();
        } else if (addedByUser) {
            JOptionPane.showMessageDialog(this,
                    app.getText(GFLTexts.IMAGE_OVERLAPPED_BODY),
                    app.getText(GFLTexts.IMAGE_OVERLAPPED_TITLE),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifySelectedChip() {
        int selected = getSelectedRCIndex();
        if (-1 < selected) {
            Chip c = ImageModifyDialog.modify(app, rcs.get(selected).chip);
            if (c != null && ImageDialog.isValid(c)) {
                rcs.get(selected).chip = c;
                refresh();
            }
        }
    }

    private void deleteSelectedChip() {
        int selected = getSelectedRCIndex();
        if (-1 < selected) {
            setPopupImageVisible(selected, false);
            rcs.remove(selected);
            refresh();
        }
    }

    private static Rectangle getRectWithPts(Point p1, Point p2) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int width = Math.max(p1.x, p2.x) - x;
        int height = Math.max(p1.y, p2.y) - y;
        return new Rectangle(x, y, width, height);
    }

    private static Rectangle zoomRect(Rectangle r, int zoom) {
        return new Rectangle(r.x * zoom / 100, r.y * zoom / 100, r.width * zoom / 100, r.height * zoom / 100);
    }

    private static Rectangle unzoomRect(Rectangle r, int zoom) {
        return new Rectangle(r.x * 100 / zoom, r.y * 100 / zoom, r.width * 100 / zoom, r.height * 100 / zoom);
    }

    public static boolean isValid(Chip chip) {
        if (chip == null || chip.getShape() == null || chip.getShape() == Shape.NONE) {
            return false;
        }
        return chip.getPt().sum() == chip.getSize();
    }

    private int getSelectedRCIndex() {
        for (int i = 0; i < rcs.size(); i++) {
            Rectangle rect = rcs.get(i).rect;
            if (Fn.isInside(currentP, zoomRect(rect, zoom))) {
                return i;
            }
        }
        return -1;
    }

    private void popupChipImage() {
        int selected = getSelectedRCIndex();
        if (-1 < selected) {
            Chip chip = rcs.get(selected).chip;
            if (isValid(chip) && !chipImagePoppedup.containsKey(selected)) {
                setPopupImageVisible(selected, true);
            }
        }

        for (int i = 0; i < rcs.size(); i++) {
            if (i != selected && chipImagePoppedup.containsKey(i)) {
                setPopupImageVisible(i, false);
            }
        }
    }

    private void setPopupImageVisible(int i, boolean b) {
        if (b) {
            Chip chip = rcs.get(i).chip;
            Rectangle rect = rcs.get(i).rect;
            ImageIcon icon = GFLGraphics.chip(app, chip);
            JLabel label = genChipLabel(icon, zoomRect(rect, zoom));
            chipImagePoppedup.put(i, label);
            imageLabel.add(label);
        } else {
            JLabel label = chipImagePoppedup.get(i);
            if (label != null) {
                chipImagePoppedup.remove(i);
                imageLabel.remove(label);
            }
        }
        imageLabel.revalidate();
        imageLabel.repaint();
    }

    private static JLabel genChipLabel(ImageIcon icon, Rectangle rect) {
        Rectangle fitRect = Fn.fit(icon.getIconWidth(), icon.getIconHeight(), rect);
        Image scaled = icon.getImage().getScaledInstance(fitRect.width, fitRect.height, Image.SCALE_SMOOTH);
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(scaled));
        label.setBounds(fitRect);
        label.setOpaque(true);
        return label;
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
        okButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        aScrollPane = new javax.swing.JScrollPane();
        addToggleButton = new javax.swing.JToggleButton();
        deleteToggleButton = new javax.swing.JToggleButton();
        zoomSpinner = new javax.swing.JSpinner();
        scanProgressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setType(java.awt.Window.Type.UTILITY);

        cancelButton.setText("취소");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("확인");
        okButton.setFocusable(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        openButton.setText("불러오기");
        openButton.setFocusable(false);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        aScrollPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(aScrollPane))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 415, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(aScrollPane))
        );

        addToggleButton.setText("추가");
        addToggleButton.setFocusable(false);
        addToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToggleButtonActionPerformed(evt);
            }
        });

        deleteToggleButton.setText("삭제");
        deleteToggleButton.setFocusable(false);
        deleteToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zoomSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scanProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(openButton)
                    .addComponent(addToggleButton)
                    .addComponent(deleteToggleButton)
                    .addComponent(zoomSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scanProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        openImage();
    }//GEN-LAST:event_openButtonActionPerformed

    private void addToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToggleButtonActionPerformed
        setInteractionMode(interactionMode == Interaction.ADD ? Interaction.DISABLED : Interaction.ADD);
    }//GEN-LAST:event_addToggleButtonActionPerformed

    private void deleteToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteToggleButtonActionPerformed
        setInteractionMode(interactionMode == Interaction.DELETE ? Interaction.DISABLED : Interaction.DELETE);
    }//GEN-LAST:event_deleteToggleButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        rcs.sort((o1, o2) -> {
            Rectangle r1 = o1.rect;
            Rectangle r2 = o2.rect;

            if (r2.y < r1.y + r1.height && r1.y < r2.y + r2.height) {
                return Integer.compare(r1.x, r2.x);
            }

            return Integer.compare(r1.y, r2.y);
        });
        cancelled = false;
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane aScrollPane;
    private javax.swing.JToggleButton addToggleButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JToggleButton deleteToggleButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JButton openButton;
    private javax.swing.JProgressBar scanProgressBar;
    private javax.swing.JSpinner zoomSpinner;
    // End of variables declaration//GEN-END:variables
}
