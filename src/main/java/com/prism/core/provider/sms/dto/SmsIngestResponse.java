package com.prism.core.provider.sms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SmsIngestResponse {
    private int messagesReceived;
    private Map<String, String> extractedCharacteristics;  // key -> value
    private boolean processedSuccessfully;
    private String message;
}
