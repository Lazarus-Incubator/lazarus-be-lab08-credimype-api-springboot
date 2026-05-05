package com.creditflow.disbursements.domain;

import com.creditflow.audit.domain.AuditableDomainEvent;
import java.time.Instant;
import java.util.Map;

public record DisbursementOrderCreatedEvent(
        Long institutionId,
        Long actorUserId,
        String entityId,
        String previousStatus,
        String newStatus,
        Map<String, Object> detail,
        Instant occurredAt) implements AuditableDomainEvent {

    @Override
    public String action() {
        return "DISBURSEMENT_ORDER_CREATED";
    }

    @Override
    public String entityType() {
        return "DISBURSEMENT_ORDER";
    }
}
