package com.example.management.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    UUID customerId,
    String status,
    List<OrderItemResponse> items,
    BigDecimal totalAmount,
    String currency,
    LocalDateTime createdAt
) {
    public record OrderItemResponse(UUID productId, int quantity, BigDecimal unitPrice) {}
}
