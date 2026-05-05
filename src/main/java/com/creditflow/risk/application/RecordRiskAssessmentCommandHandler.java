package com.creditflow.risk.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.approvals.domain.ApprovalLimitPolicy;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.risk.domain.RiskAssessment;
import com.creditflow.risk.domain.RiskAssessmentRecordedEvent;
import com.creditflow.risk.domain.RiskRecommendation;
import com.creditflow.risk.infrastructure.RiskAssessmentRepository;
import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.domain.RiskScore;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordRiskAssessmentCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final ApprovalLimitPolicy approvalLimitPolicy;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RecordRiskAssessmentCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                              RiskAssessmentRepository riskAssessmentRepository,
                                              CreditApplicationTransitionPolicy transitionPolicy,
                                              ApprovalLimitPolicy approvalLimitPolicy,
                                              TenantAccessPolicy tenantAccessPolicy,
                                              CurrentUserService currentUserService,
                                              ApplicationEventPublisher eventPublisher,
                                              ObjectMapper objectMapper,
                                              Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.transitionPolicy = transitionPolicy;
        this.approvalLimitPolicy = approvalLimitPolicy;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public Long handle(RecordRiskAssessmentCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        tenantAccessPolicy.assertCanAssessRisk(user, application.getInstitutionId());
        if (riskAssessmentRepository.findByApplicationId(application.getId()).isPresent()) {
            throw new DomainRuleViolationException("The application already has a risk assessment");
        }
        Instant now = Instant.now(clock);
        RiskAssessment assessment = RiskAssessment.record(
                application.getId(),
                application.getInstitutionId(),
                RiskScore.of(command.score()),
                command.debtToIncomeRatio(),
                serializeFlags(command.flags()),
                command.recommendation(),
                user.userId(),
                now);
        RiskAssessment saved = riskAssessmentRepository.save(assessment);
        boolean committeeRequired = approvalLimitPolicy.requiresCommittee(application, saved);
        String previousStatus = application.getStatus().name();
        application.recordRiskAssessment(saved.getId(), committeeRequired, transitionPolicy, now);
        eventPublisher.publishEvent(new RiskAssessmentRecordedEvent(
                application.getInstitutionId(),
                user.userId(),
                application.getId().toString(),
                previousStatus,
                application.getStatus().name(),
                Map.of(
                        "riskAssessmentId", saved.getId(),
                        "score", saved.getScore().value(),
                        "riskLevel", saved.getRiskLevel().name(),
                        "recommendation", saved.getRecommendation().name(),
                        "committeeRequired", committeeRequired),
                now));
        return application.getId();
    }

    private String serializeFlags(List<String> flags) {
        try {
            return objectMapper.writeValueAsString(flags == null ? List.of() : flags);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize risk flags", ex);
        }
    }

    public record RecordRiskAssessmentCommand(
            Long applicationId,
            Integer score,
            BigDecimal debtToIncomeRatio,
            List<String> flags,
            RiskRecommendation recommendation) {
    }
}
