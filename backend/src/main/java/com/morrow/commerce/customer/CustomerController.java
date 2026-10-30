package com.morrow.commerce.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerRepository customers;

    public CustomerController(CustomerRepository customers) { this.customers = customers; }

    @GetMapping
    public List<Customer> list() { return customers.findAll(); }

    @PostMapping
    public Customer create(@Valid @RequestBody CreateCustomer request) {
        if (customers.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A customer with this email already exists");
        }
        return customers.save(new Customer(request.name(), request.email(), request.city(), request.preferredPayment(), 0, 0, false));
    }

    public record CreateCustomer(@NotBlank String name, @Email @NotBlank String email,
                                 @NotBlank String city, @NotBlank String preferredPayment) {}
}
