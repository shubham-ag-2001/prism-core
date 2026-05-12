package com.prism.core.provider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.employer.dto.EmployerDataResponse;
import com.prism.core.provider.entity.ProviderResponse;
import com.prism.core.provider.entity.RawSignal;
import com.prism.core.provider.repository.ProviderResponseRepository;
import com.prism.core.provider.repository.RawSignalRepository;
import com.prism.core.scoring.engine.model.ScoringInput;
import com.prism.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregates raw signals and provider responses into a unified ScoringInput
 * ready for the scoring engine.
 *
 * Signal priority:
 *  1. Employer Platform API  →  D2 Activity, D4 Social
 *  2. SMS Extractor          →  D1 Income, D3 Spending
 *  3. Defaults               →  used when no real signal exists (hackathon fallback)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignalAggregatorService {

    private final RawSignalRepository      rawSignalRepository;
    private final ProviderResponseRepository providerResponseRepository;
    private final ObjectMapper             objectMapper;

    public ScoringInput aggregate(User user) {
        UUID userId = user.getId();
        boolean hasSmsSignals = rawSignalRepository.existsByUserId(userId);
        boolean hasEmployerData = providerResponseRepository.existsByUserIdAndProviderType(userId, ProviderType.PLATFORM);

        log.info("[Aggregator] userId={} hasSmsSignals={} hasEmployerData={}", userId, hasSmsSignals, hasEmployerData);

        // Read SMS signals
        Map<String, String> sms = readSmsSignals(userId);

        // Read latest employer response
        EmployerDataResponse employer = readLatestEmployerData(userId);

        return buildScoringInput(userId.toString(), sms, employer);
    }

    // ── SMS Signal Reader ─────────────────────────────────────────────────────

    private Map<String, String> readSmsSignals(UUID userId) {
        List<RawSignal> signals = rawSignalRepository
                .findByUserIdAndProviderTypeOrderByCreatedAtDesc(userId, ProviderType.SMS);

        // For each signal key, take the most recent value
        return signals.stream()
                .collect(Collectors.toMap(
                        RawSignal::getSignalKey,
                        RawSignal::getSignalValue,
                        (existing, replacement) -> existing   // keep latest (first due to DESC order)
                ));
    }

    // ── Employer Data Reader ──────────────────────────────────────────────────

    private EmployerDataResponse readLatestEmployerData(UUID userId) {
        // Take the most recently fetched employer response regardless of platform
        List<ProviderResponse> responses = providerResponseRepository
                .findByUserIdAndProviderTypeOrderByFetchedAtDesc(userId, ProviderType.PLATFORM);

        if (responses.isEmpty()) return null;

        try {
            return objectMapper.readValue(responses.get(0).getResponseJson(), EmployerDataResponse.class);
        } catch (Exception e) {
            log.warn("[Aggregator] Failed to parse employer response: {}", e.getMessage());
            return null;
        }
    }

    // ── ScoringInput Builder ──────────────────────────────────────────────────

    private ScoringInput buildScoringInput(String userId, Map<String, String> sms, EmployerDataResponse employer) {
        ScoringInput.ScoringInputBuilder b = ScoringInput.builder().userId(userId);

        // ─── D1 — Income (primarily from SMS) ─────────────────────────────────
        b.avgWeeklyCredit90d(doubleOrDefault(sms, "avg_weekly_credit_sms_90d", 3500.0));
        b.incomeSpikeRatio(doubleOrDefault(sms, "income_spike_ratio", 1.15));
        b.incomeCv(doubleOrDefault(sms, "income_cv", 0.25));
        b.incomeGrowthFebToMar(doubleOrDefault(sms, "income_growth_feb_to_mar", 3.0));
        b.incomeGrowthMarToApr(doubleOrDefault(sms, "income_growth_mar_to_apr", 2.5));
        b.lowIncomeWeekCount90d(doubleOrDefault(sms, "low_income_week_count_90d", 2.0));
        b.avgRecoveryWeeks(doubleOrDefault(sms, "avg_recovery_weeks", 2.0));
        b.seasonalAdjustmentFactor(doubleOrDefault(sms, "seasonal_adjustment_factor", 0.96));
        b.platformIncomeSourceCount(doubleOrDefault(sms, "platform_income_source_count", employer != null ? 1.0 : 1.0));
        b.hasNonGigIncome(boolOrDefault(sms, "has_non_gig_income", false));
        b.incomeLast30d(doubleOrDefault(sms, "income_last_30d", employer != null ? employer.getTotalEarningsLast90DaysRupees() / 3.0 : 15000.0));
        b.incomeLast90d(doubleOrDefault(sms, "income_last_90d", employer != null ? (double) employer.getTotalEarningsLast90DaysRupees() : 42000.0));

        // ─── D2 — Activity (primarily from employer platform) ─────────────────
        if (employer != null) {
            b.platformTenureDays((double) employer.getAccountTenureDays());
            b.orderCompletionRate((1.0 - employer.getCancellationRate()) * 100.0);
            b.ordersPerActiveDay(employer.getActiveOrdersLast30Days() > 0
                    ? employer.getActiveOrdersLast30Days() / 30.0 : 5.0);
            b.activeDaysPerWeek(Math.min(7.0, employer.getActiveOrdersLast30Days() / 4.0));
            b.accountAgeActiveRatio(Math.min(1.0, employer.getTotalCompletedOrders() /
                    Math.max(1.0, employer.getAccountTenureDays() * 5.0)));
            b.lateCancellationRatio(employer.getCancellationRate() * 100.0);
        } else {
            b.platformTenureDays(400.0);
            b.orderCompletionRate(88.0);
            b.ordersPerActiveDay(8.0);
            b.activeDaysPerWeek(5.0);
            b.accountAgeActiveRatio(0.60);
            b.lateCancellationRatio(0.6);
        }
        b.longestInactivityStreakDays(5.0);
        b.gapFrequency90d(2.0);
        b.peakHourParticipationRate(0.55);
        b.cancellationSpikeFlag(false);

        // ─── D3 — Spending (from SMS) ─────────────────────────────────────────
        b.walletTopupFrequency(doubleOrDefault(sms, "wallet_topup_frequency", 6.0));
        b.spendToEarnRatio(doubleOrDefault(sms, "spend_to_earn_ratio", 0.72));
        b.avgMonthEndBalance(doubleOrDefault(sms, "avg_month_end_balance_sms", 5500.0));
        b.negativeBalanceFlag(boolOrDefault(sms, "negative_balance_flag", false));
        b.spendVolatilityIndex(doubleOrDefault(sms, "spend_volatility_index", 0.25));
        b.discretionarySpendRatio(doubleOrDefault(sms, "discretionary_spend_ratio", 0.18));
        b.gamblingFantasySpendFlag(boolOrDefault(sms, "gambling_fantasy_spend_flag", false));
        b.largeTransactionFrequency(doubleOrDefault(sms, "large_transaction_frequency", 1.5));
        b.missedInstalmentCount90d(doubleOrDefault(sms, "missed_instalment_count_90d", 0.0));
        b.activeInstalmentCount(doubleOrDefault(sms, "active_instalment_count", 1.0));
        b.monthlyRemittanceAmount(doubleOrDefault(sms, "monthly_remittance_amount", 2500.0));
        b.remittanceConsistencyScore(doubleOrDefault(sms, "remittance_consistency_score", 75.0));
        b.spendingLast30d(doubleOrDefault(sms, "spending_last_30d", 11000.0));
        b.spendingLast90d(doubleOrDefault(sms, "spending_last_90d", 33000.0));
        b.debtToIncomeRatio(doubleOrDefault(sms, "debt_to_income_ratio", 0.15));

        // ─── D4 — Social (from employer platform) ─────────────────────────────
        if (employer != null) {
            b.avgCustomerRating(employer.getOverallRating());
            b.ratingLast30d(employer.getOverallRating());
            b.ratingLast90d(employer.getOverallRating());
            b.repeatCustomerRatio(0.18);  // not provided by employer API yet
        } else {
            b.avgCustomerRating(4.3);
            b.ratingLast30d(4.35);
            b.ratingLast90d(4.3);
            b.repeatCustomerRatio(0.18);
        }
        b.unresolvedDisputeCount(0.0);
        b.disputeSpikeFlag(false);
        b.responseRateToOffers(0.65);

        // ─── D5 — Risk (device signals — not yet collected, use safe defaults) ─
        b.rsk01PriorDefault(false);
        b.unusualLoginLocationFlag(false);
        b.multipleDeviceLoginFlag(false);
        b.rsk04MultipleAccountDetected(false);
        b.hasLocationSpoofApp(false);
        b.hasRecordFabricationTools(false);
        b.isDeviceRooted(false);
        b.hasUnofficialApks(false);
        b.creditHungryScore(15.0);
        b.isDeveloperModeOn(false);

        // ─── D6 — Identity (from onboarding) ──────────────────────────────────
        // PAN verified = tier 2, otherwise tier 1
        b.identityVerificationTier(2);
        b.phoneVintageDays(500.0);

        // ─── D7 — Temporal (derived from employer tenure if available) ─────────
        double tenureMonths = employer != null ? employer.getAccountTenureDays() / 30.0 : 5.0;
        b.consecutiveActiveMonths(Math.min(tenureMonths, 24.0));
        b.incomeStabilityWindows(5.0);
        b.completionRateDrift(-1.5);
        b.spendRatioDrift(2.0);
        b.longestOntimePaymentStreak(8.0);
        b.postDisruptionIncomeRecoveryDays(8.0);
        b.postDisruptionActivityRecoveryDays(6.0);
        b.tenureWeightedReliabilityScore(employer != null
                ? Math.min(95.0, tenureMonths * 3.5) : 72.0);

        return b.build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Double doubleOrDefault(Map<String, String> map, String key, Double defaultValue) {
        String val = map.get(key);
        if (val == null || val.isBlank()) return defaultValue;
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private Boolean boolOrDefault(Map<String, String> map, String key, Boolean defaultValue) {
        String val = map.get(key);
        if (val == null || val.isBlank()) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }
}
