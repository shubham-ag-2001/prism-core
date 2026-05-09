package com.prism.core.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpSentResponse {
    private String phone;
    private String message;
    private int expiresInMinutes;
}
