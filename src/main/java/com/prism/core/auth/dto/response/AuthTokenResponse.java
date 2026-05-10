package com.prism.core.auth.dto.response;

import com.prism.core.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresInSeconds;
    private UUID userId;
    private String phone;
    private UserRole role;
    private boolean isNewUser;
}
