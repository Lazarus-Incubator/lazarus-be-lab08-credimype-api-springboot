package com.creditflow.institutions.infrastructure;

import com.creditflow.institutions.domain.Institution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    List<Institution> findByIdIn(List<Long> ids);
}
