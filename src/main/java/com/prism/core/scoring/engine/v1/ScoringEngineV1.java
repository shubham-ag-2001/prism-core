package com.prism.core.scoring.engine.v1;

import com.prism.core.scoring.engine.ScoringEngineVersion;
import com.prism.core.scoring.engine.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.prism.core.scoring.engine.v1.V1DimensionConfig.*;
import static com.prism.core.scoring.engine.v1.V1FlagScorer.*;

@Slf4j
@Component
public class ScoringEngineV1 implements ScoringEngineVersion {

    @Override
    public String getVersion() { return "v1.0"; }

    @Override
    public ScoringResult compute(ScoringInput i) {
        List<String> killSwitches = new ArrayList<>();
        List<String> alerts       = new ArrayList<>();
        List<DimensionResult> dims = new ArrayList<>();

        dims.add(computeD1(i));
        dims.add(computeD2(i));
        dims.add(computeD3(i));
        dims.add(computeD4(i));
        dims.add(computeD5(i, killSwitches, alerts));
        dims.add(computeD6(i));
        dims.add(computeD7(i));

        double raw = dims.stream().mapToDouble(DimensionResult::getScore).sum();
        int finalScore = (int) Math.round(Math.min(900, Math.max(300, raw)));

        log.info("[v1.0] userId={} rawTotal={} finalScore={}", i.getUserId(), raw, finalScore);
        return ScoringResult.builder()
                .userId(i.getUserId()).prismScore(finalScore)
                .scoreBand(ScoringResult.toScoreBand(finalScore))
                .engineVersion("v1.0").rawTotal(raw)
                .killSwitchesTriggered(killSwitches).alerts(alerts)
                .dimensions(dims).build();
    }

    // ─── D1 — INCOME ──────────────────────────────────────────────────────────
    private DimensionResult computeD1(ScoringInput i) {
        List<VectorResult> vecs = new ArrayList<>();

        // INC01: weighted flags (avg_weekly_credit: 65%, income_spike_ratio: 35% HIGH)
        double[] inc01 = weightedVector(
                new double[]{0.65, 0.35}, new boolean[]{false, false},
                scoreAvgWeeklyCredit(i.getAvgWeeklyCredit90d()),
                scoreIncomeSpikeRatio(i.getIncomeSpikeRatio()));
        vecs.add(vec("INC01", "Weekly Income Mean", inc01[0], INC01_MAX, false, null));

        // INC02: self-sufficient
        vecs.add(selfVec("INC02", "Income CoV", scoreIncomeCv(i.getIncomeCv()), INC02_MAX));

        // INC03: self-sufficient (average of two growth pairs)
        Double avgGrowth = avgOf(i.getIncomeGrowthFebToMar(), i.getIncomeGrowthMarToApr());
        vecs.add(selfVec("INC03", "MoM Income Growth", scoreIncomeMomGrowth(avgGrowth), INC03_MAX));

        // INC04: weighted flags (low_income_count: 55% CRITICAL, avg_recovery: 45% CRITICAL)
        double[] inc04 = weightedVector(
                new double[]{0.55, 0.45}, new boolean[]{true, true},
                scoreLowIncomeWeekCount(i.getLowIncomeWeekCount90d()),
                scoreAvgRecoveryWeeks(i.getAvgRecoveryWeeks()));
        vecs.add(vec("INC04", "Income Recovery Speed", inc04[0], INC04_MAX, false, null));

        // INC05: self-sufficient
        vecs.add(selfVec("INC05", "Seasonal Adjustment", scoreSeasonalAdjustment(i.getSeasonalAdjustmentFactor()), INC05_MAX));

        // INC06: weighted flags (platform_count: 60% CRITICAL, has_non_gig: 40% HIGH)
        double[] inc06 = weightedVector(
                new double[]{0.60, 0.40}, new boolean[]{true, false},
                scorePlatformIncomeSourceCount(i.getPlatformIncomeSourceCount()),
                scoreHasNonGigIncome(i.getHasNonGigIncome()));
        vecs.add(vec("INC06", "Income Source Diversity", inc06[0], INC06_MAX, false, null));

        // INC07: self-sufficient (compute ratio internally)
        Double inc07ratio = ratio(i.getIncomeLast30d(), i.getIncomeLast90d());
        vecs.add(selfVec("INC07", "Income Trend 30/60/90", scoreIncomeTrendRatio(inc07ratio), INC07_MAX));

        return dimensionResult("D1_INCOME", "Income", D1_MIN, D1_MAX, vecs, false);
    }

