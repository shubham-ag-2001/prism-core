package com.prism.core.provider.repository;

import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.entity.RawSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RawSignalRepository extends JpaRepository<RawSignal, UUID> {

    /** All signals for a user of a given provider type, newest first */
    List<RawSignal> findByUserIdAndProviderTypeOrderByCreatedAtDesc(UUID userId, ProviderType providerType);

    /** Latest set of SMS signals for a user */
    List<RawSignal> findByUserIdAndProviderTypeOrderByCreatedAtAsc(UUID userId, ProviderType providerType);

    /** Check if any signals exist for a user */
    boolean existsByUserId(UUID userId);
}
