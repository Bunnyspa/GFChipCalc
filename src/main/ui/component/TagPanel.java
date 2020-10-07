package main.ui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.function.Predicate;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import main.App;
import main.puzzle.Tag;
import main.ui.renderer.TagTableCellRenderer;
import main.ui.resource.GFLTexts;

/**
 *
 * @author Bunnyspa
 */
public class TagPanel extends JPanel {

    private final App app;
    private final JDialog dialog;
    private final Predicate<Tag> checkFn;
    private final boolean checkBox;
    private final DefaultTableModel tm;

    public TagPanel(App app, JDialog dialog, Predicate<Tag> checkFn, boolean editable) {
        initComponents();

        this.app = app;
        this.dialog = dialog;
        this.checkFn = checkFn;
        this.checkBox = (checkFn != null);
        tm = new TagTableModel(checkBox);

        init();
        if (!editable) {
            editPanel.setVisible(false);
        }
    }

    private void init() {
        addButton.setText(app.getText(GFLTexts.ACTION_ADD));
        colorButton.setText(app.getText(GFLTexts.CHIP_COLOR));
        applyButton.setText(app.getText(GFLTexts.ACTION_APPLY));

        tagTable.setModel(tm);
        tagTable.setSelectionBackground(app.orange());
        tagTable.setDefaultRenderer(Tag.class, new TagTableCellRenderer(app, checkBox));
        if (checkBox) {
            tm.addColumn("bool");
        }
        tm.addColumn("tag");
        tagTable.getTableHeader().setUI(null);

        app.mf.inv_getAllTags().forEach((t) -> {
            if (checkBox) {
                Object[] obj = {checkFn.test(t), t};
                tm.addRow(obj);
            } else {
                Object[] obj = {t};
                tm.addRow(obj);
            }

        });
        if (checkBox) {
            resizeCheckBoxWidth(tagTable);
        }
        addListeners();
    }

    private void addListeners() {
        nameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == ',' || c == ';') {
                    e.consume();  // ignore event
                }
            }
        });

        tagTable.getSelectionModel().addListSelectionListener((e) -> {
            boolean selected = tagTable.getSelectedRowCount() > 0;
            nameTextField.setEnabled(selected);
            colorButton.setEnabled(selected);
            applyButton.setEnabled(selected);
            if (selected) {
                Tag tag = getTag(getSelectedRow());
                nameTextField.setText(tag.getName());
                nameTextField.setForeground(tag.getColor());
            } else {
                nameTextField.setText("");
                nameTextField.setForeground(Color.BLACK);
            }
        });
    }

    public void addTableModelListener(TableModelListener l) {
        tm.addTableModelListener(l);
    }

    public void addTableMouseListener(MouseListener l) {
        tagTable.addMouseListener(l);
    }

    public int getCount() {
        return tm.getRowCount();
    }

    public int getSelectedRow() {
        return tagTable.getSelectedRow();
    }

    public boolean isChecked(int i) {
        if (checkBox) {
            return (boolean) tm.getValueAt(i, 0);
        }
        return false;
    }

    public void setChecked(int i, boolean b) {
        if (checkBox) {
            tm.setValueAt(b, i, 0);
        }
    }

    public Tag getTag(int i) {
        if (checkBox) {
            return (Tag) tm.getValueAt(i, 1);
        }
        return (Tag) tm.getValueAt(i, 0);
    }

    private void addTag() {
        Tag tag = new Tag();
        if (checkBox) {
            tm.addRow(new Object[]{false, tag});
            resizeCheckBoxWidth(tagTable);
        } else {
            tm.addRow(new Object[]{tag});
        }
    }

    private void applyTagChange() {
        Tag tag = getTag(getSelectedRow());
        tag.setName(nameTextField.getText());
        tag.setColor(nameTextField.getForeground());
        app.mf.inv_getAllTags().stream().filter((t) -> tag.equals(t)).forEach((t) -> {
            t.setName(nameTextField.getText());
            t.setColor(nameTextField.getForeground());
        });
        tagTable.repaint();
        app.mf.invStat_enableSave();
        app.mf.invStat_loadStats();
    }

    private void changeColor() {
        Color color = JColorChooser.showDialog(dialog, "색상", nameTextField.getForeground());
        nameTextField.setForeground(color);
    }

    private static void resizeCheckBoxWidth(JTable tagTable) {
        if (tagTable.getRowCount() > 0) {
            // Col 0
            TableCellRenderer tcr0 = tagTable.getCellRenderer(0, 0);
            Component c0 = tagTable.prepareRenderer(tcr0, 0, 0);
            int width = c0.getPreferredSize().width;
            int height = c0.getPreferredSize().height;
            tagTable.getColumnModel().getColumn(0).setMaxWidth(width);
            // Col 1
            TableCellRenderer tcr1 = tagTable.getCellRenderer(0, 1);
            Component c1 = tagTable.prepareRenderer(tcr1, 0, 1);
            height = Math.max(height, c1.getPreferredSize().height);
            for (int i = 0; i < tagTable.getRowCount(); i++) {
                tagTable.setRowHeight(height);
            }
        }
    }

    private class TagTableModel extends DefaultTableModel {

        private final boolean checkBox;

        public TagTableModel(boolean checkBox) {
            this.checkBox = checkBox;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return checkBox && column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return checkBox && columnIndex == 0 ? Boolean.class : Tag.class;
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

        jScrollPane2 = new javax.swing.JScrollPane();
        tagTable = new javax.swing.JTable();
        editPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        colorButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();

        jScrollPane2.setPreferredSize(new java.awt.Dimension(250, 250));

        tagTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tagTable.setShowHorizontalLines(false);
        tagTable.setShowVerticalLines(false);
        jScrollPane2.setViewportView(tagTable);

        applyButton.setText("적용");
        applyButton.setEnabled(false);
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        nameTextField.setEnabled(false);
        nameTextField.setPreferredSize(new java.awt.Dimension(100, 22));

        colorButton.setText("색상");
        colorButton.setEnabled(false);
        colorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed(evt);
            }
        });

        addButton.setText("추가");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addComponent(colorButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyButton))
            .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        editPanelLayout.setVerticalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editPanelLayout.createSequentialGroup()
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorButton)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applyButton)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(editPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        applyTagChange();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void colorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorButtonActionPerformed
        changeColor();
    }//GEN-LAST:event_colorButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addTag();
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton colorButton;
    private javax.swing.JPanel editPanel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTable tagTable;
    // End of variables declaration//GEN-END:variables
}