    // ─── D2 — ACTIVITY ────────────────────────────────────────────────────────
    private DimensionResult computeD2(ScoringInput i) {
        List<VectorResult> vecs = new ArrayList<>();
        vecs.add(selfVec("ACT01", "Platform Tenure", scorePlatformTenure(i.getPlatformTenureDays()), ACT01_MAX));
        vecs.add(selfVec("ACT02", "Order Completion Rate", scoreOrderCompletionRate(i.getOrderCompletionRate()), ACT02_MAX));
        vecs.add(selfVec("ACT03", "Orders Per Active Day", scoreOrdersPerDay(i.getOrdersPerActiveDay()), ACT03_MAX));
        vecs.add(selfVec("ACT04", "Active Days Per Week", scoreActiveDaysPerWeek(i.getActiveDaysPerWeek()), ACT04_MAX));
        vecs.add(selfVec("ACT05", "Account Age/Active Ratio", scoreAccountAgeRatio(i.getAccountAgeActiveRatio()), ACT05_MAX));

        // ACT06: longest_inactivity: 60% CRITICAL, gap_frequency: 40% HIGH
        double[] act06 = weightedVector(
                new double[]{0.60, 0.40}, new boolean[]{true, false},
                scoreLongestInactivityStreak(i.getLongestInactivityStreakDays()),
                scoreGapFrequency(i.getGapFrequency90d()));
        vecs.add(vec("ACT06", "Gap Days", act06[0], ACT06_MAX, false, null));

        vecs.add(selfVec("ACT07", "Peak Hour Rate", scorePeakHourRate(i.getPeakHourParticipationRate()), ACT07_MAX));

        // ACT08: late_cancel: 55% CRITICAL, spike_flag: 45% VERY CRITICAL + cap
        boolean spikeFlag = Boolean.TRUE.equals(i.getCancellationSpikeFlag());
        double[] act08 = weightedVector(
                new double[]{0.55, 0.45}, new boolean[]{true, true},
                scoreLateCancellationRatio(i.getLateCancellationRatio()),
                scoreCancellationSpikeFlag(i.getCancellationSpikeFlag()));
        double act08score = spikeFlag ? Math.min(act08[0], 20) : act08[0];
        vecs.add(vec("ACT08", "Cancellation Ratio", act08score, ACT08_MAX, spikeFlag, spikeFlag ? 20.0 : null));

        return dimensionResult("D2_ACTIVITY", "Activity", D2_MIN, D2_MAX, vecs, false);
    }

