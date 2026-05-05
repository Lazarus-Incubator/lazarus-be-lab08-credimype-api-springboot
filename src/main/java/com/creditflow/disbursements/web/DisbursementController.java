package com.creditflow.disbursements.web;

import com.creditflow.disbursements.application.DisbursementQueryService;
import com.creditflow.disbursements.application.ExecuteDisbursementCommandHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disbursement-orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Disbursements", description = "Disbursement order lifecycle endpoints")
public class DisbursementController {

    private final ExecuteDisbursementCommandHandler executeDisbursementCommandHandler;
    private final DisbursementQueryService disbursementQueryService;

    public DisbursementController(ExecuteDisbursementCommandHandler executeDisbursementCommandHandler,
                                  DisbursementQueryService disbursementQueryService) {
        this.executeDisbursementCommandHandler = executeDisbursementCommandHandler;
        this.disbursementQueryService = disbursementQueryService;
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute a disbursement order using an idempotency key")
    public DisbursementQueryService.DisbursementOrderView execute(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") @Schema(example = "cfm-execute-001") @NotBlank String idempotencyKey) {
        executeDisbursementCommandHandler.handle(
                new ExecuteDisbursementCommandHandler.ExecuteDisbursementCommand(id, idempotencyKey));
        return disbursementQueryService.getById(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single disbursement order by id")
    public DisbursementQueryService.DisbursementOrderView getById(@PathVariable Long id) {
        return disbursementQueryService.getById(id);
    }
}
