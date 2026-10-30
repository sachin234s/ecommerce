package com.morrow.markets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/markets")
public class MarketController {
    private static final Map<String, Market> MARKETS = Map.of(
            "IN", new Market("IN", "India", "INR", "en-IN", "Asia/Kolkata",
                    List.of("UPI", "CARD", "NETBANKING", "COD"), "GST"),
            "US", new Market("US", "United States", "USD", "en-US", "America/New_York",
                    List.of("CARD", "WALLET"), "Sales tax")
    );

    @GetMapping
    public List<Market> list() { return List.copyOf(MARKETS.values()); }

    @GetMapping("/{marketId}")
    public Market get(@PathVariable String marketId) {
        Market market = MARKETS.get(marketId.toUpperCase());
        if (market == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Market configuration not found");
        return market;
    }

    public record Market(String id, String name, String currency, String locale,
                         String timeZone, List<String> paymentMethods, String taxSystem) {}
}
