package com.prism.core.scoring.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ScoreHistoryEntry {
    private UUID snapshotId;
    private Integer prismScore;
    private String ruleSetVersion;
    private Instant computedAt;
    private Instant expiresAt;
}
