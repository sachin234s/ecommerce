package com.morrow.commerce.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    @Column(nullable = false)
    private String currency;
    private long unitPriceMinor;
    private int stock;
    private int reserved;

    protected Product() {}

    public Product(String name, String category, String variant, String warehouse, String market, String currency, long unitPriceMinor, int stock, int reserved) {
        this.name = name;
        this.category = category;
        this.variant = variant;
        this.warehouse = warehouse;
        this.market = market;
        this.currency = currency;
        this.unitPriceMinor = unitPriceMinor;
        this.stock = stock;
        this.reserved = reserved;
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
    public int getAvailable() { return Math.max(stock - reserved, 0); }
}
