package com.creditflow.institutions.web;

import com.creditflow.institutions.application.InstitutionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/institutions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Institutions", description = "Institution catalog endpoints")
public class InstitutionController {

    private final InstitutionQueryService institutionQueryService;

    public InstitutionController(InstitutionQueryService institutionQueryService) {
        this.institutionQueryService = institutionQueryService;
    }

    @GetMapping
    @Operation(summary = "List institutions visible to the authenticated user")
    public List<InstitutionQueryService.InstitutionView> list() {
        return institutionQueryService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single institution by id")
    public InstitutionQueryService.InstitutionView getById(@PathVariable Long id) {
        return institutionQueryService.getById(id);
    }
}
