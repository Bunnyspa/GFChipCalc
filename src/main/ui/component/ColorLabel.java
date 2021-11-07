package main.ui.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JLabel;
import main.App;
import main.ui.resource.AppColor;

public class ColorLabel extends JLabel {

    private final App app;
    private int preset;

    public ColorLabel(App app) {
        this.app = app;
    }

    public void setColorPreset(int preset) {
        this.preset = preset;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int[] threeWidths = {0, getWidth() / 3, getWidth() * 2 / 3, getWidth()};
        int[] multWidths = new int[AppColor.Index.colors(preset).length + 1];
        multWidths[0] = 0;
        for (int i = 1; i < multWidths.length; i++) {
            multWidths[i] = getWidth() * i / AppColor.Index.colors(preset).length;
        }

        int[] heights = {0, getHeight() / 3, getHeight() * 2 / 3, getHeight()};

        // 1
        g2d.setColor(AppColor.Three.orange(preset));
        g2d.fill(new Rectangle(threeWidths[0], heights[0], threeWidths[1], heights[1]));
        g2d.setColor(AppColor.Three.green(preset));
        g2d.fill(new Rectangle(threeWidths[1], heights[0], threeWidths[2], heights[1]));
        g2d.setColor(AppColor.Three.blue(preset));
        g2d.fill(new Rectangle(threeWidths[2], heights[0], threeWidths[3], heights[1]));

        // 2
        LinearGradientPaint lgp = new LinearGradientPaint(
                new Point(0, 0),
                new Point(getWidth(), 0),
                new float[]{0f, 0.5f, 1f},
                new Color[]{AppColor.Three.orange(preset), AppColor.Three.green(preset), AppColor.Three.blue(preset)});
        g2d.setPaint(lgp);
        g2d.fill(new Rectangle(0, heights[1], getWidth(), heights[2]));

        // 3
        for (int i = 0; i < multWidths.length - 1; i++) {
            g2d.setColor(AppColor.Index.colors(preset)[i]);
            g2d.fill(new Rectangle(multWidths[i], heights[2], multWidths[i + 1], heights[3]));
        }

        g2d.dispose();
        super.paintComponent(g);
    }
}
