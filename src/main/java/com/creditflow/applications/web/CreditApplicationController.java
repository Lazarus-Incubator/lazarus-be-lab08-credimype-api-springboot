package com.creditflow.applications.web;

import com.creditflow.applications.application.CancelCreditApplicationCommandHandler;
import com.creditflow.applications.application.CreateCreditApplicationCommandHandler;
import com.creditflow.applications.application.CreditApplicationQueryService;
import com.creditflow.applications.application.SendToCommitteeCommandHandler;
import com.creditflow.applications.application.StartCreditReviewCommandHandler;
import com.creditflow.applications.application.SubmitCreditApplicationCommandHandler;
import com.creditflow.applications.domain.CreditApplicationStatus;
import com.creditflow.approvals.application.ApproveCreditApplicationCommandHandler;
import com.creditflow.approvals.application.RejectCreditApplicationCommandHandler;
import com.creditflow.disbursements.application.CreateDisbursementOrderCommandHandler;
import com.creditflow.disbursements.application.DisbursementQueryService;
import com.creditflow.risk.application.RecordRiskAssessmentCommandHandler;
import com.creditflow.risk.domain.RiskRecommendation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credit-applications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Credit Applications", description = "Core credit workflow endpoints")
public class CreditApplicationController {

    private final CreditApplicationQueryService creditApplicationQueryService;
    private final CreateCreditApplicationCommandHandler createCreditApplicationCommandHandler;
    private final SubmitCreditApplicationCommandHandler submitCreditApplicationCommandHandler;
    private final StartCreditReviewCommandHandler startCreditReviewCommandHandler;
    private final RecordRiskAssessmentCommandHandler recordRiskAssessmentCommandHandler;
    private final SendToCommitteeCommandHandler sendToCommitteeCommandHandler;
    private final ApproveCreditApplicationCommandHandler approveCreditApplicationCommandHandler;
    private final RejectCreditApplicationCommandHandler rejectCreditApplicationCommandHandler;
    private final CancelCreditApplicationCommandHandler cancelCreditApplicationCommandHandler;
    private final CreateDisbursementOrderCommandHandler createDisbursementOrderCommandHandler;
    private final DisbursementQueryService disbursementQueryService;

    public CreditApplicationController(CreditApplicationQueryService creditApplicationQueryService,
                                       CreateCreditApplicationCommandHandler createCreditApplicationCommandHandler,
                                       SubmitCreditApplicationCommandHandler submitCreditApplicationCommandHandler,
                                       StartCreditReviewCommandHandler startCreditReviewCommandHandler,
                                       RecordRiskAssessmentCommandHandler recordRiskAssessmentCommandHandler,
                                       SendToCommitteeCommandHandler sendToCommitteeCommandHandler,
                                       ApproveCreditApplicationCommandHandler approveCreditApplicationCommandHandler,
                                       RejectCreditApplicationCommandHandler rejectCreditApplicationCommandHandler,
                                       CancelCreditApplicationCommandHandler cancelCreditApplicationCommandHandler,
                                       CreateDisbursementOrderCommandHandler createDisbursementOrderCommandHandler,
                                       DisbursementQueryService disbursementQueryService) {
        this.creditApplicationQueryService = creditApplicationQueryService;
        this.createCreditApplicationCommandHandler = createCreditApplicationCommandHandler;
        this.submitCreditApplicationCommandHandler = submitCreditApplicationCommandHandler;
        this.startCreditReviewCommandHandler = startCreditReviewCommandHandler;
        this.recordRiskAssessmentCommandHandler = recordRiskAssessmentCommandHandler;
        this.sendToCommitteeCommandHandler = sendToCommitteeCommandHandler;
        this.approveCreditApplicationCommandHandler = approveCreditApplicationCommandHandler;
        this.rejectCreditApplicationCommandHandler = rejectCreditApplicationCommandHandler;
        this.cancelCreditApplicationCommandHandler = cancelCreditApplicationCommandHandler;
        this.createDisbursementOrderCommandHandler = createDisbursementOrderCommandHandler;
        this.disbursementQueryService = disbursementQueryService;
    }

