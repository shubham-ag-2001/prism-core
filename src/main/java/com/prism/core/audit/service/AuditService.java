package com.prism.core.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.audit.entity.AuditLog;
import com.prism.core.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async("scoringTaskExecutor")
    public void log(String entityType, String entityId, String action, UUID actorId, Object payload) {
        try {
            String payloadJson = payload != null ? objectMapper.writeValueAsString(payload) : null;
            AuditLog entry = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .actorId(actorId)
                    .payloadJson(payloadJson)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: entity={}, action={}", entityType, action, e);
        }
    }
}
