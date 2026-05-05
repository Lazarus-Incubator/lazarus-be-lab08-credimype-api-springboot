package com.creditflow.disbursements.infrastructure;

import com.creditflow.disbursements.domain.IdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByInstitutionIdAndIdempotencyKey(Long institutionId, String idempotencyKey);
}
