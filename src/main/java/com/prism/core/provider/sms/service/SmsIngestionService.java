package com.prism.core.provider.sms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.provider.sms.dto.SmsIngestRequest;
import com.prism.core.provider.sms.dto.SmsIngestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsIngestionService {

    private final ObjectMapper objectMapper;

    @Value("${prism.mock.sms-extractor-base-url}")
    private String smsExtractorBaseUrl;

    public SmsIngestResponse ingest(UUID userId, SmsIngestRequest request) {
        log.info("SMS ingestion for user={}, messages={}", userId, request.getMessages().size());

        try {
            // Call mock 3rd-party SMS characteristic extractor
            RestTemplate restTemplate = new RestTemplate();
            String url = smsExtractorBaseUrl + "/extract";

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId.toString());
            payload.put("messages", request.getMessages());

            @SuppressWarnings("unchecked")
            Map<String, String> characteristics = restTemplate.postForObject(
                    url, payload, Map.class);

            if (characteristics == null) characteristics = getMockCharacteristics();

            return SmsIngestResponse.builder()
                    .messagesReceived(request.getMessages().size())
                    .extractedCharacteristics(characteristics)
                    .processedSuccessfully(true)
                    .message("SMS characteristics extracted successfully")
                    .build();

        } catch (Exception e) {
            log.warn("SMS extractor call failed, using mock characteristics: {}", e.getMessage());
            return SmsIngestResponse.builder()
                    .messagesReceived(request.getMessages().size())
                    .extractedCharacteristics(getMockCharacteristics())
                    .processedSuccessfully(true)
                    .message("SMS characteristics extracted (mock fallback)")
                    .build();
        }
    }

    private Map<String, String> getMockCharacteristics() {
        Map<String, String> mock = new HashMap<>();
        mock.put("avg_weekly_credit_sms_90d", "12500.00");
        mock.put("income_spike_ratio", "0.15");
        mock.put("low_income_week_count_90d", "3");
        mock.put("avg_recovery_weeks", "1.5");
        mock.put("platform_income_source_count", "2");
        mock.put("has_non_gig_income", "false");
        mock.put("avg_month_end_balance_sms", "5200.00");
        mock.put("negative_balance_flag", "false");
        mock.put("discretionary_spend_ratio", "0.22");
        mock.put("gambling_fantasy_spend_flag", "false");
        mock.put("monthly_remittance_amount", "3000.00");
        mock.put("remittance_consistency_score", "0.75");
        mock.put("missed_instalment_count_90d", "0");
        mock.put("active_instalment_count", "1");
        return mock;
    }
}
