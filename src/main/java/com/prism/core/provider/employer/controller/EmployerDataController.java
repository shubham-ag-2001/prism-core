package com.prism.core.provider.employer.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.provider.employer.dto.EmployerDataResponse;
import com.prism.core.provider.employer.dto.EmployerPlatformDto;
import com.prism.core.provider.employer.dto.FetchEmployerDataRequest;
import com.prism.core.provider.employer.service.EmployerProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers/employer")
@RequiredArgsConstructor
public class EmployerDataController {

    private final EmployerProviderService employerProviderService;

    /**
     * Returns all supported employer platforms (from DB).
     * Android uses this to build the platform picker dropdown in onboarding.
     */
    @GetMapping("/platforms")
    public ResponseEntity<ApiResponse<List<EmployerPlatformDto>>> getPlatforms() {
        return ResponseEntity.ok(ApiResponse.success(employerProviderService.getActivePlatforms()));
    }

    /**
     * Fetches employer data for a given platform (mock for hackathon).
     * Returns standardized activity metrics regardless of the employer.
     */
    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<EmployerDataResponse>> fetchEmployerData(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody FetchEmployerDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                employerProviderService.fetchEmployerData(
                        userDetails.getUserId(), request.getPlatformKey())));
    }
}
