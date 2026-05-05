package com.creditflow.disbursements.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.creditproducts.domain.CurrencyCode;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.domain.ApplicationNumber;
import com.creditflow.shared.domain.MoneyAmount;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DisbursementEligibilityPolicyTest {

    private final DisbursementEligibilityPolicy policy = new DisbursementEligibilityPolicy();

    @Test
    void shouldAllowOrderCreationForApprovedApplication() {
        CreditApplication application = approvedApplication();

        assertDoesNotThrow(() -> policy.assertCanCreateOrder(UserRole.OPERATIONS_OFFICER, application));
    }

    @Test
    void shouldRejectOrderCreationForRejectedApplication() {
        CreditApplication application = approvedApplication();
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.REJECTED);

        assertThrows(DomainRuleViolationException.class, () -> policy.assertCanCreateOrder(UserRole.OPERATIONS_OFFICER, application));
    }

    @Test
    void shouldAllowExecutionWhenApplicationIsPendingDisbursement() {
        CreditApplication application = approvedApplication();
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.DISBURSEMENT_PENDING);
        DisbursementOrder order = DisbursementOrder.create(
                1L,
                1L,
                MoneyAmount.of(new BigDecimal("20000.00")),
                CurrencyCode.PEN,
                "BCP",
                "123-456",
                7L,
                Instant.parse("2026-05-05T11:00:00Z"));

        assertDoesNotThrow(() -> policy.assertCanExecute(UserRole.OPERATIONS_OFFICER, application, order));
    }

    private CreditApplication approvedApplication() {
        CreditApplication application = CreditApplication.draft(
                ApplicationNumber.of("CFM-2026-001101"),
                1L,
                1L,
                1L,
                1L,
                MoneyAmount.of(new BigDecimal("20000.00")),
                12,
                "Capital de trabajo",
                2L,
                Instant.parse("2026-05-05T10:00:00Z"));
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.APPROVED);
        ReflectionTestUtils.setField(application, "approvedAmount", MoneyAmount.of(new BigDecimal("18000.00")));
        return application;
    }
}
