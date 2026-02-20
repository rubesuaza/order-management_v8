package com.example.management.application.ports.in;

import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;

import java.util.Optional;

/**
 * Input port: retrieve an order by id.
 */
public interface GetOrderUseCase {

    Optional<Order> getById(OrderId orderId);
}