    // ─── D3 — SPENDING ────────────────────────────────────────────────────────
    private DimensionResult computeD3(ScoringInput i) {
        List<VectorResult> vecs = new ArrayList<>();
        vecs.add(selfVec("SPD01", "Wallet Top-Up Frequency", scoreWalletTopupFrequency(i.getWalletTopupFrequency()), SPD01_MAX));
        vecs.add(selfVec("SPD02", "Spend-to-Earn Ratio", scoreSpendToEarnRatio(i.getSpendToEarnRatio()), SPD02_MAX));

        // SPD03: avg_balance: 40% CRITICAL, negative_flag: 60% VERY CRITICAL + cap
        boolean negFlag = Boolean.TRUE.equals(i.getNegativeBalanceFlag());
        double[] spd03 = weightedVector(
                new double[]{0.40, 0.60}, new boolean[]{true, true},
                scoreAvgMonthEndBalance(i.getAvgMonthEndBalance()),
                scoreNegativeBalanceFlag(i.getNegativeBalanceFlag()));
        double spd03score = negFlag ? Math.min(spd03[0], 25) : spd03[0];
        vecs.add(vec("SPD03", "Savings Buffer", spd03score, SPD03_MAX, negFlag, negFlag ? 25.0 : null));

        vecs.add(selfVec("SPD04", "Spend Volatility", scoreSpendVolatility(i.getSpendVolatilityIndex()), SPD04_MAX));

        // SPD05: discretionary: 45% CRITICAL, gambling: 55% VERY CRITICAL + cap
        boolean gambFlag = Boolean.TRUE.equals(i.getGamblingFantasySpendFlag());
        double[] spd05 = weightedVector(
                new double[]{0.45, 0.55}, new boolean[]{true, true},
                scoreDiscretionarySpendRatio(i.getDiscretionarySpendRatio()),
                scoreGamblingFlag(i.getGamblingFantasySpendFlag()));
        double spd05score = gambFlag ? Math.min(spd05[0], 20) : spd05[0];
        vecs.add(vec("SPD05", "Category Spend Distribution", spd05score, SPD05_MAX, gambFlag, gambFlag ? 20.0 : null));

        vecs.add(selfVec("SPD06", "Large Transaction Frequency", scoreLargeTransactionFreq(i.getLargeTransactionFrequency()), SPD06_MAX));

        // SPD07: missed: 65% VERY CRITICAL + cap, active: 35% HIGH
        boolean missedCap = i.getMissedInstalmentCount90d() != null && i.getMissedInstalmentCount90d() >= 3;
        double[] spd07 = weightedVector(
                new double[]{0.65, 0.35}, new boolean[]{true, false},
                scoreMissedInstalmentCount(i.getMissedInstalmentCount90d()),
                scoreActiveInstalmentCount(i.getActiveInstalmentCount()));
        double spd07score = missedCap ? Math.min(spd07[0], 15) : spd07[0];
        vecs.add(vec("SPD07", "Instalment Behaviour", spd07score, SPD07_MAX, missedCap, missedCap ? 15.0 : null));

        // SPD08: remittance_amount: 40% CRITICAL, consistency: 60% CRITICAL
        double[] spd08 = weightedVector(
                new double[]{0.40, 0.60}, new boolean[]{true, true},
                scoreMonthlyRemittanceAmount(i.getMonthlyRemittanceAmount()),
                scoreRemittanceConsistency(i.getRemittanceConsistencyScore()));
        vecs.add(vec("SPD08", "Remittance Index", spd08[0], SPD08_MAX, false, null));

        // SPD09: self-sufficient (ratio)
        Double spd09ratio = ratio(i.getSpendingLast30d(), i.getSpendingLast90d());
        vecs.add(selfVec("SPD09", "Spending Trend 30/60/90", scoreSpendingTrendRatio(spd09ratio), SPD09_MAX));

        vecs.add(selfVec("SPD10", "Active Debt Obligation", scoreDebtToIncomeRatio(i.getDebtToIncomeRatio()), SPD10_MAX));

        return dimensionResult("D3_SPENDING", "Spending", D3_MIN, D3_MAX, vecs, false);
    }

    // ─── D4 — SOCIAL ──────────────────────────────────────────────────────────
    private DimensionResult computeD4(ScoringInput i) {
        List<VectorResult> vecs = new ArrayList<>();
        vecs.add(selfVec("SOC01", "Avg Customer Rating", scoreAvgCustomerRating(i.getAvgCustomerRating()), SOC01_MAX));

        Double soc02ratio = (i.getRatingLast30d() != null && i.getRatingLast90d() != null && i.getRatingLast90d() != 0)
                ? i.getRatingLast30d() / i.getRatingLast90d() : null;
        vecs.add(selfVec("SOC02", "Rating Trend", scoreRatingTrendRatio(soc02ratio), SOC02_MAX));

        vecs.add(selfVec("SOC03", "Repeat Customer Ratio", scoreRepeatCustomerRatio(i.getRepeatCustomerRatio()), SOC03_MAX));

        // SOC04: unresolved: 70% VERY CRITICAL + cap, spike: 30% CRITICAL
        boolean disputeCap = i.getUnresolvedDisputeCount() != null && i.getUnresolvedDisputeCount() >= 3;
        double[] soc04 = weightedVector(
                new double[]{0.70, 0.30}, new boolean[]{true, true},
                scoreUnresolvedDisputeCount(i.getUnresolvedDisputeCount()),
                scoreDisputeSpikeFlag(i.getDisputeSpikeFlag()));
        double soc04score = disputeCap ? Math.min(soc04[0], 10) : soc04[0];
        vecs.add(vec("SOC04", "Dispute Rate", soc04score, SOC04_MAX, disputeCap, disputeCap ? 10.0 : null));

        vecs.add(selfVec("SOC05", "Response Rate", scoreResponseRate(i.getResponseRateToOffers()), SOC05_MAX));

        return dimensionResult("D4_SOCIAL", "Social", D4_MIN, D4_MAX, vecs, false);
    }

