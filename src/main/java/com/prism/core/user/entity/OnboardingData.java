package com.prism.core.user.entity;

import com.prism.core.common.entity.BaseEntity;
import com.prism.core.common.enums.OnboardingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "onboarding_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "employer_name", length = 200)
    private String employerName;

    @Column(name = "employer_type", length = 50)
    private String employerType;

    @Column(name = "work_start_date")
    private LocalDate workStartDate;

    @Column(name = "work_history_json", columnDefinition = "TEXT")
    private String workHistoryJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OnboardingStatus status = OnboardingStatus.PENDING;

    @Column(name = "submitted_at")
    private Instant submittedAt;
}
