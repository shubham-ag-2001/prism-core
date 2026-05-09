package com.prism.core.auth.entity;

import com.prism.core.common.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_records", indexes = {
        @Index(name = "idx_otp_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    @Builder.Default
    private OtpPurpose purpose = OtpPurpose.LOGIN;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
