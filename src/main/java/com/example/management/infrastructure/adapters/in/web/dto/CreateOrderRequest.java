package com.example.management.infrastructure.adapters.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull UUID customerId,
    @NotEmpty(message = "items must not be empty") @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
        @NotNull UUID productId,
        @Min(value = 1, message = "quantity must be greater than zero") int quantity,
        @NotNull java.math.BigDecimal unitPrice
    ) {}
}
