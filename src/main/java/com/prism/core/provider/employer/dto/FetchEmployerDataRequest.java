package com.prism.core.provider.employer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FetchEmployerDataRequest {

    @NotBlank(message = "Platform key is required")
    private String platformKey;  // e.g., "ZOMATO", "UBER"
}
