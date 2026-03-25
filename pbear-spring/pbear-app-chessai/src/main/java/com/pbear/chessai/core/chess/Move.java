package com.pbear.chessai.core.chess;

/**
 * Represents a chess move.
 * from/to are 0-based square indices (0=a1, 7=h1, 56=a8, 63=h8).
 * promotion is null unless this is a pawn promotion.
 * capture is the captured piece (null if none, or en passant piece).
 */
public record Move(int from, int to, PieceType promotion, Piece capture) {

    public Move(int from, int to) {
        this(from, to, null, null);
    }

    @SuppressWarnings("unused")
    public Move(int from, int to, PieceType promotion) {
        this(from, to, promotion, null);
    }

    /** Returns the UCI string, e.g. "e2e4", "e7e8q" */
    public String toUci() {
        StringBuilder sb = new StringBuilder();
        sb.append(squareName(from));
        sb.append(squareName(to));
        if (promotion != null) {
            sb.append(promotion.toChar());
        }
        return sb.toString();
    }

    public static String squareName(int sq) {
        int file = sq % 8;
        int rank = sq / 8;
        return String.valueOf((char) ('a' + file)) + (rank + 1);
    }

    public static int squareIndex(String name) {
        int file = name.charAt(0) - 'a';
        int rank = name.charAt(1) - '1';
        return rank * 8 + file;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return toUci();
    }
}
