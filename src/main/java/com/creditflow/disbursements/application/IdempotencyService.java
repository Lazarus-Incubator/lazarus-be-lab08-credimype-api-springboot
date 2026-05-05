package com.creditflow.disbursements.application;

import com.creditflow.disbursements.domain.IdempotencyOperationType;
import com.creditflow.disbursements.domain.IdempotencyRecord;
import com.creditflow.disbursements.infrastructure.IdempotencyRecordRepository;
import com.creditflow.shared.application.exception.IdempotencyConflictException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Coordinates idempotent execution of financially sensitive operations.
 *
 * <p>Disbursement execution is the critical write path that most needs replay safety. This service
 * keeps key compatibility rules explicit so repeated requests either return the same successful
 * result or fail fast when the key was reused for a different target.</p>
 */
@Service
public class IdempotencyService {

    public static final String DISBURSEMENT_ORDER_RESOURCE = "DISBURSEMENT_ORDER";

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final Clock clock;

    public IdempotencyService(IdempotencyRecordRepository idempotencyRecordRepository, Clock clock) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.clock = clock;
    }

    public Optional<IdempotencyRecord> findRecord(Long institutionId, String idempotencyKey) {
        return idempotencyRecordRepository.findByInstitutionIdAndIdempotencyKey(institutionId, idempotencyKey);
    }

    public void assertCompatible(IdempotencyRecord record,
                                 IdempotencyOperationType operationType,
                                 String resourceType,
                                 Long resourceId) {
        // Idempotency is evaluated at institution level to avoid cross-tenant key collisions.
        if (!record.getResourceType().equals(resourceType)) {
            throw new IdempotencyConflictException("The provided idempotency key is already associated with another operation");
        }
    }

    public void registerSuccess(Long institutionId,
                                String idempotencyKey,
                                IdempotencyOperationType operationType,
                                String resourceType,
                                Long resourceId,
                                String responseStatus) {
        try {
            idempotencyRecordRepository.save(IdempotencyRecord.success(
                    institutionId,
                    idempotencyKey,
                    operationType,
                    resourceType,
                    resourceId,
                    responseStatus,
                    Instant.now(clock)));
        } catch (DataIntegrityViolationException ex) {
            IdempotencyRecord existing = findRecord(institutionId, idempotencyKey)
                    .orElseThrow(() -> ex);
            assertCompatible(existing, operationType, resourceType, resourceId);
        }
    }
}
