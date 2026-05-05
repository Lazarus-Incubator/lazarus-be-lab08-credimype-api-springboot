package com.creditflow.borrowers.application;

import com.creditflow.borrowers.domain.Borrower;
import com.creditflow.borrowers.domain.DocumentType;
import com.creditflow.borrowers.infrastructure.BorrowerRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.domain.DocumentNumber;
import com.creditflow.shared.domain.MoneyAmount;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates borrowers inside the current tenant.
 *
 * <p>The handler keeps borrower registration small and explicit because document uniqueness is one
 * of the most common originations constraints students will inspect during debugging labs.</p>
 */
@Service
public class CreateBorrowerCommandHandler {

    private final BorrowerRepository borrowerRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final Clock clock;

    public CreateBorrowerCommandHandler(BorrowerRepository borrowerRepository,
                                        CurrentUserService currentUserService,
                                        TenantAccessPolicy tenantAccessPolicy,
                                        Clock clock) {
        this.borrowerRepository = borrowerRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.clock = clock;
    }

    @Transactional
    public Long handle(CreateBorrowerCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        tenantAccessPolicy.assertCanWriteInstitutionData(user, user.institutionId(),
                com.creditflow.identity.domain.UserRole.INSTITUTION_ADMIN,
                com.creditflow.identity.domain.UserRole.BRANCH_OFFICER);

        DocumentNumber documentNumber = DocumentNumber.of(command.documentNumber());
        if (borrowerRepository.existsByInstitutionIdAndDocumentNumber(user.institutionId(), documentNumber)) {
            throw new DomainRuleViolationException("A borrower with the same document already exists in this institution");
        }

        Borrower borrower = Borrower.register(
                user.institutionId(),
                command.documentType(),
                documentNumber,
                command.legalName(),
                command.tradeName(),
                command.economicActivity(),
                MoneyAmount.of(command.monthlyRevenue()),
                Instant.now(clock));
        return borrowerRepository.save(borrower).getId();
    }

    public record CreateBorrowerCommand(
            DocumentType documentType,
            String documentNumber,
            String legalName,
            String tradeName,
            String economicActivity,
            java.math.BigDecimal monthlyRevenue) {
    }
}
