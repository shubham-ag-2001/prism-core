package com.prism.core.scoring.repository;

import com.prism.core.scoring.entity.ScoreRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreRecommendationRepository extends JpaRepository<ScoreRecommendation, UUID> {
    
    List<ScoreRecommendation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<ScoreRecommendation> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(UUID userId, Instant after);

    void deleteByUserId(UUID userId);
}
