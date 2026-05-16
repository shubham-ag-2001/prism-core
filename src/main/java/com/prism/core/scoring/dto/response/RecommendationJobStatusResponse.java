package com.prism.core.scoring.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RecommendationJobStatusResponse {
    private UUID jobId;
    private String status;              // PENDING | RUNNING | DONE | FAILED
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
    private List<RecommendationDto> recommendations;  // non-null only when DONE
}
