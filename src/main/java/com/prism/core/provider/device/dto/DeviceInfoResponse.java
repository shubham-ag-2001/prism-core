package com.prism.core.provider.device.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceInfoResponse {
    private boolean processedSuccessfully;
    private int signalsExtracted;
    private String message;

    // Derived flags (for transparency/debugging)
    private boolean isRooted;
    private boolean isDeveloperModeEnabled;
    private boolean isEmulator;
    private boolean hasLocationSpoofApp;
    private boolean hasRecordFabricationTools;
    private boolean hasUnofficialApks;
    private double creditHungryScore;
}
