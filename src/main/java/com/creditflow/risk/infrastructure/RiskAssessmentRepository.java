package com.creditflow.risk.infrastructure;

import com.creditflow.risk.domain.RiskAssessment;
import com.creditflow.risk.domain.RiskLevel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findByApplicationId(Long applicationId);

    @Query("""
            select r.riskLevel as riskLevel, count(r.id) as total, avg(r.score) as averageScore
            from RiskAssessment r
            where (:institutionId is null or r.institutionId = :institutionId)
            group by r.riskLevel
            """)
    List<RiskSummaryProjection> summarizeByRiskLevel(@Param("institutionId") Long institutionId);

    interface RiskSummaryProjection {
        RiskLevel getRiskLevel();

        long getTotal();

        double getAverageScore();
    }
}
