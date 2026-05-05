package com.creditflow.applications.domain;

import com.creditflow.audit.domain.AuditableDomainEvent;
import java.time.Instant;
import java.util.Map;

public record CreditApplicationCreatedEvent(
        Long institutionId,
        Long actorUserId,
        String entityId,
        Map<String, Object> detail,
        Instant occurredAt) implements AuditableDomainEvent {

    @Override
    public String action() {
        return "CREDIT_APPLICATION_CREATED";
    }

    @Override
    public String entityType() {
        return "CREDIT_APPLICATION";
    }

    @Override
    public String previousStatus() {
        return null;
    }

    @Override
    public String newStatus() {
        return CreditApplicationStatus.DRAFT.name();
    }
}
