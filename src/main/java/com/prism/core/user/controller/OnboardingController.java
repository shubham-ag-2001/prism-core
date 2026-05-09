package com.prism.core.user.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.user.dto.request.OnboardingRequest;
import com.prism.core.user.dto.response.OnboardingStatusResponse;
import com.prism.core.user.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> submitOnboarding(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Onboarding submitted",
                onboardingService.submitOnboarding(userDetails.getUserId(), request)));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getOnboardingStatus(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                onboardingService.getOnboardingStatus(userDetails.getUserId())));
    }
}
