package com.pbear.chessai.core.chess;

import org.springframework.stereotype.Component;

/**
 * Encodes/decodes chess moves as integer indices in [0, 4095].
 * Index = from * 64 + to  (ignores promotion piece — queen promotion assumed for max prob)
 * Promotion is tracked separately but the primary index is still from*64+to.
 */
@SuppressWarnings("unused")
@Component
public class MoveEncoder {

    public static final int OUTPUT_SIZE = 4096; // 64 × 64

    /** Encode a move to an index [0, 4095]. */
    public int encode(Move move) {
        return move.from() * 64 + move.to();
    }

    /** Decode an index back to a Move (no capture info, no promotion). */
    public Move decode(int index) {
        int from = index / 64;
        int to   = index % 64;
        return new Move(from, to);
    }

    /**
     * Find the best legal move matching the given index.
     * Returns null if no legal move matches.
     */
    public Move findBestMatch(int index, java.util.List<Move> legalMoves) {
        int from = index / 64;
        int to   = index % 64;
        Move queenPromo = null;
        for (Move m : legalMoves) {
            if (m.from() == from && m.to() == to) {
                if (m.promotion() == null) return m;
                if (m.promotion() == PieceType.QUEEN) queenPromo = m;
            }
        }
        return queenPromo; // prefer queen promotion if only promo moves remain
    }
}
