package com.creditflow.approvals.domain;

import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.domain.MoneyAmount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "approval_decision")
public class ApprovalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalDecisionResult decision;

    @Column(name = "approved_amount")
    private MoneyAmount approvedAmount;

    @Column(length = 500)
    private String reason;

    @Column(name = "decided_by_user_id", nullable = false)
    private Long decidedByUserId;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_source", nullable = false)
    private DecisionSource decisionSource;

    protected ApprovalDecision() {
    }

    public static ApprovalDecision approve(Long applicationId,
                                           Long institutionId,
                                           MoneyAmount approvedAmount,
                                           Long decidedByUserId,
                                           Instant decidedAt,
                                           DecisionSource decisionSource) {
        if (approvedAmount == null) {
            throw new DomainRuleViolationException("Approved amount is required");
        }
        ApprovalDecision decision = new ApprovalDecision();
        decision.applicationId = applicationId;
        decision.institutionId = institutionId;
        decision.decision = ApprovalDecisionResult.APPROVED;
        decision.approvedAmount = approvedAmount;
        decision.decidedByUserId = decidedByUserId;
        decision.decidedAt = decidedAt;
        decision.decisionSource = decisionSource;
        return decision;
    }

    public static ApprovalDecision reject(Long applicationId,
                                          Long institutionId,
                                          String reason,
                                          Long decidedByUserId,
                                          Instant decidedAt,
                                          DecisionSource decisionSource) {
        if (reason == null || reason.isBlank()) {
            throw new DomainRuleViolationException("Rejection reason is required");
        }
        ApprovalDecision decision = new ApprovalDecision();
        decision.applicationId = applicationId;
        decision.institutionId = institutionId;
        decision.decision = ApprovalDecisionResult.REJECTED;
        decision.reason = reason;
        decision.decidedByUserId = decidedByUserId;
        decision.decidedAt = decidedAt;
        decision.decisionSource = decisionSource;
        return decision;
    }

    public Long getId() {
        return id;
    }

    public ApprovalDecisionResult getDecision() {
        return decision;
    }

    public MoneyAmount getApprovedAmount() {
        return approvedAmount;
    }

    public String getReason() {
        return reason;
    }

    public DecisionSource getDecisionSource() {
        return decisionSource;
    }
}
