package com.tigergame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageAssets {
    public static final String BACKGROUND_PATH = "/images/tiger-temple.png";

    private ImageAssets() {
    }

    public static BufferedImage loadBackground() {
        try {
            return ImageIO.read(ImageAssets.class.getResource(BACKGROUND_PATH));
        } catch (IOException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
