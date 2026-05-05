package com.creditflow.reports.web;

import com.creditflow.reports.application.PipelineReportQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports", description = "Operational reports for pipeline and risk monitoring")
public class ReportsController {

    private final PipelineReportQueryService pipelineReportQueryService;

    public ReportsController(PipelineReportQueryService pipelineReportQueryService) {
        this.pipelineReportQueryService = pipelineReportQueryService;
    }

    @GetMapping("/credit-pipeline")
    @Operation(summary = "Return pipeline counts grouped by credit application status")
    public PipelineReportQueryService.CreditPipelineView creditPipeline() {
        return pipelineReportQueryService.creditPipeline();
    }

    @GetMapping("/risk-summary")
    @Operation(summary = "Return aggregated risk counts and average score by risk level")
    public PipelineReportQueryService.RiskSummaryView riskSummary() {
        return pipelineReportQueryService.riskSummary();
    }
}
