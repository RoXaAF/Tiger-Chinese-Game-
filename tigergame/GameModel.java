package com.tigergame;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameModel {
    public static final int NODE_COUNT = 29;
    public static final int TIGER_START = 12;
    public static final int TRAP_APEX = 28;

    public static final int[] NODE_X = {
            0, 1, 2, 3, 4,
            0, 1, 2, 3, 4,
            0, 1, 2, 3, 4,
            0, 1, 2, 3, 4,
            0, 1, 2, 3, 4,
            1, 2, 3, 2
    };

    public static final int[] NODE_Y = {
            0, 0, 0, 0, 0,
            1, 1, 1, 1, 1,
            2, 2, 2, 2, 2,
            3, 3, 3, 3, 3,
            4, 4, 4, 4, 4,
            5, 5, 5, 6
    };

    private static final int[][] ADJACENCY = {
            {1, 5, 6},
            {0, 2, 6},
            {1, 3, 6, 7, 8},
            {2, 4, 8},
            {3, 9, 8},
            {0, 6, 10},
            {0, 1, 2, 5, 7, 10, 11, 12},
            {2, 6, 8, 12},
            {2, 3, 4, 7, 9, 12, 13, 14},
            {4, 8, 14},
            {5, 6, 11, 15, 16},
            {6, 10, 12, 16},
            {6, 7, 8, 11, 13, 16, 17, 18},
            {8, 12, 14, 18},
            {8, 9, 13, 18, 19},
            {10, 16, 20},
            {10, 11, 12, 15, 17, 20, 21, 22},
            {12, 16, 18, 22},
            {12, 13, 14, 17, 19, 22, 23, 24},
            {14, 18, 24},
            {15, 21},
            {16, 20, 22},
            {16, 17, 18, 21, 23, 25, 26, 27},
            {18, 22, 24},
            {19, 23},
            {22, 26, 28},
            {22, 25, 27, 28},
            {22, 26, 28},
            {25, 26, 27}
    };

    private final Piece[] board = new Piece[NODE_COUNT];
    private final Map<Integer, Set<Integer>> neighbors = new HashMap<Integer, Set<Integer>>();
    private final Deque<GameState> undoStack = new ArrayDeque<GameState>();
    private Piece turn = Piece.DOG;
    private boolean gameOver;

    public GameModel() {
        for (int node = 0; node < NODE_COUNT; node++) {
            Set<Integer> linkedNodes = new HashSet<Integer>();
            for (int i = 0; i < ADJACENCY[node].length; i++) {
                linkedNodes.add(Integer.valueOf(ADJACENCY[node][i]));
            }
            neighbors.put(Integer.valueOf(node), linkedNodes);
        }
    }

    public void reset() {
        Arrays.fill(board, Piece.EMPTY);
        for (int node = 0; node < NODE_COUNT; node++) {
            if (node != TIGER_START && isStartingDogNode(node)) {
                board[node] = Piece.DOG;
            }
        }
        board[TIGER_START] = Piece.TIGER;
        undoStack.clear();
        turn = Piece.DOG;
        gameOver = false;
    }

    private boolean isStartingDogNode(int node) {
        int x = NODE_X[node];
        int y = NODE_Y[node];
        return node <= 24 && (x == 0 || x == 4 || y == 0 || y == 4);
    }

    public boolean isLegalStep(int from, int to) {
        if (board[from] != turn || board[to] != Piece.EMPTY) {
            return false;
        }
        return getNeighbors(from).contains(Integer.valueOf(to));
    }

    public MoveResult applyMove(int from, int to) {
        if (!isLegalStep(from, to)) {
            return new MoveResult(false, Collections.emptyList());
        }

        Piece moving = board[from];
        board[from] = Piece.EMPTY;
        board[to] = moving;

        List<Integer> captured = new ArrayList<Integer>();
        if (moving == Piece.TIGER) {
            captured = tigerCapturesFrom(to);
            for (int i = 0; i < captured.size(); i++) {
                board[captured.get(i).intValue()] = Piece.EMPTY;
            }
        }

        // Check whether dogs have won
        if (dogsHaveWon()) {
            gameOver = true;
        }

        turn = moving == Piece.DOG ? Piece.TIGER : Piece.DOG;

        return new MoveResult(true, captured);
    }
    private boolean dogsCanTrapTigerNextMove() {
        int tiger = tigerNode();

        if (tiger < 0) {
            return false;
        }

        List<Integer> tigerMoves = new ArrayList<Integer>();

        for (Integer neighbor : getNeighbors(tiger)) {
            if (board[neighbor.intValue()] == Piece.EMPTY) {
                tigerMoves.add(neighbor);
            }
        }

        // Only interested in positions where tiger has exactly one move
        if (tigerMoves.size() != 1) {
            return false;
        }

        int tigerDestination = tigerMoves.get(0).intValue();

        // Simulate tiger move
        board[tiger] = Piece.EMPTY;
        board[tigerDestination] = Piece.TIGER;

        boolean dogsCanWin = false;

        // Try every dog move
        for (int from = 0; from < NODE_COUNT && !dogsCanWin; from++) {

            if (board[from] != Piece.DOG) {
                continue;
            }

            for (Integer toNode : getNeighbors(from)) {

                int to = toNode.intValue();

                if (board[to] != Piece.EMPTY) {
                    continue;
                }

                // Simulate dog move
                board[from] = Piece.EMPTY;
                board[to] = Piece.DOG;

                // Check whether tiger is trapped
                if (!tigerHasValidMove()) {
                    dogsCanWin = true;
                }

                // Restore dog move
                board[to] = Piece.EMPTY;
                board[from] = Piece.DOG;

                if (dogsCanWin) {
                    break;
                }
            }
        }

        // Restore tiger move
        board[tigerDestination] = Piece.EMPTY;
        board[tiger] = Piece.TIGER;

        return dogsCanWin;
    }
    
    public boolean dogsHaveWon() {
        int tiger = tigerNode();

        if (tiger < 0) {
            return true;
        }

        // Tiger entered the apex trap
        if (tiger == TRAP_APEX) {
            return true;
        }

        int legalMoveCount = 0;
        boolean trapAvailable = false;

        for (Integer neighbor : getNeighbors(tiger)) {
            if (board[neighbor.intValue()] == Piece.EMPTY) {
                legalMoveCount++;

                if (neighbor.intValue() == TRAP_APEX) {
                    trapAvailable = true;
                }
            }
        }

        // Tiger completely trapped
        if (legalMoveCount == 0) {
            return true;
        }

        // Tiger's only move is the apex trap
        if (legalMoveCount == 1 && trapAvailable) {
            return true;
        }

        // NEW RULE:
        // Tiger has exactly one move and that move
        // leaves him trapped.
        if (legalMoveCount == 1) {

            int move = -1;

            for (Integer neighbor : getNeighbors(tiger)) {
                if (board[neighbor.intValue()] == Piece.EMPTY) {
                    move = neighbor.intValue();
                    break;
                }
            }

            Piece oldTiger = board[tiger];

            // Simulate the move
            board[tiger] = Piece.EMPTY;
            board[move] = Piece.TIGER;

            boolean trapped = !tigerHasValidMove();

            // Restore the board
            board[move] = Piece.EMPTY;
            board[tiger] = oldTiger;

            if (trapped) {
                return true;
            }
        }
        if (dogsCanTrapTigerNextMove()) {
            return true;
        }
        return false;
    }
    private List<Integer> tigerCapturesFrom(int tigerNode) {
        List<Integer> captured = new ArrayList<Integer>();
        List<Integer> dogNeighbors = new ArrayList<Integer>();
        for (Integer nextNode : getNeighbors(tigerNode)) {
            int next = nextNode.intValue();
            if (board[next] == Piece.DOG) {
                dogNeighbors.add(Integer.valueOf(next));
            }
        }

        for (int i = 0; i < dogNeighbors.size(); i++) {
            for (int j = i + 1; j < dogNeighbors.size(); j++) {
                int a = dogNeighbors.get(i).intValue();
                int b = dogNeighbors.get(j).intValue();
                if (NODE_X[a] + NODE_X[b] == NODE_X[tigerNode] * 2
                        && NODE_Y[a] + NODE_Y[b] == NODE_Y[tigerNode] * 2) {
                    captured.add(Integer.valueOf(a));
                    captured.add(Integer.valueOf(b));
                }
            }
        }
        return captured;
    }

    public boolean tigerHasValidMove() {
        int tiger = tigerNode();
        if (tiger < 0) {
            return false;
        }
        for (Integer nextNode : getNeighbors(tiger)) {
            int next = nextNode.intValue();
            if (board[next] == Piece.EMPTY) {
                return true;
            }
        }
        return false;
    }

    public int countDogs() {
        int count = 0;
        for (int i = 0; i < board.length; i++) {
            if (board[i] == Piece.DOG) {
                count++;
            }
        }
        return count;
    }

    private int tigerNode() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == Piece.TIGER) {
                return i;
            }
        }
        return -1;
    }

    public GameState snapshot() {
        return new GameState((Piece[]) board.clone(), turn, gameOver);
    }

    public void restore(GameState state) {
        System.arraycopy(state.getBoard(), 0, board, 0, board.length);
        turn = state.getTurn();
        gameOver = state.isGameOver();
    }

    public void pushUndo(GameState state) {
        undoStack.push(state);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            restore(undoStack.pop());
            gameOver = false;
        }
    }

    public Piece getPiece(int node) {
        return board[node];
    }

    public Piece getTurn() {
        return turn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public Set<Integer> getNeighbors(int node) {
        return neighbors.get(Integer.valueOf(node));
    }
}
