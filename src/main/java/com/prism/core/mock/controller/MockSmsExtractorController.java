package com.prism.core.mock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock 3rd-party SMS Characteristic Extractor.
 *
 * In production this would be an external API.
 * For the hackathon, it's hosted within the same app at /mock/sms-extractor/**
 */
@Slf4j
@RestController
@RequestMapping("/mock/sms-extractor")
public class MockSmsExtractorController {

    @PostMapping("/extract")
    public ResponseEntity<Map<String, String>> extract(@RequestBody Map<String, Object> payload) {
        log.info("[MOCK SMS EXTRACTOR] Received request for userId={}", payload.get("userId"));

        Map<String, String> characteristics = new HashMap<>();
        // Income dimension
        characteristics.put("avg_weekly_credit_sms_90d", "14200.00");
        characteristics.put("income_spike_ratio", "0.12");
        characteristics.put("low_income_week_count_90d", "2");
        characteristics.put("avg_recovery_weeks", "1.2");
        characteristics.put("platform_income_source_count", "2");
        characteristics.put("has_non_gig_income", "true");
        // Spending dimension
        characteristics.put("avg_month_end_balance_sms", "6800.00");
        characteristics.put("negative_balance_flag", "false");
        characteristics.put("discretionary_spend_ratio", "0.18");
        characteristics.put("gambling_fantasy_spend_flag", "false");
        characteristics.put("monthly_remittance_amount", "4000.00");
        characteristics.put("remittance_consistency_score", "0.82");
        characteristics.put("missed_instalment_count_90d", "0");
        characteristics.put("active_instalment_count", "2");
        // Temporal
        characteristics.put("income_stability_window", "85");
        characteristics.put("post_disruption_income_recovery_days", "7");

        return ResponseEntity.ok(characteristics);
    }
}
