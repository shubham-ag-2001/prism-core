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

    @Value("${prism.scoring.cache-validity-days:30}")
    private int cacheValidityDays;

    @Value("${prism.scoring.current-rule-set-version:v1.0}")
    private String currentRuleSetVersion;

    // ─── Fetch / Trigger Score ────────────────────────────────────────────────

    @Transactional
    public FetchScoreResponse fetchOrTriggerScore(UUID userId) {
        // 1. Check cache
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
    public PrismScoreResponse getLatestScore(UUID userId) {
        PrismScoreSnapshot snap = snapshotRepository
                .findTopByUserIdAndScoringStatusOrderByComputedAtDesc(userId, ScoringStatus.COMPLETE)
                .orElseThrow(() -> new PrismException(
                        "No score available yet. Please trigger a score calculation.",
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        ErrorCode.NO_SCORE_AVAILABLE));

        return PrismScoreResponse.builder()
                .snapshotId(snap.getId())
                .prismScore(snap.getFinalScore())
                .status(snap.getScoringStatus())
                .ruleSetVersion(snap.getRuleSetVersion())
                .computedAt(snap.getComputedAt())
                .expiresAt(snap.getExpiresAt())
                .isCached(snap.getExpiresAt() != null && snap.getExpiresAt().isAfter(Instant.now()))
                .build();
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
        // TODO (Phase 3): inject ScoringPipelineOrchestrator and call orchestrate(jobId)
        // This stub simulates a mock score after a short delay for hackathon demo
        try {
            Thread.sleep(2000); // simulate processing time

            ScoringJob job = scoringJobRepository.findById(jobId).orElseThrow();
            job.setStatus(JobStatus.RUNNING);
            scoringJobRepository.save(job);

            Thread.sleep(3000); // simulate scoring

            PrismScoreSnapshot snapshot = job.getSnapshot();
            snapshot.setFinalScore(650 + (int)(Math.random() * 200)); // mock: 650–850
            snapshot.setScoringStatus(ScoringStatus.COMPLETE);
            snapshot.setComputedAt(Instant.now());
            snapshot.setExpiresAt(Instant.now().plus(cacheValidityDays, ChronoUnit.DAYS));
            snapshotRepository.save(snapshot);

            job.setStatus(JobStatus.COMPLETE);
            job.setCompletedAt(Instant.now());
            scoringJobRepository.save(job);

            log.info("[MOCK SCORE] job={} score={}", jobId, snapshot.getFinalScore());
        } catch (Exception e) {
            log.error("Mock scoring pipeline failed for job={}", jobId, e);
            try {
                ScoringJob job = scoringJobRepository.findById(jobId).orElseThrow();
                job.setStatus(JobStatus.FAILED);
                job.setFailureReason(e.getMessage());
                scoringJobRepository.save(job);
            } catch (Exception ignored) {}
        }
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
