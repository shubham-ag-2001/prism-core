package com.prism.core.scoring.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.common.enums.JobStatus;
import com.prism.core.common.enums.ScoringStatus;
import com.prism.core.scoring.engine.model.ScoringInput;
import com.prism.core.scoring.engine.model.ScoringResult;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import com.prism.core.scoring.entity.ScoringJob;
import com.prism.core.scoring.repository.PrismScoreSnapshotRepository;
import com.prism.core.scoring.repository.ScoringJobRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Wires a ScoringJob through the full pipeline:
 *   1. Load job + user from DB
 *   2. Assemble ScoringInput (mock baseline for Phase 3 hackathon)
 *   3. Resolve engine version from registry
 *   4. Execute engine.compute()
 *   5. Persist results to PrismScoreSnapshot
 *   6. Mark job COMPLETE (or FAILED on exception)
 *
 * Phase 4 upgrade: replace buildDefaultInput() with real signal reading
 * from RawSignal + ProviderResponse rows for the authenticated user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoringPipelineOrchestrator {

    private final ScoringEngineRegistry            engineRegistry;
    private final ScoringJobRepository             jobRepository;
    private final PrismScoreSnapshotRepository     snapshotRepository;
    private final UserProfileRepository            userProfileRepository;
    private final ObjectMapper                     objectMapper;

    private static final int CACHE_DAYS = 30;

    @Transactional
    public void orchestrate(UUID jobId) {
        ScoringJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        try {
            job.setStatus(JobStatus.RUNNING);
            jobRepository.save(job);

            User user = job.getSnapshot().getUser();
            String version = job.getSnapshot().getRuleSetVersion();

            ScoringInput input = buildInput(user);

            ScoringEngineVersion engine = engineRegistry.get(version);
            ScoringResult result = engine.compute(input);
            log.info("[Orchestrator] job={} userId={} score={} band={}",
                    jobId, user.getId(), result.getPrismScore(), result.getScoreBand());

            PrismScoreSnapshot snapshot = job.getSnapshot();
            snapshot.setFinalScore(result.getPrismScore());
            snapshot.setScoreBand(result.getScoreBand());
            snapshot.setScoringStatus(ScoringStatus.COMPLETE);
            snapshot.setComputedAt(Instant.now());
            snapshot.setExpiresAt(Instant.now().plus(CACHE_DAYS, ChronoUnit.DAYS));
            snapshot.setDimensionBreakdownJson(objectMapper.writeValueAsString(result));
            snapshot.setKillSwitchesTriggered(objectMapper.writeValueAsString(result.getKillSwitchesTriggered()));
            snapshot.setAlertsJson(objectMapper.writeValueAsString(result.getAlerts()));
            snapshotRepository.save(snapshot);

            job.setStatus(JobStatus.COMPLETE);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);

        } catch (Exception e) {
            log.error("[Orchestrator] Pipeline FAILED for job={}", jobId, e);
            job.setStatus(JobStatus.FAILED);
            job.setFailureReason(e.getMessage());
            jobRepository.save(job);
        }
    }

    /**
     * Assemble ScoringInput for a user.
     *
     * Phase 3 / Hackathon: returns a realistic baseline mock input.
     * Phase 4: replace this with real data reads from RawSignal
     *           and ProviderResponse rows for the user.
     */
    private ScoringInput buildInput(User user) {
        return buildDefaultInput(user);
    }

    /**
     * Realistic baseline mock values for a typical gig worker.
     * All dimensions score moderately, producing a PRISM score in the 680-730 range.
     */
    private ScoringInput buildDefaultInput(User user) {
        return ScoringInput.builder()
                .userId(user.getId().toString())
                // D1 — Income
                .avgWeeklyCredit90d(3500.0).incomeSpikeRatio(1.15)
                .incomeCv(0.25)
                .incomeGrowthFebToMar(3.0).incomeGrowthMarToApr(2.5)
                .lowIncomeWeekCount90d(2.0).avgRecoveryWeeks(2.0)
                .seasonalAdjustmentFactor(0.96)
                .platformIncomeSourceCount(2.0).hasNonGigIncome(false)
                .incomeLast30d(15000.0).incomeLast90d(42000.0)
                // D2 — Activity
                .platformTenureDays(400.0)
                .orderCompletionRate(88.0)
                .ordersPerActiveDay(8.0)
                .activeDaysPerWeek(5.0)
                .accountAgeActiveRatio(0.60)
                .longestInactivityStreakDays(5.0).gapFrequency90d(2.0)
                .peakHourParticipationRate(0.55)
                .lateCancellationRatio(0.6).cancellationSpikeFlag(false)
                // D3 — Spending
                .walletTopupFrequency(6.0)
                .spendToEarnRatio(0.72)
                .avgMonthEndBalance(5500.0).negativeBalanceFlag(false)
                .spendVolatilityIndex(0.25)
                .discretionarySpendRatio(0.18).gamblingFantasySpendFlag(false)
                .largeTransactionFrequency(1.5)
                .missedInstalmentCount90d(0.0).activeInstalmentCount(1.0)
                .monthlyRemittanceAmount(2500.0).remittanceConsistencyScore(75.0)
                .spendingLast30d(11000.0).spendingLast90d(33000.0)
                .debtToIncomeRatio(0.15)
                // D4 — Social
                .avgCustomerRating(4.3)
                .ratingLast30d(4.35).ratingLast90d(4.3)
                .repeatCustomerRatio(0.18)
                .unresolvedDisputeCount(0.0).disputeSpikeFlag(false)
                .responseRateToOffers(0.65)
                // D5 — Risk
                .rsk01PriorDefault(false)
                .unusualLoginLocationFlag(false).multipleDeviceLoginFlag(false)
                .rsk04MultipleAccountDetected(false)
                .hasLocationSpoofApp(false).hasRecordFabricationTools(false)
                .isDeviceRooted(false).hasUnofficialApks(false)
                .creditHungryScore(15.0).isDeveloperModeOn(false)
                // D6 — Identity
                .identityVerificationTier(2)
                .phoneVintageDays(500.0)
                // D7 — Temporal
                .consecutiveActiveMonths(5.0)
                .incomeStabilityWindows(5.0)
                .completionRateDrift(-1.5).spendRatioDrift(2.0)
                .longestOntimePaymentStreak(8.0)
                .postDisruptionIncomeRecoveryDays(8.0)
                .postDisruptionActivityRecoveryDays(6.0)
                .tenureWeightedReliabilityScore(72.0)
                .build();
    }
}
