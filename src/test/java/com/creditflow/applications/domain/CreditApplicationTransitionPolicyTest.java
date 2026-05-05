package com.creditflow.applications.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.creditflow.shared.application.exception.InvalidStateTransitionException;
import com.creditflow.shared.domain.ApplicationNumber;
import com.creditflow.shared.domain.MoneyAmount;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CreditApplicationTransitionPolicyTest {

    private final CreditApplicationTransitionPolicy policy = new CreditApplicationTransitionPolicy();

    @Test
    void shouldAllowSubmitFromDraft() {
        CreditApplication application = draftApplication();

        assertDoesNotThrow(() -> policy.assertCanSubmit(application));
    }

    @Test
    void shouldRejectSubmitFromSubmitted() {
        CreditApplication application = draftApplication();
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.SUBMITTED);

        assertThrows(InvalidStateTransitionException.class, () -> policy.assertCanSubmit(application));
    }

    @Test
    void shouldRequireCommitteeFlagBeforeSendingToCommittee() {
        CreditApplication application = draftApplication();
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.RISK_REVIEWED);
        ReflectionTestUtils.setField(application, "committeeRequired", false);

        assertThrows(RuntimeException.class, () -> policy.assertCanSendToCommittee(application));
    }

    @Test
    void shouldAllowApproveWhenRiskAssessmentExistsAndAmountIsValid() {
        CreditApplication application = draftApplication();
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.RISK_REVIEWED);
        ReflectionTestUtils.setField(application, "riskAssessmentId", 99L);

        assertDoesNotThrow(() -> policy.assertCanApprove(application, MoneyAmount.of(new BigDecimal("15000.00"))));
    }

    @Test
    void shouldRejectCancellationOnceApproved() {
        CreditApplication application = draftApplication();
        ReflectionTestUtils.setField(application, "status", CreditApplicationStatus.APPROVED);

        assertThrows(InvalidStateTransitionException.class, () -> policy.assertCanCancel(application));
    }

    private CreditApplication draftApplication() {
        CreditApplication application = CreditApplication.draft(
                ApplicationNumber.of("CFM-2026-000999"),
                1L,
                1L,
                1L,
                1L,
                MoneyAmount.of(new BigDecimal("20000.00")),
                12,
                "Capital de trabajo",
                3L,
                Instant.parse("2026-05-05T10:00:00Z"));
        ReflectionTestUtils.setField(application, "id", 999L);
        return application;
    }
}
