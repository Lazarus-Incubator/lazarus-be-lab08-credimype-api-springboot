package com.creditflow.approvals.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationApprovedEvent;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.approvals.domain.ApprovalDecision;
import com.creditflow.approvals.domain.ApprovalLimitPolicy;
import com.creditflow.approvals.domain.DecisionSource;
import com.creditflow.approvals.infrastructure.ApprovalDecisionRepository;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.risk.domain.RiskAssessment;
import com.creditflow.risk.infrastructure.RiskAssessmentRepository;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.domain.MoneyAmount;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles direct approvals and committee approvals while delegating authority limits to
 * {@link ApprovalLimitPolicy}.
 */
@Service
public class ApproveCreditApplicationCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ApprovalDecisionRepository approvalDecisionRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final ApprovalLimitPolicy approvalLimitPolicy;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public ApproveCreditApplicationCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                                  RiskAssessmentRepository riskAssessmentRepository,
                                                  ApprovalDecisionRepository approvalDecisionRepository,
                                                  CreditApplicationTransitionPolicy transitionPolicy,
                                                  ApprovalLimitPolicy approvalLimitPolicy,
                                                  TenantAccessPolicy tenantAccessPolicy,
                                                  CurrentUserService currentUserService,
                                                  ApplicationEventPublisher eventPublisher,
                                                  Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.approvalDecisionRepository = approvalDecisionRepository;
        this.transitionPolicy = transitionPolicy;
        this.approvalLimitPolicy = approvalLimitPolicy;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Long handle(ApproveCreditApplicationCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        MoneyAmount approvedAmount = MoneyAmount.of(command.approvedAmount());

        DecisionSource source;
        Instant now = Instant.now(clock);
        String previousStatus = application.getStatus().name();
        if (application.getStatus() == CreditApplicationStatus.PENDING_COMMITTEE) {
            tenantAccessPolicy.assertCanApproveOrReject(user, application.getInstitutionId(), UserRole.COMMITTEE_MEMBER);
            // Committee keeps an exception lane because the risk file may still be reconciling when the vote is registered.
            application.approveFromCommittee(approvedAmount, now);
            source = DecisionSource.COMMITTEE;
        } else {
            RiskAssessment assessment = riskAssessmentRepository.findByApplicationId(application.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Risk assessment was not found"));
            tenantAccessPolicy.assertCanApproveOrReject(user, application.getInstitutionId(), UserRole.INSTITUTION_ADMIN);
            approvalLimitPolicy.assertDirectApprovalAllowed(user.role(), application, assessment, approvedAmount);
            application.approve(approvedAmount, transitionPolicy, now);
            source = DecisionSource.MANAGER;
        }

        ApprovalDecision decision = approvalDecisionRepository.save(ApprovalDecision.approve(
                application.getId(),
                application.getInstitutionId(),
                approvedAmount,
                user.userId(),
                now,
                source));
        eventPublisher.publishEvent(new CreditApplicationApprovedEvent(
                application.getInstitutionId(),
                user.userId(),
                application.getId().toString(),
                previousStatus,
                application.getStatus().name(),
                Map.of(
                        "approvalDecisionId", decision.getId(),
                        "approvedAmount", approvedAmount.toBigDecimal(),
                        "decisionSource", source.name()),
                now));
        return application.getId();
    }

    public record ApproveCreditApplicationCommand(Long applicationId, java.math.BigDecimal approvedAmount) {
    }
}
