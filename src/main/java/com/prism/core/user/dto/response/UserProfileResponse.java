package com.prism.core.user.dto.response;

import com.prism.core.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID userId;
    private String phone;
    private UserRole role;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private String city;
    private String state;
    private boolean isActive;
}
