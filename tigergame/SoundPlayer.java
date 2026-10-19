package com.tigergame;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;

public class SoundPlayer {
    private SoundPlayer() {
    }

    public static void playTone(final int hz, final int millis) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    float sampleRate = 44100f;
                    AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                    SourceDataLine line = AudioSystem.getSourceDataLine(format);
                    line.open(format);
                    line.start();
                    byte[] buffer = new byte[(int) (millis * sampleRate / 1000)];
                    for (int i = 0; i < buffer.length; i++) {
                        double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
                        buffer[i] = (byte) (Math.sin(angle) * 50);
                    }
                    line.write(buffer, 0, buffer.length);
                    line.drain();
                    line.close();
                } catch (Exception ignored) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }, "sound-effect").start();
    }
}
