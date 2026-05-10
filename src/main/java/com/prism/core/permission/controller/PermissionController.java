package com.prism.core.permission.controller;

import com.prism.core.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    @GetMapping("/required")
    public ResponseEntity<ApiResponse<List<PermissionMetadata>>> getRequiredPermissions() {
        List<PermissionMetadata> permissions = List.of(
                new PermissionMetadata("READ_SMS",
                        "android.permission.READ_SMS",
                        "Required to analyse your income and spending patterns from bank SMS",
                        true),
                new PermissionMetadata("RECEIVE_SMS",
                        "android.permission.RECEIVE_SMS",
                        "Required to receive real-time transaction updates",
                        false),
                new PermissionMetadata("ACCESS_FINE_LOCATION",
                        "android.permission.ACCESS_FINE_LOCATION",
                        "Used to determine city-tier for cost-of-living normalisation",
                        false),
                new PermissionMetadata("READ_PHONE_STATE",
                        "android.permission.READ_PHONE_STATE",
                        "Used to detect multiple SIM cards and device integrity",
                        false)
        );
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    public record PermissionMetadata(
            String name,
            String androidPermission,
            String rationale,
            boolean required
    ) {}
}
