package com.creditflow.applications.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationCancelledEvent;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.AccessDeniedBusinessException;
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
public class CancelCreditApplicationCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CancelCreditApplicationCommandHandler(CreditApplicationRepository creditApplicationRepository,
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
    public Long handle(CancelCreditApplicationCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        tenantAccessPolicy.assertCanReadBranch(user, application.getInstitutionId(), application.getBranchId());
        if (user.role() != UserRole.INSTITUTION_ADMIN
                && user.role() != UserRole.AUDITOR
                && !user.userId().equals(application.getCreatedByUserId())) {
            throw new AccessDeniedBusinessException("Only the creator or an institution administrator can cancel the application");
        }
        Instant now = Instant.now(clock);
        String previousStatus = application.getStatus().name();
        application.cancel(transitionPolicy, now);
        eventPublisher.publishEvent(new CreditApplicationCancelledEvent(
                application.getInstitutionId(),
                user.userId(),
                application.getId().toString(),
                application.getStatus().name(),
                previousStatus,
                Map.of("reason", command.reason() == null ? "" : command.reason()),
                now));
        return application.getId();
    }

    public record CancelCreditApplicationCommand(Long applicationId, String reason) {
    }
}
