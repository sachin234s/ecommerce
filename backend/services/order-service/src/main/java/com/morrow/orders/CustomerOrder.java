package com.morrow.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING_PAYMENT", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String customerReference;
    @Column(nullable = false)
    private String market;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private String status;
    private long totalMinor;
    private int itemCount;
    @Column(nullable = false)
    private Instant createdAt;
    @Version
    private long version;

    protected CustomerOrder() {}

    public CustomerOrder(String customerReference, String market, String currency,
                         String paymentMethod, long totalMinor, int itemCount) {
        this.customerReference = customerReference;
        this.market = market.toUpperCase();
        this.currency = currency.toUpperCase();
        this.paymentMethod = paymentMethod;
        this.totalMinor = totalMinor;
        this.itemCount = itemCount;
        this.status = "PENDING_PAYMENT";
        this.createdAt = Instant.now();
    }

    public void transition(String nextStatus) {
        if (!ALLOWED_STATUSES.contains(nextStatus)) throw new IllegalArgumentException("Unsupported order status");
        boolean allowed = switch (status) {
            case "PENDING_PAYMENT" -> Set.of("CONFIRMED", "CANCELLED").contains(nextStatus);
            case "CONFIRMED" -> Set.of("PROCESSING", "CANCELLED", "REFUNDED").contains(nextStatus);
            case "PROCESSING" -> Set.of("SHIPPED", "CANCELLED", "REFUNDED").contains(nextStatus);
            case "SHIPPED" -> Set.of("DELIVERED", "REFUNDED").contains(nextStatus);
            default -> false;
        };
        if (!allowed) throw new IllegalStateException("Cannot transition order from " + status + " to " + nextStatus);
        status = nextStatus;
    }

    public Long getId() { return id; }
    public String getCustomerReference() { return customerReference; }
    public String getMarket() { return market; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public long getTotalMinor() { return totalMinor; }
    public int getItemCount() { return itemCount; }
    public Instant getCreatedAt() { return createdAt; }
    public long getVersion() { return version; }
}
