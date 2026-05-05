package com.creditflow.borrowers.domain;

import com.creditflow.shared.application.exception.DomainRuleViolationException;
import com.creditflow.shared.domain.DocumentNumber;
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
@Table(name = "borrower")
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false)
    private DocumentNumber documentNumber;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "economic_activity", nullable = false)
    private String economicActivity;

    @Column(name = "monthly_revenue", nullable = false)
    private MoneyAmount monthlyRevenue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowerStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Borrower() {
    }

    public static Borrower register(Long institutionId,
                                    DocumentType documentType,
                                    DocumentNumber documentNumber,
                                    String legalName,
                                    String tradeName,
                                    String economicActivity,
                                    MoneyAmount monthlyRevenue,
                                    Instant createdAt) {
        Borrower borrower = new Borrower();
        borrower.institutionId = institutionId;
        borrower.documentType = documentType;
        borrower.documentNumber = documentNumber;
        borrower.legalName = legalName;
        borrower.tradeName = tradeName;
        borrower.economicActivity = economicActivity;
        borrower.monthlyRevenue = monthlyRevenue;
        borrower.status = BorrowerStatus.ACTIVE;
        borrower.createdAt = createdAt;
        borrower.validateDocumentFormat();
        return borrower;
    }

    public Long getId() {
        return id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public DocumentNumber getDocumentNumber() {
        return documentNumber;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getEconomicActivity() {
        return economicActivity;
    }

    public MoneyAmount getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public BorrowerStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void ensureCanCreateNewApplications() {
        if (status == BorrowerStatus.BLOCKED) {
            throw new DomainRuleViolationException("Blocked borrowers cannot create new credit applications");
        }
    }

    private void validateDocumentFormat() {
        String value = documentNumber.value();
        boolean valid = switch (documentType) {
            case RUC -> value.matches("\\d{11}");
            case DNI -> value.matches("\\d{8}");
            case CE -> value.matches("[A-Z0-9]{9,12}");
        };
        if (!valid) {
            throw new DomainRuleViolationException("Document number does not match the selected document type");
        }
    }
}
