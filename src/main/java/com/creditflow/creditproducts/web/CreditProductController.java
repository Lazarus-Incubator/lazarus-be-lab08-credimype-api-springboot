package com.creditflow.creditproducts.web;

import com.creditflow.creditproducts.application.CreditProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credit-products")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Credit Products", description = "Credit product catalog")
public class CreditProductController {

    private final CreditProductQueryService creditProductQueryService;

    public CreditProductController(CreditProductQueryService creditProductQueryService) {
        this.creditProductQueryService = creditProductQueryService;
    }

    @GetMapping
    @Operation(summary = "List credit products visible to the authenticated user")
    public List<CreditProductQueryService.CreditProductView> list() {
        return creditProductQueryService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single credit product by id")
    public CreditProductQueryService.CreditProductView getById(@PathVariable Long id) {
        return creditProductQueryService.getById(id);
    }
}
