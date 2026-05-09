package com.prism.core.scoreconfig.entity;

import com.prism.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "dimension_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dimension_key", nullable = false, unique = true, length = 50)
    private String dimensionKey;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
