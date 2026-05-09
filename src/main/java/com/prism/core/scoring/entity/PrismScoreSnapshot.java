package com.prism.core.scoring.entity;

import com.prism.core.common.enums.ScoringStatus;
import com.prism.core.common.enums.TriggerType;
import com.prism.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prism_score_snapshot", indexes = {
        @Index(name = "idx_snapshot_user_id", columnList = "user_id"),
        @Index(name = "idx_snapshot_status", columnList = "scoring_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PrismScoreSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "final_score")
    private Integer finalScore;               // 300-900, null until COMPLETE

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_status", nullable = false, length = 20)
    @Builder.Default
    private ScoringStatus scoringStatus = ScoringStatus.PENDING;

    @Column(name = "rule_set_version", nullable = false, length = 20)
    @Builder.Default
    private String ruleSetVersion = "v1.0";

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false, length = 30)
    @Builder.Default
    private TriggerType triggeredBy = TriggerType.MANUAL;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "computed_at")
    private Instant computedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;               // computedAt + 30 days

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
