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
@Table(name = "provider_response", indexes = {
        @Index(name = "idx_provider_response_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProviderResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User this response belongs to.
     * Populated at fetch time — snapshot_id is null until scoring begins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Linked to a specific scoring run for audit.
     * Nullable — provider responses exist before any job is created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    private PrismScoreSnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private ProviderType providerType;

    /** Platform key for employer responses e.g. ZOMATO, UBER */
    @Column(name = "platform_key", length = 50)
    private String platformKey;

    @Column(name = "request_payload_json", columnDefinition = "TEXT")
    private String requestPayloadJson;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "is_mock", nullable = false)
    @Builder.Default
    private boolean isMock = true;

    @CreatedDate
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;
}
