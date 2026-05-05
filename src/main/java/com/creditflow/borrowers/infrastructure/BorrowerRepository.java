package com.creditflow.borrowers.infrastructure;

import com.creditflow.borrowers.domain.Borrower;
import com.creditflow.shared.domain.DocumentNumber;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    List<Borrower> findByInstitutionId(Long institutionId);

    Optional<Borrower> findByIdAndInstitutionId(Long id, Long institutionId);

    boolean existsByInstitutionIdAndDocumentNumber(Long institutionId, DocumentNumber documentNumber);
}
