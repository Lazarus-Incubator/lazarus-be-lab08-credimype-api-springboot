package com.creditflow.risk.domain;

import com.creditflow.shared.domain.RiskScore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "risk_assessment")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private RiskScore score;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "debt_to_income_ratio", nullable = false, precision = 8, scale = 4)
    private BigDecimal debtToIncomeRatio;

    @Column(name = "flags_json", columnDefinition = "text")
    private String flags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskRecommendation recommendation;

    @Column(name = "assessed_by_user_id", nullable = false)
    private Long assessedByUserId;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    protected RiskAssessment() {
    }

    public static RiskAssessment record(Long applicationId,
                                        Long institutionId,
                                        RiskScore score,
                                        BigDecimal debtToIncomeRatio,
                                        String flags,
                                        RiskRecommendation recommendation,
                                        Long assessedByUserId,
                                        Instant assessedAt) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.applicationId = applicationId;
        assessment.institutionId = institutionId;
        assessment.score = score;
        assessment.riskLevel = RiskLevel.fromScore(score);
        assessment.debtToIncomeRatio = debtToIncomeRatio;
        assessment.flags = flags;
        assessment.recommendation = recommendation;
        assessment.assessedByUserId = assessedByUserId;
        assessment.assessedAt = assessedAt;
        return assessment;
    }

    public Long getId() {
        return id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public RiskScore getScore() {
        return score;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public BigDecimal getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }

    public String getFlags() {
        return flags;
    }

    public RiskRecommendation getRecommendation() {
        return recommendation;
    }

    public Long getAssessedByUserId() {
        return assessedByUserId;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }
}
