package com.prism.core.scoring.service;

import com.prism.core.common.enums.JobStatus;
import com.prism.core.common.enums.ScoringStatus;
import com.prism.core.common.enums.TriggerType;
import com.prism.core.common.exception.ErrorCode;
import com.prism.core.common.exception.PrismException;
import com.prism.core.scoring.dto.response.*;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import com.prism.core.scoring.entity.ScoringJob;
import com.prism.core.scoring.repository.PrismScoreSnapshotRepository;
import com.prism.core.scoring.repository.ScoringJobRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final UserRepository                 userRepository;
    private final PrismScoreSnapshotRepository   snapshotRepository;
    private final ScoringJobRepository           scoringJobRepository;
    private final com.prism.core.scoring.engine.ScoringPipelineOrchestrator orchestrator;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${prism.scoring.cache-validity-days:30}")
    private int cacheValidityDays;

    @Value("${prism.scoring.current-rule-set-version:v1.0}")
    private String currentRuleSetVersion;

    // ─── Fetch / Trigger Score ────────────────────────────────────────────────

    @Transactional
    public FetchScoreResponse fetchOrTriggerScore(UUID userId, boolean force) {
        // 1. Check cache if not forcing re-calculation
        if (!force) {
            Optional<PrismScoreSnapshot> cached =
                    snapshotRepository.findValidCachedSnapshot(userId, Instant.now());

            if (cached.isPresent()) {
                PrismScoreSnapshot snap = cached.get();
                log.info("Cache HIT for user={}, score={}", userId, snap.getFinalScore());
                return FetchScoreResponse.builder()
                        .cached(true)
                        .snapshotId(snap.getId())
                        .prismScore(snap.getFinalScore())
                        .scoringStatus(snap.getScoringStatus())
                        .message("Returning cached PRISM score")
                        .build();
            }
        }

        // 2. Cache MISS — trigger new scoring pipeline
        log.info("Cache MISS for user={}, creating new scoring job", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PrismException.notFound("User not found"));

        PrismScoreSnapshot snapshot = PrismScoreSnapshot.builder()
                .user(user)
                .ruleSetVersion(currentRuleSetVersion)
                .triggeredBy(TriggerType.MANUAL)
                .build();
        snapshotRepository.save(snapshot);

        ScoringJob job = ScoringJob.builder()
                .user(user)
                .snapshot(snapshot)
                .status(JobStatus.PENDING)
                .build();
        scoringJobRepository.save(job);

        // 3. Trigger async pipeline (Phase 3 will fill this in)
        triggerScoringPipelineAsync(job.getId());

        return FetchScoreResponse.builder()
                .cached(false)
                .jobId(job.getId())
                .jobStatus(JobStatus.PENDING)
                .scoringStatus(ScoringStatus.PENDING)
                .message("Scoring pipeline started. Poll /score/job/" + job.getId() + " for status.")
                .build();
    }

    // ─── Poll Job Status ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ScoringJobResponse getJobStatus(UUID jobId) {
        ScoringJob job = scoringJobRepository.findById(jobId)
                .orElseThrow(() -> PrismException.notFound("Scoring job not found"));

        return ScoringJobResponse.builder()
                .jobId(job.getId())
                .snapshotId(job.getSnapshot() != null ? job.getSnapshot().getId() : null)
                .status(job.getStatus())
                .failureReason(job.getFailureReason())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .message(buildJobMessage(job.getStatus()))
                .build();
    }

    // ─── Latest Score ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PrismScoreResponse getLatestScore(UUID userId, boolean includeBreakdown) {
        PrismScoreSnapshot snap = snapshotRepository
                .findTopByUserIdAndScoringStatusOrderByComputedAtDesc(userId, ScoringStatus.COMPLETE)
                .orElseThrow(() -> new PrismException(
                        "No score available yet. Please trigger a score calculation.",
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        ErrorCode.NO_SCORE_AVAILABLE));

        PrismScoreResponse.PrismScoreResponseBuilder builder = PrismScoreResponse.builder()
                .snapshotId(snap.getId())
                .prismScore(snap.getFinalScore())
                .scoreBand(snap.getScoreBand())
                .status(snap.getScoringStatus())
                .ruleSetVersion(snap.getRuleSetVersion())
                .computedAt(snap.getComputedAt())
                .expiresAt(snap.getExpiresAt())
                .isCached(snap.getExpiresAt() != null && snap.getExpiresAt().isAfter(Instant.now()));

        if (includeBreakdown) {
            try {
                if (snap.getDimensionBreakdownJson() != null) {
                    builder.dimensionBreakdown(objectMapper.readTree(snap.getDimensionBreakdownJson()));
                }
                if (snap.getKillSwitchesTriggered() != null) {
                    builder.killSwitchesTriggered(objectMapper.readValue(snap.getKillSwitchesTriggered(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
                }
                if (snap.getAlertsJson() != null) {
                    builder.alerts(objectMapper.readValue(snap.getAlertsJson(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
                }
            } catch (Exception e) {
                log.warn("Failed to parse breakdown JSON for snapshot={}", snap.getId(), e);
            }
        }

        return builder.build();
    }

    // ─── Last Computed Time ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Long getLastScoreComputedTime(UUID userId) {
        return snapshotRepository
                .findTopByUserIdAndScoringStatusOrderByComputedAtDesc(userId, ScoringStatus.COMPLETE)
                .map(snap -> snap.getComputedAt().toEpochMilli())
                .orElse(null);
    }

    // ─── Score History ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ScoreHistoryEntry> getScoreHistory(UUID userId) {
        return snapshotRepository
                .findByUserIdAndScoringStatusOrderByComputedAtDesc(userId, ScoringStatus.COMPLETE)
                .stream()
                .map(snap -> ScoreHistoryEntry.builder()
                        .snapshotId(snap.getId())
                        .prismScore(snap.getFinalScore())
                        .ruleSetVersion(snap.getRuleSetVersion())
                        .computedAt(snap.getComputedAt())
                        .expiresAt(snap.getExpiresAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Async Pipeline Stub (Phase 3 will implement) ─────────────────────────

    @Async("scoringTaskExecutor")
    public void triggerScoringPipelineAsync(UUID jobId) {
        log.info("[Scoring] Starting pipeline for job={}", jobId);
        orchestrator.orchestrate(jobId);
    }

    private String buildJobMessage(JobStatus status) {
        return switch (status) {
            case PENDING  -> "Scoring pipeline queued";
            case RUNNING  -> "Scoring pipeline in progress";
            case COMPLETE -> "Scoring complete";
            case FAILED   -> "Scoring failed. Please retry.";
        };
    }
}
