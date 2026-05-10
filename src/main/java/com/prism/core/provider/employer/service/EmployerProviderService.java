package com.prism.core.provider.employer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.common.exception.PrismException;
import com.prism.core.provider.employer.dto.EmployerDataResponse;
import com.prism.core.provider.employer.dto.EmployerPlatformDto;
import com.prism.core.provider.employer.entity.EmployerPlatform;
import com.prism.core.provider.employer.repository.EmployerPlatformRepository;
import com.prism.core.scoring.repository.PrismScoreSnapshotRepository;
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

    private final EmployerPlatformRepository employerPlatformRepository;
    private final ObjectMapper objectMapper;

    // ─── List Platforms ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmployerPlatformDto> getActivePlatforms() {
        return employerPlatformRepository.findAllByActiveTrue()
                .stream()
                .map(p -> EmployerPlatformDto.builder()
                        .platformKey(p.getPlatformKey())
                        .displayName(p.getDisplayName())
                        .category(p.getCategory())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Fetch Employer Data ──────────────────────────────────────────────────

    @Transactional
    public EmployerDataResponse fetchEmployerData(UUID userId, String platformKey) {
        EmployerPlatform platform = employerPlatformRepository
                .findByPlatformKeyAndActiveTrue(platformKey.toUpperCase())
                .orElseThrow(() -> PrismException.notFound(
                        "Employer platform '" + platformKey + "' not found or not supported"));

        log.info("[MOCK EMPLOYER] Fetching data for userId={} platform={}", userId, platformKey);

        // Single mock implementation — realistic randomised data for any platform
        return buildMockResponse(platform);
    }

    // ─── Mock Builder ─────────────────────────────────────────────────────────

    private EmployerDataResponse buildMockResponse(EmployerPlatform platform) {
        Random rng = new Random();

        int tenureDays       = 180 + rng.nextInt(900);      // 6 months to 3.5 years
        int totalOrders      = 500 + rng.nextInt(3000);
        int last30Orders     = 20 + rng.nextInt(120);
        double rating        = Math.round((3.8 + rng.nextDouble() * 1.2) * 10.0) / 10.0;
        double cancelRate    = Math.round(rng.nextDouble() * 0.10 * 1000.0) / 1000.0;
        int earnings90d      = 15000 + rng.nextInt(60000);

        return EmployerDataResponse.builder()
                .platformKey(platform.getPlatformKey())
                .platformName(platform.getDisplayName())
                .accountStatus("ACTIVE")
                .accountTenureDays(tenureDays)
                .overallRating(rating)
                .totalCompletedOrders(totalOrders)
                .activeOrdersLast30Days(last30Orders)
                .cancellationRate(cancelRate)
                .totalEarningsLast90DaysRupees(earnings90d)
                .employerCategory(platform.getCategory())
                .isMock(true)
                .build();
    }
}
