package com.creditflow.applications.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationSubmittedEvent;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
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
public class SubmitCreditApplicationCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public SubmitCreditApplicationCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                                 CreditApplicationTransitionPolicy transitionPolicy,
                                                 CurrentUserService currentUserService,
                                                 TenantAccessPolicy tenantAccessPolicy,
                                                 ApplicationEventPublisher eventPublisher,
                                                 Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.transitionPolicy = transitionPolicy;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Long handle(SubmitCreditApplicationCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        tenantAccessPolicy.assertCanOriginateForBranch(user, application.getInstitutionId(), application.getBranchId());
        String previousStatus = application.getStatus().name();
        Instant now = Instant.now(clock);
        application.submit(transitionPolicy, now);
        eventPublisher.publishEvent(new CreditApplicationSubmittedEvent(
                application.getInstitutionId(),
                user.userId(),
                application.getId().toString(),
                previousStatus,
                application.getStatus().name(),
                Map.of("applicationNumber", application.getApplicationNumber().value()),
                now));
        return application.getId();
    }

    public record SubmitCreditApplicationCommand(Long applicationId) {
    }
}
