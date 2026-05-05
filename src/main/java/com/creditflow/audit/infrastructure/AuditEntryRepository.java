package com.creditflow.audit.infrastructure;

import com.creditflow.audit.domain.AuditEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
}
