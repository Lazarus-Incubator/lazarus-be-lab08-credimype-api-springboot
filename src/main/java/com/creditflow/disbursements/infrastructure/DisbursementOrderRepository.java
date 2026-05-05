package com.creditflow.disbursements.infrastructure;

import com.creditflow.disbursements.domain.DisbursementOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisbursementOrderRepository extends JpaRepository<DisbursementOrder, Long> {

    Optional<DisbursementOrder> findByIdAndInstitutionId(Long id, Long institutionId);
}
