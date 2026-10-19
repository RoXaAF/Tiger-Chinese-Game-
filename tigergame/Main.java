package com.tigergame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        MusicPlayer.startLoop();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChineseTigerHuntingGame game = new ChineseTigerHuntingGame();
                game.setVisible(true);
            }
        });
        MusicPlayer.stopLoop();
    }
    
}
