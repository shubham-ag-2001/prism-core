package com.prism.core.provider.employer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Full platform response covering all D2 (Activity), D4 (Social), and D7 (Temporal) fields
 * that are sourced from the employer platform API.
 */
@Data
@Builder
public class EmployerDataResponse {

    // ── Identity ───────────────────────────────────────────────────────────────
    private String platformKey;
    private String platformName;
    private String accountStatus;            // ACTIVE, SUSPENDED, INACTIVE
    private String employerCategory;         // FOOD_DELIVERY, RIDE_HAILING, etc.
    private boolean isMock;

    // ── D2 — Activity ──────────────────────────────────────────────────────────
    private Integer accountTenureDays;       // ACT01 — platform tenure in days
    private Double orderCompletionRate;      // ACT02 — % e.g. 92.5
    private Integer totalCompletedOrders;
    private Integer activeOrdersLast30Days;
    private Double ordersPerActiveDay;       // ACT03
    private Double activeDaysPerWeek;        // ACT04
    private Double accountAgeActiveRatio;    // ACT05
    private Integer longestInactivityStreakDays; // ACT06
    private Integer gapFrequency90d;         // ACT06
    private Double peakHourParticipationRate; // ACT07
    private Double cancellationRate;         // ACT08 raw rate e.g. 0.03
    private Boolean cancellationSpikeFlag;   // ACT08

    // ── D4 — Social ────────────────────────────────────────────────────────────
    private Double overallRating;            // SOC01
    private Double ratingLast30d;            // SOC02
    private Double ratingLast90d;            // SOC02
    private Double repeatCustomerRatio;      // SOC03
    private Integer unresolvedDisputeCount;  // SOC04
    private Boolean disputeSpikeFlag;        // SOC04
    private Double responseRateToOffers;     // SOC05

    // ── D7 — Temporal ─────────────────────────────────────────────────────────
    private Double consecutiveActiveMonths;  // TMP01
    private Double completionRateDrift;      // TMP03 (trend: negative = improving)
    private Double tenureWeightedReliabilityScore; // TMP06

    // ── Financials (supporting) ────────────────────────────────────────────────
    private Integer totalEarningsLast90DaysRupees;
}
