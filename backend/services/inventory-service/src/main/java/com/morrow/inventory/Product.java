package com.morrow.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private String variant;
    @Column(nullable = false)
    private String warehouse;
    @Column(nullable = false)
    private String market;
    @Column(nullable = false, length = 3)
    private String currency;
    private long unitPriceMinor;
    private int stock;
    @Version
    private long version;
    @Column(nullable = false)
    private Instant createdAt;

    protected Product() {}

    public Product(String name, String category, String variant, String warehouse,
                   String market, String currency, long unitPriceMinor, int stock) {
        this.name = name;
        this.category = category;
        this.variant = variant;
        this.warehouse = warehouse;
        this.market = market.toUpperCase();
        this.currency = currency.toUpperCase();
        this.unitPriceMinor = unitPriceMinor;
        this.stock = stock;
        this.createdAt = Instant.now();
    }

    public void adjustStock(int delta) {
        if (stock + delta < 0) throw new IllegalArgumentException("Stock adjustment cannot make inventory negative");
        stock += delta;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getVariant() { return variant; }
    public String getWarehouse() { return warehouse; }
    public String getMarket() { return market; }
    public String getCurrency() { return currency; }
    public long getUnitPriceMinor() { return unitPriceMinor; }
    public int getStock() { return stock; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
}
