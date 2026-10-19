package com.tigergame;

public class GameState {
    private final Piece[] board;
    private final Piece turn;
    private final boolean gameOver;

    public GameState(Piece[] board, Piece turn, boolean gameOver) {
        this.board = board;
        this.turn = turn;
        this.gameOver = gameOver;
    }

    public Piece[] getBoard() {
        return board;
    }

    public Piece getTurn() {
        return turn;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
