package com.creditflow.borrowers.application;

import com.creditflow.borrowers.domain.Borrower;
import com.creditflow.borrowers.infrastructure.BorrowerRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BorrowerQueryService {

    private final BorrowerRepository borrowerRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public BorrowerQueryService(BorrowerRepository borrowerRepository,
                                CurrentUserService currentUserService,
                                TenantAccessPolicy tenantAccessPolicy) {
        this.borrowerRepository = borrowerRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public List<BorrowerView> list() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        List<Borrower> borrowers = user.isPlatformAdmin()
                ? borrowerRepository.findAll(Sort.by("legalName"))
                : borrowerRepository.findByInstitutionId(user.institutionId());
        return borrowers.stream().map(BorrowerView::from).toList();
    }

    public BorrowerView getById(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower was not found"));
        tenantAccessPolicy.assertCanReadInstitution(user, borrower.getInstitutionId());
        return BorrowerView.from(borrower);
    }

    public record BorrowerView(
            Long id,
            Long institutionId,
            String documentType,
            String documentNumber,
            String legalName,
            String tradeName,
            String economicActivity,
            BigDecimal monthlyRevenue,
            String status,
            java.time.Instant createdAt) {

        private static BorrowerView from(Borrower borrower) {
            return new BorrowerView(
                    borrower.getId(),
                    borrower.getInstitutionId(),
                    borrower.getDocumentType().name(),
                    borrower.getDocumentNumber().value(),
                    borrower.getLegalName(),
                    borrower.getTradeName(),
                    borrower.getEconomicActivity(),
                    borrower.getMonthlyRevenue().toBigDecimal(),
                    borrower.getStatus().name(),
                    borrower.getCreatedAt());
        }
    }
}
