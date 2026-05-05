package com.creditflow.disbursements.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private IdempotencyOperationType operationType;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "response_status", nullable = false)
    private String responseStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    public static IdempotencyRecord success(Long institutionId,
                                            String idempotencyKey,
                                            IdempotencyOperationType operationType,
                                            String resourceType,
                                            Long resourceId,
                                            String responseStatus,
                                            Instant createdAt) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.institutionId = institutionId;
        record.idempotencyKey = idempotencyKey;
        record.operationType = operationType;
        record.resourceType = resourceType;
        record.resourceId = resourceId;
        record.responseStatus = responseStatus;
        record.createdAt = createdAt;
        return record;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public IdempotencyOperationType getOperationType() {
        return operationType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }
}
