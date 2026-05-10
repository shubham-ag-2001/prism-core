package com.prism.core.auth.service;

import com.prism.core.auth.dto.request.RefreshTokenRequest;
import com.prism.core.auth.dto.request.SendOtpRequest;
import com.prism.core.auth.dto.request.VerifyOtpRequest;
import com.prism.core.auth.dto.response.AuthTokenResponse;
import com.prism.core.auth.dto.response.OtpSentResponse;
import com.prism.core.auth.entity.OtpRecord;
import com.prism.core.auth.entity.RefreshToken;
import com.prism.core.auth.repository.OtpRecordRepository;
import com.prism.core.auth.repository.RefreshTokenRepository;
import com.prism.core.common.enums.OtpPurpose;
import com.prism.core.common.enums.UserRole;
import com.prism.core.common.exception.ErrorCode;
import com.prism.core.common.exception.PrismException;
import com.prism.core.common.security.JwtService;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final OtpRecordRepository    otpRecordRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService             jwtService;

    @Value("${prism.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${prism.otp.mock-enabled:true}")
    private boolean mockOtpEnabled;

    @Value("${prism.jwt.refresh-token-expiry-ms:2592000000}")
    private long refreshTokenExpiryMs;

    // ─── Send OTP ────────────────────────────────────────────────────────────

    @Transactional
    public OtpSentResponse sendOtp(SendOtpRequest request) {
        String phone = request.getPhone();
        String otp   = generateOtp();

        OtpRecord record = OtpRecord.builder()
                .phone(phone)
                .otpHash(hash(otp))
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(Instant.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES))
                .build();

        otpRecordRepository.save(record);

        if (mockOtpEnabled) {
            log.info("🔑 [MOCK OTP] phone={} otp={}", phone, otp);
        }
        // In production: call SMS gateway here

        return OtpSentResponse.builder()
                .phone(phone)
                .message("OTP sent successfully")
                .expiresInMinutes(otpExpiryMinutes)
                .build();
    }

    // ─── Verify OTP ──────────────────────────────────────────────────────────

    @Transactional
    public AuthTokenResponse verifyOtp(VerifyOtpRequest request) {
        OtpRecord record = otpRecordRepository
                .findTopByPhoneAndPurposeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        request.getPhone(), OtpPurpose.LOGIN, Instant.now())
                .orElseThrow(() -> new PrismException(
                        "OTP not found or expired", org.springframework.http.HttpStatus.UNAUTHORIZED,
                        ErrorCode.OTP_EXPIRED));

        if (!record.getOtpHash().equals(hash(request.getOtp()))) {
            throw new PrismException("Invalid OTP",
                    org.springframework.http.HttpStatus.UNAUTHORIZED, ErrorCode.OTP_INVALID);
        }

        record.setUsed(true);
        otpRecordRepository.save(record);

        // Create user if first login
        boolean isNewUser = !userRepository.existsByPhone(request.getPhone());
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .phone(request.getPhone())
                                .role(UserRole.GIG_WORKER)
                                .build()));

        return issueTokens(user, isNewUser);
    }

    // ─── Refresh Token ───────────────────────────────────────────────────────

    @Transactional
    public AuthTokenResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hash(request.getRefreshToken());
        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndIsRevokedFalse(tokenHash)
                .orElseThrow(() -> PrismException.unauthorized("Invalid or revoked refresh token"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw PrismException.unauthorized("Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(stored.getUser(), false);
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    @Transactional
    public void logout(java.util.UUID userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private AuthTokenResponse issueTokens(User user, boolean isNewUser) {
        String accessToken  = jwtService.generateAccessToken(user.getId(), user.getPhone(), user.getRole());
        String refreshRaw   = generateSecureToken();
        String refreshHash  = hash(refreshRaw);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshHash)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiryMs))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshRaw)
                .tokenType("Bearer")
                .expiresInSeconds(86400)
                .userId(user.getId())
                .phone(user.getPhone())
                .role(user.getRole())
                .isNewUser(isNewUser)
                .build();
    }

    private String generateOtp() {
        SecureRandom rng = new SecureRandom();
        return String.format("%06d", rng.nextInt(1_000_000));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
