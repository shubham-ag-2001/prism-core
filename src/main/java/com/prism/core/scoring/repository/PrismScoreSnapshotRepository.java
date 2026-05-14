package com.prism.core.scoring.repository;

import com.prism.core.common.enums.ScoringStatus;
import com.prism.core.scoring.entity.PrismScoreSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrismScoreSnapshotRepository extends JpaRepository<PrismScoreSnapshot, UUID> {

    // Find the latest COMPLETE snapshot that hasn't expired (LIMIT 1 prevents
    // IncorrectResultSizeDataAccessException when a user re-triggers scoring within
    // the 30-day cache window and accumulates multiple COMPLETE snapshots)
    @Query("SELECT s FROM PrismScoreSnapshot s WHERE s.user.id = :userId " +
           "AND s.scoringStatus = 'COMPLETE' AND s.expiresAt > :now " +
           "ORDER BY s.computedAt DESC LIMIT 1")
    Optional<PrismScoreSnapshot> findValidCachedSnapshot(UUID userId, Instant now);

    // Find the latest COMPLETE snapshot regardless of expiry
    Optional<PrismScoreSnapshot> findTopByUserIdAndScoringStatusOrderByComputedAtDesc(
            UUID userId, ScoringStatus status);

    // History of all COMPLETE snapshots for a user
    List<PrismScoreSnapshot> findByUserIdAndScoringStatusOrderByComputedAtDesc(
            UUID userId, ScoringStatus status);

    // Latest snapshot of any status (used by sms/last_fetched to get score timestamp)
    Optional<PrismScoreSnapshot> findTopByUserIdOrderByComputedAtDesc(UUID userId);
}
