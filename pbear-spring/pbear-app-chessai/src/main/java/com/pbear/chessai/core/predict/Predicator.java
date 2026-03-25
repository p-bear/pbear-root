package com.pbear.chessai.core.predict;

import com.pbear.chessai.core.chess.BoardEncoder;
import com.pbear.chessai.core.chess.GameState;
import com.pbear.chessai.core.chess.Move;
import com.pbear.chessai.core.chess.MoveEncoder;
import com.pbear.chessai.core.chess.MoveGenerator;
import com.pbear.chessai.core.model.ModelProvider;
import com.pbear.chessai.core.parser.FenParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class Predicator {
  private final ModelProvider modelProvider;
  private final FenParser fenParser;
  private final BoardEncoder boardEncoder;
  private final MoveGenerator moveGenerator;
  private final MoveEncoder moveEncoder;

  public Flux<MoveResult> predict(final String fen, final int topN, final String modelName) {
    return this.modelProvider.getModel(modelName)
        .switchIfEmpty(Mono.error(new RuntimeException("Model not found!")))
        .flatMapIterable(model -> {
          log.info("Predicting model {}", model.name());

          GameState state = fenParser.parse(fen);
          float[] features = boardEncoder.encode(state);
          INDArray input = Nd4j.create(new float[][]{features});

          INDArray output = model.data().output(input, false);
          float[] probs = output.toFloatVector();

          List<Move> legalMoves = moveGenerator.generateLegalMoves(state);
          Set<Integer> legalIndices = new HashSet<>();
          Map<Integer, Move> indexToMove = new HashMap<>();

          for (Move m : legalMoves) {
            int idx = moveEncoder.encode(m);
            legalIndices.add(idx);
            // Keep move with the highest priority (prefer non-promotion, then queen promo)
            indexToMove.merge(idx, m, (existing, challenger) -> {
              if (existing.promotion() == null) return existing;
              return challenger;
            });
          }

          // Score only legal moves
          List<MoveResult> results = new ArrayList<>();
          for (int idx : legalIndices) {
            Move move = indexToMove.get(idx);
            float prob = idx < probs.length ? probs[idx] : 0.0f;
            results.add(new MoveResult(move.toUci(), prob));
          }

          results.sort(Comparator.comparingDouble(MoveResult::probability).reversed());

          return results.subList(0, Math.min(topN, results.size()));
        });
  }
}
