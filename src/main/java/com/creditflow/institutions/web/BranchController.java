package com.creditflow.institutions.web;

import com.creditflow.institutions.application.BranchQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/branches")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Branches", description = "Branch catalog endpoints")
public class BranchController {

    private final BranchQueryService branchQueryService;

    public BranchController(BranchQueryService branchQueryService) {
        this.branchQueryService = branchQueryService;
    }

    @GetMapping
    @Operation(summary = "List branches visible to the authenticated user")
    public List<BranchQueryService.BranchView> list() {
        return branchQueryService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single branch by id")
    public BranchQueryService.BranchView getById(@PathVariable Long id) {
        return branchQueryService.getById(id);
    }
}
