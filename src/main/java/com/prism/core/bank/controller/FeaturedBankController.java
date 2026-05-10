package com.prism.core.bank.controller;

import com.prism.core.bank.dto.FeaturedBankResponse;
import com.prism.core.bank.service.FeaturedBankService;
import com.prism.core.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
public class FeaturedBankController {

    private final FeaturedBankService featuredBankService;

    /**
     * Returns all active featured banks sorted by display order.
     * Used to render the "Partner Banks" screen in the Android app.
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<FeaturedBankResponse>>> getFeaturedBanks() {
        return ResponseEntity.ok(ApiResponse.success(featuredBankService.getActiveBanks()));
    }

    /**
     * Returns detailed info for a single bank.
     * Used when user taps on a bank card to see full details before clicking through.
     */
    @GetMapping("/featured/{id}")
    public ResponseEntity<ApiResponse<FeaturedBankResponse>> getBankById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(featuredBankService.getBankById(id)));
    }
}
