package com.creditflow.institutions.application;

import com.creditflow.identity.domain.UserRole;
import com.creditflow.institutions.domain.Branch;
import com.creditflow.institutions.domain.TenantAccessPolicy;
import com.creditflow.institutions.infrastructure.BranchRepository;
import com.creditflow.shared.application.exception.ResourceNotFoundException;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BranchQueryService {

    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;
    private final TenantAccessPolicy tenantAccessPolicy;

    public BranchQueryService(BranchRepository branchRepository,
                              CurrentUserService currentUserService,
                              TenantAccessPolicy tenantAccessPolicy) {
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
        this.tenantAccessPolicy = tenantAccessPolicy;
    }

    public List<BranchView> list() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.isPlatformAdmin()) {
            return branchRepository.findAll(Sort.by("city", "name")).stream().map(BranchView::from).toList();
        }
        if (user.role() == UserRole.BRANCH_OFFICER && user.branchId() != null) {
            return List.of(getById(user.branchId()));
        }
        return branchRepository.findByInstitutionId(user.institutionId()).stream()
                .map(BranchView::from)
                .toList();
    }

    public BranchView getById(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch was not found"));
        tenantAccessPolicy.assertCanReadBranch(user, branch.getInstitutionId(), branch.getId());
        return BranchView.from(branch);
    }

    public record BranchView(
            Long id,
            Long institutionId,
            String code,
            String name,
            String city,
            String status) {

        private static BranchView from(Branch branch) {
            return new BranchView(
                    branch.getId(),
                    branch.getInstitutionId(),
                    branch.getCode(),
                    branch.getName(),
                    branch.getCity(),
                    branch.getStatus().name());
        }
    }
}
