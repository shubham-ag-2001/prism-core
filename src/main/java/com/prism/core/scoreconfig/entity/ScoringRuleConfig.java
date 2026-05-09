package com.prism.core.scoreconfig.entity;

import com.prism.core.common.enums.ConfigLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scoring_rule_config", indexes = {
        @Index(name = "idx_scoring_rule_key", columnList = "config_key, rule_set_version")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ScoringRuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "config_key", nullable = false, length = 200)
    private String configKey;           // dimension_key | vector_key | flag_key

    @Enumerated(EnumType.STRING)
    @Column(name = "config_level", nullable = false, length = 20)
    private ConfigLevel configLevel;

    @Column(name = "weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;          // e.g., 0.2500 for 25%

    @Column(name = "rule_set_version", nullable = false, length = 20)
    @Builder.Default
    private String ruleSetVersion = "v1.0";

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
