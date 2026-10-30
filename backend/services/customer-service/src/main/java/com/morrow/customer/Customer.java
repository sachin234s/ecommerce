package com.morrow.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String market;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String preferredPayment;
    @Column(nullable = false)
    private boolean marketingConsent;
    @Column(nullable = false)
    private Instant createdAt;

    protected Customer() {}

    public Customer(String name, String email, String market, String city, String preferredPayment, boolean marketingConsent) {
        this.name = name;
        this.email = email.toLowerCase();
        this.market = market.toUpperCase();
        this.city = city;
        this.preferredPayment = preferredPayment;
        this.marketingConsent = marketingConsent;
        this.createdAt = Instant.now();
    }

    public void update(String name, String email, String market, String city, String preferredPayment, boolean marketingConsent) {
        this.name = name;
        this.email = email.toLowerCase();
        this.market = market.toUpperCase();
        this.city = city;
        this.preferredPayment = preferredPayment;
        this.marketingConsent = marketingConsent;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMarket() { return market; }
    public String getCity() { return city; }
    public String getPreferredPayment() { return preferredPayment; }
    public boolean isMarketingConsent() { return marketingConsent; }
    public Instant getCreatedAt() { return createdAt; }
}
