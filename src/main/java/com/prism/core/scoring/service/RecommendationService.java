package com.prism.core.scoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.common.enums.ScoringStatus;
import com.prism.core.scoring.dto.response.RecommendationDto;
import com.prism.core.scoring.dto.response.RecommendationJobStatusResponse;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import com.prism.core.scoring.entity.RecommendationJob;
import com.prism.core.scoring.entity.ScoreRecommendation;
import com.prism.core.scoring.repository.PrismScoreSnapshotRepository;
import com.prism.core.scoring.repository.RecommendationJobRepository;
import com.prism.core.scoring.repository.ScoreRecommendationRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class RecommendationService {

    private final ScoreRecommendationRepository scoreRecommendationRepository;
    private final RecommendationJobRepository   recommendationJobRepository;
    private final PrismScoreSnapshotRepository  snapshotRepository;
    private final UserRepository                userRepository;
    private final RecommendationAsyncWorker     asyncWorker;   // ← separate bean, @Async works
    private final ObjectMapper                  objectMapper;

    // ── Get cached recommendations ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RecommendationDto> getOrGenerateRecommendations(UUID userId) {
        Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        List<ScoreRecommendation> recentRecs = scoreRecommendationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, oneDayAgo);

        if (!recentRecs.isEmpty()) {
            log.info("Returning {} cached recommendations for user={}", recentRecs.size(), userId);
            return recentRecs.stream().map(this::toDto).collect(Collectors.toList());
        }

        log.info("No recent recommendations for user={}. Trigger /recommendations/generate first.", userId);
        return List.of();
    }

    // ── Trigger Async Job ──────────────────────────────────────────────────────

    /**
     * Creates a RecommendationJob row, then hands off to the async worker bean.
     * Returns the jobId immediately — the HTTP response is sent before Ollama is called.
     */
    @Transactional
    public UUID triggerAsyncGeneration(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        RecommendationJob job = RecommendationJob.builder()
                .user(user)
                .status(RecommendationJob.Status.PENDING)
                .build();
        job = recommendationJobRepository.save(job);

        UUID jobId = job.getId();
        log.info("[REC] Created recommendation job={} for user={}", jobId, userId);

        // Resolve breakdown BEFORE handing off to async thread (so it's in the same TX)
        JsonNode breakdown = resolveBreakdown(userId);

        // Delegate to separate bean — @Async proxy is honoured here (cross-bean call)
        asyncWorker.execute(userId, jobId, breakdown);

        return jobId;   // ← returns immediately, Ollama runs in background
    }

    // ── Poll Job Status ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RecommendationJobStatusResponse getJobStatus(UUID jobId) {
        RecommendationJob job = recommendationJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        List<RecommendationDto> recs = List.of();
        if (job.getStatus() == RecommendationJob.Status.DONE) {
            recs = scoreRecommendationRepository
                    .findByJobId(jobId)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }

        return RecommendationJobStatusResponse.builder()
                .jobId(jobId)
                .status(job.getStatus().name())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .recommendations(recs.isEmpty() ? null : recs)
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private JsonNode resolveBreakdown(UUID userId) {
        Optional<PrismScoreSnapshot> latest = snapshotRepository
                .findTopByUserIdAndScoringStatusOrderByComputedAtDesc(userId, ScoringStatus.COMPLETE);
        if (latest.isPresent() && latest.get().getDimensionBreakdownJson() != null) {
            try {
                return objectMapper.readTree(latest.get().getDimensionBreakdownJson());
            } catch (Exception e) {
                log.warn("[REC] Failed to parse dimension breakdown for user={}", userId);
            }
        }
        return null;
    }

    private RecommendationDto toDto(ScoreRecommendation entity) {
        return RecommendationDto.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .source(entity.getSource())      // ← LLM or MOCK
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
