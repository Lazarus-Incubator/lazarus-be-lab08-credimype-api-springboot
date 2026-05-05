package com.creditflow.applications.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationCreatedEvent;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.borrowers.domain.Borrower;
import com.creditflow.borrowers.infrastructure.BorrowerRepository;
import com.creditflow.creditproducts.domain.CreditProduct;
import com.creditflow.creditproducts.infrastructure.CreditProductRepository;
import com.creditflow.institutions.domain.Branch;
import com.creditflow.institutions.domain.Institution;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.institutions.infrastructure.BranchRepository;
import com.creditflow.institutions.infrastructure.InstitutionRepository;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.domain.MoneyAmount;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates draft credit applications after validating tenant, branch, borrower and product
 * preconditions.
 */
@Service
public class CreateCreditApplicationCommandHandler {

    private final CreditApplicationRepository creditApplicationRepository;
    private final InstitutionRepository institutionRepository;
    private final BranchRepository branchRepository;
    private final BorrowerRepository borrowerRepository;
    private final CreditProductRepository creditProductRepository;
    private final TenantAccessPolicy tenantAccessPolicy;
    private final CurrentUserService currentUserService;
    private final ApplicationNumberGenerator applicationNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CreateCreditApplicationCommandHandler(CreditApplicationRepository creditApplicationRepository,
                                                 InstitutionRepository institutionRepository,
                                                 BranchRepository branchRepository,
                                                 BorrowerRepository borrowerRepository,
                                                 CreditProductRepository creditProductRepository,
                                                 TenantAccessPolicy tenantAccessPolicy,
                                                 CurrentUserService currentUserService,
                                                 ApplicationNumberGenerator applicationNumberGenerator,
                                                 ApplicationEventPublisher eventPublisher,
                                                 Clock clock) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.institutionRepository = institutionRepository;
        this.branchRepository = branchRepository;
        this.borrowerRepository = borrowerRepository;
        this.creditProductRepository = creditProductRepository;
        this.tenantAccessPolicy = tenantAccessPolicy;
        this.currentUserService = currentUserService;
        this.applicationNumberGenerator = applicationNumberGenerator;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public Long handle(CreateCreditApplicationCommand command) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        // branchId remains request-driven to support assisted origination scenarios across the institution.
        tenantAccessPolicy.assertCanWriteInstitutionData(user, user.institutionId(),
                com.creditflow.identity.domain.UserRole.INSTITUTION_ADMIN,
                com.creditflow.identity.domain.UserRole.BRANCH_OFFICER);

        Institution institution = institutionRepository.findById(user.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution was not found"));
        Branch branch = branchRepository.findByIdAndInstitutionId(command.branchId(), user.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch was not found"));
        Borrower borrower = borrowerRepository.findByIdAndInstitutionId(command.borrowerId(), user.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower was not found"));
        CreditProduct product = creditProductRepository.findByIdAndInstitutionId(command.productId(), user.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit product was not found"));

        institution.ensureActiveForOrigination();
        branch.ensureActiveForOrigination();
        borrower.ensureCanCreateNewApplications();
        MoneyAmount requestedAmount = MoneyAmount.of(command.requestedAmount());
        product.ensureAvailableFor(requestedAmount, command.termMonths());

        Instant now = Instant.now(clock);
        CreditApplication application = CreditApplication.draft(
                applicationNumberGenerator.next(),
                user.institutionId(),
                branch.getId(),
                borrower.getId(),
                product.getId(),
                requestedAmount,
                command.termMonths(),
                command.purpose(),
                user.userId(),
                now);
        CreditApplication saved = creditApplicationRepository.save(application);
        eventPublisher.publishEvent(new CreditApplicationCreatedEvent(
                saved.getInstitutionId(),
                user.userId(),
                saved.getId().toString(),
                Map.of(
                        "applicationNumber", saved.getApplicationNumber().value(),
                        "branchId", saved.getBranchId(),
                        "borrowerId", saved.getBorrowerId(),
                        "productId", saved.getProductId(),
                        "requestedAmount", saved.getRequestedAmount().toBigDecimal()),
                now));
        return saved.getId();
    }

    public record CreateCreditApplicationCommand(
            Long branchId,
            Long borrowerId,
            Long productId,
            java.math.BigDecimal requestedAmount,
            Integer termMonths,
            String purpose) {
    }
}
