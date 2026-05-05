package com.creditflow.disbursements.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.creditflow.applications.domain.CreditApplicationTransitionPolicy;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.disbursements.domain.DisbursementEligibilityPolicy;
import com.creditflow.disbursements.domain.DisbursementOrder;
import com.creditflow.disbursements.domain.IdempotencyOperationType;
import com.creditflow.disbursements.domain.IdempotencyRecord;
import com.creditflow.disbursements.infrastructure.DisbursementOrderRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExecuteDisbursementCommandHandlerTest {

    @Mock
    private DisbursementOrderRepository disbursementOrderRepository;
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private TenantAccessPolicy tenantAccessPolicy;
    @Mock
    private DisbursementEligibilityPolicy eligibilityPolicy;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldReturnExistingOrderWhenIdempotencyKeyWasAlreadyUsedForSameOrder() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-05T12:00:00Z"), ZoneOffset.UTC);
        ExecuteDisbursementCommandHandler handler = new ExecuteDisbursementCommandHandler(
                disbursementOrderRepository,
                creditApplicationRepository,
                tenantAccessPolicy,
                eligibilityPolicy,
                new CreditApplicationTransitionPolicy(),
                idempotencyService,
                currentUserService,
                eventPublisher,
                clock);

        DisbursementOrder order = DisbursementOrder.create(
                7L,
                1L,
                com.creditflow.shared.domain.MoneyAmount.of(new java.math.BigDecimal("30000.00")),
                com.creditflow.creditproducts.domain.CurrencyCode.PEN,
                "BCP",
                "193-884512-0-44",
                7L,
                Instant.parse("2026-05-05T11:00:00Z"));
        ReflectionTestUtils.setField(order, "id", 1L);

        when(currentUserService.requireCurrentUser())
                .thenReturn(new AuthenticatedUser(7L, "operaciones.andina@creditflow.pe", com.creditflow.identity.domain.UserRole.OPERATIONS_OFFICER, 1L, null));
        when(disbursementOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(idempotencyService.findRecord(1L, "repeat-key"))
                .thenReturn(Optional.of(IdempotencyRecord.success(
                        1L,
                        "repeat-key",
                        IdempotencyOperationType.EXECUTE_DISBURSEMENT,
                        IdempotencyService.DISBURSEMENT_ORDER_RESOURCE,
                        1L,
                        "EXECUTED",
                        Instant.parse("2026-05-05T11:30:00Z"))));

        Long result = handler.handle(new ExecuteDisbursementCommandHandler.ExecuteDisbursementCommand(1L, "repeat-key"));

        assertEquals(1L, result);
        verify(disbursementOrderRepository, never()).save(order);
        verify(creditApplicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