    // ─── D5 — RISK (with kill switches) ───────────────────────────────────────
    private DimensionResult computeD5(ScoringInput i, List<String> killSwitches, List<String> alerts) {
        boolean ks1 = Boolean.TRUE.equals(i.getRsk01PriorDefault());
        boolean ks4 = Boolean.TRUE.equals(i.getRsk04MultipleAccountDetected());

        if (ks1) { killSwitches.add("RSK01"); alerts.add("ALERT: Prior loan default detected"); }
        if (ks4) { killSwitches.add("RSK04"); alerts.add("ALERT: Multiple account detection triggered"); }

        if (ks1 || ks4) {
            return DimensionResult.builder()
                    .dimensionKey("D5_RISK").displayName("Risk")
                    .score(D5_MIN).minScore(D5_MIN).maxScore(D5_MAX)
                    .performanceRatio(0.0).killSwitchTriggered(true)
                    .vectors(Collections.emptyList()).build();
        }

        List<VectorResult> vecs = new ArrayList<>();
        vecs.add(selfVec("RSK01", "Prior Loan Default", scoreRsk01PriorDefault(i.getRsk01PriorDefault()), RSK01_MAX));

        double[] rsk02 = weightedVector(
                new double[]{0.45, 0.55}, new boolean[]{true, true},
                scoreUnusualLoginLocation(i.getUnusualLoginLocationFlag()),
                scoreMultipleDeviceLogin(i.getMultipleDeviceLoginFlag()));
        vecs.add(vec("RSK02", "Account Takeover Risk", rsk02[0], RSK02_MAX, false, null));

        vecs.add(selfVec("RSK04", "Multiple Account Detection", scoreRsk04MultipleAccount(i.getRsk04MultipleAccountDetected()), RSK04_MAX));

        // RSK05: 6 flags with individual caps
        boolean locSpoof = Boolean.TRUE.equals(i.getHasLocationSpoofApp());
        boolean recFab   = Boolean.TRUE.equals(i.getHasRecordFabricationTools());
        boolean rooted   = Boolean.TRUE.equals(i.getIsDeviceRooted());
        double[] rsk05 = weightedVector(
                new double[]{0.30, 0.25, 0.20, 0.10, 0.10, 0.05}, new boolean[]{true, true, true, false, true, false},
                scoreLocationSpoofApp(i.getHasLocationSpoofApp()),
                scoreRecordFabricationTools(i.getHasRecordFabricationTools()),
                scoreDeviceRooted(i.getIsDeviceRooted()),
                scoreUnofficialApks(i.getHasUnofficialApks()),
                scoreCreditHungryScore(i.getCreditHungryScore()),
                scoreDeveloperMode(i.getIsDeveloperModeOn()));
        double rsk05cap = 100;
        if (locSpoof) rsk05cap = Math.min(rsk05cap, 15);
        if (recFab)   rsk05cap = Math.min(rsk05cap, 15);
        if (rooted)   rsk05cap = Math.min(rsk05cap, 20);
        boolean rsk05capped = rsk05cap < 100;
        double rsk05score = rsk05capped ? Math.min(rsk05[0], rsk05cap) : rsk05[0];
        vecs.add(vec("RSK05", "Device Integrity", rsk05score, RSK05_MAX, rsk05capped, rsk05capped ? rsk05cap : null));

        return dimensionResult("D5_RISK", "Risk", D5_MIN, D5_MAX, vecs, false);
    }

    // ─── D6 — IDENTITY ────────────────────────────────────────────────────────
    private DimensionResult computeD6(ScoringInput i) {
        List<VectorResult> vecs = new ArrayList<>();
        vecs.add(selfVec("IDN01", "Identity Verification Tier", scoreIdentityTier(i.getIdentityVerificationTier()), IDN01_MAX));
        vecs.add(selfVec("IDN02", "Phone Vintage", scorePhoneVintage(i.getPhoneVintageDays()), IDN02_MAX));
        // IDN03 is normaliser only — not scored
        return dimensionResult("D6_IDENTITY", "Identity", D6_MIN, D6_MAX, vecs, false);
    }

