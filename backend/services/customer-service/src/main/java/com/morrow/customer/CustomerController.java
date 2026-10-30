package com.morrow.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{id}")
    public Customer get(@PathVariable long id) {
        return customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @PostMapping
    public Customer create(@Valid @RequestBody CreateCustomer request) {
        if (customers.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer email is already registered");
        }
        return customers.save(new Customer(request.name(), request.email(), request.market(), request.city(),
                request.preferredPayment(), request.marketingConsent()));
    }

    @PatchMapping("/{id}")
    public Customer update(@PathVariable long id, @Valid @RequestBody UpdateCustomer request) {
        Customer customer = get(id);
        if (!customer.getEmail().equalsIgnoreCase(request.email())
                && customers.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer email is already registered");
        }
        customer.update(request.name(), request.email(), request.market(), request.city(),
                request.preferredPayment(), request.marketingConsent());
        return customers.save(customer);
    }

    public record CreateCustomer(@NotBlank String name, @Email @NotBlank String email,
                                 @NotBlank String market, @NotBlank String city,
                                 @NotBlank String preferredPayment, boolean marketingConsent) {}
    public record UpdateCustomer(@NotBlank String name, @Email @NotBlank String email,
                                 @NotBlank String market, @NotBlank String city,
                                 @NotBlank String preferredPayment, boolean marketingConsent) {}
}
