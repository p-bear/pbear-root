package com.pbear.chessai.core.chess;

import lombok.Getter;

/**
 * Immutable snapshot of the full chess game state.
 * board[rank][file], rank 0 = rank 1 (white side), rank 7 = rank 8 (black side).
 */
public class GameState {

    // board[rank][file], null means empty square
    private final Piece[][] board;

    @Getter
    private final Color sideToMove;

    // Castling rights
    @Getter
    private final boolean castleWhiteKingside;
    @Getter
    private final boolean castleWhiteQueenside;
    @Getter
    private final boolean castleBlackKingside;
    @Getter
    private final boolean castleBlackQueenside;

    // En passant target square index (-1 if none)
    @Getter
    private final int enPassantSquare;

    @Getter
    private final int halfMoveClock;
    @Getter
    private final int fullMoveNumber;

    public GameState(Piece[][] board,
                     Color sideToMove,
                     boolean castleWhiteKingside,
                     boolean castleWhiteQueenside,
                     boolean castleBlackKingside,
                     boolean castleBlackQueenside,
                     int enPassantSquare,
                     int halfMoveClock,
                     int fullMoveNumber) {
        // Defensive copy
        this.board = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            System.arraycopy(board[r], 0, this.board[r], 0, 8);
        }
        this.sideToMove = sideToMove;
        this.castleWhiteKingside = castleWhiteKingside;
        this.castleWhiteQueenside = castleWhiteQueenside;
        this.castleBlackKingside = castleBlackKingside;
        this.castleBlackQueenside = castleBlackQueenside;
        this.enPassantSquare = enPassantSquare;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveNumber = fullMoveNumber;
    }

    // --- Accessors ---

    public Piece getPiece(int square) {
        return board[square / 8][square % 8];
    }

    public Piece getPiece(int rank, int file) {
        return board[rank][file];
    }

    public Piece[][] getBoard() {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, 8);
        }
        return copy;
    }

  /**
     * Apply a move and return the new GameState.
     * Does NOT validate legality – caller must ensure the move is legal.
     */
    public GameState applyMove(Move move) {
        Piece[][] newBoard = getBoard();

        int fromRank = move.from() / 8;
        int fromFile = move.from() % 8;
        int toRank   = move.to()   / 8;
        int toFile   = move.to()   % 8;

        Piece moving = newBoard[fromRank][fromFile];
        if (moving == null) {
            throw new IllegalStateException("No piece at from-square: " + Move.squareName(move.from()));
        }

        // Determine new castling rights
        boolean cwk = castleWhiteKingside;
        boolean cwq = castleWhiteQueenside;
        boolean cbk = castleBlackKingside;
        boolean cbq = castleBlackQueenside;

        // King moves remove all castling rights for that color
        if (moving.type() == PieceType.KING) {
            if (moving.color() == Color.WHITE) { cwk = false; cwq = false; }
            else                               { cbk = false; cbq = false; }
        }
        // Rook moves or captures remove specific castling right
        if (move.from() == 0  || move.to() == 0)  cwq = false;
        if (move.from() == 7  || move.to() == 7)  cwk = false;
        if (move.from() == 56 || move.to() == 56) cbq = false;
        if (move.from() == 63 || move.to() == 63) cbk = false;

        // En passant
        int newEp = -1;
        if (moving.type() == PieceType.PAWN) {
            int rankDiff = Math.abs(toRank - fromRank);
            if (rankDiff == 2) {
                // double pawn push
                int epRank = (fromRank + toRank) / 2;
                newEp = epRank * 8 + fromFile;
            }
        }

        // Move piece
        newBoard[fromRank][fromFile] = null;

        // En passant capture
        if (moving.type() == PieceType.PAWN && move.to() == enPassantSquare && enPassantSquare != -1) {
          // same rank as moving pawn
          newBoard[fromRank][toFile] = null;
        }

        // Castling: move rook as well
        if (moving.type() == PieceType.KING) {
            int fileDiff = toFile - fromFile;
            if (fileDiff == 2) { // kingside
                newBoard[fromRank][7] = null;
                newBoard[fromRank][5] = new Piece(moving.color(), PieceType.ROOK);
            } else if (fileDiff == -2) { // queenside
                newBoard[fromRank][0] = null;
                newBoard[fromRank][3] = new Piece(moving.color(), PieceType.ROOK);
            }
        }

        // Promotion
        Piece placed;
        if (move.promotion() != null) {
            placed = new Piece(moving.color(), move.promotion());
        } else {
            placed = moving;
        }
        newBoard[toRank][toFile] = placed;

        // Half-move clock
        int newHalf = (moving.type() == PieceType.PAWN || move.capture() != null) ? 0 : halfMoveClock + 1;
        int newFull = fullMoveNumber + (sideToMove == Color.BLACK ? 1 : 0);

        return new GameState(newBoard, sideToMove.opposite(),
                cwk, cwq, cbk, cbq,
                newEp, newHalf, newFull);
    }

    /** FEN representation */
    public String toFen() {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int empty = 0;
            for (int file = 0; file < 8; file++) {
                Piece p = board[rank][file];
                if (p == null) {
                    empty++;
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0; }
                    sb.append(p.toChar());
                }
            }
            if (empty > 0) sb.append(empty);
            if (rank > 0) sb.append('/');
        }
        sb.append(' ').append(sideToMove == Color.WHITE ? 'w' : 'b');
        sb.append(' ');
        String castling = (castleWhiteKingside ? "K" : "") +
                          (castleWhiteQueenside ? "Q" : "") +
                          (castleBlackKingside ? "k" : "") +
                          (castleBlackQueenside ? "q" : "");
        sb.append(castling.isEmpty() ? "-" : castling);
        sb.append(' ');
        sb.append(enPassantSquare == -1 ? "-" : Move.squareName(enPassantSquare));
        sb.append(' ').append(halfMoveClock);
        sb.append(' ').append(fullMoveNumber);
        return sb.toString();
    }
}
