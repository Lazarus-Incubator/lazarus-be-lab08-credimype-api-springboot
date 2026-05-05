package com.creditflow.approvals.domain;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.risk.domain.RiskAssessment;
import com.creditflow.risk.domain.RiskLevel;
import com.creditflow.risk.domain.RiskRecommendation;
import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.domain.MoneyAmount;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Encodes who may approve an application after risk review and when committee review is mandatory.
 *
 * <p>The policy protects the lab from silent approval drift. A change in score, recommendation or
 * amount should alter approval authority in one place instead of leaving multiple handlers with
 * slightly different thresholds.</p>
 */
@Component
public class ApprovalLimitPolicy {

    private static final MoneyAmount LOW_RISK_DIRECT_LIMIT = MoneyAmount.of(new BigDecimal("50000.00"));
    private static final MoneyAmount MEDIUM_RISK_DIRECT_LIMIT = MoneyAmount.of(new BigDecimal("30000.00"));

    public boolean requiresCommittee(CreditApplication application, RiskAssessment assessment) {
        if (assessment.getRecommendation() == RiskRecommendation.REVIEW_COMMITTEE) {
            return true;
        }
        if (assessment.getRecommendation() == RiskRecommendation.REJECT) {
            return false;
        }
        if (assessment.getRiskLevel() == RiskLevel.CRITICAL || assessment.getRiskLevel() == RiskLevel.HIGH) {
            return true;
        }
        return assessment.getRiskLevel() == RiskLevel.MEDIUM
                && application.getRequestedAmount().isGreaterThan(MEDIUM_RISK_DIRECT_LIMIT);
    }

    public void assertDirectApprovalAllowed(UserRole role,
                                            CreditApplication application,
                                            RiskAssessment assessment,
                                            MoneyAmount approvedAmount) {
        if (role != UserRole.INSTITUTION_ADMIN) {
            throw new DomainRuleViolationException("Only institution administrators can directly approve non-committee cases");
        }
        if (assessment.getRecommendation() == RiskRecommendation.REJECT) {
            throw new DomainRuleViolationException("Applications recommended for rejection cannot be approved");
        }
        if (requiresCommittee(application, assessment)) {
            throw new DomainRuleViolationException("This application requires committee approval");
        }
        MoneyAmount limit = assessment.getRiskLevel() == RiskLevel.LOW ? LOW_RISK_DIRECT_LIMIT : MEDIUM_RISK_DIRECT_LIMIT;
        if (approvedAmount.isGreaterThan(limit)) {
            throw new DomainRuleViolationException("Approved amount exceeds the direct approval limit");
        }
    }

    public void assertCommitteeApprovalAllowed(UserRole role,
                                               RiskAssessment assessment,
                                               MoneyAmount approvedAmount,
                                               MoneyAmount requestedAmount) {
        if (role != UserRole.COMMITTEE_MEMBER) {
            throw new DomainRuleViolationException("Only committee members can approve committee cases");
        }
        if (assessment.getRiskLevel() == RiskLevel.CRITICAL || assessment.getRecommendation() == RiskRecommendation.REJECT) {
            throw new DomainRuleViolationException("Critical or rejected recommendations cannot be approved even by committee");
        }
        if (approvedAmount.isGreaterThan(requestedAmount)) {
            throw new DomainRuleViolationException("Approved amount cannot exceed the requested amount");
        }
    }
}
