package com.prism.core.scoring.engine.v1;

import com.prism.core.scoring.engine.util.ScoreRangeLookup;

/**
 * All flag → score functions for engine v1.0.
 * Each method returns a score in [0–100], or null if the input is null (missing flag).
 * Null handling (redistribute vs. reduce max) is done in ScoringEngineV1.
 */
public final class V1FlagScorer {

    private V1FlagScorer() {}

    // ─── D1 — INCOME ──────────────────────────────────────────────────────────

    public static Double scoreAvgWeeklyCredit(Double v) {
        if (v == null) return null;
        double[][] t = {{1000,10},{2000,25},{3000,45},{4000,65},{5000,80},{6000,90},{8000,95},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreIncomeSpikeRatio(Double v) {
        if (v == null) return null;
        // 1.0-1.2→100, 1.2-1.5→80, 1.5-2.0→55, 2.0-2.5→30, 2.5+→10
        double[][] t = {{1.2,100},{1.5,80},{2.0,55},{2.5,30},{Double.MAX_VALUE,10}};
        return ScoreRangeLookup.lookup(v, t);
    }

    /** INC02 — Income coefficient of variation */
    public static Double scoreIncomeCv(Double v) {
        if (v == null) return null;
        double[][] t = {{0.10,100},{0.20,85},{0.30,70},{0.40,50},{0.50,30},{0.60,15},{Double.MAX_VALUE,0}};
        return ScoreRangeLookup.lookup(v, t);
    }

    /** INC03 — MoM growth %; pass in the averaged value */
    public static Double scoreIncomeMomGrowth(Double avgGrowthPct) {
        if (avgGrowthPct == null) return null;
        double v = avgGrowthPct;
        if (v <= -15) return 0.0;
        if (v <= -10) return 15.0;
        if (v <= -5)  return 30.0;
        if (v <= 0)   return 50.0;
        if (v <= 5)   return 70.0;
        if (v <= 10)  return 85.0;
        return 100.0;
    }

    public static Double scoreLowIncomeWeekCount(Double v) {
        if (v == null) return null;
        if (v == 0) return 100.0;
        if (v <= 1) return 85.0;
        if (v <= 2) return 70.0;
        if (v <= 3) return 55.0;
        if (v <= 4) return 35.0;
        if (v <= 5) return 20.0;
        return 5.0;
    }

    public static Double scoreAvgRecoveryWeeks(Double v) {
        if (v == null) return null;
        double[][] t = {{1,100},{2,80},{3,60},{4,35},{5,15},{Double.MAX_VALUE,0}};
        return ScoreRangeLookup.lookup(v, t);
    }

    /** INC05 — Seasonal adjustment factor */
    public static Double scoreSeasonalAdjustment(Double v) {
        if (v == null) return null;
        if (v >= 0.98) return 100.0;
        if (v >= 0.95) return 80.0;
        if (v >= 0.92) return 60.0;
        if (v >= 0.90) return 40.0;
        if (v >= 0.88) return 25.0;
        return 10.0;
    }

    public static Double scorePlatformIncomeSourceCount(Double v) {
        if (v == null) return null;
        if (v >= 4) return 100.0;
        if (v >= 3) return 90.0;
        if (v >= 2) return 70.0;
        return 40.0;
    }

    public static Double scoreHasNonGigIncome(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 100, 0);
    }

    /** INC07 — pass the computed ratio = last30d / (last90d/3) */
    public static Double scoreIncomeTrendRatio(Double ratio) {
        if (ratio == null) return null;
        if (ratio >= 1.15) return 100.0;
        if (ratio >= 1.05) return 85.0;
        if (ratio >= 0.95) return 65.0;
        if (ratio >= 0.85) return 45.0;
        if (ratio >= 0.75) return 25.0;
        return 10.0;
    }

    // ─── D2 — ACTIVITY ────────────────────────────────────────────────────────

    public static Double scorePlatformTenure(Double days) {
        if (days == null) return null;
        double[][] t = {{30,5},{90,20},{180,40},{365,60},{730,80},{1095,92},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(days, t);
    }

    public static Double scoreOrderCompletionRate(Double pct) {
        if (pct == null) return null;
        double[][] t = {{60,0},{70,20},{80,45},{85,60},{90,75},{95,88},{98,95},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(pct, t);
    }

    public static Double scoreOrdersPerDay(Double v) {
        if (v == null) return null;
        double[][] t = {{3,10},{5,30},{7,55},{9,75},{11,88},{13,95},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreActiveDaysPerWeek(Double v) {
        if (v == null) return null;
        double[][] t = {{2,5},{3,25},{4,45},{5,65},{6,82},{6.5,92},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreAccountAgeRatio(Double v) {
        if (v == null) return null;
        double[][] t = {{0.20,5},{0.35,20},{0.50,40},{0.65,60},{0.75,78},{0.85,90},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreLongestInactivityStreak(Double days) {
        if (days == null) return null;
        double[][] t = {{3,100},{7,80},{14,55},{21,30},{30,10},{Double.MAX_VALUE,0}};
        return ScoreRangeLookup.lookup(days, t);
    }

    public static Double scoreGapFrequency(Double v) {
        if (v == null) return null;
        if (v == 0) return 100.0;
        if (v <= 1) return 85.0;
        if (v <= 2) return 65.0;
        if (v <= 3) return 45.0;
        if (v <= 4) return 25.0;
        return 5.0;
    }

    public static Double scorePeakHourRate(Double v) {
        if (v == null) return null;
        double[][] t = {{0.20,10},{0.30,25},{0.40,40},{0.50,55},{0.60,70},{0.70,85},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreLateCancellationRatio(Double v) {
        if (v == null) return null;
        double[][] t = {{0.5,100},{1.0,85},{2.0,65},{4.0,40},{8.0,15},{Double.MAX_VALUE,0}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreCancellationSpikeFlag(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100); // true→0, false→100
    }

    // ─── D3 — SPENDING ────────────────────────────────────────────────────────

    public static Double scoreWalletTopupFrequency(Double v) {
        if (v == null) return null;
        double[][] t = {{0,10},{2,25},{4,45},{6,65},{8,80},{10,90},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    /** SPD02 — higher ratio is worse; use descending lookup */
    public static Double scoreSpendToEarnRatio(Double v) {
        if (v == null) return null;
        if (v > 1.20) return 0.0;
        if (v > 1.10) return 10.0;
        if (v > 1.00) return 20.0;
        if (v > 0.90) return 35.0;
        if (v > 0.80) return 50.0;
        if (v > 0.70) return 65.0;
        if (v > 0.60) return 80.0;
        if (v > 0.50) return 92.0;
        return 100.0;
    }

    public static Double scoreAvgMonthEndBalance(Double v) {
        if (v == null) return null;
        if (v < 0) return 0.0;
        double[][] t = {{1000,15},{3000,35},{5000,55},{8000,75},{12000,90},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreNegativeBalanceFlag(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100); // true→0, false→100
    }

    public static Double scoreSpendVolatility(Double v) {
        if (v == null) return null;
        if (v > 0.80) return 0.0;
        if (v > 0.60) return 15.0;
        if (v > 0.50) return 30.0;
        if (v > 0.40) return 45.0;
        if (v > 0.30) return 60.0;
        if (v > 0.20) return 75.0;
        if (v > 0.10) return 90.0;
        return 100.0;
    }

    public static Double scoreDiscretionarySpendRatio(Double v) {
        if (v == null) return null;
        if (v < 0.10) return 100.0;
        if (v < 0.15) return 85.0;
        if (v < 0.20) return 70.0;
        if (v < 0.25) return 50.0;
        if (v < 0.30) return 30.0;
        if (v < 0.40) return 15.0;
        return 0.0;
    }

    public static Double scoreGamblingFlag(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100);
    }

    public static Double scoreLargeTransactionFreq(Double v) {
        if (v == null) return null;
        double[][] t = {{0.5,100},{1.0,85},{2.0,70},{3.0,50},{4.0,30},{5.0,15},{Double.MAX_VALUE,0}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreMissedInstalmentCount(Double v) {
        if (v == null) return null;
        if (v == 0) return 100.0;
        if (v <= 1) return 60.0;
        if (v <= 2) return 30.0;
        if (v <= 3) return 10.0;
        return 0.0;
    }

    public static Double scoreActiveInstalmentCount(Double v) {
        if (v == null) return null;
        if (v == 0) return 80.0;
        if (v == 1) return 100.0;
        if (v == 2) return 75.0;
        if (v == 3) return 45.0;
        if (v == 4) return 20.0;
        return 5.0;
    }

    public static Double scoreMonthlyRemittanceAmount(Double v) {
        if (v == null) return null;
        if (v == 0) return 50.0;
        double[][] t = {{2000,65},{4000,80},{6000,90},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreRemittanceConsistency(Double v) {
        if (v == null) return null;
        double[][] t = {{20,10},{40,30},{60,50},{75,70},{85,85},{95,95},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    /** SPD09 — pass the computed ratio = last30d / (last90d/3) */
    public static Double scoreSpendingTrendRatio(Double ratio) {
        if (ratio == null) return null;
        if (ratio >= 1.20) return 0.0;
        if (ratio >= 1.10) return 15.0;
        if (ratio >= 1.00) return 35.0;
        if (ratio >= 0.90) return 65.0;
        if (ratio >= 0.80) return 85.0;
        return 100.0;
    }

    public static Double scoreDebtToIncomeRatio(Double v) {
        if (v == null) return null;
        if (v > 0.60) return 0.0;
        if (v > 0.50) return 10.0;
        if (v > 0.40) return 25.0;
        if (v > 0.30) return 40.0;
        if (v > 0.20) return 58.0;
        if (v > 0.10) return 75.0;
        if (v > 0.05) return 90.0;
        return 100.0;
    }

    // ─── D4 — SOCIAL ──────────────────────────────────────────────────────────

    public static Double scoreAvgCustomerRating(Double v) {
        if (v == null) return null;
        if (v < 3.0) return 0.0;
        if (v < 3.5) return 20.0;
        if (v < 4.0) return 45.0;
        if (v < 4.3) return 65.0;
        if (v < 4.6) return 80.0;
        if (v < 4.8) return 92.0;
        return 100.0;
    }

    /** SOC02 — pass the ratio = rating30d / rating90d */
    public static Double scoreRatingTrendRatio(Double ratio) {
        if (ratio == null) return null;
        if (ratio < 0.90) return 0.0;
        if (ratio < 0.95) return 25.0;
        if (ratio < 0.98) return 50.0;
        if (ratio < 1.00) return 70.0;
        if (ratio < 1.02) return 85.0;
        return 100.0;
    }

    public static Double scoreRepeatCustomerRatio(Double v) {
        if (v == null) return null;
        double[][] t = {{0.05,10},{0.10,25},{0.15,45},{0.20,60},{0.25,75},{0.30,88},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreUnresolvedDisputeCount(Double v) {
        if (v == null) return null;
        if (v == 0) return 100.0;
        if (v <= 1) return 55.0;
        if (v <= 2) return 20.0;
        if (v <= 3) return 5.0;
        return 0.0;
    }

    public static Double scoreDisputeSpikeFlag(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 20, 100);
    }

    public static Double scoreResponseRate(Double v) {
        if (v == null) return null;
        if (v < 0.30) return 0.0;
        double[][] t = {{0.40,20},{0.50,40},{0.60,55},{0.70,70},{0.80,85},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(v, t);
    }

    // ─── D5 — RISK ────────────────────────────────────────────────────────────

    public static Double scoreRsk01PriorDefault(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100);
    }

    public static Double scoreUnusualLoginLocation(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 30, 100);
    }

    public static Double scoreMultipleDeviceLogin(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 40, 100);
    }

    public static Double scoreRsk04MultipleAccount(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100);
    }

    public static Double scoreLocationSpoofApp(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100);
    }

    public static Double scoreRecordFabricationTools(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100);
    }

    public static Double scoreDeviceRooted(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 0, 100);
    }

    public static Double scoreUnofficialApks(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 40, 100);
    }

    public static Double scoreCreditHungryScore(Double v) {
        if (v == null) return null;
        double[][] t = {{10,100},{20,85},{35,65},{50,45},{65,25},{80,10},{Double.MAX_VALUE,0}};
        return ScoreRangeLookup.lookup(v, t);
    }

    public static Double scoreDeveloperMode(Boolean v) {
        if (v == null) return null;
        return ScoreRangeLookup.lookupBoolean(v, 60, 100);
    }

    // ─── D6 — IDENTITY ────────────────────────────────────────────────────────

    /** IDN01 — tier: 0→0, 1→55, 2→100 */
    public static Double scoreIdentityTier(Integer v) {
        if (v == null) return null;
        if (v <= 0) return 0.0;
        if (v == 1) return 55.0;
        return 100.0;
    }

    public static Double scorePhoneVintage(Double days) {
        if (days == null) return null;
        double[][] t = {{90,5},{180,20},{365,40},{730,65},{1095,82},{1825,92},{Double.MAX_VALUE,100}};
        return ScoreRangeLookup.lookup(days, t);
    }

    // ─── D7 — TEMPORAL CONSISTENCY ────────────────────────────────────────────

    /**
     * TMP01 — Consecutive active months streak.
     * Uses linear interpolation between defined breakpoints.
     */
    public static Double scoreConsecutiveActiveMonths(Double months) {
        if (months == null) return null;
        double[][] breakpoints = {{0,0},{1,15},{2,30},{3,45},{4,58},{6,70},{9,82},{12,90},{18,96},{24,100}};
        if (months <= 0) return 0.0;
        if (months >= 24) return 100.0;
        for (int i = 0; i < breakpoints.length - 1; i++) {
            if (months <= breakpoints[i + 1][0]) {
                return ScoreRangeLookup.interpolate(
                        months,
                        breakpoints[i][0], breakpoints[i][1],
                        breakpoints[i + 1][0], breakpoints[i + 1][1]);
            }
        }
        return 100.0;
    }

    /** TMP02 — Income stability windows */
    public static Double scoreIncomeStabilityWindows(Double windows) {
        if (windows == null) return null;
        if (windows <= 0) return 0.0;
        if (windows <= 1) return 20.0;
        if (windows <= 2) return 40.0;
        if (windows <= 3) return 58.0;
        if (windows <= 4) return 72.0;
        if (windows <= 5) return 83.0;
        if (windows <= 6) return 91.0;
        return 100.0;
    }

    /** TMP03 — completion_rate_drift (drift %) */
    public static Double scoreCompletionRateDrift(Double v) {
        if (v == null) return null;
        if (v > -1)   return 100.0;
        if (v > -3)   return 80.0;
        if (v > -5)   return 60.0;
        if (v > -8)   return 35.0;
        if (v > -12)  return 15.0;
        return 0.0;
    }

    /** TMP03 — spend_ratio_drift (drift %) */
    public static Double scoreSpendRatioDrift(Double v) {
        if (v == null) return null;
        if (v < 1)  return 100.0;
        if (v < 3)  return 85.0;
        if (v < 5)  return 65.0;
        if (v < 8)  return 40.0;
        if (v < 12) return 20.0;
        return 0.0;
    }

    /** TMP04 — Longest on-time payment streak */
    public static Double scoreLongestOntimeStreak(Double v) {
        if (v == null) return null;
        if (v <= 0) return 0.0;
        if (v <= 2) return 15.0;
        if (v <= 5) return 35.0;
        if (v <= 9) return 55.0;
        if (v <= 14) return 72.0;
        if (v <= 20) return 87.0;
        return 100.0;
    }

    public static Double scorePostDisruptionIncomeRecovery(Double days) {
        if (days == null) return null;
        if (days <= 5)  return 100.0;
        if (days <= 10) return 85.0;
        if (days <= 15) return 65.0;
        if (days <= 25) return 40.0;
        if (days <= 35) return 20.0;
        return 0.0;
    }

    public static Double scorePostDisruptionActivityRecovery(Double days) {
        if (days == null) return null;
        if (days <= 3)  return 100.0;
        if (days <= 7)  return 85.0;
        if (days <= 12) return 65.0;
        if (days <= 20) return 40.0;
        if (days <= 30) return 20.0;
        return 0.0;
    }

    /** TMP06 — Tenure weighted reliability */
    public static Double scoreTenureWeightedReliability(Double v) {
        if (v == null) return null;
        if (v < 20) return 0.0;
        if (v < 35) return 20.0;
        if (v < 50) return 40.0;
        if (v < 65) return 58.0;
        if (v < 75) return 72.0;
        if (v < 85) return 85.0;
        return 100.0;
    }
}
