package com.creditflow.institutions.infrastructure;

import com.creditflow.institutions.domain.Branch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByInstitutionId(Long institutionId);

    Optional<Branch> findByIdAndInstitutionId(Long id, Long institutionId);
}
