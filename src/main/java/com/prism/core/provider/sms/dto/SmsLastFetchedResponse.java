package com.prism.core.provider.sms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsLastFetchedResponse {

    /**
     * Epoch millis of when the last PRISM score was *calculated* for this user.
     * Null if no score has ever been computed.
     */
    private Long scoreLastCalculated;

    /**
     * Epoch millis of when sms/ingest was last called for this user
     * (i.e., when SMS data was last submitted and characteristics were stored).
     * Null if SMS ingestion has never run.
     */
    private Long smsLastIngested;
}
