package com.tigergame;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class LandingPanel extends JPanel {
    private static final String TITLE_PATH = "/images/tiger-hunter-title.png";

    private final BufferedImage background;
    private final Runnable startAction;

    public LandingPanel(BufferedImage background, Runnable startAction) {
        this.background = background;
        this.startAction = startAction;
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(780, 760));
        add(buildContent(), new GridBagConstraints());
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel(loadTitleIcon());
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setText(title.getIcon() == null ? "TIGER HUNTER" : null);
        title.setForeground(new Color(238, 226, 196));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 44f));

        JButton startButton = new TempleButton("Start Game");
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 20f));
        startButton.setBorder(BorderFactory.createEmptyBorder(13, 34, 13, 34));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                startAction.run();
            }
        });

        JPanel textPanel = new JPanel(new BorderLayout(0, 8));
        textPanel.setOpaque(false);
        textPanel.add(title, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (background != null) {
            drawCoverImage(g, background);
        } else {
            g.setColor(new Color(36, 31, 25));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(new Color(0, 0, 0, 95));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
    }

    private void drawCoverImage(Graphics2D g, Image image) {
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        double scale = Math.max((double) getWidth() / imageWidth, (double) getHeight() / imageHeight);
        int width = (int) Math.ceil(imageWidth * scale);
        int height = (int) Math.ceil(imageHeight * scale);
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g.drawImage(image, x, y, width, height, this);
    }

    private ImageIcon loadTitleIcon() {
        java.net.URL titleUrl = LandingPanel.class.getResource(TITLE_PATH);
        if (titleUrl == null) {
            return null;
        }

        ImageIcon source = new ImageIcon(titleUrl);
        int targetWidth = 520;
        int targetHeight = Math.max(1, targetWidth * source.getIconHeight() / source.getIconWidth());
        Image scaled = source.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
