package com.prism.core.provider.sms.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.provider.sms.dto.SmsIngestRequest;
import com.prism.core.provider.sms.dto.SmsIngestResponse;
import com.prism.core.provider.sms.service.SmsIngestionService;
import com.prism.core.scoring.service.ScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers/sms")
@RequiredArgsConstructor
public class SmsIngestionController {

    private final SmsIngestionService smsIngestionService;
    private final ScoringService scoringService;

    @GetMapping("/last_fetched")
    public ResponseEntity<ApiResponse<Long>> getLastFetched(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        Long lastComputedTime = scoringService.getLastScoreComputedTime(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(
                lastComputedTime != null ? "Last score calculation time retrieved" : "No score calculated yet",
                lastComputedTime));
    }

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<SmsIngestResponse>> ingestSms(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody SmsIngestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "SMS data received and processed",
                smsIngestionService.ingest(userDetails.getUserId(), request)));
    }
}
