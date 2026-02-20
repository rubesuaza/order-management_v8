package com.example.management.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateOrderResponse(
    UUID orderId,
    String status,
    BigDecimal totalAmount,
    LocalDateTime createdAt
) {}
