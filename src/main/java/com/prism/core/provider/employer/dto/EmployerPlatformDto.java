package com.prism.core.provider.employer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployerPlatformDto {
    private String platformKey;
    private String displayName;
    private String category;
}
