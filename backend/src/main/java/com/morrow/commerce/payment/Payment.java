package com.morrow.commerce.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String orderReference;
    @Column(nullable = false)
    private String customer;
    @Column(nullable = false)
    private String market;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private String method;
    @Column(nullable = false)
    private String status;
    private long amountMinor;
    @Column(nullable = false)
    private Instant createdAt;

    protected Payment() {}

    public Payment(String orderReference, String customer, String market, String currency, String method, String status, long amountMinor) {
        this.orderReference = orderReference;
        this.customer = customer;
        this.market = market;
        this.currency = currency;
        this.method = method;
        this.status = status;
        this.amountMinor = amountMinor;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getOrderReference() { return orderReference; }
    public String getCustomer() { return customer; }
    public String getMarket() { return market; }
    public String getCurrency() { return currency; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public long getAmountMinor() { return amountMinor; }
    public Instant getCreatedAt() { return createdAt; }
}
