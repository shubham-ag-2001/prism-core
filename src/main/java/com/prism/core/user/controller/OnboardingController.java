package com.prism.core.user.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.user.dto.request.OnboardingRequest;
import com.prism.core.user.dto.request.VerifyPanOtpRequest;
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

    /**
     * Step 1: Submit PAN + employer data.
     * Saves to DB, sets status IN_PROGRESS, and sends a PAN_VERIFICATION OTP
     * to the user's registered phone. OTP visible in console (mock mode).
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> submitOnboarding(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Onboarding data saved. OTP sent to your phone for PAN verification.",
                onboardingService.submitOnboarding(userDetails.getUserId(), request)));
    }

    /**
     * Step 2: Verify the PAN OTP received after onboarding submission.
     * On success: runs mock PAN KYC check and marks onboarding COMPLETED.
     */
    @PostMapping("/verify-pan")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> verifyPanOtp(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody VerifyPanOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "PAN verified successfully. Onboarding complete!",
                onboardingService.verifyPanOtp(userDetails.getUserId(), request)));
    }

    /**
     * Get current onboarding status — used to determine app routing on login.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getOnboardingStatus(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                onboardingService.getOnboardingStatus(userDetails.getUserId())));
    }
}
