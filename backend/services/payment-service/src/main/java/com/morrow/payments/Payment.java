package com.morrow.payments;

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
    private String market;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false)
    private String method;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false, unique = true)
    private String idempotencyKey;
    private long amountMinor;
    @Column(nullable = false)
    private Instant createdAt;

    protected Payment() {}

    public Payment(String orderReference, String market, String currency,
                   String method, String idempotencyKey, long amountMinor) {
        this.orderReference = orderReference;
        this.market = market.toUpperCase();
        this.currency = currency.toUpperCase();
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.amountMinor = amountMinor;
        this.status = "SANDBOX_PENDING";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getOrderReference() { return orderReference; }
    public String getMarket() { return market; }
    public String getCurrency() { return currency; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public long getAmountMinor() { return amountMinor; }
    public Instant getCreatedAt() { return createdAt; }
}
