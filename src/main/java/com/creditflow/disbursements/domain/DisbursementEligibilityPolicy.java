package com.creditflow.disbursements.domain;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.shared.application.exception.DomainRuleViolationException;
import org.springframework.stereotype.Component;

/**
 * Validates the last operational step of the workflow: turning an approved credit into a
 * disbursement.
 *
 * <p>Disbursements are financially sensitive and must never be created or executed from an
 * inconsistent application state. This policy keeps those preconditions explicit and easy to test.</p>
 */
@Component
public class DisbursementEligibilityPolicy {

    public void assertCanCreateOrder(UserRole role, CreditApplication application) {
        if (role != UserRole.OPERATIONS_OFFICER && role != UserRole.INSTITUTION_ADMIN) {
            throw new DomainRuleViolationException("Only operations officers or institution administrators can create disbursement orders");
        }
        if (application.getStatus() != CreditApplicationStatus.APPROVED) {
            throw new DomainRuleViolationException("A disbursement order can only be created for approved applications");
        }
        if (application.getApprovedAmount() == null) {
            throw new DomainRuleViolationException("Approved amount is required before creating a disbursement order");
        }
    }

    public void assertCanExecute(UserRole role, CreditApplication application, DisbursementOrder order) {
        if (role != UserRole.OPERATIONS_OFFICER && role != UserRole.INSTITUTION_ADMIN) {
            throw new DomainRuleViolationException("Only operations officers or institution administrators can execute disbursements");
        }
        if (application.getStatus() != CreditApplicationStatus.DISBURSEMENT_PENDING) {
            throw new DomainRuleViolationException("The application is not ready for disbursement execution");
        }
        if (order.getStatus() != DisbursementStatus.CREATED) {
            throw new DomainRuleViolationException("Only created disbursement orders can be executed");
        }
    }
}
