package com.pbear.chessai.core.parser;

import com.pbear.chessai.core.chess.Color;
import com.pbear.chessai.core.chess.GameState;
import com.pbear.chessai.core.chess.Move;
import com.pbear.chessai.core.chess.Piece;
import org.springframework.stereotype.Component;

/**
 * Parses FEN strings into GameState objects.
 * FEN format: <pieces> <side> <castling> <ep> <halfmove> <fullmove>
 */
@Component
public class FenParser {

    @SuppressWarnings("unused")
    public static final String STARTING_FEN =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public GameState parse(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid FEN (too few fields): " + fen);
        }

        Piece[][] board = parsePiecePlacement(parts[0]);
        Color sideToMove = parseSide(parts[1]);
        boolean[] castling = parseCastling(parts[2]);
        int ep = parseEnPassant(parts[3]);
        int halfMove  = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;
        int fullMove  = parts.length > 5 ? Integer.parseInt(parts[5]) : 1;

        return new GameState(board, sideToMove,
                castling[0], castling[1], castling[2], castling[3],
                ep, halfMove, fullMove);
    }

    private Piece[][] parsePiecePlacement(String placement) {
        Piece[][] board = new Piece[8][8];
        String[] ranks = placement.split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN piece placement: " + placement);
        }
        for (int i = 0; i < 8; i++) {
            int rank = 7 - i; // FEN rank 8 first -> board rank 7
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    board[rank][file] = Piece.fromChar(c);
                    file++;
                }
            }
        }
        return board;
    }

    private Color parseSide(String side) {
        return side.equals("w") ? Color.WHITE : Color.BLACK;
    }

    private boolean[] parseCastling(String castling) {
        // [whiteKingside, whiteQueenside, blackKingside, blackQueenside]
        return new boolean[]{
            castling.contains("K"),
            castling.contains("Q"),
            castling.contains("k"),
            castling.contains("q")
        };
    }

    private int parseEnPassant(String ep) {
        if (ep.equals("-")) return -1;
        return Move.squareIndex(ep);
    }
}
