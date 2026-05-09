package com.prism.core.user.service;

import com.prism.core.common.enums.OnboardingStatus;
import com.prism.core.common.enums.VerificationStatus;
import com.prism.core.common.exception.ErrorCode;
import com.prism.core.common.exception.PrismException;
import com.prism.core.user.dto.request.OnboardingRequest;
import com.prism.core.user.dto.response.OnboardingStatusResponse;
import com.prism.core.user.entity.OnboardingData;
import com.prism.core.user.entity.PanVerification;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.OnboardingDataRepository;
import com.prism.core.user.repository.PanVerificationRepository;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository           userRepository;
    private final OnboardingDataRepository onboardingDataRepository;
    private final PanVerificationRepository panVerificationRepository;

    @Transactional
    public OnboardingStatusResponse submitOnboarding(UUID userId, OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PrismException.notFound("User not found"));

        // PAN deduplication check (excluding current user)
        boolean panTakenByOther = onboardingDataRepository
                .existsByPanNumberAndUserIdNot(request.getPanNumber(), userId);
        if (panTakenByOther) {
            throw PrismException.conflict(
                    "This PAN is already associated with another account. " +
                    "If this is you, please contact support.",
                    ErrorCode.PAN_ALREADY_EXISTS);
        }

        // Create or update onboarding data
        OnboardingData data = onboardingDataRepository.findByUserId(userId)
                .orElseGet(() -> OnboardingData.builder().user(user).build());

        data.setPanNumber(request.getPanNumber());
        data.setEmployerName(request.getEmployerName());
        data.setEmployerType(request.getEmployerType());
        data.setWorkStartDate(request.getWorkStartDate());
        data.setWorkHistoryJson(request.getWorkHistoryJson());
        data.setStatus(OnboardingStatus.IN_PROGRESS);
        data.setSubmittedAt(Instant.now());
        onboardingDataRepository.save(data);

        // Trigger mock PAN verification
        PanVerification panVerification = verifyPanMock(user, request.getPanNumber());

        if (panVerification.getVerificationStatus() == VerificationStatus.VERIFIED) {
            data.setStatus(OnboardingStatus.COMPLETED);
            onboardingDataRepository.save(data);
            log.info("Onboarding completed for user={}", userId);
        }

        return buildStatusResponse(userId, data, panVerification);
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(UUID userId) {
        OnboardingData data = onboardingDataRepository.findByUserId(userId).orElse(null);
        PanVerification pan = panVerificationRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        return buildStatusResponse(userId, data, pan);
    }

    // ─── Mock PAN verification ───────────────────────────────────────────────

    private PanVerification verifyPanMock(User user, String panNumber) {
        log.info("[MOCK] Verifying PAN={} for user={}", panNumber, user.getId());

        // Mock: all PANs verify successfully in demo mode
        PanVerification verification = PanVerification.builder()
                .user(user)
                .panNumber(panNumber)
                .verificationStatus(VerificationStatus.VERIFIED)
                .verificationResponseJson("{\"status\":\"VALID\",\"name\":\"MOCK USER\",\"mock\":true}")
                .verifiedAt(Instant.now())
                .build();
        return panVerificationRepository.save(verification);
    }

    private OnboardingStatusResponse buildStatusResponse(UUID userId,
                                                          OnboardingData data,
                                                          PanVerification pan) {
        return OnboardingStatusResponse.builder()
                .userId(userId)
                .status(data != null ? data.getStatus() : OnboardingStatus.PENDING)
                .panVerified(pan != null && pan.getVerificationStatus() == VerificationStatus.VERIFIED)
                .profileComplete(data != null && data.getStatus() == OnboardingStatus.COMPLETED)
                .submittedAt(data != null ? data.getSubmittedAt() : null)
                .build();
    }
}
