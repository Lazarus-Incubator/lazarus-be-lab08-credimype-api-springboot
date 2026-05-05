package com.creditflow.audit.application;

import com.creditflow.audit.domain.AuditEntry;
import com.creditflow.audit.domain.AuditableDomainEvent;
import com.creditflow.audit.infrastructure.AuditEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailListener {

    private final AuditEntryRepository auditEntryRepository;
    private final ObjectMapper objectMapper;

    public AuditTrailListener(AuditEntryRepository auditEntryRepository, ObjectMapper objectMapper) {
        this.auditEntryRepository = auditEntryRepository;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void handle(AuditableDomainEvent event) {
        auditEntryRepository.save(AuditEntry.create(
                event.institutionId(),
                event.actorUserId(),
                event.action(),
                event.entityType(),
                event.entityId(),
                event.previousStatus(),
                event.newStatus(),
                serialize(event.detail()),
                event.occurredAt()));
    }

    private String serialize(Object detail) {
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize audit detail", ex);
        }
    }
}
