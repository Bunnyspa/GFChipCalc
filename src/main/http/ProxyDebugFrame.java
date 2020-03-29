package main.http;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Bunnyspa
 */
public class ProxyDebugFrame extends javax.swing.JFrame {

    private final DefaultListModel<ProxyMessage> model = new DefaultListModel<>();
    private final ListCellRenderer renderer = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DefaultListCellRenderer cr = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ProxyMessage pm = (ProxyMessage) value;
            cr.setText(pm.reqUrl);
            return cr;
        }
    };
    private Proxy proxy;

    public ProxyDebugFrame() {
        initComponents();
        initList();
    }

//    private void initProxy() {
//        try {
//            proxy = new ProxyThread(this);
//            proxy.start();
//            jLabel1.setText("프록시 호스트 이름: " + proxy.getAddress() + " 프록시 포트: " + proxy.getPort());
//            jButton2.setEnabled(false);
//        } catch (IOException ex) {
//            Logger.getLogger(ProxyDebugFrame.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    private void initList() {
        list.setModel(model);
        list.setCellRenderer(renderer);
        list.addListSelectionListener((e) -> {
            if (list.isSelectionEmpty()) {
                hLabel.setText("");
                bLabel.setText("");
            } else {
                ProxyMessage pm = list.getSelectedValue();
                hLabel.setText(toHTML(pm.getRequestHeader()));
                bLabel.setText(toHTML(pm.getRepsonseHeader()));
            }
        });
    }

    private String toHTML(String s) {
        return "<html>" + s.replace(System.lineSeparator(), "<br>") + "</html>";
    }

    public void parse(ProxyMessage pm) {
        model.addElement(pm);
    }

    private void terminate() {
        if (proxy != null) {
            proxy.stop();
        }
        this.dispose();
    }

    private void save() {
        if (!model.isEmpty()) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss");
            String path = formatter.format(new Date()) + ".txt";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
                String delim = System.lineSeparator() + "=====" + System.lineSeparator();
                for (Enumeration<ProxyMessage> elements = model.elements(); elements.hasMoreElements();) {
                    ProxyMessage pm = elements.nextElement();
                    bw.write(pm.toData());
                    bw.write(delim);
                }
                jLabel2.setText(path + "로 저장됨");
            } catch (IOException ex) {
                Logger.getLogger(ProxyDebugFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clear() {
        model.clear();
    }

    private void open() {
        clear();
        JFileChooser chooser = new JFileChooser(new File("."));
        int retval = chooser.showOpenDialog(this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            jLabel2.setText(chooser.getSelectedFile().getName() + " 불러옴");
            try (BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                boolean isResponse = false;
                // PM
                String reqMethod = "";
                String reqUrl = "";
                String reqVersion = "";
                Map<String, String> reqHeader = new HashMap<>();
                int resCode = 0;
                String resMsg = "";
                Map<String, String> resHeader = new HashMap<>();
                // iteration
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("-----")) {
                        isResponse = true;
                    } else if (line.startsWith("=====")) {
                        ProxyMessage pm = new ProxyMessage(
                                reqMethod, reqUrl, reqVersion,
                                reqHeader,
                                resCode, resMsg,
                                resHeader
                        );
                        parse(pm);
                        reqMethod = "";
                        reqUrl = "";
                        reqVersion = "";
                        reqHeader.clear();
                        resCode = 0;
                        resMsg = "";
                        resHeader.clear();
                        isResponse = false;
                    } else if (!isResponse && line.contains("HTTP/")) {
                        int break1 = line.indexOf(' ');
                        int break2 = line.indexOf(' ', break1 + 1);
                        reqMethod = line.substring(0, break1);
                        reqUrl = line.substring(break1 + 1, break2);
                        reqVersion = line.substring(break2 + 1, line.length());
                    } else if (isResponse && line.contains("HTTP/")) {
                        int break1 = line.indexOf(' ');
                        int break2 = line.indexOf(' ', break1 + 1);
                        resCode = Integer.valueOf(line.substring(break1 + 1, break2));
                        resMsg = line.substring(break2 + 1, line.length());
                    } else if (!isResponse) {
                        String key = line.substring(0, line.indexOf(": "));
                        String value = line.substring(line.indexOf(": ") + 2, line.length());
                        reqHeader.put(key, value);
                    } else if (isResponse) {
                        String key = line.substring(0, line.indexOf(": "));
                        String value = line.substring(line.indexOf(": ") + 2, line.length());
                        resHeader.put(key, value);
                    }
                }
            } catch (Exception ex) {
            }
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

        saveButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        hLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        bLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        saveButton.setText("저장");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jScrollPane2.setPreferredSize(new java.awt.Dimension(500, 500));
        jScrollPane2.setViewportView(list);

        jPanel2.add(jScrollPane2);

        jPanel1.setPreferredSize(new java.awt.Dimension(500, 500));
        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        hLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jScrollPane1.setViewportView(hLabel);

        jPanel1.add(jScrollPane1);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        bLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jScrollPane3.setViewportView(bLabel);

        jPanel1.add(jScrollPane3);

        jPanel2.add(jPanel1);

        jPanel3.setLayout(new java.awt.GridLayout(1, 0));
        jPanel3.add(jLabel1);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jPanel3.add(jLabel2);

        jButton1.setText("열기");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("프록시 시작");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("지우기");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(saveButton)
                            .addComponent(jButton1)
                            .addComponent(jButton3))
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        terminate();
    }//GEN-LAST:event_formWindowClosing

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       // initProxy();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        open();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        clear();
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bLabel;
    private javax.swing.JLabel hLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<ProxyMessage> list;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
