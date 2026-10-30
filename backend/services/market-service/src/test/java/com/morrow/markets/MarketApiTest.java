package com.morrow.markets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MarketApiTest {
    @Autowired MockMvc mvc;

    @Test
    void exposesMarketSpecificCurrencyTaxAndPaymentOptions() throws Exception {
        mvc.perform(get("/api/v1/markets/IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.paymentMethods[0]").value("UPI"));
        mvc.perform(get("/api/v1/markets/US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
