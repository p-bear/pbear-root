package com.pbear.chessai.core.chess;

public enum PieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

    public char toChar() {
        return switch (this) {
            case PAWN   -> 'p';
            case KNIGHT -> 'n';
            case BISHOP -> 'b';
            case ROOK   -> 'r';
            case QUEEN  -> 'q';
            case KING   -> 'k';
        };
    }

    public static PieceType fromChar(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'p' -> PAWN;
            case 'n' -> KNIGHT;
            case 'b' -> BISHOP;
            case 'r' -> ROOK;
            case 'q' -> QUEEN;
            case 'k' -> KING;
            default  -> throw new IllegalArgumentException("Unknown piece char: " + c);
        };
    }
}
