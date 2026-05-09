package com.prism.core.provider.entity;

import com.prism.core.common.enums.ProviderType;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider_response")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private PrismScoreSnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private ProviderType providerType;

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
