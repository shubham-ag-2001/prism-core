package com.prism.core.scoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.provider.llm.service.LlmProviderService;
import com.prism.core.scoring.dto.response.RecommendationDto;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import com.prism.core.scoring.entity.ScoreRecommendation;
import com.prism.core.scoring.repository.PrismScoreSnapshotRepository;
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
    private final PrismScoreSnapshotRepository  snapshotRepository;
    private final UserRepository                userRepository;
    private final LlmProviderService            llmProviderService;
    private final ObjectMapper                  objectMapper;

    /**
     * Gets recommendations for a user.
     * If valid recommendations exist in the DB (generated in the last 24h), returns them.
     * Otherwise, triggers the LLM generation, saves them, and returns them.
     */
    @Transactional
    public List<RecommendationDto> getOrGenerateRecommendations(UUID userId) {
        // 1. Check for recent recommendations (e.g., generated within the last 24 hours)
        Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        List<ScoreRecommendation> recentRecs = scoreRecommendationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, oneDayAgo);

        if (!recentRecs.isEmpty()) {
            log.info("Returning {} cached recommendations for user={}", recentRecs.size(), userId);
            return recentRecs.stream().map(this::toDto).collect(Collectors.toList());
        }

        // 2. No recent recommendations found -> Generate new ones
        log.info("No recent recommendations found for user={}. Triggering generation.", userId);
        return generateAndStoreRecommendations(userId);
    }

    private List<RecommendationDto> generateAndStoreRecommendations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Get latest score breakdown if available
        JsonNode breakdown = null;
        Optional<PrismScoreSnapshot> latestScore = snapshotRepository
                .findTopByUserIdAndScoringStatusOrderByComputedAtDesc(userId, com.prism.core.common.enums.ScoringStatus.COMPLETE);
        
        if (latestScore.isPresent() && latestScore.get().getDimensionBreakdownJson() != null) {
            try {
                breakdown = objectMapper.readTree(latestScore.get().getDimensionBreakdownJson());
            } catch (Exception e) {
                log.warn("Failed to parse dimension breakdown JSON for recommendations", e);
            }
        }

        // Generate recommendations via LLM mock
        List<RecommendationDto> dtos = llmProviderService.generateRecommendations(userId, breakdown);

        // Delete old recommendations
        scoreRecommendationRepository.deleteByUserId(userId);

        // Store new recommendations
        List<ScoreRecommendation> entities = dtos.stream().map(dto -> ScoreRecommendation.builder()
                .user(user)
                .category(dto.getCategory())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build()).collect(Collectors.toList());
        
        scoreRecommendationRepository.saveAll(entities);

        // Update DTOs with IDs from saved entities and created timestamps
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setId(entities.get(i).getId());
            dtos.get(i).setCreatedAt(entities.get(i).getCreatedAt());
        }

        return dtos;
    }

    private RecommendationDto toDto(ScoreRecommendation entity) {
        return RecommendationDto.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
