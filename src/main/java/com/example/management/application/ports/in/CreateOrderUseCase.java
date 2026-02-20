package com.example.management.application.ports.in;

import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;

import java.util.List;
import java.util.UUID;

/**
 * Input port: create a new order with the given customer and items.
 */
public interface CreateOrderUseCase {

    /**
     * Creates an order. Returns the created order (with id and computed total).
     */
    Order create(UUID customerId, List<OrderItemCommand> items);

    record OrderItemCommand(UUID productId, int quantity, java.math.BigDecimal unitPrice) {}
}
