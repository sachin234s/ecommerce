package com.morrow.payments;

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

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:payment-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class PaymentApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createsIdempotentSandboxIntentWithoutCapturingPayment() throws Exception {
        var request = post("/api/v1/payments/intents").header("Idempotency-Key", "checkout-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"orderReference":"ORD-1","market":"IN","currency":"INR","method":"UPI sandbox","amountMinor":12000}
                        """);
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SANDBOX_PENDING"));
        mvc.perform(post("/api/v1/payments/intents").header("Idempotency-Key", "checkout-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderReference":"ORD-1","market":"IN","currency":"INR","method":"UPI sandbox","amountMinor":12000}
                                """))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/payments")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(post("/api/v1/payments/intents").header("Idempotency-Key", "checkout-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderReference":"ORD-1","market":"IN","currency":"INR","method":"UPI sandbox","amountMinor":13000}
                                """))
                .andExpect(status().isConflict());
    }
}
