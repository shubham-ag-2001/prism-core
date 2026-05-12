package com.prism.core.provider.pan.service;

import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.entity.RawSignal;
import com.prism.core.provider.repository.RawSignalRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock PAN/Credit Bureau signal service.
 *
 * In production this would call a credit bureau (CIBIL/Experian) or
 * government KYC API (Karza, Signzy) using the verified PAN number to retrieve:
 *  - Prior loan default history  → RSK01
 *  - Longest on-time payment streak → TMP04
 *  - Active debt / debt-to-income ratio → D3-SPD10
 *
 * Signals are stored as RawSignal rows with provider_type=PAN.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PanSignalService {

    private final RawSignalRepository rawSignalRepository;
    private final UserRepository      userRepository;

    /**
     * Called after successful PAN OTP verification during onboarding.
     * Fetches (mock) bureau data and persists as PAN-typed signals.
     */
    @Transactional
    public void fetchAndPersistPanSignals(UUID userId, String panNumber) {
        log.info("[PAN MOCK] Fetching bureau signals for user={} pan={}", userId, panNumber);

        User user = userRepository.getReferenceById(userId);

        // ── Mock bureau data ──────────────────────────────────────────────────
        // In production: call Karza/Signzy/Experian with panNumber and get back
        // credit history, active loans, EMI track record, defaults, etc.
        boolean priorDefault         = false;       // RSK01: no prior default (good citizen)
        double longestOntimeStreak   = 12.0;        // TMP04: 12 months consecutive on-time
        double debtToIncomeRatio     = 0.15;        // D3-SPD10: 15% of income goes to EMI

        List<RawSignal> signals = new ArrayList<>();
        signals.add(signal(user, "rsk01_prior_default",          String.valueOf(priorDefault)));
        signals.add(signal(user, "longest_ontime_payment_streak", String.valueOf(longestOntimeStreak)));
        signals.add(signal(user, "debt_to_income_ratio",          String.valueOf(debtToIncomeRatio)));

        rawSignalRepository.saveAll(signals);
        log.info("[PAN MOCK] Persisted {} PAN signals for user={}", signals.size(), userId);
    }

    private RawSignal signal(User user, String key, String value) {
        return RawSignal.builder()
                .user(user)
                .providerType(ProviderType.PAN)
                .signalKey(key)
                .signalValue(value)
                .build();
    }
}
