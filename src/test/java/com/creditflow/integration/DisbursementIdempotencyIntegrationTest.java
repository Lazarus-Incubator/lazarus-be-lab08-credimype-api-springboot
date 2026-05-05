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
class DisbursementIdempotencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldApproveOrderExecutionIdempotently() throws Exception {
        String operationsToken = login("operaciones.andina@creditflow.pe", "password123");

        String createOrderResponse = mockMvc.perform(post("/api/credit-applications/6/disbursement-orders")
                        .header("Authorization", "Bearer " + operationsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "destinationBank": "BBVA",
                                  "destinationAccount": "0011-2200-3344"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = objectMapper.readTree(createOrderResponse).get("id").asLong();

        mockMvc.perform(post("/api/disbursement-orders/{id}/execute", orderId)
                        .header("Authorization", "Bearer " + operationsToken)
                        .header("Idempotency-Key", "integration-disb-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"))
                .andExpect(jsonPath("$.idempotencyKey").value("integration-disb-001"));

        mockMvc.perform(post("/api/disbursement-orders/{id}/execute", orderId)
                        .header("Authorization", "Bearer " + operationsToken)
                        .header("Idempotency-Key", "integration-disb-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"))
                .andExpect(jsonPath("$.idempotencyKey").value("integration-disb-001"));
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
