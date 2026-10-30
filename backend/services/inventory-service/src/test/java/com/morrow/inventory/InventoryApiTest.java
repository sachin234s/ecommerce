package com.morrow.inventory;

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

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:inventory-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class InventoryApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createsProductAndPreventsNegativeStock() throws Exception {
        mvc.perform(post("/api/v1/inventory").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Linen Shirt","category":"Apparel","variant":"Sand/M","warehouse":"Mumbai","market":"IN","currency":"INR","unitPriceMinor":249900,"stock":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
        mvc.perform(patch("/api/v1/inventory/1/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"delta":-4}
                                """))
                .andExpect(status().isConflict());
    }
}
