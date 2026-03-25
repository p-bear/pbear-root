package com.pbear.chessai.rest.dto;

public record PostPredictReq(String fen, int topN, String modelName) {}
