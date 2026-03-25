package com.pbear.chessai.core.chess;

public record Piece(Color color, PieceType type) {

    public char toChar() {
        char c = type.toChar();
        return color == Color.WHITE ? Character.toUpperCase(c) : c;
    }

    public static Piece fromChar(char c) {
        Color color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
        PieceType type = PieceType.fromChar(c);
        return new Piece(color, type);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return String.valueOf(toChar());
    }
}
