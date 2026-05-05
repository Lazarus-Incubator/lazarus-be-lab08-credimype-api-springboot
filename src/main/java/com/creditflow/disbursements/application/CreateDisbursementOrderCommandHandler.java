package com.creditflow.disbursements.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.creditproducts.domain.CreditProduct;
import com.creditflow.creditproducts.infrastructure.CreditProductRepository;
import com.creditflow.disbursements.domain.DisbursementEligibilityPolicy;
import com.creditflow.disbursements.domain.DisbursementOrder;
import com.creditflow.disbursements.domain.DisbursementOrderCreatedEvent;
import com.creditflow.disbursements.domain.DisbursementStatus;
import com.creditflow.disbursements.infrastructure.DisbursementOrderRepository;
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
public class CreateDisbursementOrderCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditProductRepository creditProductRepository;
    private final DisbursementOrderRepository disbursementOrderRepository;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final DisbursementEligibilityPolicy eligibilityPolicy;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CreateDisbursementOrderCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                                 CreditProductRepository creditProductRepository,
                                                 DisbursementOrderRepository disbursementOrderRepository,
                                                 CreditApplicationTransitionPolicy transitionPolicy,
                                                 DisbursementEligibilityPolicy eligibilityPolicy,
                                                 TenantAccessPolicy tenantAccessPolicy,
                                                 CurrentUserService currentUserService,
                                                 ApplicationEventPublisher eventPublisher,
                                                 Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditProductRepository = creditProductRepository;
        this.disbursementOrderRepository = disbursementOrderRepository;
        this.transitionPolicy = transitionPolicy;
        this.eligibilityPolicy = eligibilityPolicy;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Long handle(CreateDisbursementOrderCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditApplication application = creditApplicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        tenantAccessPolicy.assertCanCreateDisbursementOrder(user, application.getInstitutionId());
        eligibilityPolicy.assertCanCreateOrder(user.role(), application);

        CreditProduct product = creditProductRepository.findByIdAndInstitutionId(application.getProductId(), application.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit product was not found"));
        Instant now = Instant.now(clock);
        DisbursementOrder order = DisbursementOrder.create(
                application.getId(),
                application.getInstitutionId(),
                application.getApprovedAmount(),
                product.getCurrency(),
                command.destinationBank(),
                command.destinationAccount(),
                user.userId(),
                now);
        DisbursementOrder saved = disbursementOrderRepository.save(order);
        String previousStatus = application.getStatus().name();
        application.markDisbursementPending(transitionPolicy, now);
        eventPublisher.publishEvent(new DisbursementOrderCreatedEvent(
                application.getInstitutionId(),
                user.userId(),
                saved.getId().toString(),
                null,
                DisbursementStatus.CREATED.name(),
                Map.of(
                        "applicationId", application.getId(),
                        "applicationPreviousStatus", previousStatus,
                        "applicationNewStatus", application.getStatus().name(),
                        "amount", saved.getAmount().toBigDecimal()),
                now));
        return saved.getId();
    }

    public record CreateDisbursementOrderCommand(
            Long applicationId,
            String destinationBank,
            String destinationAccount) {
    }
}
