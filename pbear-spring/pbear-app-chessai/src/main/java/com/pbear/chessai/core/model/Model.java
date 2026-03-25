package com.pbear.chessai.core.model;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.util.Date;

public record Model(
    String name,
    MultiLayerNetwork data,
    Date updateDate
) {}
