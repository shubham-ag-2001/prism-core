package com.prism.core.user.repository;

import com.prism.core.user.entity.PanVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PanVerificationRepository extends JpaRepository<PanVerification, UUID> {
    Optional<PanVerification> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
