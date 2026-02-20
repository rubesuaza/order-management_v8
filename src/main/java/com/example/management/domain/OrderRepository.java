package com.example.management.domain;

import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;

import java.util.Optional;

/**
 * Technology-agnostic contract for persisting and loading orders (domain layer).
 * Infrastructure adapters implement this contract.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId orderId);
}
