package com.prism.core.bank.controller;

import com.prism.core.bank.dto.CreateBankRequest;
import com.prism.core.bank.dto.FeaturedBankResponse;
import com.prism.core.bank.service.FeaturedBankService;
import com.prism.core.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/banks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BankAdminController {

    private final FeaturedBankService featuredBankService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeaturedBankResponse>> addBank(
            @Valid @RequestBody CreateBankRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank added successfully",
                        featuredBankService.createBank(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeaturedBankResponse>> updateBank(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBankRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bank updated successfully",
                featuredBankService.updateBank(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeBank(@PathVariable UUID id) {
        featuredBankService.removeBank(id);
        return ResponseEntity.ok(ApiResponse.success("Bank removed successfully"));
    }
}
