package com.prism.core.provider.llm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.repository.ProviderResponseRepository;
import com.prism.core.provider.repository.RawSignalRepository;
import com.prism.core.scoring.dto.response.RecommendationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderService {

    private final RawSignalRepository rawSignalRepository;
    private final ProviderResponseRepository providerResponseRepository;

    /**
     * Mocks a call to an LLM (e.g., GPT-4) to generate recommendations.
     * In a real implementation, this would build a prompt using the user's
     * raw signals and latest score breakdown, send it to the LLM API, and parse the JSON response.
     */
    public List<RecommendationDto> generateRecommendations(UUID userId, JsonNode latestScoreBreakdown) {
        log.info("[LLM MOCK] Generating recommendations for user={}", userId);
        
        List<RecommendationDto> recs = new ArrayList<>();
        Instant now = Instant.now();

        boolean hasSms = rawSignalRepository.existsByUserId(userId);
        boolean hasEmployer = providerResponseRepository.existsByUserIdAndProviderType(userId, ProviderType.PLATFORM);

        // 1. Missing Data Recommendations
        if (!hasSms) {
            recs.add(RecommendationDto.builder()
                    .category("PROFILE")
                    .title("Link Bank Account")
                    .description("We noticed you haven't linked your bank account. Linking it allows us to analyze your financial history, which can significantly improve your PRISM score.")
                    .createdAt(now)
                    .build());
        }

        if (!hasEmployer) {
            recs.add(RecommendationDto.builder()
                    .category("PROFILE")
                    .title("Connect Work Platform")
                    .description("Connect your delivery or ride-sharing platform (like Zomato or Uber) to include your work history and earnings in your score calculation.")
                    .createdAt(now)
                    .build());
        }

        // 2. Score Breakdown Recommendations (Mock logic)
        if (latestScoreBreakdown != null && latestScoreBreakdown.has("dimensionScores")) {
            JsonNode dims = latestScoreBreakdown.get("dimensionScores");
            
            // Example logic: if Activity (D2) is low
            if (dims.has("D2_ACTIVITY") && dims.get("D2_ACTIVITY").get("weightedScore").asDouble() < 150.0) {
                recs.add(RecommendationDto.builder()
                        .category("ACTIVITY")
                        .title("Increase Delivery Volume")
                        .description("Your activity score is slightly low. Try to complete 15 more deliveries this month and maintain a consistent schedule to boost your score.")
                        .createdAt(now)
                        .build());
            }

            // Example logic: if Spending (D3) is low
            if (dims.has("D3_SPENDING") && dims.get("D3_SPENDING").get("weightedScore").asDouble() < 120.0) {
                recs.add(RecommendationDto.builder()
                        .category("SPENDING")
                        .title("Maintain Bank Balance")
                        .description("Avoid dropping your bank balance below ₹1,000 at the end of the month. Consistent savings behavior improves your spending score.")
                        .createdAt(now)
                        .build());
            }
        }

        // 3. General Fallback Recommendation
        if (recs.isEmpty()) {
            recs.add(RecommendationDto.builder()
                    .category("GENERAL")
                    .title("Keep Up the Good Work")
                    .description("Your profile looks solid! Maintain your current earning consistency and avoid missed payments to keep your score high.")
                    .createdAt(now)
                    .build());
        }

        return recs;
    }
}
