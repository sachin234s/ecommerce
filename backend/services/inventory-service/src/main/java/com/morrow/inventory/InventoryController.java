package com.morrow.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
                request.market(), request.currency(), request.unitPriceMinor(), request.stock()));
    }

    @PatchMapping("/{id}/stock")
    @Transactional
    public Product adjustStock(@PathVariable long id, @Valid @RequestBody StockAdjustment adjustment) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        try {
            product.adjustStock(adjustment.delta());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return product;
    }

    public record CreateProduct(@NotBlank String name, @NotBlank String category, @NotBlank String variant,
                                @NotBlank String warehouse, @NotBlank String market, @NotBlank String currency,
                                @Min(0) long unitPriceMinor, @Min(0) int stock) {}
    public record StockAdjustment(int delta) {}
}
