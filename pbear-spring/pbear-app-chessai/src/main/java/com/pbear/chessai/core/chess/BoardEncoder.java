package com.pbear.chessai.core.chess;

import org.springframework.stereotype.Component;

/**
 * Encodes a GameState into a float vector for neural network input.
 * Layout (total 781 features):
 *   [0..767]   : 12 bitboards (6 piece types × 2 colors), each 64 bits
 *   [768..771] : castling rights (WK, WQ, BK, BQ)
 *   [772..779] : en passant file one-hot (8 files, or all-zero if none)
 *   [780]      : side to move (1.0 = White, 0.0 = Black)
 */
@SuppressWarnings("unused")
@Component
public class BoardEncoder {

    public static final int INPUT_SIZE = 781;

    // Channel indices for each (color, piece) combination
    private static final int CHANNEL_WHITE_PAWN   = 0;
    private static final int CHANNEL_WHITE_KNIGHT = 1;
    private static final int CHANNEL_WHITE_BISHOP = 2;
    private static final int CHANNEL_WHITE_ROOK   = 3;
    private static final int CHANNEL_WHITE_QUEEN  = 4;
    private static final int CHANNEL_WHITE_KING   = 5;
    private static final int CHANNEL_BLACK_PAWN   = 6;
    private static final int CHANNEL_BLACK_KNIGHT = 7;
    private static final int CHANNEL_BLACK_BISHOP = 8;
    private static final int CHANNEL_BLACK_ROOK   = 9;
    private static final int CHANNEL_BLACK_QUEEN  = 10;
    private static final int CHANNEL_BLACK_KING   = 11;

    public float[] encode(GameState state) {
        float[] features = new float[INPUT_SIZE];

        // 1) Piece bitboards
        for (int sq = 0; sq < 64; sq++) {
            Piece p = state.getPiece(sq);
            if (p == null) continue;
            int channel = pieceChannel(p);
            features[channel * 64 + sq] = 1.0f;
        }

        // 2) Castling rights
        features[768] = state.isCastleWhiteKingside()  ? 1.0f : 0.0f;
        features[769] = state.isCastleWhiteQueenside() ? 1.0f : 0.0f;
        features[770] = state.isCastleBlackKingside()  ? 1.0f : 0.0f;
        features[771] = state.isCastleBlackQueenside() ? 1.0f : 0.0f;

        // 3) En passant file one-hot
        int ep = state.getEnPassantSquare();
        if (ep != -1) {
            int epFile = ep % 8;
            features[772 + epFile] = 1.0f;
        }

        // 4) Side to move
        features[780] = state.getSideToMove() == Color.WHITE ? 1.0f : 0.0f;

        return features;
    }

    private int pieceChannel(Piece p) {
        int base = p.color() == Color.WHITE ? 0 : 6;
        int offset = switch (p.type()) {
            case PAWN   -> 0;
            case KNIGHT -> 1;
            case BISHOP -> 2;
            case ROOK   -> 3;
            case QUEEN  -> 4;
            case KING   -> 5;
        };
        return base + offset;
    }
}
