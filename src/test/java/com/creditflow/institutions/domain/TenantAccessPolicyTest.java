package com.creditflow.institutions.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.creditflow.identity.domain.UserRole;
import com.creditflow.shared.application.exception.AccessDeniedBusinessException;
import com.creditflow.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

class TenantAccessPolicyTest {

    private final TenantAccessPolicy policy = new TenantAccessPolicy();

    @Test
    void platformAdminCanReadAnyInstitution() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "platform.admin@creditflow.pe", UserRole.PLATFORM_ADMIN, null, null);

        assertDoesNotThrow(() -> policy.assertCanReadInstitution(user, 99L));
    }

    @Test
    void institutionUserCannotReadAnotherInstitution() {
        AuthenticatedUser user = new AuthenticatedUser(2L, "admin.andina@creditflow.pe", UserRole.INSTITUTION_ADMIN, 1L, null);

        assertThrows(AccessDeniedBusinessException.class, () -> policy.assertCanReadInstitution(user, 2L));
    }

    @Test
    void branchOfficerCannotOriginateOutsideOwnBranch() {
        AuthenticatedUser user = new AuthenticatedUser(3L, "oficial.lima@creditflow.pe", UserRole.BRANCH_OFFICER, 1L, 1L);

        assertThrows(AccessDeniedBusinessException.class, () -> policy.assertCanOriginateForBranch(user, 1L, 2L));
    }

    @Test
    void auditorCannotMutateInstitutionData() {
        AuthenticatedUser user = new AuthenticatedUser(8L, "auditor.andina@creditflow.pe", UserRole.AUDITOR, 1L, null);

        assertThrows(AccessDeniedBusinessException.class, () -> policy.assertCanWriteInstitutionData(user, 1L, UserRole.INSTITUTION_ADMIN));
    }
}
