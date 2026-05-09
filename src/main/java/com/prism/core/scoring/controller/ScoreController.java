package com.prism.core.scoring.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.scoring.dto.response.*;
import com.prism.core.scoring.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoringService scoringService;

    /**
     * Main entry point — checks 30-day cache, returns score if fresh,
     * otherwise triggers async pipeline and returns job ID for polling.
     */
    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<FetchScoreResponse>> fetchScore(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                scoringService.fetchOrTriggerScore(userDetails.getUserId())));
    }

    /**
     * Poll async scoring job status.
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<ScoringJobResponse>> getJobStatus(
            @PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(scoringService.getJobStatus(jobId)));
    }

    /**
     * Get the latest COMPLETE PRISM score for the authenticated user.
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PrismScoreResponse>> getLatestScore(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                scoringService.getLatestScore(userDetails.getUserId())));
    }

    /**
     * Get full score history (list of past snapshots with scores and dates).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ScoreHistoryEntry>>> getScoreHistory(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                scoringService.getScoreHistory(userDetails.getUserId())));
    }
}
