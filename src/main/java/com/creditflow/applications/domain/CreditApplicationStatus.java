package com.creditflow.applications.domain;

public enum CreditApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    RISK_REVIEWED,
    PENDING_COMMITTEE,
    APPROVED,
    REJECTED,
    DISBURSEMENT_PENDING,
    DISBURSED,
    CANCELLED
}
