package com.prism.core.scoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feature_flag_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FeatureFlagValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private PrismScoreSnapshot snapshot;

    @Column(name = "flag_key", nullable = false, length = 150)
    private String flagKey;

    @Column(name = "vector_key", nullable = false, length = 100)
    private String vectorKey;

    @Column(name = "computed_value", columnDefinition = "TEXT")
    private String computedValue;

    @Column(name = "score_contribution", precision = 8, scale = 4)
    private BigDecimal scoreContribution;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
