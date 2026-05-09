package com.prism.core.user.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.common.security.PrismUserDetails;
import com.prism.core.user.dto.request.UpdateProfileRequest;
import com.prism.core.user.dto.response.UserProfileResponse;
import com.prism.core.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(
            @AuthenticationPrincipal PrismUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userDetails.getUserId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal PrismUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated", userService.updateProfile(userDetails.getUserId(), request)));
    }
}
