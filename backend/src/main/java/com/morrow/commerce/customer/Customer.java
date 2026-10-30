package com.morrow.commerce.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

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
    private String city;
    @Column(nullable = false)
    private String preferredPayment;
    private long lifetimeValueMinor;
    private int orderCount;
    private boolean vip;

    protected Customer() {}

    public Customer(String name, String email, String city, String preferredPayment, long lifetimeValueMinor, int orderCount, boolean vip) {
        this.name = name;
        this.email = email;
        this.city = city;
        this.preferredPayment = preferredPayment;
        this.lifetimeValueMinor = lifetimeValueMinor;
        this.orderCount = orderCount;
        this.vip = vip;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public String getPreferredPayment() { return preferredPayment; }
    public long getLifetimeValueMinor() { return lifetimeValueMinor; }
    public int getOrderCount() { return orderCount; }
    public boolean isVip() { return vip; }
}
