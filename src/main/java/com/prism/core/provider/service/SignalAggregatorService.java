package com.prism.core.provider.service;

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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregates all signal sources into a unified ScoringInput for the engine.
 *
 * Signal source → Dimension mapping (per spreadsheet):
 *
 *   SMS    → D1 Income, D3 Spending (+ some D7 Temporal fields)
 *   PLATFORM → D2 Activity, D4 Social, D7 Temporal (employer API)
 *   PAN    → RSK01 Prior Default, TMP04 On-time streak, D3-SPD10 Debt ratio
 *   DEVICE → D5 Risk integrity flags (RSK05)
 *   SELF   → D6 Identity (KYC tier, phone vintage — derived from onboarding)
 *
 * Priority: Real DB signal always wins. Safe defaults used when signal is absent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignalAggregatorService {

    private final RawSignalRepository        rawSignalRepository;
    private final ProviderResponseRepository providerResponseRepository;
    private final ObjectMapper               objectMapper;

    public ScoringInput aggregate(User user) {
        UUID userId = user.getId();

        // ── Read all signal types ─────────────────────────────────────────────
        Map<String, String> sms     = readSignals(userId, ProviderType.SMS);
        Map<String, String> pan     = readSignals(userId, ProviderType.PAN);
        Map<String, String> device  = readSignals(userId, ProviderType.DEVICE);
        EmployerDataResponse emp    = readLatestEmployerData(userId);

        log.info("[Aggregator] userId={} sms={} pan={} device={} hasEmployer={}",
                userId, sms.size(), pan.size(), device.size(), emp != null);

        return buildScoringInput(userId.toString(), sms, pan, device, emp);
    }

    // ── Signal Readers ────────────────────────────────────────────────────────

    private Map<String, String> readSignals(UUID userId, ProviderType type) {
        List<RawSignal> signals = rawSignalRepository
                .findByUserIdAndProviderTypeOrderByCreatedAtDesc(userId, type);
        // Most-recent value wins for each key (DESC order so first occurrence = latest)
        return signals.stream()
                .collect(Collectors.toMap(
                        RawSignal::getSignalKey,
                        RawSignal::getSignalValue,
                        (existing, replacement) -> existing
                ));
    }

    private EmployerDataResponse readLatestEmployerData(UUID userId) {
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

    private ScoringInput buildScoringInput(String userId,
                                           Map<String, String> sms,
                                           Map<String, String> pan,
                                           Map<String, String> device,
                                           EmployerDataResponse emp) {

        ScoringInput.ScoringInputBuilder b = ScoringInput.builder().userId(userId);

        // ─── D1 — Income [SOURCE: SMS] ─────────────────────────────────────────
        b.avgWeeklyCredit90d(          dbl(sms, "avg_weekly_credit_sms_90d",    3500.0));
        b.incomeSpikeRatio(            dbl(sms, "income_spike_ratio",           1.15));
        b.incomeCv(                    dbl(sms, "income_cv",                    0.25));
        b.incomeGrowthFebToMar(        dbl(sms, "income_growth_feb_to_mar",     3.0));
        b.incomeGrowthMarToApr(        dbl(sms, "income_growth_mar_to_apr",     2.5));
        b.lowIncomeWeekCount90d(       dbl(sms, "low_income_week_count_90d",    2.0));
        b.avgRecoveryWeeks(            dbl(sms, "avg_recovery_weeks",           2.0));
        b.seasonalAdjustmentFactor(    dbl(sms, "seasonal_adjustment_factor",   0.96));
        b.platformIncomeSourceCount(   dbl(sms, "platform_income_source_count", emp != null ? 1.0 : 1.0));
        b.hasNonGigIncome(             bool(sms, "has_non_gig_income",          false));
        b.incomeLast30d(               dbl(sms, "income_last_30d",
                emp != null ? emp.getTotalEarningsLast90DaysRupees() / 3.0 : 15000.0));
        b.incomeLast90d(               dbl(sms, "income_last_90d",
                emp != null ? (double) emp.getTotalEarningsLast90DaysRupees() : 42000.0));

        // ─── D2 — Activity [SOURCE: PLATFORM] ─────────────────────────────────
        if (emp != null) {
            b.platformTenureDays(       (double) emp.getAccountTenureDays());
            b.orderCompletionRate(      emp.getOrderCompletionRate());
            b.ordersPerActiveDay(       emp.getOrdersPerActiveDay());
            b.activeDaysPerWeek(        emp.getActiveDaysPerWeek());
            b.accountAgeActiveRatio(    emp.getAccountAgeActiveRatio());
            b.longestInactivityStreakDays((double) emp.getLongestInactivityStreakDays());
            b.gapFrequency90d(          (double) emp.getGapFrequency90d());
            b.peakHourParticipationRate(emp.getPeakHourParticipationRate());
            b.lateCancellationRatio(    emp.getCancellationRate() * 100.0);
            b.cancellationSpikeFlag(    emp.getCancellationSpikeFlag());
        } else {
            b.platformTenureDays(400.0).orderCompletionRate(88.0).ordersPerActiveDay(8.0)
             .activeDaysPerWeek(5.0).accountAgeActiveRatio(0.60)
             .longestInactivityStreakDays(5.0).gapFrequency90d(2.0)
             .peakHourParticipationRate(0.55).lateCancellationRatio(0.6)
             .cancellationSpikeFlag(false);
        }

        // ─── D3 — Spending [SOURCE: SMS] ──────────────────────────────────────
        b.walletTopupFrequency(       dbl(sms, "wallet_topup_frequency",       6.0));
        b.spendToEarnRatio(           dbl(sms, "spend_to_earn_ratio",          0.72));
        b.avgMonthEndBalance(         dbl(sms, "avg_month_end_balance_sms",    5500.0));
        b.negativeBalanceFlag(        bool(sms, "negative_balance_flag",       false));
        b.spendVolatilityIndex(       dbl(sms, "spend_volatility_index",       0.25));
        b.discretionarySpendRatio(    dbl(sms, "discretionary_spend_ratio",    0.18));
        b.gamblingFantasySpendFlag(   bool(sms, "gambling_fantasy_spend_flag", false));
        b.largeTransactionFrequency(  dbl(sms, "large_transaction_frequency",  1.5));
        b.missedInstalmentCount90d(   dbl(sms, "missed_instalment_count_90d",  0.0));
        b.activeInstalmentCount(      dbl(sms, "active_instalment_count",      1.0));
        b.monthlyRemittanceAmount(    dbl(sms, "monthly_remittance_amount",    2500.0));
        b.remittanceConsistencyScore( dbl(sms, "remittance_consistency_score", 75.0));
        b.spendingLast30d(            dbl(sms, "spending_last_30d",            11000.0));
        b.spendingLast90d(            dbl(sms, "spending_last_90d",            33000.0));
        // SPD10 — debt-to-income ratio: prefer PAN bureau data, fallback to SMS, then default
        b.debtToIncomeRatio(          dbl(pan.isEmpty() ? sms : pan, "debt_to_income_ratio", 0.15));

        // ─── D4 — Social [SOURCE: PLATFORM] ───────────────────────────────────
        if (emp != null) {
            b.avgCustomerRating(        emp.getOverallRating());
            b.ratingLast30d(            emp.getRatingLast30d());
            b.ratingLast90d(            emp.getRatingLast90d());
            b.repeatCustomerRatio(      emp.getRepeatCustomerRatio());
            b.unresolvedDisputeCount(   (double) emp.getUnresolvedDisputeCount());
            b.disputeSpikeFlag(         emp.getDisputeSpikeFlag());
            b.responseRateToOffers(     emp.getResponseRateToOffers());
        } else {
            b.avgCustomerRating(4.3).ratingLast30d(4.35).ratingLast90d(4.3)
             .repeatCustomerRatio(0.18).unresolvedDisputeCount(0.0)
             .disputeSpikeFlag(false).responseRateToOffers(0.65);
        }

        // ─── D5 — Risk [SOURCE: DEVICE + PAN + SELF] ──────────────────────────
        // RSK01: PAN bureau data
        b.rsk01PriorDefault(           bool(pan, "rsk01_prior_default",          false));
        // RSK02: SELF (login anomaly detection — not yet collected, safe default)
        b.unusualLoginLocationFlag(    false);
        b.multipleDeviceLoginFlag(     false);
        // RSK04: SELF (multiple account detection — not yet collected)
        b.rsk04MultipleAccountDetected(false);
        // RSK05: DEVICE signals
        b.hasLocationSpoofApp(         bool(device, "has_location_spoof_app",       false));
        b.hasRecordFabricationTools(   bool(device, "has_record_fabrication_tools",  false));
        b.isDeviceRooted(              bool(device, "is_device_rooted",             false));
        b.hasUnofficialApks(           bool(device, "has_unofficial_apks",          false));
        b.creditHungryScore(           dbl(device,  "credit_hungry_score",          15.0));
        b.isDeveloperModeOn(           bool(device, "is_developer_mode_on",         false));

        // ─── D6 — Identity [SOURCE: SELF / Onboarding] ────────────────────────
        // PAN verified → tier 2, otherwise tier 1 (tier set during scoring via onboarding status)
        b.identityVerificationTier(2);
        b.phoneVintageDays(500.0);   // TODO: derive from user.createdAt vs now

        // ─── D7 — Temporal Consistency [SOURCE: SMS + PLATFORM + PAN] ─────────
        // TMP01 — consecutive active months: from employer tenure
        b.consecutiveActiveMonths(emp != null ? emp.getConsecutiveActiveMonths() : 5.0);
        // TMP02 — income stability windows: SMS-derived
        b.incomeStabilityWindows(      dbl(sms, "income_stability_windows",   5.0));
        // TMP03 — behavioral drift: platform + SMS
        b.completionRateDrift(emp != null ? emp.getCompletionRateDrift() : -1.5);
        b.spendRatioDrift(             dbl(sms, "spend_ratio_drift",          2.0));
        // TMP04 — longest on-time payment streak: PAN bureau
        b.longestOntimePaymentStreak(  dbl(pan, "longest_ontime_payment_streak", 8.0));
        // TMP05 — disruption recovery: SMS
        b.postDisruptionIncomeRecoveryDays(   dbl(sms, "post_disruption_income_recovery_days",    8.0));
        b.postDisruptionActivityRecoveryDays( dbl(sms, "post_disruption_activity_recovery_days",  6.0));
        // TMP06 — tenure-weighted reliability: employer platform
        b.tenureWeightedReliabilityScore(
                emp != null ? emp.getTenureWeightedReliabilityScore() : 72.0);

        return b.build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Double dbl(Map<String, String> map, String key, Double def) {
        String v = map.get(key);
        if (v == null || v.isBlank()) return def;
        try { return Double.parseDouble(v.trim()); } catch (NumberFormatException e) { return def; }
    }

    private Boolean bool(Map<String, String> map, String key, Boolean def) {
        String v = map.get(key);
        if (v == null || v.isBlank()) return def;
        return Boolean.parseBoolean(v.trim());
    }
}
