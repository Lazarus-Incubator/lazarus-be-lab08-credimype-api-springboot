package com.creditflow.creditproducts.application;

import com.creditflow.creditproducts.domain.CreditProduct;
import com.creditflow.creditproducts.infrastructure.CreditProductRepository;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CreditProductQueryService {

    private final CreditProductRepository creditProductRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public CreditProductQueryService(CreditProductRepository creditProductRepository,
                                     CurrentUserService currentUserService,
                                     TenantAccessPolicy tenantAccessPolicy) {
        this.creditProductRepository = creditProductRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public List<CreditProductView> list() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        List<CreditProduct> products = user.isPlatformAdmin()
                ? creditProductRepository.findAll(Sort.by("name"))
                : creditProductRepository.findByInstitutionId(user.institutionId());
        return products.stream().map(CreditProductView::from).toList();
    }

    public CreditProductView getById(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CreditProduct product = creditProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit product was not found"));
        tenantAccessPolicy.assertCanReadInstitution(user, product.getInstitutionId());
        return CreditProductView.from(product);
    }

    public record CreditProductView(
            Long id,
            Long institutionId,
            String code,
            String name,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Integer minTermMonths,
            Integer maxTermMonths,
            BigDecimal annualRate,
            String status) {

        private static CreditProductView from(CreditProduct product) {
            return new CreditProductView(
                    product.getId(),
                    product.getInstitutionId(),
                    product.getCode(),
                    product.getName(),
                    product.getCurrency().name(),
                    product.getMinAmount().toBigDecimal(),
                    product.getMaxAmount().toBigDecimal(),
                    product.getMinTermMonths(),
                    product.getMaxTermMonths(),
                    product.getAnnualRate(),
                    product.getStatus().name());
        }
    }
}
