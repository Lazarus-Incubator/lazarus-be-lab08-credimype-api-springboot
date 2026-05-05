package com.creditflow.creditproducts.infrastructure;

import com.creditflow.creditproducts.domain.CreditProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditProductRepository extends JpaRepository<CreditProduct, Long> {

    List<CreditProduct> findByInstitutionId(Long institutionId);

    Optional<CreditProduct> findByIdAndInstitutionId(Long id, Long institutionId);
}
