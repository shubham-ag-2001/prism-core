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

        // Build mock response (Phase 4: replace with real platform API call)
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

    // ─── Mock Builder ─────────────────────────────────────────────────────────

    private EmployerDataResponse buildMockResponse(EmployerPlatform platform) {
        Random rng = new Random();

        int tenureDays   = 180 + rng.nextInt(900);      // 6 months to 3.5 years
        int totalOrders  = 500 + rng.nextInt(3000);
        int last30Orders = 20  + rng.nextInt(120);
        double rating    = Math.round((3.8 + rng.nextDouble() * 1.2) * 10.0) / 10.0;
        double cancelRate= Math.round(rng.nextDouble() * 0.10 * 1000.0) / 1000.0;
        int earnings90d  = 15000 + rng.nextInt(60000);

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
