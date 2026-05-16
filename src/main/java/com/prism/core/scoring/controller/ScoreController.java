package com.prism.core.scoring.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.scoring.dto.response.*;
import com.prism.core.scoring.service.RecommendationService;
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
    private final RecommendationService recommendationService;

    /**
     * Main entry point — checks 30-day cache, returns score if fresh,
     * otherwise triggers async pipeline and returns job ID for polling.
     */
    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<FetchScoreResponse>> fetchScore(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(ApiResponse.success(
                scoringService.fetchOrTriggerScore(userDetails.getUserId(), force)));
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
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @RequestParam(defaultValue = "false") boolean breakdown) {
        return ResponseEntity.ok(ApiResponse.success(
                scoringService.getLatestScore(userDetails.getUserId(), breakdown)));
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

    /**
     * Get AI-generated actionable recommendations to improve the PRISM score.
     * Returns cached recs if available (within 24h). For fresh LLM generation use /generate.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getRecommendations(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                "Recommendations retrieved",
                recommendationService.getOrGenerateRecommendations(userDetails.getUserId())));
    }

    /**
     * Trigger async LLM recommendation generation.
     * Returns a jobId immediately — poll /recommendations/status/{jobId} for results.
     */
    @PostMapping("/recommendations/generate")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> triggerRecommendations(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        UUID jobId = recommendationService.triggerAsyncGeneration(userDetails.getUserId());
        return ResponseEntity.accepted().body(ApiResponse.success(
                "Recommendation generation started. Poll status endpoint for results.",
                java.util.Map.of("jobId", jobId, "pollUrl", "/api/v1/score/recommendations/status/" + jobId)
        ));
    }

    /**
     * Poll the status of an async recommendation generation job.
     * When status=DONE, the recommendations array is included in the response.
     */
    @GetMapping("/recommendations/status/{jobId}")
    public ResponseEntity<ApiResponse<com.prism.core.scoring.dto.response.RecommendationJobStatusResponse>> getRecommendationJobStatus(
            @PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getJobStatus(jobId)));
    }
}
