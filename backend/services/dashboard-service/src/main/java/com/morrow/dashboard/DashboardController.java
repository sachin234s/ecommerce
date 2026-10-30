package com.morrow.dashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final RestClient restClient;
    private final String customerUrl;
    private final String inventoryUrl;
    private final String orderUrl;
    private final String paymentUrl;

    public DashboardController(RestClient.Builder builder,
                               @Value("${CUSTOMER_SERVICE_URL:http://localhost:8081}") String customerUrl,
                               @Value("${INVENTORY_SERVICE_URL:http://localhost:8082}") String inventoryUrl,
                               @Value("${ORDER_SERVICE_URL:http://localhost:8083}") String orderUrl,
                               @Value("${PAYMENT_SERVICE_URL:http://localhost:8084}") String paymentUrl) {
        this.restClient = builder.build();
        this.customerUrl = customerUrl;
        this.inventoryUrl = inventoryUrl;
        this.orderUrl = orderUrl;
        this.paymentUrl = paymentUrl;
    }

    @GetMapping("/summary")
    public Summary summary(@RequestHeader(value = "Authorization", required = false) String authorization) {
        List<CustomerView> customers = get(customerUrl, "customers",
                new ParameterizedTypeReference<List<CustomerView>>() {}, authorization);
        List<ProductView> products = get(inventoryUrl, "inventory",
                new ParameterizedTypeReference<List<ProductView>>() {}, authorization);
        List<OrderView> orders = get(orderUrl, "orders",
                new ParameterizedTypeReference<List<OrderView>>() {}, authorization);
        List<PaymentView> payments = get(paymentUrl, "payments",
                new ParameterizedTypeReference<List<PaymentView>>() {}, authorization);
        long capturedSalesMinor = payments.stream().filter(payment -> "CAPTURED".equals(payment.status()))
                .mapToLong(PaymentView::amountMinor).sum();
        long inventoryAlerts = products.stream().filter(product -> product.stock() < 10).count();
        return new Summary(capturedSalesMinor, orders.size(), customers.size(), products.size(),
                payments.size(), inventoryAlerts);
    }

    private <T> List<T> get(String baseUrl, String resource, ParameterizedTypeReference<List<T>> responseType, String authorization) {
        var request = restClient.get().uri(baseUrl + "/api/v1/" + resource);
        if (authorization != null && !authorization.isBlank()) request.header("Authorization", authorization);
        List<T> result = request.retrieve().body(responseType);
        if (result == null) throw new IllegalStateException("Domain service returned an empty response");
        return result;
    }

    public record Summary(long salesMinor, long orderCount, long customerCount,
                          long productCount, long paymentCount, long inventoryAlerts) {}
    public record CustomerView(long id) {}
    public record ProductView(long id, int stock) {}
    public record OrderView(long id) {}
    public record PaymentView(long id, String status, long amountMinor) {}
}
