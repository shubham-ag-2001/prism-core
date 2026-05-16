package com.prism.core.user.service;

import com.prism.core.audit.service.AuditService;
import com.prism.core.auth.repository.RefreshTokenRepository;
import com.prism.core.common.exception.PrismException;
import com.prism.core.user.dto.request.UpdateProfileRequest;
import com.prism.core.user.dto.response.UserProfileResponse;
import com.prism.core.user.entity.User;
import com.prism.core.user.entity.UserProfile;
import com.prism.core.user.entity.OnboardingData;
import com.prism.core.user.repository.UserProfileRepository;
import com.prism.core.user.repository.UserRepository;
import com.prism.core.user.repository.OnboardingDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository        userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OnboardingDataRepository onboardingDataRepository;
    private final AuditService           auditService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PrismException.notFound("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        OnboardingData onboardingData = onboardingDataRepository.findByUserId(userId).orElse(null);

        return UserProfileResponse.builder()
                .userId(user.getId())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.isActive())
                .fullName(profile != null ? profile.getFullName() : null)
                .email(profile != null ? profile.getEmail() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .city(profile != null ? profile.getCity() : null)
                .state(profile != null ? profile.getState() : null)
                .pan(onboardingData != null ? onboardingData.getPanNumber() : null)
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PrismException.notFound("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());

        if (request.getFullName()    != null) profile.setFullName(request.getFullName());
        if (request.getEmail()       != null) profile.setEmail(request.getEmail());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getCity()        != null) profile.setCity(request.getCity());
        if (request.getState()       != null) profile.setState(request.getState());

        userProfileRepository.save(profile);
        return getProfile(userId);
    }

    /**
     * Soft-delete: marks user inactive + revokes all refresh tokens.
     * Preserves audit log, scoring history, and lender records for compliance.
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PrismException.notFound("User not found"));

        user.setActive(false);
        userRepository.save(user);

        // Revoke all active sessions
        refreshTokenRepository.deleteAllByUserId(userId);

        auditService.log("USER", userId.toString(), "ACCOUNT_DELETED", userId, null);
    }
}
