package it.aruba.monitoring.controller;

import org.springframework.boot.test.context.SpringBootTest;


import it.aruba.monitoring.repository.ServiceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    @BeforeEach
    void setup() {
        serviceRecordRepository.deleteAll();
        // opzionale: puoi inserire qualche record seed se vuoi
    }

    @Test
    void shouldReturnSummaryWithAuth() throws Exception {

        mockMvc.perform(get("/api/report/summary")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.activeServicesByType").exists())
                .andExpect(jsonPath("$.averageSpendPerCustomer").exists())
                .andExpect(jsonPath("$.customersWithMultipleExpiredServices").exists())
                .andExpect(jsonPath("$.customersWithServicesExpiringSoon").exists());
    }

    @Test
    void summaryShouldReturn401WithoutAuth() throws Exception {

        mockMvc.perform(get("/api/report/summary"))
                .andExpect(status().isUnauthorized());
    }



}