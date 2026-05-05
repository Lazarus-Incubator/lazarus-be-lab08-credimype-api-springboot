package com.creditflow.applications.domain;

import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.application.exception.InvalidStateTransitionException;
import com.creditflow.shared.domain.MoneyAmount;
import org.springframework.stereotype.Component;

/**
 * Evaluates whether a credit application can move to the requested state.
 *
 * <p>This policy centralizes workflow invariants so controllers and command handlers do not
 * duplicate transition rules. A transition that looks valid from the UI may still be rejected here
 * if risk evaluation, committee review, approval amount, or disbursement preconditions are not
 * satisfied.</p>
 */
@Component
public class CreditApplicationTransitionPolicy {

    public void assertCanSubmit(CreditApplication application) {
        requireStatus(application, CreditApplicationStatus.DRAFT, CreditApplicationStatus.SUBMITTED);
    }

    public void assertCanStartReview(CreditApplication application) {
        requireStatus(application, CreditApplicationStatus.SUBMITTED, CreditApplicationStatus.UNDER_REVIEW);
    }

    public void assertCanRecordRisk(CreditApplication application) {
        requireStatus(application, CreditApplicationStatus.UNDER_REVIEW, CreditApplicationStatus.RISK_REVIEWED);
    }

    public void assertCanSendToCommittee(CreditApplication application) {
        requireStatus(application, CreditApplicationStatus.RISK_REVIEWED, CreditApplicationStatus.PENDING_COMMITTEE);
        if (!application.isCommitteeRequired()) {
            throw new DomainRuleViolationException("The application is not marked as requiring committee review");
        }
    }

    public void assertCanApprove(CreditApplication application, MoneyAmount approvedAmount) {
        if (application.getStatus() != CreditApplicationStatus.RISK_REVIEWED
                && application.getStatus() != CreditApplicationStatus.PENDING_COMMITTEE) {
            throw invalidTransition(application, CreditApplicationStatus.APPROVED);
        }
        if (application.getRiskAssessmentId() == null) {
            throw new DomainRuleViolationException("A risk assessment is required before approval");
        }
        if (approvedAmount == null) {
            throw new DomainRuleViolationException("Approved amount is required");
        }
        if (approvedAmount.isGreaterThan(application.getRequestedAmount())) {
            throw new DomainRuleViolationException("Approved amount cannot exceed the requested amount");
        }
    }

    public void assertCanReject(CreditApplication application, String reason) {
        if (application.getStatus() != CreditApplicationStatus.RISK_REVIEWED
                && application.getStatus() != CreditApplicationStatus.PENDING_COMMITTEE) {
            throw invalidTransition(application, CreditApplicationStatus.REJECTED);
        }
        if (reason == null || reason.isEmpty()) {
            throw new DomainRuleViolationException("Rejection reason is required");
        }
    }

    public void assertCanCreateDisbursementOrder(CreditApplication application) {
        requireStatus(application, CreditApplicationStatus.APPROVED, CreditApplicationStatus.DISBURSEMENT_PENDING);
        if (application.getApprovedAmount() == null) {
            throw new DomainRuleViolationException("Approved amount is required before creating a disbursement order");
        }
    }

    public void assertCanMarkDisbursed(CreditApplication application) {
        requireStatus(application, CreditApplicationStatus.DISBURSEMENT_PENDING, CreditApplicationStatus.DISBURSED);
    }

    public void assertCanCancel(CreditApplication application) {
        CreditApplicationStatus status = application.getStatus();
        if (status == CreditApplicationStatus.APPROVED
                || status == CreditApplicationStatus.DISBURSEMENT_PENDING
                || status == CreditApplicationStatus.DISBURSED
                || status == CreditApplicationStatus.REJECTED
                || status == CreditApplicationStatus.CANCELLED) {
            throw invalidTransition(application, CreditApplicationStatus.CANCELLED);
        }
    }

    private void requireStatus(CreditApplication application,
                               CreditApplicationStatus expected,
                               CreditApplicationStatus target) {
        if (application.getStatus() != expected) {
            throw invalidTransition(application, target);
        }
    }

    private InvalidStateTransitionException invalidTransition(CreditApplication application,
                                                              CreditApplicationStatus target) {
        return new InvalidStateTransitionException(
                "Cannot move application from %s to %s".formatted(application.getStatus(), target));
    }
}
