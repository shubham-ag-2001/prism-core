package com.prism.core.scoring.repository;

import com.prism.core.scoring.entity.ScoringJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoringJobRepository extends JpaRepository<ScoringJob, UUID> {
    Optional<ScoringJob> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
