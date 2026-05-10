package com.prism.core.auth.repository;

import com.prism.core.auth.entity.OtpRecord;
import com.prism.core.common.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRecordRepository extends JpaRepository<OtpRecord, UUID> {

    Optional<OtpRecord> findTopByPhoneAndPurposeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phone, OtpPurpose purpose, Instant now);
}
