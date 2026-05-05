package com.creditflow.risk.domain;

import com.creditflow.audit.domain.AuditableDomainEvent;
import java.time.Instant;
import java.util.Map;

public record RiskAssessmentRecordedEvent(
        Long institutionId,
        Long actorUserId,
        String entityId,
        String previousStatus,
        String newStatus,
        Map<String, Object> detail,
        Instant occurredAt) implements AuditableDomainEvent {

    @Override
    public String action() {
        return "RISK_ASSESSMENT_RECORDED";
    }

    @Override
    public String entityType() {
        return "CREDIT_APPLICATION";
    }
}
