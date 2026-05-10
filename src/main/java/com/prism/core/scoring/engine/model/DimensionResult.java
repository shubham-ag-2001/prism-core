package com.prism.core.scoring.engine.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DimensionResult {
    private String dimensionKey;        // e.g. "D1_INCOME"
    private String displayName;
    private double score;               // actual score in min–max range
    private double minScore;
    private double maxScore;
    private double performanceRatio;    // (score - min) / (max - min), 0.0–1.0
    private boolean killSwitchTriggered;
    private List<VectorResult> vectors;
}
