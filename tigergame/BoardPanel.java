package com.tigergame;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BoardPanel extends JPanel {
    private static final int NODE_RADIUS = 18;
    private static final String TIGER_ICON_PATH = "/images/tiger-piece.png";
    private static final String DOG_ICON_PATH = "/images/dog-piece.png";
    private static final String BOARD_PAPER_PATH = "/images/board-paper.png";

    private final GameModel model;
    private final Runnable statusUpdater;
    private final BufferedImage background;
    private final BufferedImage tigerIcon = loadIcon(TIGER_ICON_PATH);
    private final BufferedImage dogIcon = loadIcon(DOG_ICON_PATH);
    private final BufferedImage boardPaper = loadIcon(BOARD_PAPER_PATH);
    private MoveListener moveListener;
    private int selectedNode = -1;
    private Integer animationFrom;
    private Integer animationTo;
    private Piece animationPiece;
    private double animationProgress;
    private Timer animationTimer;
    private List<Integer> fadingNodes = Collections.emptyList();

    public BoardPanel(GameModel model, Runnable statusUpdater, BufferedImage background) {
        this.model = model;
        this.statusUpdater = statusUpdater;
        this.background = background;
        setPreferredSize(new Dimension(760, 650));
        setBackground(new Color(250, 247, 238));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int node = findNodeAt(event.getPoint());
                if (node >= 0 && moveListener != null) {
                    moveListener.clicked(node);
                }
            }
        });
    }

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public int getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(int selectedNode) {
        this.selectedNode = selectedNode;
        repaint();
    }

    public void clearSelection() {
        selectedNode = -1;
        repaint();
    }

    public boolean isAnimating() {
        return animationTimer != null && animationTimer.isRunning();
    }

    public void animateMove(int from, int to, Piece piece, List<Integer> capturedNodes, final Runnable after) {
        animationFrom = Integer.valueOf(from);
        animationTo = Integer.valueOf(to);
        animationPiece = piece;
        animationProgress = 0.0;
        fadingNodes = new ArrayList<Integer>(capturedNodes);
        statusUpdater.run();

        animationTimer = new Timer(16, null);
        animationTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                animationProgress += 0.09;
                if (animationProgress >= 1.0) {
                    animationTimer.stop();
                    animationFrom = null;
                    animationTo = null;
                    animationPiece = null;
                    fadingNodes = Collections.emptyList();
                    after.run();
                }
                repaint();
            }
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        drawBackground(g);
        drawBoard(g);
        drawPieces(g);
        g.dispose();
    }

    private void drawBackground(Graphics2D g) {
        if (background != null) {
            drawCoverImage(g, background);
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawBoard(Graphics2D g) {
        int step = boardStep();
        int left = (getWidth() - step * 4) / 2;
        int top = (getHeight() - step * 6) / 2;
        int boardWidth = step * 4;
        int boardHeight = step * 6;

        Shape boardPaper = new RoundRectangle2D.Double(left - 48, top - 44, boardWidth + 96, boardHeight + 88, 36, 36);
        drawBoardPaper(g, boardPaper);
        g.setColor(new Color(139, 90, 43, 120));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(boardPaper);

        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int node = 0; node < GameModel.NODE_COUNT; node++) {
            Set<Integer> nextNodes = model.getNeighbors(node);
            for (Integer nextNode : nextNodes) {
                int next = nextNode.intValue();
                if (node < next) {
                    Point a = pointFor(node, left, top, step);
                    Point b = pointFor(next, left, top, step);
                    g.setColor(Color.BLACK);
                    g.drawLine(a.x, a.y, b.x, b.y);
                }
            }
        }

        for (int node = 0; node < GameModel.NODE_COUNT; node++) {
            Point p = pointFor(node, left, top, step);
            boolean center = node == GameModel.TIGER_START;
            int radius = center ? 27 : 16;
            g.setColor(new Color(255, 247, 221));
            g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
            g.setStroke(new BasicStroke(center ? 5f : 4f));
            g.setColor(Color.BLACK);
            g.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
        }

        Point labelPoint = pointFor(GameModel.TRAP_APEX, left, top, step);
        g.setFont(getFont().deriveFont(Font.BOLD, 14f));
        FontMetrics labelMetrics = g.getFontMetrics();
        String label = "TRAP";
        g.setColor(Color.BLACK);
        g.drawString(label, labelPoint.x - labelMetrics.stringWidth(label) / 2, labelPoint.y - 24);
    }

    private void drawBoardPaper(Graphics2D g, Shape paperShape) {
        Graphics2D paper = (Graphics2D) g.create();
        paper.setClip(paperShape);
        java.awt.Rectangle bounds = paperShape.getBounds();
        paper.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
        if (boardPaper != null) {
            paper.drawImage(boardPaper, bounds.x, bounds.y, bounds.width, bounds.height, this);
        } else {
            paper.setColor(new Color(235, 205, 150));
            paper.fill(paperShape);
        }
        paper.dispose();
    }

    private void drawPieces(Graphics2D g) {
        int step = boardStep();
        int left = (getWidth() - step * 4) / 2;
        int top = (getHeight() - step * 6) / 2;

        for (int node = 0; node < GameModel.NODE_COUNT; node++) {
            Piece piece = model.getPiece(node);
            if (piece == Piece.EMPTY) {
                continue;
            }
            if (animationTo != null && node == animationTo.intValue()) {
                continue;
            }
            drawPiece(g, piece, pointFor(node, left, top, step), node == selectedNode);
        }

        if (!fadingNodes.isEmpty()) {
            float alpha = (float) Math.max(0.0, 1.0 - animationProgress);
            Graphics2D faded = (Graphics2D) g.create();
            faded.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            for (int i = 0; i < fadingNodes.size(); i++) {
                int node = fadingNodes.get(i).intValue();
                drawPiece(faded, Piece.DOG, pointFor(node, left, top, step), false);
            }
            faded.dispose();
        }

        if (animationFrom != null && animationTo != null && animationPiece != null) {
            Point a = pointFor(animationFrom.intValue(), left, top, step);
            Point b = pointFor(animationTo.intValue(), left, top, step);
            int x = (int) Math.round(a.x + (b.x - a.x) * animationProgress);
            int y = (int) Math.round(a.y + (b.y - a.y) * animationProgress);
            drawPiece(g, animationPiece, new Point(x, y), false);
        }
    }

    private void drawPiece(Graphics2D g, Piece piece, Point center, boolean selected) {
        int radius = piece == Piece.TIGER ? 30 : 24;
        if (selected) {
            g.setColor(new Color(255, 206, 84));
            g.fillOval(center.x - radius - 7, center.y - radius - 7, (radius + 7) * 2, (radius + 7) * 2);
        }

        Color top = piece == Piece.TIGER ? new Color(211, 82, 45) : new Color(37, 105, 142);
        Color bottom = piece == Piece.TIGER ? new Color(132, 42, 28) : new Color(25, 67, 104);
        g.setPaint(new GradientPaint(center.x, center.y - radius, top, center.x, center.y + radius, bottom));
        g.fillOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(255, 255, 255, 170));
        g.drawOval(center.x - radius + 2, center.y - radius + 2, radius * 2 - 4, radius * 2 - 4);

        if (piece == Piece.TIGER) {
            drawPieceIcon(g, tigerIcon, center, radius, true);
        } else {
            drawPieceIcon(g, dogIcon, center, radius, false);
        }
    }

    private void drawPieceIcon(Graphics2D g, BufferedImage icon, Point center, int radius, boolean tiger) {
        if (icon == null) {
            if (tiger) {
                drawTigerIcon(g, center, radius);
            } else {
                drawDogIcon(g, center, radius);
            }
            return;
        }

        int size = tiger ? radius * 2 - 9 : radius * 2 - 8;
        int x = center.x - size / 2;
        int y = center.y - size / 2;
        g.drawImage(icon, x, y, size, size, this);
    }

    private BufferedImage loadIcon(String path) {
        try {
            return ImageIO.read(BoardPanel.class.getResource(path));
        } catch (IOException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void drawDogIcon(Graphics2D g, Point center, int radius) {
        int headR = radius / 2;
        g.setColor(Color.BLACK);
        g.fillOval(center.x - headR, center.y - headR + 1, headR * 2, headR * 2);
        g.fillPolygon(
                new int[]{center.x - headR + 2, center.x - headR - 8, center.x - 4},
                new int[]{center.y - 6, center.y - headR - 6, center.y - headR + 1},
                3
        );
        g.fillPolygon(
                new int[]{center.x + headR - 2, center.x + headR + 8, center.x + 4},
                new int[]{center.y - 6, center.y - headR - 6, center.y - headR + 1},
                3
        );
        g.fillRoundRect(center.x - 7, center.y + 4, 14, 11, 8, 8);
        g.setColor(new Color(245, 245, 245));
        g.fillOval(center.x - 7, center.y - 3, 4, 4);
        g.fillOval(center.x + 3, center.y - 3, 4, 4);
        g.setColor(Color.BLACK);
        g.fillOval(center.x - 6, center.y - 2, 2, 2);
        g.fillOval(center.x + 4, center.y - 2, 2, 2);
    }

    private void drawTigerIcon(Graphics2D g, Point center, int radius) {
        Color gold = new Color(245, 188, 54);
        Color dark = new Color(82, 39, 16);
        int faceR = radius / 2 + 4;

        g.setColor(gold);
        g.fillOval(center.x - faceR, center.y - faceR + 1, faceR * 2, faceR * 2);
        g.fillOval(center.x - faceR - 5, center.y - faceR + 2, 12, 12);
        g.fillOval(center.x + faceR - 7, center.y - faceR + 2, 12, 12);

        g.setColor(dark);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(center.x - 11, center.y - 10, center.x - 4, center.y - 5);
        g.drawLine(center.x + 11, center.y - 10, center.x + 4, center.y - 5);
        g.drawLine(center.x - 13, center.y + 1, center.x - 4, center.y + 1);
        g.drawLine(center.x + 13, center.y + 1, center.x + 4, center.y + 1);
        g.drawLine(center.x, center.y - 15, center.x, center.y - 7);

        g.setColor(Color.BLACK);
        g.fillOval(center.x - 8, center.y - 3, 4, 4);
        g.fillOval(center.x + 4, center.y - 3, 4, 4);
        g.fillOval(center.x - 3, center.y + 5, 6, 4);
        g.setColor(new Color(255, 237, 156));
        g.fillOval(center.x - 7, center.y + 7, 14, 8);
    }

    private Point pointFor(int node, int left, int top, int step) {
        return new Point(left + GameModel.NODE_X[node] * step, top + (6 - GameModel.NODE_Y[node]) * step);
    }

    private int boardStep() {
        int pad = 86;
        return Math.max(48, Math.min((getWidth() - pad * 2) / 4, (getHeight() - pad * 2) / 6));
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

    private int findNodeAt(Point point) {
        int step = boardStep();
        int left = (getWidth() - step * 4) / 2;
        int top = (getHeight() - step * 6) / 2;
        for (int node = 0; node < GameModel.NODE_COUNT; node++) {
            Point nodePoint = pointFor(node, left, top, step);
            if (point.distance(nodePoint) <= NODE_RADIUS + 16) {
                return node;
            }
        }
        return -1;
    }
}
