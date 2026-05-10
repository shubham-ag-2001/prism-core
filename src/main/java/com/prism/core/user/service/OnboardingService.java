package com.prism.core.user.service;

import com.prism.core.auth.entity.OtpRecord;
import com.prism.core.auth.repository.OtpRecordRepository;
import com.prism.core.common.enums.OnboardingStatus;
import com.prism.core.common.enums.OtpPurpose;
import com.prism.core.common.enums.VerificationStatus;
import com.prism.core.common.exception.ErrorCode;
import com.prism.core.common.exception.PrismException;
import com.prism.core.user.dto.request.OnboardingRequest;
import com.prism.core.user.dto.request.VerifyPanOtpRequest;
import com.prism.core.user.dto.response.OnboardingStatusResponse;
import com.prism.core.user.entity.OnboardingData;
import com.prism.core.user.entity.PanVerification;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.OnboardingDataRepository;
import com.prism.core.user.repository.PanVerificationRepository;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository            userRepository;
    private final OnboardingDataRepository  onboardingDataRepository;
    private final PanVerificationRepository panVerificationRepository;
    private final OtpRecordRepository       otpRecordRepository;

    @Value("${prism.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${prism.otp.mock-enabled:true}")
    private boolean mockOtpEnabled;

    // ─── Step 1: Submit Onboarding ────────────────────────────────────────────
    /**
     * Saves employer/PAN data, sets status to IN_PROGRESS,
     * and sends a PAN_VERIFICATION OTP to the user's phone.
     *
     * The OTP acts as the user's informed consent to run a KYC check on their PAN.
     * In production, this would trigger a real NSDL/Karza API call on OTP verification.
     */
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

        // Save / update onboarding data
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

        // Send PAN verification OTP (same infrastructure as login OTP)
        sendPanVerificationOtp(user.getPhone());

        log.info("Onboarding submitted for user={}. PAN verification OTP sent.", userId);
        return buildStatusResponse(userId, data, null);
    }

    // ─── Step 2: Verify PAN OTP ───────────────────────────────────────────────
    /**
     * Validates the PAN_VERIFICATION OTP the user received after submitting onboarding.
     * On success:
     *   - Calls mock PAN verifier (real API in production)
     *   - Persists PanVerification record
     *   - Marks onboarding as COMPLETED
     */
    @Transactional
    public OnboardingStatusResponse verifyPanOtp(UUID userId, VerifyPanOtpRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PrismException.notFound("User not found"));

        OnboardingData data = onboardingDataRepository.findByUserId(userId)
                .orElseThrow(() -> new PrismException(
                        "No onboarding data found. Please submit onboarding first.",
                        HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST));

        // Validate the OTP
        OtpRecord record = otpRecordRepository
                .findTopByPhoneAndPurposeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        user.getPhone(), OtpPurpose.PAN_VERIFICATION, Instant.now())
                .orElseThrow(() -> new PrismException(
                        "OTP not found or expired. Please resubmit onboarding to get a new OTP.",
                        HttpStatus.UNAUTHORIZED, ErrorCode.OTP_EXPIRED));

        if (!record.getOtpHash().equals(hash(request.getOtp()))) {
            throw new PrismException("Invalid OTP", HttpStatus.UNAUTHORIZED, ErrorCode.OTP_INVALID);
        }

        record.setUsed(true);
        otpRecordRepository.save(record);

        // Mock PAN verification — in production: call NSDL/Karza API here
        PanVerification verification = runMockPanVerification(user, data.getPanNumber());

        // Mark onboarding complete
        data.setStatus(OnboardingStatus.COMPLETED);
        onboardingDataRepository.save(data);
        log.info("PAN verified and onboarding completed for user={}", userId);

        return buildStatusResponse(userId, data, verification);
    }

    // ─── Get Status ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(UUID userId) {
        OnboardingData data = onboardingDataRepository.findByUserId(userId).orElse(null);
        PanVerification pan = panVerificationRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        return buildStatusResponse(userId, data, pan);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private void sendPanVerificationOtp(String phone) {
        String otp = generateOtp();
        OtpRecord record = OtpRecord.builder()
                .phone(phone)
                .otpHash(hash(otp))
                .purpose(OtpPurpose.PAN_VERIFICATION)
                .expiresAt(Instant.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES))
                .build();
        otpRecordRepository.save(record);

        if (mockOtpEnabled) {
            log.info("🔑 [MOCK PAN OTP] phone={} otp={}", phone, otp);
        }
        // In production: call SMS gateway with "Your PRISM PAN verification OTP is: XXXXXX"
    }

    /**
     * Mock PAN verification — returns VERIFIED for all PANs.
     * In production: replace body with a real HTTP call to Karza/Sandbox/NSDL.
     */
    private PanVerification runMockPanVerification(User user, String panNumber) {
        log.info("[MOCK] Verifying PAN={} for user={}", panNumber, user.getId());
        PanVerification verification = PanVerification.builder()
                .user(user)
                .panNumber(panNumber)
                .verificationStatus(VerificationStatus.VERIFIED)
                .verificationResponseJson(
                        "{\"status\":\"VALID\",\"name\":\"MOCK USER\"," +
                        "\"aadhaarLinked\":true,\"mock\":true}")
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

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
