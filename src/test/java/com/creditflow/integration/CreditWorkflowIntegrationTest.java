package com.creditflow.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreditWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateSubmitReviewAndAssessRisk() throws Exception {
        String branchOfficerToken = login("oficial.lima@creditflow.pe", "password123");
        String analystToken = login("analista.andina@creditflow.pe", "password123");
        String riskOfficerToken = login("riesgo.andina@creditflow.pe", "password123");

        String createResponse = mockMvc.perform(post("/api/credit-applications")
                        .header("Authorization", "Bearer " + branchOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId": 1,
                                  "borrowerId": 1,
                                  "productId": 1,
                                  "requestedAmount": 21000.00,
                                  "termMonths": 12,
                                  "purpose": "Capital de trabajo para temporada alta"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long applicationId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(post("/api/credit-applications/{id}/submit", applicationId)
                        .header("Authorization", "Bearer " + branchOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mockMvc.perform(post("/api/credit-applications/{id}/start-review", applicationId)
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        mockMvc.perform(post("/api/credit-applications/{id}/risk-assessment", applicationId)
                        .header("Authorization", "Bearer " + riskOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "score": 81,
                                  "debtToIncomeRatio": 0.29,
                                  "flags": ["stable_sales"],
                                  "recommendation": "APPROVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RISK_REVIEWED"))
                .andExpect(jsonPath("$.committeeRequired").value(false));
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }
}
