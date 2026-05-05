package com.creditflow.disbursements.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.disbursements.domain.DisbursementEligibilityPolicy;
import com.creditflow.disbursements.domain.DisbursementExecutedEvent;
import com.creditflow.disbursements.domain.DisbursementOrder;
import com.creditflow.disbursements.domain.DisbursementStatus;
import com.creditflow.disbursements.domain.IdempotencyOperationType;
import com.creditflow.disbursements.domain.IdempotencyRecord;
import com.creditflow.disbursements.infrastructure.DisbursementOrderRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.IdempotencyConflictException;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Executes a disbursement order exactly once per tenant and idempotency key.
 */
@Service
public class ExecuteDisbursementCommandHandler {

    private final DisbursementOrderRepository disbursementOrderRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final DisbursementEligibilityPolicy eligibilityPolicy;
    private final CreditApplicationTransitionPolicy transitionPolicy;
    private final IdempotencyService idempotencyService;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public ExecuteDisbursementCommandHandler(DisbursementOrderRepository disbursementOrderRepository,
                                             CreditApplicationRepository creditApplicationRepository,
                                             TenantAccessPolicy tenantAccessPolicy,
                                             DisbursementEligibilityPolicy eligibilityPolicy,
                                             CreditApplicationTransitionPolicy transitionPolicy,
                                             IdempotencyService idempotencyService,
                                             CurrentUserService currentUserService,
                                             ApplicationEventPublisher eventPublisher,
                                             Clock clock) {
        this.disbursementOrderRepository = disbursementOrderRepository;
        this.creditApplicationRepository = creditApplicationRepository;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.eligibilityPolicy = eligibilityPolicy;
        this.transitionPolicy = transitionPolicy;
        this.idempotencyService = idempotencyService;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public Long handle(ExecuteDisbursementCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        DisbursementOrder order = disbursementOrderRepository.findById(command.disbursementOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Disbursement order was not found"));
        tenantAccessPolicy.assertCanExecuteDisbursement(user, order.getInstitutionId());

        IdempotencyRecord existingRecord = idempotencyService.findRecord(order.getInstitutionId(), command.idempotencyKey())
                .orElse(null);
        if (existingRecord != null) {
            idempotencyService.assertCompatible(
                    existingRecord,
                    IdempotencyOperationType.EXECUTE_DISBURSEMENT,
                    IdempotencyService.DISBURSEMENT_ORDER_RESOURCE,
                    order.getId());
            // Audit stays at the edge of the operational flow so retries leave the same observable trace as the original attempt.
            eventPublisher.publishEvent(new DisbursementExecutedEvent(
                    order.getInstitutionId(),
                    user.userId(),
                    order.getId().toString(),
                    order.getStatus().name(),
                    order.getStatus().name(),
                    Map.of(
                            "applicationId", order.getApplicationId(),
                            "idempotencyKey", command.idempotencyKey(),
                            "replayed", true),
                    Instant.now(clock)));
            return order.getId();
        }
        if (order.getIdempotencyKey() != null && !order.getIdempotencyKey().equals(command.idempotencyKey())) {
            throw new IdempotencyConflictException("The disbursement order was already executed with another idempotency key");
        }

        CreditApplication application = creditApplicationRepository.findById(order.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        eligibilityPolicy.assertCanExecute(user.role(), application, order);

        Instant now = Instant.now(clock);
        String previousOrderStatus = order.getStatus().name();
        String previousApplicationStatus = application.getStatus().name();
        // The execution steps stay separated to keep the operational path easier to read during incident review.
        order.markExecuted(user.userId(), command.idempotencyKey(), now);
        application.markDisbursed(transitionPolicy, now);
        disbursementOrderRepository.save(order);
        idempotencyService.registerSuccess(
                order.getInstitutionId(),
                command.idempotencyKey(),
                IdempotencyOperationType.EXECUTE_DISBURSEMENT,
                IdempotencyService.DISBURSEMENT_ORDER_RESOURCE,
                order.getId(),
                order.getStatus().name());
        eventPublisher.publishEvent(new DisbursementExecutedEvent(
                order.getInstitutionId(),
                user.userId(),
                order.getId().toString(),
                previousOrderStatus,
                DisbursementStatus.EXECUTED.name(),
                Map.of(
                        "applicationId", application.getId(),
                        "applicationPreviousStatus", previousApplicationStatus,
                        "applicationNewStatus", application.getStatus().name(),
                        "idempotencyKey", command.idempotencyKey()),
                now));
        return order.getId();
    }

    public record ExecuteDisbursementCommand(Long disbursementOrderId, String idempotencyKey) {
    }
}
