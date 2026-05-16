package com.prism.core.scoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.prism.core.provider.llm.service.LlmProviderService;
import com.prism.core.scoring.dto.response.RecommendationDto;
import com.prism.core.scoring.entity.RecommendationJob;
import com.prism.core.scoring.entity.ScoreRecommendation;
import com.prism.core.scoring.repository.RecommendationJobRepository;
import com.prism.core.scoring.repository.ScoreRecommendationRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Separate bean so that @Async works correctly.
 * Spring @Async uses AOP proxies — self-invocation (this.method()) bypasses the proxy,
 * so the async method MUST live in a different bean from the one that calls it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationAsyncWorker {

    private final LlmProviderService            llmProviderService;
    private final RecommendationJobRepository   recommendationJobRepository;
    private final ScoreRecommendationRepository scoreRecommendationRepository;
    private final UserRepository                userRepository;

    /**
     * Called by RecommendationService.triggerAsyncGeneration().
     * Runs in a separate thread (Spring async thread pool).
     * Returns immediately to the caller — job status is tracked via the DB.
     */
    @Async
    public void execute(UUID userId, UUID jobId, JsonNode breakdown) {
        log.info("[REC] Async worker started for job={} user={}", jobId, userId);
        updateStatus(jobId, RecommendationJob.Status.RUNNING, null);

        try {
            List<RecommendationDto> dtos = llmProviderService.generateRecommendations(userId, breakdown);
            saveRecommendations(userId, jobId, dtos);
            updateStatus(jobId, RecommendationJob.Status.DONE, null);
            log.info("[REC] job={} DONE — {} recommendations saved", jobId, dtos.size());
        } catch (Exception e) {
            log.error("[REC] job={} FAILED: {}", jobId, e.getMessage(), e);
            updateStatus(jobId, RecommendationJob.Status.FAILED, e.getMessage());
        }
    }

    @Transactional
    protected void saveRecommendations(UUID userId, UUID jobId, List<RecommendationDto> dtos) {
        User user = userRepository.getReferenceById(userId);
        RecommendationJob jobRef = recommendationJobRepository.getReferenceById(jobId);

        scoreRecommendationRepository.deleteByUserId(userId);

        List<ScoreRecommendation> entities = dtos.stream().map(dto -> ScoreRecommendation.builder()
                .user(user)
                .category(dto.getCategory())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .job(jobRef)
                .source(dto.getSource() != null ? dto.getSource() : "LLM")
                .build()).collect(Collectors.toList());

        scoreRecommendationRepository.saveAll(entities);
    }

    @Transactional
    protected void updateStatus(UUID jobId, RecommendationJob.Status status, String error) {
        recommendationJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setErrorMessage(error);
            recommendationJobRepository.save(job);
        });
    }
}
