package com.example.management.infrastructure.adapters.in.web.dto;

import java.util.UUID;

public record PayOrderResponse(UUID orderId, String status) {}
