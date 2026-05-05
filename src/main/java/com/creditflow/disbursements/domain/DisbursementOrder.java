package com.creditflow.disbursements.domain;

import com.creditflow.creditproducts.domain.CurrencyCode;
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
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "disbursement_order")
public class DisbursementOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private MoneyAmount amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency;

    @Column(name = "destination_bank", nullable = false)
    private String destinationBank;

    @Column(name = "destination_account", nullable = false)
    private String destinationAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisbursementStatus status;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "executed_by_user_id")
    private Long executedByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Version
    private Long version;

    protected DisbursementOrder() {
    }

    public static DisbursementOrder create(Long applicationId,
                                           Long institutionId,
                                           MoneyAmount amount,
                                           CurrencyCode currency,
                                           String destinationBank,
                                           String destinationAccount,
                                           Long createdByUserId,
                                           Instant createdAt) {
        DisbursementOrder order = new DisbursementOrder();
        order.applicationId = applicationId;
        order.institutionId = institutionId;
        order.amount = amount;
        order.currency = currency;
        order.destinationBank = destinationBank;
        order.destinationAccount = destinationAccount;
        order.status = DisbursementStatus.CREATED;
        order.createdByUserId = createdByUserId;
        order.createdAt = createdAt;
        return order;
    }

    public void markExecuted(Long executedByUserId, String idempotencyKey, Instant executedAt) {
        if (status != DisbursementStatus.CREATED) {
            throw new DomainRuleViolationException("The disbursement order has already been processed");
        }
        this.status = DisbursementStatus.EXECUTED;
        this.executedByUserId = executedByUserId;
        this.executedAt = executedAt;
        this.idempotencyKey = idempotencyKey;
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

    public MoneyAmount getAmount() {
        return amount;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public String getDestinationBank() {
        return destinationBank;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public DisbursementStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Long getExecutedByUserId() {
        return executedByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public Long getVersion() {
        return version;
    }
}
