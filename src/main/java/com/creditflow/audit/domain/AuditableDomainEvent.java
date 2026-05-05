package com.creditflow.audit.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Contract implemented by internal domain events that must be mirrored into the audit trail.
 *
 * <p>The application publishes small in-process events after successful state changes. The audit
 * module translates them into durable {@link AuditEntry} rows so command handlers stay focused on
 * business actions instead of repeating persistence code for each audit record.</p>
 */
public interface AuditableDomainEvent {

    Long institutionId();

    Long actorUserId();

    String action();

    String entityType();

    String entityId();

    String previousStatus();

    String newStatus();

    Map<String, Object> detail();

    Instant occurredAt();
}
