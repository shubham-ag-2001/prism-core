package com.prism.core.scoring.engine.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VectorResult {
    private String vectorKey;       // e.g. "INC01"
    private String displayName;
    private double vectorScore;     // 0–100
    private double maxPts;          // max point contribution for this vector
    private double contribution;    // vectorScore × maxPts / 100
    private boolean capApplied;
    private Double appliedCap;      // what the cap value was, if applied
}
