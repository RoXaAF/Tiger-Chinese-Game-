package com.tigergame;

import javax.sound.sampled.*;
import java.io.File;

public class MusicPlayer {

    private static Clip clip;

    public static void startLoop() {
        try {
            File file = new File(
                "C:\\Users\\TT\\Documents\\NetBeansProjects\\Final Project\\out\\music\\soundtrack.wav"
            );

            if (!file.exists()) {
                System.out.println("Music file not found: " + file.getAbsolutePath());
                return;
            }

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(file);

            clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

            System.out.println("Music started!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopLoop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            System.out.println("Music stopped!");
        }
    }
}