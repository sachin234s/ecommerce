package com.morrow.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:customer-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class CustomerApiTest {
    @Autowired MockMvc mvc;

    @Test
    void registersCustomerAndValidatesEmail() throws Exception {
        mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mira Shah","email":"mira@example.test","market":"IN","city":"Pune","preferredPayment":"UPI","marketingConsent":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.market").value("IN"));
        mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bad User","email":"invalid","market":"IN","city":"Pune","preferredPayment":"UPI","marketingConsent":false}
                                """))
                .andExpect(status().isBadRequest());
    }
}
