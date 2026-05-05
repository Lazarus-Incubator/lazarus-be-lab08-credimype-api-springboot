package com.creditflow.disbursements.domain;

import com.creditflow.audit.domain.AuditableDomainEvent;
import java.time.Instant;
import java.util.Map;

public record DisbursementExecutedEvent(
        Long institutionId,
        Long actorUserId,
        String entityId,
        String previousStatus,
        String newStatus,
        Map<String, Object> detail,
        Instant occurredAt) implements AuditableDomainEvent {

    @Override
    public String action() {
        return "DISBURSEMENT_EXECUTED";
    }

    @Override
    public String entityType() {
        return "DISBURSEMENT_ORDER";
    }
}
