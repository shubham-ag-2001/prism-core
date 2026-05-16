package com.prism.core.provider.sms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsIngestResponse {
    private int     messagesReceived;
    private int     signalsStored;       // number of characteristics saved to DB
    private boolean processedSuccessfully;
    private String  message;
}
