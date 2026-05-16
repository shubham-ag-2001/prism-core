package com.prism.core.scoring.repository;

import com.prism.core.scoring.entity.RecommendationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecommendationJobRepository extends JpaRepository<RecommendationJob, UUID> {
    Optional<RecommendationJob> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
