package com.prism.core.provider.entity;

import com.prism.core.common.enums.ProviderType;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import com.prism.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raw_signal", indexes = {
        @Index(name = "idx_raw_signal_snapshot", columnList = "snapshot_id"),
        @Index(name = "idx_raw_signal_user_id",  columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RawSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User this signal belongs to.
     * Set at ingest time — snapshot_id is null until scoring begins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Linked to a specific scoring run (set after scoring completes for audit).
     * Nullable — signals exist before any scoring job is created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    private PrismScoreSnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private ProviderType providerType;

    @Column(name = "signal_key", nullable = false, length = 150)
    private String signalKey;        // e.g. "avg_weekly_credit_sms_90d"

    @Column(name = "signal_value", columnDefinition = "TEXT")
    private String signalValue;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
