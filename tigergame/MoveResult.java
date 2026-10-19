package com.tigergame;

import java.util.List;

public class MoveResult {
    private final boolean moved;
    private final List<Integer> capturedNodes;

    public MoveResult(boolean moved, List<Integer> capturedNodes) {
        this.moved = moved;
        this.capturedNodes = capturedNodes;
    }

    public boolean isMoved() {
        return moved;
    }

    public List<Integer> getCapturedNodes() {
        return capturedNodes;
    }
}
