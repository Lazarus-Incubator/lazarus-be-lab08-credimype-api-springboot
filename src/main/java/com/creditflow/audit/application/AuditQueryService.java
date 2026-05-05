package com.creditflow.audit.application;

import com.creditflow.audit.domain.AuditEntry;
import com.creditflow.audit.infrastructure.AuditEntryRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AuditQueryService {

    private final AuditEntryRepository auditEntryRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public AuditQueryService(AuditEntryRepository auditEntryRepository,
                             CurrentUserService currentUserService,
                             TenantAccessPolicy tenantAccessPolicy) {
        this.auditEntryRepository = auditEntryRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public List<AuditEntryView> list() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        List<AuditEntry> entries = user.isPlatformAdmin()
                ? auditEntryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : auditEntryRepository.findByInstitutionIdOrderByCreatedAtDesc(user.institutionId());
        return entries.stream().map(AuditEntryView::from).toList();
    }

    public AuditEntryView getById(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        AuditEntry entry = auditEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit entry was not found"));
        if (entry.getInstitutionId() != null) {
            tenantAccessPolicy.assertCanReadInstitution(user, entry.getInstitutionId());
        }
        return AuditEntryView.from(entry);
    }

    public record AuditEntryView(
            Long id,
            Long institutionId,
            Long actorUserId,
            String action,
            String entityType,
            String entityId,
            String previousStatus,
            String newStatus,
            String detailJson,
            Instant createdAt) {

        private static AuditEntryView from(AuditEntry entry) {
            return new AuditEntryView(
                    entry.getId(),
                    entry.getInstitutionId(),
                    entry.getActorUserId(),
                    entry.getAction(),
                    entry.getEntityType(),
                    entry.getEntityId(),
                    entry.getPreviousStatus(),
                    entry.getNewStatus(),
                    entry.getDetailJson(),
                    entry.getCreatedAt());
        }
    }
}
