package com.creditflow.borrowers.web;

import com.creditflow.borrowers.application.BorrowerQueryService;
import com.creditflow.borrowers.application.CreateBorrowerCommandHandler;
import com.creditflow.borrowers.domain.DocumentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrowers")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Borrowers", description = "Borrower management within a tenant")
public class BorrowerController {

    private final BorrowerQueryService borrowerQueryService;
    private final CreateBorrowerCommandHandler createBorrowerCommandHandler;

    public BorrowerController(BorrowerQueryService borrowerQueryService,
                              CreateBorrowerCommandHandler createBorrowerCommandHandler) {
        this.borrowerQueryService = borrowerQueryService;
        this.createBorrowerCommandHandler = createBorrowerCommandHandler;
    }

    @GetMapping
    @Operation(summary = "List borrowers visible to the authenticated tenant")
    public List<BorrowerQueryService.BorrowerView> list() {
        return borrowerQueryService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new borrower inside the authenticated institution")
    public BorrowerQueryService.BorrowerView create(@Valid @RequestBody CreateBorrowerRequest request) {
        Long borrowerId = createBorrowerCommandHandler.handle(new CreateBorrowerCommandHandler.CreateBorrowerCommand(
                request.documentType(),
                request.documentNumber(),
                request.legalName(),
                request.tradeName(),
                request.economicActivity(),
                request.monthlyRevenue()));
        return borrowerQueryService.getById(borrowerId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single borrower by id")
    public BorrowerQueryService.BorrowerView getById(@PathVariable Long id) {
        return borrowerQueryService.getById(id);
    }
}

record CreateBorrowerRequest(
        @NotNull DocumentType documentType,
        @NotBlank String documentNumber,
        @NotBlank String legalName,
        String tradeName,
        @NotBlank String economicActivity,
        @NotNull @DecimalMin(value = "0.01") @Schema(example = "18500.00") BigDecimal monthlyRevenue) {
}
