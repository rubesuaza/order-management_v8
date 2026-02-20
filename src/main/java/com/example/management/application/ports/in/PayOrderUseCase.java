package com.example.management.application.ports.in;

import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;

import java.util.Optional;

/**
 * Input port: process payment for an order (transition to PAID).
 */
public interface PayOrderUseCase {

    /**
     * Marks the order as paid. Returns the updated order if found and transition is valid.
     */
    Optional<Order> pay(OrderId orderId);
}
