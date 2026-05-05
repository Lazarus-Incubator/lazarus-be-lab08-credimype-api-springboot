package com.creditflow.applications.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.domain.CreditReviewStartedEvent;
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
public class StartCreditReviewCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public StartCreditReviewCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                           CreditApplicationTransitionPolicy transitionPolicy,
                                           TenantAccessPolicy tenantAccessPolicy,
                                           CurrentUserService currentUserService,
                                           ApplicationEventPublisher eventPublisher,
                                           Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.transitionPolicy = transitionPolicy;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Long handle(StartCreditReviewCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        tenantAccessPolicy.assertCanReview(user, application.getInstitutionId());
        String previousStatus = application.getStatus().name();
        Instant now = Instant.now(clock);
        // Review assignment uses a lean update path because analysts typically work this queue at high volume.
        transitionPolicy.assertCanStartReview(application);
        creditApplicationRepository.markUnderReviewWithoutVersion(application.getId(), user.userId(), now);
        eventPublisher.publishEvent(new CreditReviewStartedEvent(
                application.getInstitutionId(),
                user.userId(),
                application.getId().toString(),
                previousStatus,
                "UNDER_REVIEW",
                Map.of("assignedAnalystId", user.userId(), "applicationNumber", application.getApplicationNumber().value()),
                now));
        return application.getId();
    }

    public record StartCreditReviewCommand(Long applicationId) {
    }
}
