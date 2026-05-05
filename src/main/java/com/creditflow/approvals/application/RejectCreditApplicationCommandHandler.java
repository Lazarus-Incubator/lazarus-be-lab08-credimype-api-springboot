package com.creditflow.approvals.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationRejectedEvent;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.approvals.domain.ApprovalDecision;
import com.creditflow.approvals.domain.DecisionSource;
import com.creditflow.approvals.infrastructure.ApprovalDecisionRepository;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectCreditApplicationCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final ApprovalDecisionRepository approvalDecisionRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public RejectCreditApplicationCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                                 ApprovalDecisionRepository approvalDecisionRepository,
                                                 CreditApplicationTransitionPolicy transitionPolicy,
                                                 TenantAccessPolicy tenantAccessPolicy,
                                                 CurrentUserService currentUserService,
                                                 ApplicationEventPublisher eventPublisher,
                                                 Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.approvalDecisionRepository = approvalDecisionRepository;
        this.transitionPolicy = transitionPolicy;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Long handle(RejectCreditApplicationCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));

        DecisionSource source;
        if (application.getStatus() == CreditApplicationStatus.PENDING_COMMITTEE) {
            tenantAccessPolicy.assertCanApproveOrReject(user, application.getInstitutionId(), UserRole.COMMITTEE_MEMBER);
            source = DecisionSource.COMMITTEE;
        } else {
            tenantAccessPolicy.assertCanApproveOrReject(user, application.getInstitutionId(), UserRole.INSTITUTION_ADMIN);
            source = DecisionSource.MANAGER;
        }

        Instant now = Instant.now(clock);
        ApprovalDecision decision = approvalDecisionRepository.save(ApprovalDecision.reject(
                application.getId(),
                application.getInstitutionId(),
                command.reason(),
                user.userId(),
                now,
                source));
        String previousStatus = application.getStatus().name();
        application.reject(command.reason(), transitionPolicy, now);
        eventPublisher.publishEvent(new CreditApplicationRejectedEvent(
                application.getInstitutionId(),
                user.userId(),
                application.getId().toString(),
                previousStatus,
                application.getStatus().name(),
                Map.of(
                        "approvalDecisionId", decision.getId(),
                        "reason", command.reason(),
                        "decisionSource", source.name()),
                now));
        return application.getId();
    }

    public record RejectCreditApplicationCommand(Long applicationId, String reason) {
    }
}
