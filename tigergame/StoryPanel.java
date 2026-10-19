package com.tigergame;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class StoryPanel extends JPanel {
    private final BufferedImage pageImage;
    private final String buttonText;
    private final Runnable nextAction;

    public StoryPanel(String imagePath, String buttonText, Runnable nextAction) {
        this.pageImage = loadImage(imagePath);
        this.buttonText = buttonText;
        this.nextAction = nextAction;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 14, 14));

        JButton nextButton = new TempleButton(buttonText);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                nextAction.run();
            }
        });
        panel.add(nextButton, BorderLayout.EAST);
        return panel;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (pageImage != null) {
            drawContainImage(g, pageImage);
        }
        g.dispose();
    }

    private void drawContainImage(Graphics2D g, Image image) {
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        int bottomReserve = 76;
        int availableHeight = Math.max(1, getHeight() - bottomReserve);
        double scale = Math.min((double) getWidth() / imageWidth, (double) availableHeight / imageHeight);
        int width = (int) Math.floor(imageWidth * scale);
        int height = (int) Math.floor(imageHeight * scale);
        int x = (getWidth() - width) / 2;
        int y = (availableHeight - height) / 2;
        g.drawImage(image, x, y, width, height, this);
    }

    private BufferedImage loadImage(String imagePath) {
        try {
            return ImageIO.read(StoryPanel.class.getResource(imagePath));
        } catch (IOException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
