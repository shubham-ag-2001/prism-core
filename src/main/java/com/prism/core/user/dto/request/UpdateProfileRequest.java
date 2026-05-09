package com.prism.core.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;

    @Email(message = "Invalid email address")
    private String email;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String city;
    private String state;
}
