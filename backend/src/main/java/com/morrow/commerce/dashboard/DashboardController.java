package com.morrow.commerce.dashboard;

import com.morrow.commerce.order.OrderRepository;
import com.morrow.commerce.payment.PaymentRepository;
import com.morrow.commerce.inventory.ProductRepository;
import com.morrow.commerce.customer.CustomerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final ProductRepository products;
    private final CustomerRepository customers;

    public DashboardController(OrderRepository orders, PaymentRepository payments,
                               ProductRepository products, CustomerRepository customers) {
        this.orders = orders;
        this.payments = payments;
        this.products = products;
        this.customers = customers;
    }

    @GetMapping("/summary")
    public Summary summary() {
        var allOrders = orders.findAll();
        var allPayments = payments.findAll();
        long salesMinor = allPayments.stream().filter(payment -> "Captured".equals(payment.getStatus()))
                .mapToLong(payment -> payment.getAmountMinor()).sum();
        long inventoryAlerts = products.findAll().stream().filter(product -> product.getAvailable() < 10).count();
        return new Summary(salesMinor, allOrders.size(), customers.count(), products.count(),
                allPayments.size(), inventoryAlerts);
    }

    public record Summary(long salesMinor, long orderCount, long customerCount,
                          long productCount, long paymentCount, long inventoryAlerts) {}
}
