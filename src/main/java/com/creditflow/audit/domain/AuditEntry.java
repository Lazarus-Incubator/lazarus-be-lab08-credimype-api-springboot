package com.creditflow.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_entry")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "detail_json", nullable = false, columnDefinition = "text")
    private String detailJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditEntry() {
    }

    public static AuditEntry create(Long institutionId,
                                    Long actorUserId,
                                    String action,
                                    String entityType,
                                    String entityId,
                                    String previousStatus,
                                    String newStatus,
                                    String detailJson,
                                    Instant createdAt) {
        AuditEntry entry = new AuditEntry();
        entry.institutionId = institutionId;
        entry.actorUserId = actorUserId;
        entry.action = action;
        entry.entityType = entityType;
        entry.entityId = entityId;
        entry.previousStatus = previousStatus;
        entry.newStatus = newStatus;
        entry.detailJson = detailJson;
        entry.createdAt = createdAt;
        return entry;
    }

    public Long getId() {
        return id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
