package com.openbots.api.ui.overlay;

import com.runemate.game.api.hybrid.local.Screen;
import com.runemate.game.api.script.framework.core.LoopingThread;

import javax.swing.*;
import java.awt.*;

public class GameOverlay extends JFrame {
    private DrawingPanel drawingPanel;
    public GameOverlay() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(1, 1, 1));
        setOpacity(0.6f);
        setContentPane(drawingPanel = new DrawingPanel());
        drawingPanel.setBackground(getBackground());
        new LoopingThread(() -> {
            final Rectangle bounds = Screen.getBounds();
            final Point location = Screen.getLocation();
            setLocation(location);
            setSize(bounds.width, bounds.height);
            drawingPanel.repaint();
        }, 500).start();
    }

    public void addPaintListener(PaintListener paintListener) {
        drawingPanel.addPaintListener(paintListener);
    }
}
