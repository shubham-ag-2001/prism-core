package com.prism.core.provider.sms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.entity.RawSignal;
import com.prism.core.provider.repository.RawSignalRepository;
import com.prism.core.provider.sms.dto.SmsIngestRequest;
import com.prism.core.provider.sms.dto.SmsIngestResponse;
import com.prism.core.provider.sms.dto.SmsLastFetchedResponse;
import com.prism.core.scoring.repository.PrismScoreSnapshotRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsIngestionService {

    private final ObjectMapper                  objectMapper;
    private final RawSignalRepository           rawSignalRepository;
    private final UserRepository                userRepository;
    private final PrismScoreSnapshotRepository  snapshotRepository;

    @Value("${prism.mock.sms-extractor-base-url}")
    private String smsExtractorBaseUrl;

    // ── Key used to record the last SMS ingestion timestamp in raw_signal ─────
    private static final String SMS_INGESTED_AT_KEY = "_sms_ingested_at";

    // ── 1. INGEST ─────────────────────────────────────────────────────────────

    /**
     * Flow:
     *   sms/ingest API  →  3rd-party extractor (or mock fallback)
     *                   →  persist characteristics as SMS raw_signal rows in DB
     *                   →  return simple ack to caller (no raw characteristics exposed)
     */
    @Transactional
    public SmsIngestResponse ingest(UUID userId, SmsIngestRequest request) {
        log.info("SMS ingestion for user={}, messages={}", userId, request.getMessages().size());

        // ── Step 1: call 3rd-party extractor ──────────────────────────────────
        Map<String, String> characteristics = callExtractor(userId, request.getMessages());

        // ── Step 2: persist all characteristics to raw_signal ─────────────────
        User user = userRepository.getReferenceById(userId);
        persistSignals(user, characteristics);

        // ── Step 3: stamp the ingestion time (used by last_fetched) ───────────
        stampIngestedAt(user);

        log.info("Persisted {} SMS signals for user={}", characteristics.size(), userId);

        return SmsIngestResponse.builder()
                .messagesReceived(request.getMessages().size())
                .signalsStored(characteristics.size())
                .processedSuccessfully(true)
                .message("SMS processed and " + characteristics.size() + " signals stored")
                .build();
    }

    // ── 2. LAST FETCHED ───────────────────────────────────────────────────────

    /**
     * Returns:
     *  - smsLastIngested  : epoch-ms of last sms/ingest call (from raw_signal sentinel row)
     *  - scoreLastCalculated : epoch-ms of last COMPLETED score snapshot
     */
    public SmsLastFetchedResponse getLastFetched(UUID userId) {

        // sms_last_ingested: look for the sentinel signal key
        Long smsLastIngested = rawSignalRepository
                .findByUserIdAndProviderTypeOrderByCreatedAtDesc(userId, ProviderType.SMS)
                .stream()
                .filter(s -> SMS_INGESTED_AT_KEY.equals(s.getSignalKey()))
                .findFirst()
                .map(s -> Long.parseLong(s.getSignalValue()))
                .orElse(null);

        // score_last_calculated: latest COMPLETE snapshot
        Long scoreLastCalculated = snapshotRepository
                .findTopByUserIdOrderByComputedAtDesc(userId)
                .map(snap -> snap.getComputedAt().toEpochMilli())
                .orElse(null);

        return SmsLastFetchedResponse.builder()
                .smsLastIngested(smsLastIngested)
                .scoreLastCalculated(scoreLastCalculated)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Map<String, String> callExtractor(UUID userId, List<SmsIngestRequest.SmsMessage> messages) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = smsExtractorBaseUrl + "/extract";

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId.toString());
            payload.put("messages", messages);   // forward the full objects (sender, body, timestamp)

            @SuppressWarnings("unchecked")
            Map<String, String> result = restTemplate.postForObject(url, payload, Map.class);

            if (result != null && !result.isEmpty()) {
                log.info("3rd-party extractor returned {} characteristics", result.size());
                return result;
            }
        } catch (Exception e) {
            log.warn("SMS extractor unavailable ({}), using mock fallback", e.getMessage());
        }
        return getMockCharacteristics();
    }

    private void persistSignals(User user, Map<String, String> characteristics) {
        List<RawSignal> signals = new ArrayList<>();
        for (Map.Entry<String, String> entry : characteristics.entrySet()) {
            signals.add(RawSignal.builder()
                    .user(user)
                    .providerType(ProviderType.SMS)
                    .signalKey(entry.getKey())
                    .signalValue(entry.getValue())
                    .build());
        }
        rawSignalRepository.saveAll(signals);
    }

    /** Writes a sentinel row so we can query when SMS was last ingested. */
    private void stampIngestedAt(User user) {
        rawSignalRepository.save(RawSignal.builder()
                .user(user)
                .providerType(ProviderType.SMS)
                .signalKey(SMS_INGESTED_AT_KEY)
                .signalValue(String.valueOf(Instant.now().toEpochMilli()))
                .build());
    }

    // ── Mock fallback ─────────────────────────────────────────────────────────

    private Map<String, String> getMockCharacteristics() {
        Map<String, String> mock = new LinkedHashMap<>();
        // D1 — Income signals
        mock.put("avg_weekly_credit_sms_90d",    "3500.00");
        mock.put("income_spike_ratio",            "1.15");
        mock.put("income_cv",                     "0.25");
        mock.put("income_growth_feb_to_mar",      "3.0");
        mock.put("income_growth_mar_to_apr",      "2.5");
        mock.put("low_income_week_count_90d",     "2");
        mock.put("avg_recovery_weeks",            "2.0");
        mock.put("seasonal_adjustment_factor",    "0.96");
        mock.put("platform_income_source_count",  "2");
        mock.put("has_non_gig_income",            "false");
        mock.put("income_last_30d",               "15000.00");
        mock.put("income_last_90d",               "42000.00");
        // D3 — Spending signals
        mock.put("wallet_topup_frequency",        "6.0");
        mock.put("spend_to_earn_ratio",           "0.72");
        mock.put("avg_month_end_balance_sms",     "5500.00");
        mock.put("negative_balance_flag",         "false");
        mock.put("spend_volatility_index",        "0.25");
        mock.put("discretionary_spend_ratio",     "0.18");
        mock.put("gambling_fantasy_spend_flag",   "false");
        mock.put("large_transaction_frequency",   "1.5");
        mock.put("missed_instalment_count_90d",   "0");
        mock.put("active_instalment_count",       "1");
        mock.put("monthly_remittance_amount",     "2500.00");
        mock.put("remittance_consistency_score",  "75.0");
        mock.put("spending_last_30d",             "11000.00");
        mock.put("spending_last_90d",             "33000.00");
        mock.put("debt_to_income_ratio",          "0.15");
        // D7 — Temporal
        mock.put("income_stability_windows",                 "5.0");
        mock.put("spend_ratio_drift",                        "2.0");
        mock.put("post_disruption_income_recovery_days",     "8.0");
        mock.put("post_disruption_activity_recovery_days",   "6.0");
        return mock;
    }
}
