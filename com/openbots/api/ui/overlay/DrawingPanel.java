package com.openbots.api.ui.overlay;

import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.script.framework.AbstractBot;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class DrawingPanel extends JPanel {
    private Collection<PaintListener> paintListeners = new ArrayList<>();
    private AbstractBot bot = Environment.getBot();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.BLACK);
        try {
            bot.getPlatform().invokeAndWait(() -> paintListeners.forEach(r -> r.onPaint(g2)));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addPaintListener(PaintListener listener) {
        paintListeners.add(listener);
        repaint();
    }

    public void removePaintListener(PaintListener listener) {
        paintListeners.remove(listener);
        repaint();
    }
}
