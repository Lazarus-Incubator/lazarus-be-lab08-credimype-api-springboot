package com.creditflow.institutions.application;

import com.creditflow.institutions.domain.Institution;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.institutions.infrastructure.InstitutionRepository;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class InstitutionQueryService {

    private final InstitutionRepository institutionRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public InstitutionQueryService(InstitutionRepository institutionRepository,
                                   CurrentUserService currentUserService,
                                   TenantAccessPolicy tenantAccessPolicy) {
        this.institutionRepository = institutionRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public List<InstitutionView> list() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.isPlatformAdmin()) {
            return institutionRepository.findAll(Sort.by("legalName")).stream()
                    .map(InstitutionView::from)
                    .toList();
        }
        return List.of(getById(user.institutionId()));
    }

    public InstitutionView getById(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        tenantAccessPolicy.assertCanReadInstitution(user, id);
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution was not found"));
        return InstitutionView.from(institution);
    }

    public record InstitutionView(
            Long id,
            String code,
            String legalName,
            String tradeName,
            String status,
            java.time.Instant createdAt) {

        private static InstitutionView from(Institution institution) {
            return new InstitutionView(
                    institution.getId(),
                    institution.getCode(),
                    institution.getLegalName(),
                    institution.getTradeName(),
                    institution.getStatus().name(),
                    institution.getCreatedAt());
        }
    }
}
