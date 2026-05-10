package com.prism.core.scoring.engine.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScoringResult {
    private String userId;
    private int prismScore;                    // 300–900, clamped and rounded
    private String scoreBand;                  // Poor / Fair / Average / Good / Very Good / Excellent
    private String engineVersion;
    private List<String> killSwitchesTriggered;
    private List<String> alerts;
    private List<DimensionResult> dimensions;
    private double rawTotal;                   // pre-clamp sum, useful for debugging

    /** Derive score band from final score */
    public static String toScoreBand(int score) {
        if (score < 450) return "Poor";
        if (score < 550) return "Fair";
        if (score < 650) return "Average";
        if (score < 750) return "Good";
        if (score < 850) return "Very Good";
        return "Excellent";
    }
}
