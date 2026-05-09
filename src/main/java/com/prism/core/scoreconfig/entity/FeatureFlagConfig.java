package com.prism.core.scoreconfig.entity;

import com.prism.core.common.entity.BaseEntity;
import com.prism.core.common.enums.DerivationSource;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "feature_flag_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlagConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "flag_key", nullable = false, unique = true, length = 150)
    private String flagKey;

    @Column(name = "vector_key", nullable = false, length = 100)
    private String vectorKey;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "data_type", nullable = false, length = 30)
    private String dataType;           // NUMERIC, BOOLEAN, CATEGORICAL

    @Enumerated(EnumType.STRING)
    @Column(name = "derivation_source", nullable = false, length = 30)
    private DerivationSource derivationSource;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
