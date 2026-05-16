package com.prism.core.provider.sms.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.provider.sms.dto.SmsIngestRequest;
import com.prism.core.provider.sms.dto.SmsIngestResponse;
import com.prism.core.provider.sms.dto.SmsLastFetchedResponse;
import com.prism.core.provider.sms.service.SmsIngestionService;
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

    /**
     * GET /api/v1/providers/sms/last_fetched
     *
     * Returns:
     *  - smsLastIngested      : epoch-ms when sms/ingest was last called for this user
     *  - scoreLastCalculated  : epoch-ms when the PRISM score was last calculated
     *
     * The FE uses smsLastIngested to decide whether to re-trigger SMS ingestion
     * (allowed once every 30 days), and scoreLastCalculated to show the score age.
     */
    @GetMapping("/last_fetched")
    public ResponseEntity<ApiResponse<SmsLastFetchedResponse>> getLastFetched(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        SmsLastFetchedResponse response = smsIngestionService.getLastFetched(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Timestamps retrieved", response));
    }

    /**
     * POST /api/v1/providers/sms/ingest
     *
     * Flow: receive raw SMS messages  →  call 3rd-party extractor (or mock)
     *       →  persist extracted characteristics as raw_signal rows in DB
     *       →  return simple ack (characteristics are NOT returned to caller)
     */
    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<SmsIngestResponse>> ingestSms(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody SmsIngestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "SMS data received and processed",
                smsIngestionService.ingest(userDetails.getUserId(), request)));
    }
}
