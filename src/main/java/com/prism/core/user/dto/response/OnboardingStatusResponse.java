package com.prism.core.user.dto.response;

import com.prism.core.common.enums.OnboardingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OnboardingStatusResponse {
    private UUID userId;
    private OnboardingStatus status;
    private boolean panVerified;
    private boolean profileComplete;
    private Instant submittedAt;
}
