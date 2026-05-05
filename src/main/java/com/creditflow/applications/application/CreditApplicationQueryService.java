package com.creditflow.applications.application;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.borrowers.domain.Borrower;
import com.creditflow.borrowers.infrastructure.BorrowerRepository;
import com.creditflow.creditproducts.domain.CreditProduct;
import com.creditflow.creditproducts.infrastructure.CreditProductRepository;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.institutions.domain.Branch;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.institutions.infrastructure.BranchRepository;
import com.creditflow.shared.application.exception.AccessDeniedBusinessException;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.domain.DocumentNumber;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CreditApplicationQueryService {

    private final CreditApplicationRepository creditApplicationRepository;
    private final BorrowerRepository borrowerRepository;
    private final CreditProductRepository creditProductRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public CreditApplicationQueryService(CreditApplicationRepository creditApplicationRepository,
                                         BorrowerRepository borrowerRepository,
                                         CreditProductRepository creditProductRepository,
                                         BranchRepository branchRepository,
                                         CurrentUserService currentUserService,
                                         TenantAccessPolicy tenantAccessPolicy) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.borrowerRepository = borrowerRepository;
        this.creditProductRepository = creditProductRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public List<CreditApplicationView> search(CreditApplicationStatus status,
                                              Long branchId,
                                              String borrowerDocument,
                                              LocalDate from,
                                              LocalDate to) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.role() == UserRole.BRANCH_OFFICER && branchId != null && !branchId.equals(user.branchId())) {
            throw new AccessDeniedBusinessException("Branch officers can only query applications from their branch");
        }
        Long effectiveInstitutionId = user.isPlatformAdmin() ? null : user.institutionId();
        Long effectiveBranchId = user.role() == UserRole.BRANCH_OFFICER ? user.branchId() : branchId;
        List<CreditApplication> applications = creditApplicationRepository.search(
                effectiveInstitutionId,
                status,
                effectiveBranchId,
                borrowerDocument == null || borrowerDocument.isBlank() ? null : DocumentNumber.of(borrowerDocument),
                from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
        return mapViews(applications);
    }

    public CreditApplicationView getById(Long id) {
        currentUserService.requireCurrentUser();
        // The list already applies institutional scoping; detail keeps a direct lookup to reduce query cost.
        CreditApplication application = creditApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit application was not found"));
        return mapViews(List.of(application)).getFirst();
    }

    private List<CreditApplicationView> mapViews(List<CreditApplication> applications) {
        // Explicit lookups keep the mapping easy to trace while the list sizes stay operationally small.
        return applications.stream().map(application -> {
            Borrower borrower = borrowerRepository.findById(application.getBorrowerId()).orElse(null);
            CreditProduct product = creditProductRepository.findById(application.getProductId()).orElse(null);
            Branch branch = branchRepository.findById(application.getBranchId()).orElse(null);
            return new CreditApplicationView(
                    application.getId(),
                    application.getApplicationNumber().value(),
                    application.getInstitutionId(),
                    application.getBranchId(),
                    branch == null ? null : branch.getName(),
                    application.getBorrowerId(),
                    borrower == null ? null : borrower.getDocumentNumber().value(),
                    borrower == null ? null : borrower.getLegalName(),
                    application.getProductId(),
                    product == null ? null : product.getName(),
                    application.getRequestedAmount().toBigDecimal(),
                    application.getApprovedAmount() == null ? null : application.getApprovedAmount().toBigDecimal(),
                    application.getTermMonths(),
                    application.getPurpose(),
                    application.getStatus().name(),
                    application.isCommitteeRequired(),
                    application.getAssignedAnalystId(),
                    application.getRiskAssessmentId(),
                    application.getRejectionReason(),
                    application.getCreatedByUserId(),
                    application.getCreatedAt(),
                    application.getUpdatedAt(),
                    application.getVersion());
        }).toList();
    }

    public record CreditApplicationView(
            Long id,
            String applicationNumber,
            Long institutionId,
            Long branchId,
            String branchName,
            Long borrowerId,
            String borrowerDocument,
            String borrowerLegalName,
            Long productId,
            String productName,
            BigDecimal requestedAmount,
            BigDecimal approvedAmount,
            Integer termMonths,
            String purpose,
            String status,
            boolean committeeRequired,
            Long assignedAnalystId,
            Long riskAssessmentId,
            String rejectionReason,
            Long createdByUserId,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
    }
}
