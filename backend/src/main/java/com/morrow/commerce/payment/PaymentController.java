package com.morrow.commerce.payment;

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
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentRepository payments;

    public PaymentController(PaymentRepository payments) { this.payments = payments; }

    @GetMapping
    public List<Payment> list() { return payments.findAll(); }

    @PostMapping
    public Payment createSandboxPayment(@Valid @RequestBody CreatePayment request) {
        return payments.save(new Payment(request.orderReference(), request.customer(), request.market(),
                request.currency(), request.method(), "Sandbox pending", request.amountMinor()));
    }

    public record CreatePayment(@NotBlank String orderReference, @NotBlank String customer,
                                @NotBlank String market, @NotBlank String currency,
                                @NotBlank String method, @Min(1) long amountMinor) {}
}
