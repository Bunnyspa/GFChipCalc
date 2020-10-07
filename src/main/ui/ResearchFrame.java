package main.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import main.App;
import main.http.ResearchConnection;
import main.puzzle.Board;
import main.puzzle.BoardTemplate;
import main.puzzle.Shape;
import main.puzzle.assembly.Assembler;
import main.ui.resource.GFLResources;
import main.ui.resource.GFLTexts;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class ResearchFrame extends javax.swing.JFrame {

    private final App app;
    private final Timer progTimer;
    private final Runnable aRunnable;
    private volatile boolean running = false;
    private final List<Thread> threads = new ArrayList<>();

    public static ResearchFrame getInstance(App app) {
        return new ResearchFrame(app);
    }

    private ResearchFrame(App app) {
        this.app = app;

        progTimer = new Timer(1000, (e) -> updateProgress());

        aRunnable = () -> {
            while (running) {
                String task = ResearchConnection.getTask();
//                System.out.println(task);
                if (task == null || task.isEmpty()) {
                    currentLabel.setText(app.getText(GFLTexts.RESEARCH_EMPTY));
                    wait_(10000);
                } else {
                    String[] split = task.split(";");
                    String boardName = Board.getTrueName(split[0]);
                    int boardStar = Integer.valueOf(split[1]);
                    if (split.length == 2) {
                        currentLabel.setText(app.getText(GFLTexts.RESEARCH_WAITING, boardName, String.valueOf(boardStar)));
                        wait_(5000);
                    } else {
                        String shapeStrs = split[2];

                        currentLabel.setText(app.getText(GFLTexts.RESEARCH_WORKING, boardName, String.valueOf(boardStar)));

                        // Run task
                        List<Shape> shapes = new ArrayList<>();
                        for (String s : shapeStrs.split(",")) {
                            shapes.add(Shape.byId(Integer.valueOf(s)));
                        }
                        BoardTemplate result = Assembler.generateBT(boardName, boardStar, shapes, () -> running);
                        if (running) {
                            if (result.isEmpty()) {
                                ResearchConnection.sendResult(shapes.stream().map(s -> String.valueOf(s.id)).collect(Collectors.joining(",")) + ";-");
                            } else {
                                ResearchConnection.sendResult(result.toData());
                            }
                        }
                    }
                }
                wait_(10);
            }
        };
        initComponents();
        init();
    }

    private synchronized void wait_(int mili) {
        try {
            wait(mili);
        } catch (InterruptedException ex) {
        }
    }

    private void init() {
        setIconImage(GFLResources.FAVICON);
        setTitle(app.getText(GFLTexts.RESEARCH_TITLE));
        wtfLabel.setText(app.getText(GFLTexts.RESEARCH_WTF));
        threadLabel.setText(app.getText(GFLTexts.RESEARCH_THREAD));
        closeButton.setText(app.getText(GFLTexts.ACTION_CLOSE));
        int max = Runtime.getRuntime().availableProcessors();
        aSpinner.setModel(new SpinnerNumberModel(1, 1, max, 1));
        stop();
        setPreferredSize(new Dimension(app.mf.getPreferredDialogSize().width, getPreferredSize().height));
        pack();
    }

    private void updateProgress() {
        String progress = ResearchConnection.getProgress();
        if (progress == null || progress.isEmpty()) {
            progressLabel.setText("");
        } else {
            String[] split = progress.split(";");
            int prog = Integer.valueOf(split[0]);
            int total = Integer.valueOf(split[1]);
            progressLabel.setText(prog + " / " + total + " (" + Fn.fPercStr((double) prog / total) + ")");
            aProgressBar.setMaximum(total);
            aProgressBar.setValue(prog);
        }
    }

    private void start() {
        running = true;
        aSpinner.setEnabled(false);
        progTimer.start();

        for (int i = 0; i < (int) aSpinner.getValue(); i++) {
            Thread thread = new Thread(aRunnable);
            thread.start();
            threads.add(thread);
        }

        startStopButton.setText(app.getText(GFLTexts.RESEARCH_STOP));
    }

    private void stop() {
        running = false;
        progTimer.stop();

        threads.forEach((t) -> {
            if (t != null) {
                try {
                    t.join();
                } catch (InterruptedException ex) {
                }
            }
        });
        threads.clear();

        aSpinner.setEnabled(true);
        currentLabel.setText(app.getText(GFLTexts.RESEARCH_READY));
        progressLabel.setText(" ");
        aProgressBar.setMaximum(1);
        aProgressBar.setValue(0);
        startStopButton.setText(app.getText(GFLTexts.RESEARCH_START));
    }

    private void terminate() {
        stop();
        app.mf.setVisible(true);
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

        startStopButton = new javax.swing.JButton();
        progressLabel = new javax.swing.JLabel();
        currentLabel = new javax.swing.JLabel();
        aSpinner = new javax.swing.JSpinner();
        threadLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        aProgressBar = new javax.swing.JProgressBar();
        wtfLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        startStopButton.setText("start stop");
        startStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopButtonActionPerformed(evt);
            }
        });

        progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        progressLabel.setText("progress");

        currentLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        currentLabel.setText("current");

        threadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        threadLabel.setText("thread");

        closeButton.setText("닫기");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        wtfLabel.setText("wtf is this piece of shit");
        wtfLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        wtfLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(startStopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(aSpinner)
                            .addComponent(threadLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(aProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(currentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addComponent(wtfLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wtfLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(threadLabel)
                    .addComponent(currentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(aSpinner)
                    .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(closeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(aProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(startStopButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopButtonActionPerformed
        if (running) {
            stop();
        } else {
            start();
        }
    }//GEN-LAST:event_startStopButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        terminate();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        terminate();
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar aProgressBar;
    private javax.swing.JSpinner aSpinner;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel currentLabel;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton startStopButton;
    private javax.swing.JLabel threadLabel;
    private javax.swing.JLabel wtfLabel;
    // End of variables declaration//GEN-END:variables
}
