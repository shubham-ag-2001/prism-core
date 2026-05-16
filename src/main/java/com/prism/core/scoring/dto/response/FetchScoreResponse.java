package com.prism.core.scoring.dto.response;

import com.prism.core.common.enums.JobStatus;
import com.prism.core.common.enums.ScoringStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FetchScoreResponse {
    private boolean cached;
    private UUID jobId;                   // present when a new job was triggered
    private UUID snapshotId;              // present when returning a cached score
    private Integer prismScore;           // present when returning a cached score
    private String scoreBand;             // Poor / Fair / Average / Good / Very Good / Excellent
    private ScoringStatus scoringStatus;
    private JobStatus jobStatus;
    private String message;
}
