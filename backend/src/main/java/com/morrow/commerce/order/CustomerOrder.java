package com.morrow.commerce.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String customer;
    @Column(nullable = false)
    private String market;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private String status;
    private long totalMinor;
    private int itemCount;
    @Column(nullable = false)
    private Instant createdAt;

    protected CustomerOrder() {}

    public CustomerOrder(String customer, String market, String currency, String paymentMethod, String status, long totalMinor, int itemCount) {
        this.customer = customer;
        this.market = market;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.totalMinor = totalMinor;
        this.itemCount = itemCount;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCustomer() { return customer; }
    public String getMarket() { return market; }
    public String getCurrency() { return currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public long getTotalMinor() { return totalMinor; }
    public int getItemCount() { return itemCount; }
    public Instant getCreatedAt() { return createdAt; }
}
