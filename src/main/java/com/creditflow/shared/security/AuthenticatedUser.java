package com.creditflow.shared.security;

import com.creditflow.identity.domain.UserRole;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Immutable view of the authenticated actor used across application services.
 *
 * <p>The object deliberately carries only tenant and authorization data needed by business rules so
 * command handlers do not depend on the persistence model of user accounts.</p>
 */
public record AuthenticatedUser(
        Long userId,
        String email,
        UserRole role,
        Long institutionId,
        Long branchId) {

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public boolean isPlatformAdmin() {
        return role == UserRole.PLATFORM_ADMIN;
    }
}
