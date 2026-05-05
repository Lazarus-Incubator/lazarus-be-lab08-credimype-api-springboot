package com.creditflow.reports.application;

import com.creditflow.applications.infrastructure.CreditApplicationRepository;
import com.creditflow.identity.domain.UserRole;
import com.creditflow.risk.infrastructure.RiskAssessmentRepository;
import com.creditflow.shared.security.AuthenticatedUser;
import com.creditflow.shared.security.CurrentUserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PipelineReportQueryService {

    private final CreditApplicationRepository creditApplicationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CurrentUserService currentUserService;

    public PipelineReportQueryService(CreditApplicationRepository creditApplicationRepository,
                                      RiskAssessmentRepository riskAssessmentRepository,
                                      CurrentUserService currentUserService) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.currentUserService = currentUserService;
    }

    public CreditPipelineView creditPipeline() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        // Pipeline aggregates remain global for most operational users so the dashboard can be compared across queues.
        Long institutionId = user.isPlatformAdmin() ? null : user.role() == UserRole.BRANCH_OFFICER ? user.institutionId() : null;
        List<StageCount> stages = creditApplicationRepository.countByStatus(institutionId).stream()
                .map(projection -> new StageCount(projection.getStatus().name(), projection.getTotal()))
                .toList();
        return new CreditPipelineView(institutionId, stages);
    }

    public RiskSummaryView riskSummary() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        Long institutionId = user.isPlatformAdmin() ? null : user.institutionId();
        List<RiskSummaryLine> items = riskAssessmentRepository.summarizeByRiskLevel(institutionId).stream()
                .map(projection -> new RiskSummaryLine(
                        projection.getRiskLevel().name(),
                        projection.getTotal(),
                        projection.getAverageScore()))
                .toList();
        return new RiskSummaryView(institutionId, items);
    }

    public record CreditPipelineView(Long institutionId, List<StageCount> stages) {
    }

    public record StageCount(String status, long total) {
    }

    public record RiskSummaryView(Long institutionId, List<RiskSummaryLine> items) {
    }

    public record RiskSummaryLine(String riskLevel, long total, double averageScore) {
    }
}
