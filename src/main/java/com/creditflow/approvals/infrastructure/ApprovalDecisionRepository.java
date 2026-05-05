package com.creditflow.approvals.infrastructure;

import com.creditflow.approvals.domain.ApprovalDecision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDecisionRepository extends JpaRepository<ApprovalDecision, Long> {
}
