package com.prism.core.provider.llm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.llm.OllamaClient;
import com.prism.core.provider.repository.ProviderResponseRepository;
import com.prism.core.provider.repository.RawSignalRepository;
import com.prism.core.scoring.dto.response.RecommendationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderService {

    private final RawSignalRepository rawSignalRepository;
    private final ProviderResponseRepository providerResponseRepository;
    private final OllamaClient ollamaClient;

    @Value("${prism.llm.use-llm:false}")
    private boolean useLlm;

    private static final List<String> DIMENSION_KEYS = List.of(
            "D1_INCOME", "D2_ACTIVITY", "D3_SPENDING", "D4_SOCIAL",
            "D5_RISK", "D6_IDENTITY", "D7_TEMPORAL"
    );

    /**
     * Entry point — branches on the useLlm feature flag.
     */
    public List<RecommendationDto> generateRecommendations(UUID userId, JsonNode latestScoreBreakdown) {
        if (useLlm) {
            log.info("[LLM] useLlm=true — calling Ollama for user={}", userId);
            try {
                return generateViaOllama(userId, latestScoreBreakdown);
            } catch (Exception e) {
                log.warn("[LLM] Ollama failed, falling back to mock. Error: {}", e.getMessage());
                return generateMock(userId, latestScoreBreakdown);
            }
        } else {
            log.info("[LLM] useLlm=false — using mock recommendations for user={}", userId);
            return generateMock(userId, latestScoreBreakdown);
        }
    }

    // ─── Ollama Real Implementation ────────────────────────────────────────────

    private List<RecommendationDto> generateViaOllama(UUID userId, JsonNode breakdown) {
        String prompt = buildPrompt(userId, breakdown);

        log.debug("[LLM PROMPT] user={} >>>\n{}\n<<<", userId, prompt);

        String rawResponse = ollamaClient.generate(prompt);

        log.debug("[LLM RESPONSE] user={} >>>\n{}\n<<<", userId, rawResponse);

        return parseOllamaResponse(rawResponse);
    }

    private String buildPrompt(UUID userId, JsonNode breakdown) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are PRISM, a credit-score AI for Indian gig workers. ");
        sb.append("Give exactly 7 short, practical recommendations — one per dimension.\n");
        sb.append("D1=Income D2=Gig-Activity D3=Spending D4=Ratings D5=Risk D6=Identity D7=Consistency\n\n");

        if (breakdown != null && breakdown.isArray()) {
            sb.append("SCORES (actual/max):\n");
            for (JsonNode dim : breakdown) {
                String key   = dim.path("dimensionKey").asText("?");
                double score = dim.path("score").asDouble(0);
                double max   = dim.path("maxScore").asDouble(100);
                double pct   = dim.path("performanceRatio").asDouble(0) * 100;
                sb.append(String.format("%s %.0f/%.0f (%.0f%%)\n", key, score, max, pct));
            }
            sb.append("\n");
        }

        sb.append("Reply ONLY with a JSON array — no markdown, no extra text:\n");
        sb.append("[{\"dimension\":\"D1_INCOME\",\"title\":\"...\",\"description\":\"...\"},");
        sb.append("{\"dimension\":\"D2_ACTIVITY\",\"title\":\"...\",\"description\":\"...\"},");
        sb.append("{\"dimension\":\"D3_SPENDING\",\"title\":\"...\",\"description\":\"...\"},");
        sb.append("{\"dimension\":\"D4_SOCIAL\",\"title\":\"...\",\"description\":\"...\"},");
        sb.append("{\"dimension\":\"D5_RISK\",\"title\":\"...\",\"description\":\"...\"},");
        sb.append("{\"dimension\":\"D6_IDENTITY\",\"title\":\"...\",\"description\":\"...\"},");
        sb.append("{\"dimension\":\"D7_TEMPORAL\",\"title\":\"...\",\"description\":\"...\"}]");
        sb.append("\nKeep each description under 2 sentences. Use ₹ for currency.");

        return sb.toString();
    }

    private List<RecommendationDto> parseOllamaResponse(String raw) {
        // Extract JSON array from the response (model may wrap it in markdown)
        String jsonStr = extractJson(raw);
        Instant now = Instant.now();
        List<RecommendationDto> result = new ArrayList<>();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode arr = mapper.readTree(jsonStr);
            if (!arr.isArray()) throw new RuntimeException("Expected JSON array");

            for (JsonNode node : arr) {
                String dimension = node.path("dimension").asText("GENERAL");
                String title     = node.path("title").asText("Improve your score");
                String desc      = node.path("description").asText("");
                result.add(RecommendationDto.builder()
                        .category(dimension)
                        .title(title)
                        .description(desc)
                        .createdAt(now)
                        .build());
            }
        } catch (Exception e) {
            log.error("[LLM] Failed to parse Ollama response: {}", e.getMessage());
            log.debug("[LLM] Raw response was: {}", raw);
            // Fall back to mock
            return generateFallbackRecs();
        }

        // Ensure we always have 7 recommendations (one per dimension)
        if (result.size() < 7) {
            Set<String> covered = new HashSet<>();
            result.forEach(r -> covered.add(r.getCategory()));
            for (String dim : DIMENSION_KEYS) {
                if (!covered.contains(dim) && result.size() < 7) {
                    result.add(RecommendationDto.builder()
                            .category(dim)
                            .title("Keep improving " + dim.split("_")[1])
                            .description("Continue building good habits in this area to improve your PRISM score.")
                            .createdAt(now)
                            .build());
                }
            }
        }

        return result;
    }

    private String extractJson(String raw) {
        // Try to grab the JSON array between first [ and last ]
        int start = raw.indexOf('[');
        int end   = raw.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        // If model wrapped in ```json ... ```
        Pattern p = Pattern.compile("```(?:json)?\\s*([\\s\\S]+?)\\s*```");
        Matcher m = p.matcher(raw);
        if (m.find()) return m.group(1).trim();
        return raw.trim();
    }

    // ─── Mock Implementation (useLlm=false) ───────────────────────────────────

    private List<RecommendationDto> generateMock(UUID userId, JsonNode latestScoreBreakdown) {
        log.info("[LLM MOCK] Generating mock recommendations for user={}", userId);
        Instant now = Instant.now();
        List<RecommendationDto> recs = new ArrayList<>();

        // Priority 1: if score breakdown is available, always return all 7 dimension recs
        if (latestScoreBreakdown != null && latestScoreBreakdown.isArray()) {
            for (JsonNode dim : latestScoreBreakdown) {
                String key   = dim.path("dimensionKey").asText();
                double ratio = dim.path("performanceRatio").asDouble(1.0);
                recs.add(buildMockRecForDimension(key, ratio, now));
            }
            // If employer data is missing, add it as a bonus tip
            boolean hasEmployer = userId != null &&
                    providerResponseRepository.existsByUserIdAndProviderType(userId, ProviderType.PLATFORM);
            if (!hasEmployer) {
                log.info("[LLM MOCK] user={} has no employer data — appending connect-platform tip", userId);
            }
            return recs;
        }

        // Priority 2: No score yet — return data-collection prompts
        boolean hasSms      = userId != null && rawSignalRepository.existsByUserId(userId);
        boolean hasEmployer = userId != null &&
                providerResponseRepository.existsByUserIdAndProviderType(userId, ProviderType.PLATFORM);

        if (!hasSms) {
            recs.add(RecommendationDto.builder().category("PROFILE").title("Link Bank Account")
                    .description("Linking your bank account lets us analyse your income and spending patterns to build a stronger PRISM score.")
                    .createdAt(now).build());
        }
        if (!hasEmployer) {
            recs.add(RecommendationDto.builder().category("PROFILE").title("Connect Work Platform")
                    .description("Connect your delivery or ride-sharing app (Zomato, Uber, Blinkit etc.) to include your gig earnings in your score.")
                    .createdAt(now).build());
        }

        // Fallback: generic recs for all 7 dimensions at neutral level
        if (recs.isEmpty()) {
            for (String key : DIMENSION_KEYS) {
                recs.add(buildMockRecForDimension(key, 0.5, now));
            }
        }

        return recs;
    }

    private RecommendationDto buildMockRecForDimension(String key, double ratio, Instant now) {
        return switch (key) {
            case "D1_INCOME" -> RecommendationDto.builder().category(key).title("Boost Your Weekly Earnings")
                    .description("Try to complete 10% more deliveries this week. Consistent weekly income above ₹3,000 significantly improves your income score.")
                    .createdAt(now).build();
            case "D2_ACTIVITY" -> RecommendationDto.builder().category(key).title("Increase Delivery Volume")
                    .description("Complete 15 more deliveries this month and maintain a consistent daily schedule to boost your activity score.")
                    .createdAt(now).build();
            case "D3_SPENDING" -> RecommendationDto.builder().category(key).title("Maintain Bank Balance")
                    .description("Avoid dropping your bank balance below ₹1,000 at month-end. Consistent savings behavior improves your spending score.")
                    .createdAt(now).build();
            case "D4_SOCIAL" -> RecommendationDto.builder().category(key).title("Improve Customer Rating")
                    .description("Focus on timely deliveries and polite customer interactions to raise your average rating above 4.0.")
                    .createdAt(now).build();
            case "D5_RISK" -> RecommendationDto.builder().category(key).title("Remove Risky Apps")
                    .description("Uninstall VPN, fake GPS, or loan apps from your device to improve your risk score.")
                    .createdAt(now).build();
            case "D6_IDENTITY" -> RecommendationDto.builder().category(key).title("Complete Your KYC")
                    .description("Complete full PAN verification to unlock better credit scoring and improve your identity tier.")
                    .createdAt(now).build();
            case "D7_TEMPORAL" -> RecommendationDto.builder().category(key).title("Stay Consistent")
                    .description("Avoid long gaps between work sessions. Working regularly for 3+ months in a row improves your temporal consistency score.")
                    .createdAt(now).build();
            default -> RecommendationDto.builder().category(key).title("Improve " + key)
                    .description("Work on improving your performance in this area to boost your PRISM score.")
                    .createdAt(now).build();
        };
    }

    private List<RecommendationDto> generateFallbackRecs() {
        return generateMock(null, null);
    }
}
