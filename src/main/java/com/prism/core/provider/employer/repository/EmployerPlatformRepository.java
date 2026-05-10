package com.prism.core.provider.employer.repository;

import com.prism.core.provider.employer.entity.EmployerPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployerPlatformRepository extends JpaRepository<EmployerPlatform, UUID> {
    List<EmployerPlatform> findAllByActiveTrue();
    Optional<EmployerPlatform> findByPlatformKeyAndActiveTrue(String platformKey);
}
