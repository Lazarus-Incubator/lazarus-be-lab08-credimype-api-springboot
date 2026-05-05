package com.creditflow.audit.web;

import com.creditflow.audit.application.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit", description = "Audit trail for critical workflow decisions")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    @Operation(summary = "List audit entries visible to the authenticated user")
    public List<AuditQueryService.AuditEntryView> list() {
        return auditQueryService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single audit entry by id")
    public AuditQueryService.AuditEntryView getById(@PathVariable Long id) {
        return auditQueryService.getById(id);
    }
}
