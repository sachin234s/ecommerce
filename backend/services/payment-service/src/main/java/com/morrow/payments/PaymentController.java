package com.morrow.payments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentRepository payments;

    public PaymentController(PaymentRepository payments) { this.payments = payments; }

    @GetMapping
    public List<Payment> list() { return payments.findAll(); }

    @PostMapping("/intents")
    public Payment createIntent(@RequestHeader(value = "Idempotency-Key", required = false) String key,
                                @Valid @RequestBody CreatePayment request) {
        String idempotencyKey = key == null || key.isBlank() ? UUID.randomUUID().toString() : key;
        if (idempotencyKey.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key must be at most 100 characters");
        }
        var existing = payments.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return requireSameIntent(existing.get(), request);
        try {
            return payments.saveAndFlush(new Payment(request.orderReference(), request.market(),
                    request.currency(), request.method(), idempotencyKey, request.amountMinor()));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return payments.findByIdempotencyKey(idempotencyKey)
                    .map(payment -> requireSameIntent(payment, request))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Payment idempotency conflict"));
        }
    }

    private Payment requireSameIntent(Payment payment, CreatePayment request) {
        boolean matches = payment.getOrderReference().equals(request.orderReference())
                && payment.getMarket().equalsIgnoreCase(request.market())
                && payment.getCurrency().equalsIgnoreCase(request.currency())
                && payment.getMethod().equals(request.method())
                && payment.getAmountMinor() == request.amountMinor();
        if (!matches) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency-Key was already used for a different payment intent");
        }
        return payment;
    }

    public record CreatePayment(@NotBlank String orderReference, @NotBlank String market,
                                @NotBlank String currency, @NotBlank String method,
                                @Min(1) long amountMinor) {}
}
