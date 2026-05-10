package com.prism.core.scoring.engine.model;

import lombok.Builder;
import lombok.Data;

/**
 * All flag values fed into the scoring engine.
 * Null = flag not available. Engine applies missing-flag rules per criticality.
 *
 * Naming convention: camelCase version of the spec flag keys.
 */
@Data
@Builder
public class ScoringInput {

    private String userId;

    // ─── D1 — INCOME ──────────────────────────────────────────────────────────
    /** INC01 */
    private Double avgWeeklyCredit90d;
    private Double incomeSpikeRatio;
    /** INC02 — self-sufficient */
    private Double incomeCv;
    /** INC03 — self-sufficient (average of two month pairs) */
    private Double incomeGrowthFebToMar;
    private Double incomeGrowthMarToApr;
    /** INC04 */
    private Double lowIncomeWeekCount90d;
    private Double avgRecoveryWeeks;
    /** INC05 — self-sufficient */
    private Double seasonalAdjustmentFactor;
    /** INC06 */
    private Double platformIncomeSourceCount;
    private Boolean hasNonGigIncome;
    /** INC07 — self-sufficient (raw values; engine computes ratio internally) */
    private Double incomeLast30d;
    private Double incomeLast90d;

    // ─── D2 — ACTIVITY ────────────────────────────────────────────────────────
    /** ACT01 — self-sufficient */
    private Double platformTenureDays;
    /** ACT02 — self-sufficient */
    private Double orderCompletionRate;
    /** ACT03 — self-sufficient */
    private Double ordersPerActiveDay;
    /** ACT04 — self-sufficient */
    private Double activeDaysPerWeek;
    /** ACT05 — self-sufficient */
    private Double accountAgeActiveRatio;
    /** ACT06 */
    private Double longestInactivityStreakDays;
    private Double gapFrequency90d;
    /** ACT07 — self-sufficient */
    private Double peakHourParticipationRate;
    /** ACT08 */
    private Double lateCancellationRatio;
    private Boolean cancellationSpikeFlag;

    // ─── D3 — SPENDING ────────────────────────────────────────────────────────
    /** SPD01 — self-sufficient */
    private Double walletTopupFrequency;
    /** SPD02 — self-sufficient */
    private Double spendToEarnRatio;
    /** SPD03 */
    private Double avgMonthEndBalance;
    private Boolean negativeBalanceFlag;
    /** SPD04 — self-sufficient */
    private Double spendVolatilityIndex;
    /** SPD05 */
    private Double discretionarySpendRatio;
    private Boolean gamblingFantasySpendFlag;
    /** SPD06 — self-sufficient */
    private Double largeTransactionFrequency;
    /** SPD07 */
    private Double missedInstalmentCount90d;
    private Double activeInstalmentCount;
    /** SPD08 */
    private Double monthlyRemittanceAmount;
    private Double remittanceConsistencyScore;
    /** SPD09 — self-sufficient (raw values; engine computes ratio) */
    private Double spendingLast30d;
    private Double spendingLast90d;
    /** SPD10 — self-sufficient */
    private Double debtToIncomeRatio;

    // ─── D4 — SOCIAL ──────────────────────────────────────────────────────────
    /** SOC01 — self-sufficient */
    private Double avgCustomerRating;
    /** SOC02 — self-sufficient */
    private Double ratingLast30d;
    private Double ratingLast90d;
    /** SOC03 — self-sufficient */
    private Double repeatCustomerRatio;
    /** SOC04 */
    private Double unresolvedDisputeCount;
    private Boolean disputeSpikeFlag;
    /** SOC05 — self-sufficient */
    private Double responseRateToOffers;

    // ─── D5 — RISK ────────────────────────────────────────────────────────────
    /** RSK01 — kill switch */
    private Boolean rsk01PriorDefault;
    /** RSK02 */
    private Boolean unusualLoginLocationFlag;
    private Boolean multipleDeviceLoginFlag;
    /** RSK04 — kill switch */
    private Boolean rsk04MultipleAccountDetected;
    /** RSK05 */
    private Boolean hasLocationSpoofApp;
    private Boolean hasRecordFabricationTools;
    private Boolean isDeviceRooted;
    private Boolean hasUnofficialApks;
    private Double creditHungryScore;
    private Boolean isDeveloperModeOn;

    // ─── D6 — IDENTITY ────────────────────────────────────────────────────────
    /** IDN01 — self-sufficient (tier: 0, 1, 2) */
    private Integer identityVerificationTier;
    /** IDN02 — self-sufficient */
    private Double phoneVintageDays;
    // IDN03 city tier normalisation — NOT scored, ignored by engine

    // ─── D7 — TEMPORAL CONSISTENCY ────────────────────────────────────────────
    /** TMP01 — self-sufficient (months) */
    private Double consecutiveActiveMonths;
    /** TMP02 — self-sufficient */
    private Double incomeStabilityWindows;
    /** TMP03 */
    private Double completionRateDrift;
    private Double spendRatioDrift;
    /** TMP04 — self-sufficient */
    private Double longestOntimePaymentStreak;
    /** TMP05 */
    private Double postDisruptionIncomeRecoveryDays;
    private Double postDisruptionActivityRecoveryDays;
    /** TMP06 — self-sufficient */
    private Double tenureWeightedReliabilityScore;
}
