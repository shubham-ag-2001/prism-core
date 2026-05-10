package com.prism.core.provider.sms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SmsIngestRequest {

    @NotNull(message = "SMS list cannot be null")
    private List<SmsMessage> messages;

    @Data
    public static class SmsMessage {
        private String sender;
        private String body;
        private Long timestamp;          // epoch millis
    }
}
