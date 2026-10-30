package com.morrow.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:order-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OrderApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createsOrderAndEnforcesStatusTransitions() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerReference":"CUS-1","market":"IN","currency":"INR","paymentMethod":"UPI","totalMinor":12500,"itemCount":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
        mvc.perform(patch("/api/v1/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(patch("/api/v1/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DELIVERED"}
                                """))
                .andExpect(status().isConflict());
    }
}
