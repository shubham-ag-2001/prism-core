package com.prism.core.scoring.dto.response;

import com.prism.core.common.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ScoringJobResponse {
    private UUID jobId;
    private UUID snapshotId;
    private JobStatus status;
    private String failureReason;
    private Instant createdAt;
    private Instant completedAt;
    private String message;
}
