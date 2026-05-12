package com.prism.core.provider.repository;

import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.entity.ProviderResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProviderResponseRepository extends JpaRepository<ProviderResponse, UUID> {

    /** Latest employer response for a specific platform */
    Optional<ProviderResponse> findTopByUserIdAndProviderTypeAndPlatformKeyOrderByFetchedAtDesc(
            UUID userId, ProviderType providerType, String platformKey);

    /** All employer responses for a user */
    List<ProviderResponse> findByUserIdAndProviderTypeOrderByFetchedAtDesc(
            UUID userId, ProviderType providerType);

    /** Check if employer data exists for a user */
    boolean existsByUserIdAndProviderType(UUID userId, ProviderType providerType);
}
