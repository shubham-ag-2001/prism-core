package com.prism.core.scoring.dto.response;

import com.prism.core.common.enums.ScoringStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PrismScoreResponse {
    private UUID snapshotId;
    private Integer prismScore;             // 300–900, null if pending
    private ScoringStatus status;
    private String ruleSetVersion;
    private Instant computedAt;
    private Instant expiresAt;
    private boolean isCached;
    private String message;
    
    private String scoreBand;
    private com.fasterxml.jackson.databind.JsonNode dimensionBreakdown;
    private java.util.List<String> killSwitchesTriggered;
    private java.util.List<String> alerts;
}
