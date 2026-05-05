package com.creditflow.identity.application;

import com.creditflow.identity.domain.UserAccount;
import com.creditflow.identity.domain.UserStatus;
import com.creditflow.identity.infrastructure.UserAccountRepository;
import com.creditflow.shared.application.exception.AuthenticationFailedException;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import com.creditflow.shared.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CurrentUserService currentUserService;

    public AuthService(UserAccountRepository userAccountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       CurrentUserService currentUserService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.currentUserService = currentUserService;
    }

    public LoginResult login(String email, String password) {
        UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid credentials"));
        if (userAccount.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid credentials");
        }
        return new LoginResult(
                jwtTokenService.generateToken(userAccount),
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getFullName(),
                userAccount.getRole().name(),
                userAccount.getInstitutionId(),
                userAccount.getBranchId());
    }

    public CurrentUserProfile me() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        UserAccount account = userAccountRepository.findById(user.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User account was not found"));
        return new CurrentUserProfile(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getRole().name(),
                account.getPasswordHash(),
                account.getInstitutionId(),
                account.getBranchId());
    }

    public record LoginResult(
            String token,
            Long userId,
            String email,
            String fullName,
            String role,
            Long institutionId,
            Long branchId) {
    }

    public record CurrentUserProfile(
            Long userId,
            String fullName,
            String email,
            String role,
            String passwordHash,
            Long institutionId,
            Long branchId) {
    }
}
