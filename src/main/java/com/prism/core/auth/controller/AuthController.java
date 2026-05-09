package com.prism.core.auth.controller;

import com.prism.core.auth.dto.request.RefreshTokenRequest;
import com.prism.core.auth.dto.request.SendOtpRequest;
import com.prism.core.auth.dto.request.VerifyOtpRequest;
import com.prism.core.auth.dto.response.AuthTokenResponse;
import com.prism.core.auth.dto.response.OtpSentResponse;
import com.prism.core.auth.service.AuthService;
import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<OtpSentResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.sendOtp(request)));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyOtp(request)));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        authService.logout(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
