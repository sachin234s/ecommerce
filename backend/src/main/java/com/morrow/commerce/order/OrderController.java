package com.morrow.commerce.order;

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
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderRepository orders;

    public OrderController(OrderRepository orders) { this.orders = orders; }

    @GetMapping
    public List<CustomerOrder> list() { return orders.findAll(); }

    @PostMapping
    public CustomerOrder create(@Valid @RequestBody CreateOrder request) {
        return orders.save(new CustomerOrder(request.customer(), request.market(), request.currency(),
                request.paymentMethod(), "Pending payment", request.totalMinor(), request.itemCount()));
    }

    public record CreateOrder(@NotBlank String customer, @NotBlank String market, @NotBlank String currency,
                              @NotBlank String paymentMethod, @Min(1) long totalMinor, @Min(1) int itemCount) {}
}
