package com.pbear.chessai.core.chess;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates all legal moves for a given GameState.
 * A move is legal if it does not leave the moving side's king in check.
 */
@Component
public class MoveGenerator {

    /** Returns all legal moves for the side to move. */
    public List<Move> generateLegalMoves(GameState state) {
        List<Move> pseudoLegal = generatePseudoLegalMoves(state);
        List<Move> legal = new ArrayList<>();
        for (Move move : pseudoLegal) {
            GameState after = state.applyMove(move);
            if (!isKingInCheck(after, state.getSideToMove())) {
                legal.add(move);
            }
        }
        return legal;
    }

    /** Returns true if the specified color's king is currently in check. */
    public boolean isKingInCheck(GameState state, Color color) {
        int kingSquare = findKing(state, color);
        if (kingSquare == -1) return false; // should not happen in a valid position
        return isSquareAttackedBy(state, kingSquare, color.opposite());
    }

    /** Returns true if the given square is attacked by any piece of the given attacker color. */
    public boolean isSquareAttackedBy(GameState state, int square, Color attacker) {
        // Check all attacker pieces
        for (int sq = 0; sq < 64; sq++) {
            Piece p = state.getPiece(sq);
            if (p != null && p.color() == attacker) {
                if (canPieceAttackSquare(state, sq, p, square)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canPieceAttackSquare(GameState state, int from, Piece piece, int target) {
        int fromRank = from / 8, fromFile = from % 8;
        int toRank   = target / 8, toFile = target % 8;
        int dr = toRank - fromRank, df = toFile - fromFile;

        return switch (piece.type()) {
            case PAWN -> {
                int dir = piece.color() == Color.WHITE ? 1 : -1;
                yield dr == dir && Math.abs(df) == 1;
            }
            case KNIGHT -> {
                int adr = Math.abs(dr), adf = Math.abs(df);
                yield (adr == 2 && adf == 1) || (adr == 1 && adf == 2);
            }
            case BISHOP -> Math.abs(dr) == Math.abs(df) && isDiagonalClear(state, from, target);
            case ROOK   -> (dr == 0 || df == 0) && isStraightClear(state, from, target);
            case QUEEN  -> {
                if (dr == 0 || df == 0) yield isStraightClear(state, from, target);
                else yield Math.abs(dr) == Math.abs(df) && isDiagonalClear(state, from, target);
            }
            case KING -> Math.abs(dr) <= 1 && Math.abs(df) <= 1;
        };
    }

    // --- Pseudo-legal move generation ---

    private List<Move> generatePseudoLegalMoves(GameState state) {
        List<Move> moves = new ArrayList<>();
        Color side = state.getSideToMove();
        for (int sq = 0; sq < 64; sq++) {
            Piece p = state.getPiece(sq);
            if (p != null && p.color() == side) {
                switch (p.type()) {
                    case PAWN   -> generatePawnMoves(state, sq, p, moves);
                    case KNIGHT -> generateKnightMoves(state, sq, p, moves);
                    case BISHOP -> generateSlidingMoves(state, sq, p, moves, true,  false);
                    case ROOK   -> generateSlidingMoves(state, sq, p, moves, false, true);
                    case QUEEN  -> generateSlidingMoves(state, sq, p, moves, true,  true);
                    case KING   -> generateKingMoves(state, sq, p, moves);
                }
            }
        }
        return moves;
    }

    private void generatePawnMoves(GameState state, int from, Piece piece, List<Move> moves) {
        int rank = from / 8, file = from % 8;
        int dir = piece.color() == Color.WHITE ? 1 : -1;
        int startRank = piece.color() == Color.WHITE ? 1 : 6;
        int promoRank = piece.color() == Color.WHITE ? 7 : 0;

        // Single push
        int toRank = rank + dir;
        if (toRank >= 0 && toRank < 8) {
            int to = toRank * 8 + file;
            if (state.getPiece(to) == null) {
                if (toRank == promoRank) {
                    addPromotionMoves(from, to, null, moves);
                } else {
                    moves.add(new Move(from, to, null, null));
                    // Double push
                    if (rank == startRank) {
                        int to2 = (rank + 2 * dir) * 8 + file;
                        if (state.getPiece(to2) == null) {
                            moves.add(new Move(from, to2, null, null));
                        }
                    }
                }
            }
            // Captures
            for (int df : new int[]{-1, 1}) {
                int toFile = file + df;
                if (toFile < 0 || toFile > 7) continue;
                int capSq = toRank * 8 + toFile;
                Piece target = state.getPiece(capSq);
                if (target != null && target.color() != piece.color()) {
                    if (toRank == promoRank) {
                        addPromotionMoves(from, capSq, target, moves);
                    } else {
                        moves.add(new Move(from, capSq, null, target));
                    }
                } else if (capSq == state.getEnPassantSquare()) {
                    // En passant - the captured piece is on the same rank as the moving pawn
                    int capturedPawnSq = rank * 8 + toFile;
                    Piece captured = state.getPiece(capturedPawnSq);
                    moves.add(new Move(from, capSq, null, captured));
                }
            }
        }
    }

    private void addPromotionMoves(int from, int to, Piece capture, List<Move> moves) {
        for (PieceType pt : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
            moves.add(new Move(from, to, pt, capture));
        }
    }

    private void generateKnightMoves(GameState state, int from, Piece piece, List<Move> moves) {
        int rank = from / 8, file = from % 8;
        int[][] deltas = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : deltas) {
            int tr = rank + d[0], tf = file + d[1];
            if (tr < 0 || tr > 7 || tf < 0 || tf > 7) continue;
            int to = tr * 8 + tf;
            Piece target = state.getPiece(to);
            if (target == null || target.color() != piece.color()) {
                moves.add(new Move(from, to, null, target));
            }
        }
    }

    private void generateSlidingMoves(GameState state, int from, Piece piece, List<Move> moves,
                                      boolean diagonal, boolean straight) {
        int rank = from / 8, file = from % 8;
        int[][] dirs;
        if (diagonal && straight) {
            dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        } else if (diagonal) {
            dirs = new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}};
        } else {
            dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1}};
        }
        for (int[] d : dirs) {
            int tr = rank + d[0], tf = file + d[1];
            while (tr >= 0 && tr < 8 && tf >= 0 && tf < 8) {
                int to = tr * 8 + tf;
                Piece target = state.getPiece(to);
                if (target == null) {
                    moves.add(new Move(from, to, null, null));
                } else {
                    if (target.color() != piece.color()) {
                        moves.add(new Move(from, to, null, target));
                    }
                    break;
                }
                tr += d[0];
                tf += d[1];
            }
        }
    }

    private void generateKingMoves(GameState state, int from, Piece piece, List<Move> moves) {
        int rank = from / 8, file = from % 8;
        for (int dr = -1; dr <= 1; dr++) {
            for (int df = -1; df <= 1; df++) {
                if (dr == 0 && df == 0) continue;
                int tr = rank + dr, tf = file + df;
                if (tr < 0 || tr > 7 || tf < 0 || tf > 7) continue;
                int to = tr * 8 + tf;
                Piece target = state.getPiece(to);
                if (target == null || target.color() != piece.color()) {
                    moves.add(new Move(from, to, null, target));
                }
            }
        }
        // Castling
        Color color = piece.color();
        if (color == Color.WHITE && rank == 0 && file == 4) {
            // Kingside
            if (state.isCastleWhiteKingside()
                    && state.getPiece(0, 5) == null && state.getPiece(0, 6) == null
                    && !isSquareAttackedBy(state, from, color.opposite())
                    && !isSquareAttackedBy(state, 5, color.opposite())
                    && !isSquareAttackedBy(state, 6, color.opposite())) {
                moves.add(new Move(from, 6, null, null));
            }
            // Queenside
            if (state.isCastleWhiteQueenside()
                    && state.getPiece(0, 1) == null && state.getPiece(0, 2) == null && state.getPiece(0, 3) == null
                    && !isSquareAttackedBy(state, from, color.opposite())
                    && !isSquareAttackedBy(state, 3, color.opposite())
                    && !isSquareAttackedBy(state, 2, color.opposite())) {
                moves.add(new Move(from, 2, null, null));
            }
        } else if (color == Color.BLACK && rank == 7 && file == 4) {
            // Kingside
            if (state.isCastleBlackKingside()
                    && state.getPiece(7, 5) == null && state.getPiece(7, 6) == null
                    && !isSquareAttackedBy(state, from, color.opposite())
                    && !isSquareAttackedBy(state, 61, color.opposite())
                    && !isSquareAttackedBy(state, 62, color.opposite())) {
                moves.add(new Move(from, 62, null, null));
            }
            // Queenside
            if (state.isCastleBlackQueenside()
                    && state.getPiece(7, 1) == null && state.getPiece(7, 2) == null && state.getPiece(7, 3) == null
                    && !isSquareAttackedBy(state, from, color.opposite())
                    && !isSquareAttackedBy(state, 59, color.opposite())
                    && !isSquareAttackedBy(state, 58, color.opposite())) {
                moves.add(new Move(from, 58, null, null));
            }
        }
    }

    // --- Path-clear helpers ---

    private boolean isStraightClear(GameState state, int from, int to) {
        int fromRank = from / 8, fromFile = from % 8;
        int toRank   = to   / 8, toFile   = to   % 8;
        if (fromRank == toRank) {
            int step = fromFile < toFile ? 1 : -1;
            for (int f = fromFile + step; f != toFile; f += step) {
                if (state.getPiece(fromRank * 8 + f) != null) return false;
            }
        } else if (fromFile == toFile) {
            int step = fromRank < toRank ? 1 : -1;
            for (int r = fromRank + step; r != toRank; r += step) {
                if (state.getPiece(r * 8 + fromFile) != null) return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean isDiagonalClear(GameState state, int from, int to) {
        int fromRank = from / 8, fromFile = from % 8;
        int toRank   = to   / 8, toFile   = to   % 8;
        int dr = Integer.signum(toRank - fromRank);
        int df = Integer.signum(toFile - fromFile);
        int r = fromRank + dr, f = fromFile + df;
        while (r != toRank || f != toFile) {
            if (state.getPiece(r * 8 + f) != null) return false;
            r += dr;
            f += df;
        }
        return true;
    }

    private int findKing(GameState state, Color color) {
        for (int sq = 0; sq < 64; sq++) {
            Piece p = state.getPiece(sq);
            if (p != null && p.color() == color && p.type() == PieceType.KING) {
                return sq;
            }
        }
        return -1;
    }
}
