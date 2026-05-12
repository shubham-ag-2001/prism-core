package com.prism.core.provider.device.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.provider.device.dto.DeviceInfoRequest;
import com.prism.core.provider.device.dto.DeviceInfoResponse;
import com.prism.core.provider.device.service.DeviceSignalProcessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers/device-info")
@RequiredArgsConstructor
public class DeviceInfoController {

    private final DeviceSignalProcessorService deviceSignalProcessorService;

    /**
     * Called by the Android app on startup to ingest device integrity signals.
     * The app sends the full installed app list and device metadata.
     * The backend derives risk flags and stores them for scoring.
     */
    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<DeviceInfoResponse>> ingestDeviceInfo(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody DeviceInfoRequest request) {
        DeviceInfoResponse response = deviceSignalProcessorService.process(
                userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(
                "Device signals processed successfully", response));
    }
}
