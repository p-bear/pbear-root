package com.pbear.chessai.core.chess;

public enum Color {
    WHITE, BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