    // ─── D7 — TEMPORAL CONSISTENCY ────────────────────────────────────────────
    private DimensionResult computeD7(ScoringInput i) {
        List<VectorResult> vecs = new ArrayList<>();
        vecs.add(selfVec("TMP01", "Consecutive Active Months", scoreConsecutiveActiveMonths(i.getConsecutiveActiveMonths()), TMP01_MAX));
        vecs.add(selfVec("TMP02", "Income Stability Windows", scoreIncomeStabilityWindows(i.getIncomeStabilityWindows()), TMP02_MAX));

        double[] tmp03 = weightedVector(
                new double[]{0.50, 0.50}, new boolean[]{true, true},
                scoreCompletionRateDrift(i.getCompletionRateDrift()),
                scoreSpendRatioDrift(i.getSpendRatioDrift()));
        vecs.add(vec("TMP03", "Behavioural Drift", tmp03[0], TMP03_MAX, false, null));

        vecs.add(selfVec("TMP04", "Longest On-Time Payment Streak", scoreLongestOntimeStreak(i.getLongestOntimePaymentStreak()), TMP04_MAX));

        double[] tmp05 = weightedVector(
                new double[]{0.50, 0.50}, new boolean[]{true, true},
                scorePostDisruptionIncomeRecovery(i.getPostDisruptionIncomeRecoveryDays()),
                scorePostDisruptionActivityRecovery(i.getPostDisruptionActivityRecoveryDays()));
        vecs.add(vec("TMP05", "Pattern Recovery Speed", tmp05[0], TMP05_MAX, false, null));

        vecs.add(selfVec("TMP06", "Tenure Weighted Reliability", scoreTenureWeightedReliability(i.getTenureWeightedReliabilityScore()), TMP06_MAX));

        return dimensionResult("D7_TEMPORAL", "Temporal Consistency", D7_MIN, D7_MAX, vecs, false);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Weighted vector computation with missing-flag rules.
     * critical[i]=true: missing weight is permanently lost (weight not redistributed).
     * critical[i]=false: missing weight is redistributed among present flags.
     */
    /**
     * Weighted vector computation with missing-flag rules.
     *
     * - critical[i]=true + flag missing: weight is permanently LOST (reduces max possible score)
     * - critical[i]=false + flag missing: weight is REDISTRIBUTED among present flags (normalize)
     *
     * Result is always in [0, (1 - lostWeight) * 100].
     */
    private double[] weightedVector(double[] weights, boolean[] critical, Double... scores) {
        double sumWeightPresent = 0;
        double sumWeightedScore = 0;
        double lostWeight       = 0;

        for (int j = 0; j < scores.length; j++) {
            if (scores[j] == null) {
                if (critical[j]) lostWeight += weights[j];
                // non-critical missing → will be redistributed by normalization below
            } else {
                sumWeightPresent += weights[j];
                sumWeightedScore += scores[j] * weights[j];
            }
        }

        // effectiveMax is reduced only by critical-missing weight
        double effectiveMax = 1.0 - lostWeight;
        if (effectiveMax <= 0 || sumWeightPresent <= 0) return new double[]{0};

        // Normalize: scale the weighted sum so the present flags fill the effectiveMax range.
        // If non-critical flags are also missing (sumWeightPresent < effectiveMax),
        // their weight is redistributed via this normalization. The cap effectiveMax*100
        // ensures critical missing weight reduces the achievable maximum.
        double vectorScore = (sumWeightedScore / sumWeightPresent) * effectiveMax;
        vectorScore = Math.min(effectiveMax * 100, Math.max(0, vectorScore));
        return new double[]{vectorScore};
    }

    private VectorResult selfVec(String key, String name, Double score, double maxPts) {
        double s = score != null ? score : 50.0; // neutral default for missing self-sufficient vector
        double contribution = s * maxPts / 100.0;
        return VectorResult.builder().vectorKey(key).displayName(name)
                .vectorScore(s).maxPts(maxPts).contribution(contribution)
                .capApplied(false).build();
    }

    private VectorResult vec(String key, String name, double score, double maxPts, boolean capApplied, Double cap) {
        return VectorResult.builder().vectorKey(key).displayName(name)
                .vectorScore(score).maxPts(maxPts).contribution(score * maxPts / 100.0)
                .capApplied(capApplied).appliedCap(cap).build();
    }

    private DimensionResult dimensionResult(String key, String name, double min, double max,
                                             List<VectorResult> vecs, boolean killSwitch) {
        double variable = vecs.stream().mapToDouble(VectorResult::getContribution).sum();
        double score = min + variable;
        double perf = (max - min) > 0 ? (score - min) / (max - min) : 0;
        return DimensionResult.builder().dimensionKey(key).displayName(name)
                .score(score).minScore(min).maxScore(max)
                .performanceRatio(perf).killSwitchTriggered(killSwitch)
                .vectors(vecs).build();
    }

    /** Compute 30d/90d trend ratio: last30d / (last90d / 3) */
    private Double ratio(Double last30d, Double last90d) {
        if (last30d == null || last90d == null || last90d == 0) return null;
        return last30d / (last90d / 3.0);
    }

    /** Average two nullable doubles */
    private Double avgOf(Double a, Double b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return (a + b) / 2.0;
    }
}
