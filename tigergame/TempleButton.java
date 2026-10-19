package com.tigergame;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

public class TempleButton extends JButton {
    private static final Color TOP = new Color(206, 142, 61);
    private static final Color HOVER = new Color(225, 164, 78);
    private static final Color PRESSED = new Color(149, 86, 43);
    private static final Color BORDER = new Color(83, 53, 31);

    public TempleButton(String text) {
        super(text);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(new Color(37, 24, 16));
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = getModel().isPressed() ? PRESSED : getModel().isRollover() ? HOVER : TOP;
        if (!isEnabled()) {
            fill = new Color(128, 116, 96);
        }

        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
        g.setColor(new Color(0, 0, 0, 55));
        g.fill(new RoundRectangle2D.Double(3, 4, getWidth() - 4, getHeight() - 3, 24, 24));
        g.setColor(fill);
        g.fill(shape);
        g.setStroke(new BasicStroke(2f));
        g.setColor(BORDER);
        g.draw(shape);
        g.dispose();

        super.paintComponent(graphics);
    }
}