    @GetMapping
    @Operation(summary = "Search credit applications by operational filters")
    public List<CreditApplicationQueryService.CreditApplicationView> list(
            @RequestParam(required = false) CreditApplicationStatus status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String borrowerDocument,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return creditApplicationQueryService.search(status, branchId, borrowerDocument, from, to);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single credit application by id")
    public CreditApplicationQueryService.CreditApplicationView getById(@PathVariable Long id) {
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new draft credit application")
    public CreditApplicationQueryService.CreditApplicationView create(@Valid @RequestBody CreateCreditApplicationRequest request) {
        Long id = createCreditApplicationCommandHandler.handle(new CreateCreditApplicationCommandHandler.CreateCreditApplicationCommand(
                request.branchId(),
                request.borrowerId(),
                request.productId(),
                request.requestedAmount(),
                request.termMonths(),
                request.purpose()));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a draft credit application into the formal workflow")
    public CreditApplicationQueryService.CreditApplicationView submit(@PathVariable Long id) {
        submitCreditApplicationCommandHandler.handle(new SubmitCreditApplicationCommandHandler.SubmitCreditApplicationCommand(id));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/start-review")
    @Operation(summary = "Assign the current analyst and move the application under review")
    public CreditApplicationQueryService.CreditApplicationView startReview(@PathVariable Long id) {
        startCreditReviewCommandHandler.handle(new StartCreditReviewCommandHandler.StartCreditReviewCommand(id));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/risk-assessment")
    @Operation(summary = "Record a risk assessment and determine whether committee review is required")
    public CreditApplicationQueryService.CreditApplicationView recordRiskAssessment(@PathVariable Long id,
                                                                                    @Valid @RequestBody RecordRiskAssessmentRequest request) {
        recordRiskAssessmentCommandHandler.handle(new RecordRiskAssessmentCommandHandler.RecordRiskAssessmentCommand(
                id,
                request.score(),
                request.debtToIncomeRatio(),
                request.flags(),
                request.recommendation()));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/send-to-committee")
    @Operation(summary = "Move a risk-reviewed application into committee pending status")
    public CreditApplicationQueryService.CreditApplicationView sendToCommittee(@PathVariable Long id) {
        sendToCommitteeCommandHandler.handle(new SendToCommitteeCommandHandler.SendToCommitteeCommand(id));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a credit application either directly or through committee decision")
    public CreditApplicationQueryService.CreditApplicationView approve(@PathVariable Long id,
                                                                       @Valid @RequestBody ApproveCreditApplicationRequest request) {
        approveCreditApplicationCommandHandler.handle(new ApproveCreditApplicationCommandHandler.ApproveCreditApplicationCommand(
                id, request.approvedAmount()));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a credit application with a mandatory reason")
    public CreditApplicationQueryService.CreditApplicationView reject(@PathVariable Long id,
                                                                      @Valid @RequestBody RejectCreditApplicationRequest request) {
        rejectCreditApplicationCommandHandler.handle(new RejectCreditApplicationCommandHandler.RejectCreditApplicationCommand(
                id, request.reason()));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a non-final credit application")
    public CreditApplicationQueryService.CreditApplicationView cancel(@PathVariable Long id,
                                                                      @RequestBody(required = false) CancelCreditApplicationRequest request) {
        cancelCreditApplicationCommandHandler.handle(new CancelCreditApplicationCommandHandler.CancelCreditApplicationCommand(
                id,
                request == null ? null : request.reason()));
        return creditApplicationQueryService.getById(id);
    }

    @PostMapping("/{id}/disbursement-orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a disbursement order for an approved credit application")
    public DisbursementQueryService.DisbursementOrderView createDisbursementOrder(@PathVariable Long id,
                                                                                  @Valid @RequestBody CreateDisbursementOrderRequest request) {
        Long orderId = createDisbursementOrderCommandHandler.handle(new CreateDisbursementOrderCommandHandler.CreateDisbursementOrderCommand(
                id,
                request.destinationBank(),
                request.destinationAccount()));
        return disbursementQueryService.getById(orderId);
    }
}

record CreateCreditApplicationRequest(
        @NotNull Long branchId,
        @NotNull Long borrowerId,
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal requestedAmount,
        @NotNull @Positive Integer termMonths,
        @NotBlank @Size(max = 500) String purpose) {
}

record RecordRiskAssessmentRequest(
        @NotNull @Schema(example = "72") Integer score,
        @NotNull @DecimalMin("0.00") BigDecimal debtToIncomeRatio,
        List<@NotBlank String> flags,
        @NotNull RiskRecommendation recommendation) {
}

record ApproveCreditApplicationRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal approvedAmount) {
}

record RejectCreditApplicationRequest(@NotEmpty @Size(max = 500) String reason) {
}

record CancelCreditApplicationRequest(String reason) {
}

record CreateDisbursementOrderRequest(
        @NotBlank String destinationBank,
        @NotBlank String destinationAccount) {
}
