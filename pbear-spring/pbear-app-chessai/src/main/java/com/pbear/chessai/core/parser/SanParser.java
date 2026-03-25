package com.pbear.chessai.core.parser;

import com.pbear.chessai.core.chess.Color;
import com.pbear.chessai.core.chess.GameState;
import com.pbear.chessai.core.chess.Move;
import com.pbear.chessai.core.chess.MoveGenerator;
import com.pbear.chessai.core.chess.Piece;
import com.pbear.chessai.core.chess.PieceType;

import java.util.List;

/**
 * Converts SAN (Standard Algebraic Notation) move strings into Move objects,
 * using MoveGenerator to disambiguate.
 */
@SuppressWarnings("unused")
public class SanParser {

    private final MoveGenerator moveGen = new MoveGenerator();

    /**
     * Parse a SAN move string in the context of a GameState and return the corresponding Move.
     * Handles: normal moves, captures, promotions, castling, and check/checkmate annotations.
     */
    @SuppressWarnings({"StatementWithEmptyBody", "ConditionalExpressionWithIdenticalBranches"})
    public Move toMove(String san, GameState state) {
        // Remove check/checkmate/annotation suffixes
        String s = san.replaceAll("[+#!?x]", "").trim();

        // Castling
        if (s.equals("O-O-O") || s.equals("0-0-0")) {
            return findCastleMove(state, false);
        }
        if (s.equals("O-O") || s.equals("0-0")) {
            return findCastleMove(state, true);
        }

        // Promotion suffix
        PieceType promotion = null;
        if (s.contains("=")) {
            int eqIdx = s.indexOf('=');
            promotion = PieceType.fromChar(s.charAt(eqIdx + 1));
            s = s.substring(0, eqIdx);
        } else if (s.length() >= 2 && Character.isLowerCase(s.charAt(s.length() - 2))
                && "qrbnQRBN".indexOf(s.charAt(s.length() - 1)) >= 0
                && Character.isDigit(s.charAt(s.length() - 2) == '8' ? s.charAt(s.length() - 2) : s.charAt(s.length() - 2))) {
            // fallthrough - handled below
        }
        // Also handle implicit promotion like "e8Q"
        if (promotion == null && s.length() >= 2) {
            char last = s.charAt(s.length() - 1);
            char secondLast = s.charAt(s.length() - 2);
            if ("QRBN".indexOf(last) >= 0 && (Character.isDigit(secondLast) || secondLast == '8' || secondLast == '1')) {
                promotion = PieceType.fromChar(last);
                s = s.substring(0, s.length() - 1);
            }
        }

        // Destination square is always the last 2 characters
        if (s.length() < 2) {
            throw new IllegalArgumentException("Invalid SAN: " + san);
        }
        String toSq = s.substring(s.length() - 2);
        if (!isValidSquare(toSq)) {
            throw new IllegalArgumentException("Invalid destination square in SAN: " + san);
        }
        int to = Move.squareIndex(toSq);

        // Piece type
        PieceType pieceType;
        String disambig;
        if (Character.isUpperCase(s.charAt(0))) {
            pieceType = PieceType.fromChar(s.charAt(0));
            disambig = s.substring(1, s.length() - 2);
        } else {
            pieceType = PieceType.PAWN;
            disambig = s.substring(0, s.length() - 2);
        }

        // Generate all legal moves and find the matching one
        List<Move> legal = moveGen.generateLegalMoves(state);
        Move matched = null;
        for (Move m : legal) {
            Piece moving = state.getPiece(m.from());
            if (moving == null) continue;
            if (moving.type() != pieceType) continue;
            if (m.to() != to) continue;
            if (promotion != null && m.promotion() != promotion) continue;
            if (promotion == null && m.promotion() != null) continue;

            // Check disambiguation
            if (!matchesDisambig(m.from(), disambig)) continue;

            if (matched != null) {
                throw new IllegalArgumentException("Ambiguous SAN move: " + san);
            }
            matched = m;
        }

        if (matched == null) {
            throw new IllegalArgumentException("Illegal SAN move: " + san + " in position " + state.toFen());
        }
        return matched;
    }

    private boolean matchesDisambig(int from, String disambig) {
        if (disambig == null || disambig.isEmpty()) return true;
        int fromFile = from % 8;
        int fromRank = from / 8;
        for (char c : disambig.toCharArray()) {
            if (c >= 'a' && c <= 'h') {
                if (fromFile != (c - 'a')) return false;
            } else if (c >= '1' && c <= '8') {
                if (fromRank != (c - '1')) return false;
            }
        }
        return true;
    }

    private Move findCastleMove(GameState state, boolean kingside) {
        Color side = state.getSideToMove();
        int fromSq = side == Color.WHITE ? 4 : 60;
        int toSq = kingside
                ? (side == Color.WHITE ? 6 : 62)
                : (side == Color.WHITE ? 2 : 58);
        List<Move> legal = moveGen.generateLegalMoves(state);
        for (Move m : legal) {
            if (m.from() == fromSq && m.to() == toSq) return m;
        }
        throw new IllegalArgumentException("Castling not available: " + (kingside ? "O-O" : "O-O-O"));
    }

    private boolean isValidSquare(String sq) {
        if (sq.length() != 2) return false;
        return sq.charAt(0) >= 'a' && sq.charAt(0) <= 'h' &&
               sq.charAt(1) >= '1' && sq.charAt(1) <= '8';
    }
}
