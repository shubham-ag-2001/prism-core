package com.prism.core.provider.employer.entity;

import com.prism.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employer_platforms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerPlatform extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "platform_key", nullable = false, unique = true, length = 30)
    private String platformKey;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
