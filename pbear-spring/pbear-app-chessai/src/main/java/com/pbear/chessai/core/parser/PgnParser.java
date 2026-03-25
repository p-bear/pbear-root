package com.pbear.chessai.core.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses PGN files into a list of games, where each game is a list of SAN move strings.
 */
@SuppressWarnings("unused")
public class PgnParser {

    // Matches move numbers like "1." or "1..."
    private static final Pattern MOVE_NUMBER = Pattern.compile("\\d+\\.+");
    // Game termination markers
    private static final Pattern TERMINATION = Pattern.compile("1-0|0-1|1/2-1/2|\\*");
    // Comment removal
    private static final Pattern COMMENT = Pattern.compile("\\{[^}]*\\}|;[^\n]*");
    // Annotation glyphs
    private static final Pattern GLYPH = Pattern.compile("\\$\\d+");
    // Variation removal (recursive not supported – flattened)
    private static final Pattern VARIATION = Pattern.compile("\\([^()]*\\)");
    // Tag pairs
    private static final Pattern TAG = Pattern.compile("\\[[^]]*]");

    /**
     * Parse a PGN file and return a list of games.
     * Each game is a list of SAN move strings (e.g., ["e4","e5","Nf3",...]).
     */
    public List<List<String>> parseFile(Path pgnFile) throws IOException {
        String content = Files.readString(pgnFile);
        return parseString(content);
    }

    public List<List<String>> parseString(String pgn) {
        List<List<String>> games = new ArrayList<>();

        // Split into game blocks by tag sections
        // Strategy: split on consecutive tag pairs followed by move text
        String[] blocks = pgn.split("(?=\\[Event )");
        for (String block : blocks) {
            if (block.isBlank()) continue;
            List<String> moves = parseGameBlock(block);
            if (!moves.isEmpty()) {
                games.add(moves);
            }
        }
        return games;
    }

    private List<String> parseGameBlock(String block) {
        // Remove tags
        String text = TAG.matcher(block).replaceAll(" ");
        // Remove comments
        text = removeNestedComments(text);
        text = COMMENT.matcher(text).replaceAll(" ");
        // Remove annotation glyphs
        text = GLYPH.matcher(text).replaceAll(" ");
        // Remove variations (simple single-level)
        for (int i = 0; i < 10; i++) {
            String prev = text;
            text = VARIATION.matcher(text).replaceAll(" ");
            if (text.equals(prev)) break;
        }
        // Remove move numbers
        text = MOVE_NUMBER.matcher(text).replaceAll(" ");
        // Remove termination markers
        text = TERMINATION.matcher(text).replaceAll(" ");

        List<String> moves = new ArrayList<>();
        for (String token : text.trim().split("\\s+")) {
            if (!token.isBlank() && !token.equals("..")) {
                moves.add(token);
            }
        }
        return moves;
    }

    private String removeNestedComments(String text) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (char c : text.toCharArray()) {
            if (c == '{') { depth++; }
            else if (c == '}') { if (depth > 0) depth--; }
            else if (depth == 0) { sb.append(c); }
        }
        return sb.toString();
    }
}
