package com.morrow.orders;

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
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderRepository orders;

    public OrderController(OrderRepository orders) { this.orders = orders; }

    @GetMapping
    public List<CustomerOrder> list() { return orders.findAll(); }

    @PostMapping
    public CustomerOrder create(@Valid @RequestBody CreateOrder request) {
        return orders.save(new CustomerOrder(request.customerReference(), request.market(), request.currency(),
                request.paymentMethod(), request.totalMinor(), request.itemCount()));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public CustomerOrder updateStatus(@PathVariable long id, @Valid @RequestBody StatusUpdate request) {
        CustomerOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        try {
            order.transition(request.status());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return order;
    }

    public record CreateOrder(@NotBlank String customerReference, @NotBlank String market,
                              @NotBlank String currency, @NotBlank String paymentMethod,
                              @Min(1) long totalMinor, @Min(1) int itemCount) {}
    public record StatusUpdate(@NotBlank String status) {}
}
