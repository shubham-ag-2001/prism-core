package com.prism.core.provider.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeviceInfoRequest {

    /** Full list of installed app package names from the device */
    private List<String> appList;

    @NotNull(message = "Device metadata is required")
    private DeviceMetadata deviceMetadata;

    @Data
    public static class DeviceMetadata {
        private boolean isRooted;
        private boolean isDeveloperModeEnabled;
        private boolean isEmulator;
        private String simIccid;    // null if no SIM
    }
}
