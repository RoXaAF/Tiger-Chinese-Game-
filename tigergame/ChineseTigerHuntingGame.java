package com.tigergame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class ChineseTigerHuntingGame extends JFrame {
    private static final String APP_TITLE = "Final Project";
    private static final String LANDING_SCREEN = "landing";
    private static final String STORY_SCREEN_ONE = "story1";
    private static final String STORY_SCREEN_TWO = "story2";
    private static final String GAME_SCREEN = "game";
    private static final String TITLE_PATH = "/images/tiger-hunter-title.png";
    private static final String STORY_ONE_PATH = "/images/story-1.png";
    private static final String STORY_TWO_PATH = "/images/story-2.png";

    private final GameModel model = new GameModel();
    private final BoardPanel boardPanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final JLabel statusLabel = new JLabel();
    private final JLabel timerLabel = new JLabel("00:00");
    private final JButton undoButton = new TempleButton("Undo");
    private final Timer clockTimer;
    private long startedAtMillis;
    private String playerOneSide = "Dogs";
    private String playerTwoSide = "Tiger";

    public ChineseTigerHuntingGame() {
        super(APP_TITLE);
        MusicPlayer.startLoop();
        BufferedImage background = ImageAssets.loadBackground();
        Dimension initialSize = initialWindowSize(background);
        boardPanel = new BoardPanel(model, new Runnable() {
            @Override
            public void run() {
                updateStatus();
            }
        }, background);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 780));
        setResizable(true);
        setLocationByPlatform(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                MusicPlayer.stopLoop();
            }
        });

        screens.add(new LandingPanel(background, new Runnable() {
            @Override
            public void run() {
                startNewGame();
            }
        }), LANDING_SCREEN);
        screens.add(new StoryPanel(STORY_ONE_PATH, "Next", new Runnable() {
            @Override
            public void run() {
                cardLayout.show(screens, STORY_SCREEN_TWO);
            }
        }), STORY_SCREEN_ONE);
        screens.add(new StoryPanel(STORY_TWO_PATH, "Start", new Runnable() {
            @Override
            public void run() {
                beginGameplay();
            }
        }), STORY_SCREEN_TWO);
        screens.add(buildGameScreen(), GAME_SCREEN);
        screens.setPreferredSize(initialSize);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(28, 22, 16));
        root.add(screens, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);

        boardPanel.setMoveListener(new MoveListener() {
            @Override
            public void clicked(int node) {
                handleBoardClick(node);
            }
        });
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                undo();
            }
        });

        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateTimer();
            }
        });
        updateStatus();
        updateTimer();
        pack();
        setLocationRelativeTo(null);
        cardLayout.show(screens, LANDING_SCREEN);
    }

    private Dimension initialWindowSize(BufferedImage background) {
        if (background == null) {
            return new Dimension(620, 920);
        }

        int height = 960;
        int width = Math.max(520, background.getWidth() * height / background.getHeight());
        return new Dimension(width, height);
    }

    private JPanel buildGameScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildTopBar(), BorderLayout.NORTH);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(buildBottomBar(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        panel.setBackground(new Color(54, 42, 29));

        JLabel title = new JLabel(loadTitleIcon(190));
        title.setText(title.getIcon() == null ? "Tiger Hunting Board" : null);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(230, 202, 147));

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton menuButton = new TempleButton("Menu");
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                returnToMenu();
            }
        });
        JButton newGameButton = new TempleButton("New Game");
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                startNewGame();
            }
        });
        actions.add(menuButton);
        actions.add(newGameButton);
        actions.add(undoButton);
        actions.add(Box.createHorizontalStrut(12));
        JLabel timeText = new JLabel("Time:");
        timeText.setForeground(new Color(230, 202, 147));
        actions.add(timeText);
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 16f));
        timerLabel.setForeground(new Color(250, 237, 198));
        actions.add(timerLabel);

        panel.add(title, BorderLayout.WEST);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private ImageIcon loadTitleIcon(int targetWidth) {
        java.net.URL titleUrl = ChineseTigerHuntingGame.class.getResource(TITLE_PATH);
        if (titleUrl == null) {
            return null;
        }

        ImageIcon source = new ImageIcon(titleUrl);
        int targetHeight = Math.max(1, targetWidth * source.getIconHeight() / source.getIconWidth());
        Image scaled = source.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 16, 14, 16));
        panel.setBackground(new Color(54, 42, 29));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(new Color(235, 214, 170));
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    private void startNewGame() {
        showSideSelectionDialog();
        model.reset();
        boardPanel.clearSelection();
        updateStatus();
        boardPanel.repaint();
        cardLayout.show(screens, STORY_SCREEN_ONE);
    }

    private void beginGameplay() {
        startedAtMillis = System.currentTimeMillis();
        clockTimer.restart();
        updateStatus();
        updateTimer();
        boardPanel.repaint();
        cardLayout.show(screens, GAME_SCREEN);
    }

    private void returnToMenu() {
        clockTimer.stop();
        boardPanel.clearSelection();
        cardLayout.show(screens, LANDING_SCREEN);
    }

    private void showSideSelectionDialog() {
        final JDialog dialog = new JDialog(this, "Select Sides", true);
        JPanel panel = new JPanel(new BorderLayout(16, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        panel.setBackground(new Color(48, 37, 25));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Choose Your Side");
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setForeground(new Color(244, 220, 166));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitle = new JLabel("Player 1 enters the temple as...");
        subtitle.setHorizontalAlignment(JLabel.CENTER);
        subtitle.setForeground(new Color(184, 160, 116));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.BOLD, 13f));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        JPanel choices = new JPanel(new GridLayout(1, 2, 14, 0));
        choices.setOpaque(false);
        JButton dogsButton = new TempleButton("<html><center>Lead the Dogs<br><small>Surround the tiger</small></center></html>");
        JButton tigerButton = new TempleButton("<html><center>Command the Tiger<br><small>Break the hunt</small></center></html>");
        dogsButton.setPreferredSize(new Dimension(190, 76));
        tigerButton.setPreferredSize(new Dimension(190, 76));
        dogsButton.setFont(dogsButton.getFont().deriveFont(Font.BOLD, 16f));
        tigerButton.setFont(tigerButton.getFont().deriveFont(Font.BOLD, 16f));
        dogsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                playerOneSide = "Dogs";
                playerTwoSide = "Tiger";
                dialog.dispose();
            }
        });
        tigerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                playerOneSide = "Tiger";
                playerTwoSide = "Dogs";
                dialog.dispose();
            }
        });
        choices.add(dogsButton);
        choices.add(tigerButton);
        panel.add(choices, BorderLayout.CENTER);

        JLabel hint = new JLabel("Dogs move first. Choose carefully.");
        hint.setHorizontalAlignment(JLabel.CENTER);
        hint.setForeground(new Color(211, 186, 138));
        hint.setFont(hint.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(hint, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void handleBoardClick(int node) {
        if (model.isGameOver() || boardPanel.isAnimating()) {
            return;
        }

        Piece piece = model.getPiece(node);
        if (boardPanel.getSelectedNode() < 0) {
            if (piece == model.getTurn()) {
                boardPanel.setSelectedNode(node);
            }
            return;
        }

        if (piece == model.getTurn()) {
            boardPanel.setSelectedNode(node);
            return;
        }

        int from = boardPanel.getSelectedNode();
        if (!model.isLegalStep(from, node)) {
            Toolkit.getDefaultToolkit().beep();
            boardPanel.clearSelection();
            return;
        }

        GameState before = model.snapshot();
        Piece moving = model.getTurn();
        final MoveResult result = model.applyMove(from, node);
        if (!result.isMoved()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        model.pushUndo(before);
        boardPanel.animateMove(from, node, moving, result.getCapturedNodes(), new Runnable() {
            @Override
            public void run() {
                SoundPlayer.playTone(result.getCapturedNodes().isEmpty() ? 660 : 880,
                        result.getCapturedNodes().isEmpty() ? 80 : 150);
                checkVictory();
                updateStatus();
                boardPanel.clearSelection();
                boardPanel.repaint();
            }
        });
        updateStatus();
    }

    private void undo() {
        if (!model.canUndo() || boardPanel.isAnimating()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        model.undo();
        clockTimer.start();
        boardPanel.clearSelection();
        updateStatus();
        boardPanel.repaint();
        SoundPlayer.playTone(440, 90);
    }

   private void checkVictory() {
    if (model.countDogs() <= 2) {
        finishGame("Tiger Victory");
        return;
    }

    if (model.dogsHaveWon()) {
        finishGame("Dog Victory");
    }
}

    private void finishGame(String message) {
        model.setGameOver(true);
        clockTimer.stop();
        updateStatus();
        SoundPlayer.playTone(message.startsWith("Tiger") ? 990 : 520, 220);
        showTempleMessage(message);
    }

    private void updateStatus() {
        String dogPlayer = "Dogs".equals(playerOneSide) ? "Player 1" : "Player 2";
        String tigerPlayer = "Tiger".equals(playerOneSide) ? "Player 1" : "Player 2";
        String turnPlayer = model.getTurn() == Piece.DOG ? dogPlayer : tigerPlayer;
        String side = model.getTurn() == Piece.DOG ? "Dogs" : "Tiger";
        String suffix = model.isGameOver() ? "Game over." : turnPlayer + " moves " + side + ". Dogs move first.";
        statusLabel.setText("Dogs: " + dogPlayer + "    Tiger: " + tigerPlayer
                + "    Dogs left: " + model.countDogs() + "    " + suffix);
        undoButton.setEnabled(model.canUndo() && !boardPanel.isAnimating());
    }

    private void updateTimer() {
        long elapsedSeconds = Math.max(0, (System.currentTimeMillis() - startedAtMillis) / 1000);
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void showTempleMessage(String message) {
        final JDialog dialog = new JDialog(this, "Game Over", true);
        JPanel panel = new JPanel(new BorderLayout(12, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        panel.setBackground(new Color(48, 37, 25));

        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setForeground(new Color(235, 214, 170));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(label, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton okButton = new TempleButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.dispose();
            }
        });
        actions.add(okButton);
        panel.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
