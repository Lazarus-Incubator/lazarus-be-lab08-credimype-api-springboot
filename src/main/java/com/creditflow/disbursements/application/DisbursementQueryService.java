package com.creditflow.disbursements.application;

import com.creditflow.disbursements.domain.DisbursementOrder;
import com.creditflow.disbursements.infrastructure.DisbursementOrderRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.CurrentUserService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class DisbursementQueryService {

    private final DisbursementOrderRepository disbursementOrderRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public DisbursementQueryService(DisbursementOrderRepository disbursementOrderRepository,
                                    CurrentUserService currentUserService,
                                    TenantAccessPolicy tenantAccessPolicy) {
        this.disbursementOrderRepository = disbursementOrderRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public DisbursementOrderView getById(Long id) {
        DisbursementOrder order = disbursementOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disbursement order was not found"));
        tenantAccessPolicy.assertCanReadInstitution(currentUserService.requireCurrentUser(), order.getInstitutionId());
        return new DisbursementOrderView(
                order.getId(),
                order.getApplicationId(),
                order.getInstitutionId(),
                order.getAmount().toBigDecimal(),
                order.getCurrency().name(),
                order.getDestinationBank(),
                order.getDestinationAccount(),
                order.getStatus().name(),
                order.getIdempotencyKey(),
                order.getCreatedByUserId(),
                order.getExecutedByUserId(),
                order.getCreatedAt(),
                order.getExecutedAt(),
                order.getVersion());
    }

    public record DisbursementOrderView(
            Long id,
            Long applicationId,
            Long institutionId,
            BigDecimal amount,
            String currency,
            String destinationBank,
            String destinationAccount,
            String status,
            String idempotencyKey,
            Long createdByUserId,
            Long executedByUserId,
            Instant createdAt,
            Instant executedAt,
            Long version) {
    }
}
