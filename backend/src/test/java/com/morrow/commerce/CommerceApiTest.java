package com.morrow.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:commerce-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class CommerceApiTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void exposesSeededDashboardAndMarketConfiguration() throws Exception {
        mvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").isNumber())
                .andExpect(jsonPath("$.customerCount").isNumber());

        mvc.perform(get("/api/v1/markets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("IN"))
                .andExpect(jsonPath("$[0].currency").value("INR"));
    }

    @Test
    void validatesAndPersistsCustomerCreation() throws Exception {
        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Customer","email":"test-customer@example.test","city":"Pune","preferredPayment":"UPI"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Customer"));

        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","city":"","preferredPayment":"UPI"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
