package com.creditflow.approvals.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.creditflow.applications.domain.CreditApplication;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.risk.domain.RiskAssessment;
import com.creditflow.risk.domain.RiskRecommendation;
import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.domain.ApplicationNumber;
import com.creditflow.shared.domain.MoneyAmount;
import com.creditflow.shared.domain.RiskScore;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ApprovalLimitPolicyTest {

    private final ApprovalLimitPolicy policy = new ApprovalLimitPolicy();

    @Test
    void shouldRequireCommitteeForHighRiskAssessment() {
        CreditApplication application = application(new BigDecimal("40000.00"));
        RiskAssessment assessment = riskAssessment(45, RiskRecommendation.REVIEW_COMMITTEE);

        assertTrue(policy.requiresCommittee(application, assessment));
    }

    @Test
    void shouldAllowDirectApprovalForLowRiskWithinLimit() {
        CreditApplication application = application(new BigDecimal("25000.00"));
        RiskAssessment assessment = riskAssessment(80, RiskRecommendation.APPROVE);

        assertFalse(policy.requiresCommittee(application, assessment));
        assertDoesNotThrow(() -> policy.assertDirectApprovalAllowed(
                UserRole.INSTITUTION_ADMIN,
                application,
                assessment,
                MoneyAmount.of(new BigDecimal("25000.00"))));
    }

    @Test
    void shouldRejectCommitteeApprovalForCriticalRisk() {
        RiskAssessment assessment = riskAssessment(20, RiskRecommendation.REJECT);

        assertThrows(DomainRuleViolationException.class, () -> policy.assertCommitteeApprovalAllowed(
                UserRole.COMMITTEE_MEMBER,
                assessment,
                MoneyAmount.of(new BigDecimal("10000.00")),
                MoneyAmount.of(new BigDecimal("12000.00"))));
    }

    private CreditApplication application(BigDecimal requestedAmount) {
        return CreditApplication.draft(
                ApplicationNumber.of("CFM-2026-001100"),
                1L,
                1L,
                1L,
                1L,
                MoneyAmount.of(requestedAmount),
                12,
                "Compra de inventario",
                2L,
                Instant.parse("2026-05-05T10:00:00Z"));
    }

    private RiskAssessment riskAssessment(int score, RiskRecommendation recommendation) {
        return RiskAssessment.record(
                1L,
                1L,
                RiskScore.of(score),
                new BigDecimal("0.35"),
                "[]",
                recommendation,
                5L,
                Instant.parse("2026-05-05T10:30:00Z"));
    }
}
