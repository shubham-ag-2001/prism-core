package com.prism.core.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OnboardingRequest {

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    private String panNumber;

    @NotBlank(message = "Employer name is required")
    @Size(max = 200)
    private String employerName;

    private String employerType;        // e.g., "FOOD_DELIVERY", "RIDE_HAILING", "FREELANCER"
    private LocalDate workStartDate;
    private String workHistoryJson;     // JSON string with work history details
}
