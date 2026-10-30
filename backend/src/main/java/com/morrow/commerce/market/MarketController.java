package com.morrow.commerce.market;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/markets")
public class MarketController {
    @GetMapping
    public List<Market> list() {
        return List.of(
                new Market("IN", "India", "INR", "en-IN", "Asia/Kolkata", List.of("UPI", "CARD", "NETBANKING", "COD"), "GST"),
                new Market("US", "Global", "USD", "en-US", "America/New_York", List.of("CARD", "WALLET"), "Sales tax")
        );
    }

    public record Market(String id, String name, String currency, String locale, String timeZone,
                         List<String> paymentMethods, String taxSystem) {}
}
