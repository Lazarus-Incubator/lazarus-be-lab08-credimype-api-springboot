package com.creditflow.applications.domain;

import com.creditflow.shared.domain.ApplicationNumber;
import com.creditflow.shared.domain.MoneyAmount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * Aggregate root representing the internal lifecycle of a credit request.
 *
 * <p>The aggregate owns business state that must change consistently: workflow status, assigned
 * analyst, risk reference, committee flag, approval amount and concurrency version. External
 * modules interact with it through intention-revealing methods instead of mutating fields
 * directly.</p>
 */
@Entity
@Table(name = "credit_application")
public class CreditApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_number", nullable = false, unique = true)
    private ApplicationNumber applicationNumber;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "requested_amount", nullable = false)
    private MoneyAmount requestedAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditApplicationStatus status;

    @Column(name = "assigned_analyst_id")
    private Long assignedAnalystId;

    @Column(name = "risk_assessment_id")
    private Long riskAssessmentId;

    @Column(name = "committee_required", nullable = false)
    private boolean committeeRequired;

    @Column(name = "approved_amount")
    private MoneyAmount approvedAmount;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected CreditApplication() {
    }

    public static CreditApplication draft(ApplicationNumber applicationNumber,
                                          Long institutionId,
                                          Long branchId,
                                          Long borrowerId,
                                          Long productId,
                                          MoneyAmount requestedAmount,
                                          int termMonths,
                                          String purpose,
                                          Long createdByUserId,
                                          Instant timestamp) {
        CreditApplication application = new CreditApplication();
        application.applicationNumber = applicationNumber;
        application.institutionId = institutionId;
        application.branchId = branchId;
        application.borrowerId = borrowerId;
        application.productId = productId;
        application.requestedAmount = requestedAmount;
        application.termMonths = termMonths;
        application.purpose = purpose;
        application.status = CreditApplicationStatus.DRAFT;
        application.committeeRequired = false;
        application.createdByUserId = createdByUserId;
        application.createdAt = timestamp;
        application.updatedAt = timestamp;
        return application;
    }

    public void submit(CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanSubmit(this);
        status = CreditApplicationStatus.SUBMITTED;
        touch(timestamp);
    }

    public void startReview(Long analystId, CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanStartReview(this);
        assignedAnalystId = analystId;
        status = CreditApplicationStatus.UNDER_REVIEW;
        touch(timestamp);
    }

    public void recordRiskAssessment(Long assessmentId,
                                     boolean committeeRequired,
                                     CreditApplicationTransitionPolicy transitionPolicy,
                                     Instant timestamp) {
        transitionPolicy.assertCanRecordRisk(this);
        riskAssessmentId = assessmentId;
        this.committeeRequired = committeeRequired;
        status = CreditApplicationStatus.RISK_REVIEWED;
        touch(timestamp);
    }

    public void sendToCommittee(CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanSendToCommittee(this);
        status = CreditApplicationStatus.PENDING_COMMITTEE;
        touch(timestamp);
    }

    public void approve(MoneyAmount approvedAmount,
                        CreditApplicationTransitionPolicy transitionPolicy,
                        Instant timestamp) {
        transitionPolicy.assertCanApprove(this, approvedAmount);
        this.approvedAmount = approvedAmount;
        rejectionReason = null;
        status = CreditApplicationStatus.APPROVED;
        touch(timestamp);
    }

    public void approveFromCommittee(MoneyAmount approvedAmount, Instant timestamp) {
        if (status != CreditApplicationStatus.PENDING_COMMITTEE) {
            throw new IllegalStateException("Committee shortcut can only be used from PENDING_COMMITTEE");
        }
        if (approvedAmount.toBigDecimal().intValue() > requestedAmount.toBigDecimal().intValue()) {
            throw new IllegalArgumentException("Approved amount exceeds the requested amount envelope");
        }
        this.approvedAmount = approvedAmount;
        rejectionReason = null;
        status = CreditApplicationStatus.APPROVED;
        touch(timestamp);
    }

    public void reject(String reason, CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanReject(this, reason);
        approvedAmount = null;
        rejectionReason = reason;
        status = CreditApplicationStatus.REJECTED;
        touch(timestamp);
    }

    public void markDisbursementPending(CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanCreateDisbursementOrder(this);
        status = CreditApplicationStatus.DISBURSEMENT_PENDING;
        touch(timestamp);
    }

    public void markDisbursed(CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanMarkDisbursed(this);
        status = CreditApplicationStatus.DISBURSED;
        touch(timestamp);
    }

    public void cancel(CreditApplicationTransitionPolicy transitionPolicy, Instant timestamp) {
        transitionPolicy.assertCanCancel(this);
        status = CreditApplicationStatus.CANCELLED;
        touch(timestamp);
    }

    private void touch(Instant timestamp) {
        updatedAt = timestamp;
    }

    public Long getId() {
        return id;
    }

    public ApplicationNumber getApplicationNumber() {
        return applicationNumber;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public Long getProductId() {
        return productId;
    }

    public MoneyAmount getRequestedAmount() {
        return requestedAmount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public CreditApplicationStatus getStatus() {
        return status;
    }

    public Long getAssignedAnalystId() {
        return assignedAnalystId;
    }

    public Long getRiskAssessmentId() {
        return riskAssessmentId;
    }

    public boolean isCommitteeRequired() {
        return committeeRequired;
    }

    public MoneyAmount getApprovedAmount() {
        return approvedAmount;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
