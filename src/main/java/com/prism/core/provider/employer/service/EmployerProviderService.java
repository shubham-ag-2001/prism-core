package com.prism.core.provider.employer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.common.enums.ProviderType;
import com.prism.core.common.exception.PrismException;
import com.prism.core.provider.employer.dto.EmployerDataResponse;
import com.prism.core.provider.employer.dto.EmployerPlatformDto;
import com.prism.core.provider.employer.entity.EmployerPlatform;
import com.prism.core.provider.employer.repository.EmployerPlatformRepository;
import com.prism.core.provider.entity.ProviderResponse;
import com.prism.core.provider.repository.ProviderResponseRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerProviderService {

    private final EmployerPlatformRepository  employerPlatformRepository;
    private final ProviderResponseRepository  providerResponseRepository;
    private final UserRepository              userRepository;
    private final ObjectMapper                objectMapper;

    // ─── List Platforms ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmployerPlatformDto> getActivePlatforms() {
        return employerPlatformRepository.findAllByActiveTrue()
                .stream()
                .sorted((a, b) -> {
                    if ("GRAB".equals(a.getPlatformKey())) return -1;
                    if ("GRAB".equals(b.getPlatformKey())) return 1;
                    return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                })
                .map(p -> EmployerPlatformDto.builder()
                        .platformKey(p.getPlatformKey())
                        .displayName(p.getDisplayName())
                        .category(p.getCategory())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Fetch & Persist Employer Data ───────────────────────────────────────

    @Transactional
    public EmployerDataResponse fetchEmployerData(UUID userId, String platformKey) {
        EmployerPlatform platform = employerPlatformRepository
                .findByPlatformKeyAndActiveTrue(platformKey.toUpperCase())
                .orElseThrow(() -> PrismException.notFound(
                        "Employer platform '" + platformKey + "' not found or not supported"));

        log.info("[EMPLOYER] Fetching data for userId={} platform={}", userId, platformKey);

        // Build full mock response (Phase 4: replace with real platform API call)
        EmployerDataResponse response = buildMockResponse(platform);

        // ── Persist employer data to DB ───────────────────────────────────────
        try {
            User user = userRepository.getReferenceById(userId);
            String responseJson = objectMapper.writeValueAsString(response);

            ProviderResponse record = ProviderResponse.builder()
                    .user(user)
                    .providerType(ProviderType.PLATFORM)
                    .platformKey(platform.getPlatformKey())
                    .responseJson(responseJson)
                    .httpStatus(200)
                    .isMock(true)
                    .build();
            providerResponseRepository.save(record);

            log.info("[EMPLOYER] Persisted employer data for userId={} platform={}", userId, platformKey);
        } catch (Exception e) {
            log.warn("[EMPLOYER] Failed to persist employer data for user={}: {}", userId, e.getMessage());
        }

        return response;
    }

    // ─── Full Mock Builder (covers all D2, D4, D7 platform fields) ───────────

    private EmployerDataResponse buildMockResponse(EmployerPlatform platform) {
        Random rng = new Random();

        int    tenureDays             = 180 + rng.nextInt(900);     // 6 months to ~3.5 years
        int    totalOrders            = 500 + rng.nextInt(3000);
        int    last30Orders           = 20  + rng.nextInt(120);
        double overallRating          = Math.round((3.8 + rng.nextDouble() * 1.2) * 10.0) / 10.0;
        double ratingLast30d          = Math.round((overallRating + rng.nextDouble() * 0.2 - 0.1) * 10.0) / 10.0;
        double ratingLast90d          = Math.round((overallRating - rng.nextDouble() * 0.1) * 10.0) / 10.0;
        double cancellationRate       = Math.round(rng.nextDouble() * 0.08 * 1000.0) / 1000.0;
        double completionRate         = Math.round((1.0 - cancellationRate) * 100.0 * 10.0) / 10.0;
        int    earnings90d            = 15000 + rng.nextInt(60000);

        // Activity signals
        double ordersPerActiveDay     = Math.round((last30Orders / 22.0) * 10.0) / 10.0;
        double activeDaysPerWeek      = Math.round((3.5 + rng.nextDouble() * 3.0) * 10.0) / 10.0;
        double accountAgeActiveRatio  = Math.min(1.0, Math.round((totalOrders / (tenureDays * 4.0)) * 100.0) / 100.0);
        int    longestInactivityDays  = 3 + rng.nextInt(14);
        int    gapFrequency90d        = rng.nextInt(5);
        double peakHourRate           = Math.round((0.4 + rng.nextDouble() * 0.5) * 100.0) / 100.0;
        boolean cancellationSpike     = cancellationRate > 0.06;

        // Social signals
        double repeatCustomerRatio    = Math.round((0.10 + rng.nextDouble() * 0.25) * 100.0) / 100.0;
        int    unresolvedDisputes     = rng.nextInt(3);
        boolean disputeSpike          = unresolvedDisputes > 1;
        double responseRateToOffers   = Math.round((0.55 + rng.nextDouble() * 0.40) * 100.0) / 100.0;

        // Temporal signals
        double consecutiveMonths      = Math.min(tenureDays / 30.0, 24.0);
        double completionRateDrift    = Math.round((-2.0 + rng.nextDouble() * 3.0) * 10.0) / 10.0; // neg = improving
        double reliabilityScore       = Math.min(95.0, consecutiveMonths * 3.5);

        return EmployerDataResponse.builder()
                .platformKey(platform.getPlatformKey())
                .platformName(platform.getDisplayName())
                .accountStatus("ACTIVE")
                .employerCategory(platform.getCategory())
                .isMock(true)
                // D2
                .accountTenureDays(tenureDays)
                .orderCompletionRate(completionRate)
                .totalCompletedOrders(totalOrders)
                .activeOrdersLast30Days(last30Orders)
                .ordersPerActiveDay(ordersPerActiveDay)
                .activeDaysPerWeek(activeDaysPerWeek)
                .accountAgeActiveRatio(accountAgeActiveRatio)
                .longestInactivityStreakDays(longestInactivityDays)
                .gapFrequency90d(gapFrequency90d)
                .peakHourParticipationRate(peakHourRate)
                .cancellationRate(cancellationRate)
                .cancellationSpikeFlag(cancellationSpike)
                // D4
                .overallRating(overallRating)
                .ratingLast30d(ratingLast30d)
                .ratingLast90d(ratingLast90d)
                .repeatCustomerRatio(repeatCustomerRatio)
                .unresolvedDisputeCount(unresolvedDisputes)
                .disputeSpikeFlag(disputeSpike)
                .responseRateToOffers(responseRateToOffers)
                // D7
                .consecutiveActiveMonths(consecutiveMonths)
                .completionRateDrift(completionRateDrift)
                .tenureWeightedReliabilityScore(reliabilityScore)
                // Financials
                .totalEarningsLast90DaysRupees(earnings90d)
                .build();
    }
}
