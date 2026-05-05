package com.creditflow.creditproducts.domain;

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
import java.math.BigDecimal;

@Entity
@Table(name = "credit_product")
public class CreditProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency;

    @Column(name = "min_amount", nullable = false)
    private MoneyAmount minAmount;

    @Column(name = "max_amount", nullable = false)
    private MoneyAmount maxAmount;

    @Column(name = "min_term_months", nullable = false)
    private Integer minTermMonths;

    @Column(name = "max_term_months", nullable = false)
    private Integer maxTermMonths;

    @Column(name = "annual_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal annualRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditProductStatus status;

    protected CreditProduct() {
    }

    public Long getId() {
        return id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public MoneyAmount getMinAmount() {
        return minAmount;
    }

    public MoneyAmount getMaxAmount() {
        return maxAmount;
    }

    public Integer getMinTermMonths() {
        return minTermMonths;
    }

    public Integer getMaxTermMonths() {
        return maxTermMonths;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public CreditProductStatus getStatus() {
        return status;
    }

    public void ensureAvailableFor(MoneyAmount requestedAmount, int termMonths) {
        if (status != CreditProductStatus.ACTIVE) {
            throw new DomainRuleViolationException("Only active credit products can be used in new applications");
        }
        if (requestedAmount.isLessThan(minAmount) || requestedAmount.isGreaterThan(maxAmount)) {
            throw new DomainRuleViolationException("Requested amount is outside the product limits");
        }
        if (termMonths < minTermMonths || termMonths > maxTermMonths) {
            throw new DomainRuleViolationException("Requested term is outside the product limits");
        }
    }
}
