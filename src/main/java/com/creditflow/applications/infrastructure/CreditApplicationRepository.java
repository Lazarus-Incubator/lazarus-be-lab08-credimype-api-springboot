package com.creditflow.applications.infrastructure;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.shared.domain.ApplicationNumber;
import com.creditflow.shared.domain.DocumentNumber;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {

    Optional<CreditApplication> findByApplicationNumber(ApplicationNumber applicationNumber);

    Optional<CreditApplication> findByIdAndInstitutionId(Long id, Long institutionId);

    @Query("""
            select a from CreditApplication a
            where (:institutionId is null or a.institutionId = :institutionId)
              and (:status is null or a.status = :status)
              and (:branchId is null or a.branchId = :branchId)
              and (:fromDate is null or a.createdAt >= :fromDate)
              and (:toDate is null or a.createdAt < :toDate)
              and (:borrowerDocument is null or exists (
                    select 1 from Borrower b
                    where b.id = a.borrowerId and b.documentNumber = :borrowerDocument
              ))
            order by a.createdAt desc
            """)
    List<CreditApplication> search(@Param("institutionId") Long institutionId,
                                   @Param("status") CreditApplicationStatus status,
                                   @Param("branchId") Long branchId,
                                   @Param("borrowerDocument") DocumentNumber borrowerDocument,
                                   @Param("fromDate") Instant fromDate,
                                   @Param("toDate") Instant toDate);

    @Query("""
            select a.status as status, count(a.id) as total
            from CreditApplication a
            where (:institutionId is null or a.institutionId = :institutionId)
            group by a.status
            """)
    List<PipelineStageCountProjection> countByStatus(@Param("institutionId") Long institutionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CreditApplication a
               set a.status = com.creditflow.applications.domain.CreditApplicationStatus.UNDER_REVIEW,
                   a.assignedAnalystId = :analystId,
                   a.updatedAt = :updatedAt
             where a.id = :applicationId
            """)
    int markUnderReviewWithoutVersion(@Param("applicationId") Long applicationId,
                                      @Param("analystId") Long analystId,
                                      @Param("updatedAt") Instant updatedAt);

    interface PipelineStageCountProjection {
        CreditApplicationStatus getStatus();

        long getTotal();
    }
}
