package com.creditflow.institutions.domain;

import com.creditflow.identity.domain.UserRole;
import com.creditflow.shared.application.exception.AccessDeniedBusinessException;
import com.creditflow.shared.security.AuthenticatedUser;
import java.util.Arrays;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Centralizes tenant and role authorization rules for the monolithic API.
 *
 * <p>The same authenticated user may be allowed to read an institution-wide report, originate
 * credit only inside a single branch, or be blocked from all mutations because the role is audit
 * only. Keeping those rules here avoids subtle leaks caused by repeating tenant checks in
 * controllers or repositories.</p>
 */
@Component
public class TenantAccessPolicy {

    public void assertCanReadInstitution(AuthenticatedUser user, Long institutionId) {
        if (user.isPlatformAdmin()) {
            return;
        }
        if (user.institutionId() == null || !user.institutionId().equals(institutionId)) {
            throw new AccessDeniedBusinessException("The authenticated user cannot access data from another institution");
        }
    }

    public void assertCanReadBranch(AuthenticatedUser user, Long institutionId, Long branchId) {
        assertCanReadInstitution(user, institutionId);
        if (user.role() == UserRole.BRANCH_OFFICER && user.branchId() != null && !user.branchId().equals(branchId)) {
            throw new AccessDeniedBusinessException("Branch officers can only access their assigned branch");
        }
    }

    public void assertCanOriginateForBranch(AuthenticatedUser user, Long institutionId, Long branchId) {
        assertWithinInstitution(user, institutionId, Set.of(UserRole.INSTITUTION_ADMIN, UserRole.BRANCH_OFFICER));
        if (user.role() == UserRole.BRANCH_OFFICER && user.branchId() != null && !user.branchId().equals(branchId)) {
            throw new AccessDeniedBusinessException("Branch officers can only originate applications for their branch");
        }
    }

    public void assertCanReview(AuthenticatedUser user, Long institutionId) {
        assertWithinInstitution(user, institutionId, Set.of(UserRole.CREDIT_ANALYST, UserRole.INSTITUTION_ADMIN));
    }

    public void assertCanAssessRisk(AuthenticatedUser user, Long institutionId) {
        assertWithinInstitution(user, institutionId, Set.of(UserRole.RISK_OFFICER, UserRole.INSTITUTION_ADMIN));
    }

    public void assertCanSendToCommittee(AuthenticatedUser user, Long institutionId) {
        assertWithinInstitution(user, institutionId, Set.of(UserRole.CREDIT_ANALYST, UserRole.INSTITUTION_ADMIN));
    }

    public void assertCanApproveOrReject(AuthenticatedUser user, Long institutionId, UserRole... roles) {
        assertWithinInstitution(user, institutionId, Set.copyOf(Arrays.asList(roles)));
    }

    public void assertCanCreateDisbursementOrder(AuthenticatedUser user, Long institutionId) {
        assertWithinInstitution(user, institutionId, Set.of(UserRole.OPERATIONS_OFFICER, UserRole.INSTITUTION_ADMIN));
    }

    public void assertCanExecuteDisbursement(AuthenticatedUser user, Long institutionId) {
        assertWithinInstitution(user, institutionId, Set.of(UserRole.OPERATIONS_OFFICER, UserRole.INSTITUTION_ADMIN));
    }

    public void assertCanWriteInstitutionData(AuthenticatedUser user, Long institutionId, UserRole... roles) {
        assertWithinInstitution(user, institutionId, Set.copyOf(Arrays.asList(roles)));
    }

    private void assertWithinInstitution(AuthenticatedUser user, Long institutionId, Set<UserRole> allowedRoles) {
        if (user.isPlatformAdmin()) {
            throw new AccessDeniedBusinessException("Platform administrators are read-only in this laboratory API");
        }
        if (user.role() == UserRole.AUDITOR) {
            throw new AccessDeniedBusinessException("Auditors can only query data");
        }
        if (user.institutionId() == null || !user.institutionId().equals(institutionId)) {
            throw new AccessDeniedBusinessException("The authenticated user does not belong to the required institution");
        }
        if (!allowedRoles.contains(user.role())) {
            throw new AccessDeniedBusinessException("The authenticated role cannot perform this action");
        }
    }
}
