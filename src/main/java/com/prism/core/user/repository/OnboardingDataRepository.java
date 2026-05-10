package com.prism.core.user.repository;

import com.prism.core.user.entity.OnboardingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingDataRepository extends JpaRepository<OnboardingData, UUID> {
    Optional<OnboardingData> findByUserId(UUID userId);
    boolean existsByPanNumber(String panNumber);
    boolean existsByPanNumberAndUserIdNot(String panNumber, UUID userId);
}
