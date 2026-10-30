package com.morrow.commerce.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final ProductRepository products;

    public InventoryController(ProductRepository products) { this.products = products; }

    @GetMapping
    public List<Product> list() { return products.findAll(); }

    @PostMapping
    public Product create(@Valid @RequestBody CreateProduct request) {
        return products.save(new Product(request.name(), request.category(), request.variant(), request.warehouse(),
                request.market(), request.currency(), request.unitPriceMinor(), request.stock(), 0));
    }

    public record CreateProduct(@NotBlank String name, @NotBlank String category, @NotBlank String variant,
                                @NotBlank String warehouse, @NotBlank String market, @NotBlank String currency,
                                @Min(0) long unitPriceMinor, @Min(0) int stock) {}
}
