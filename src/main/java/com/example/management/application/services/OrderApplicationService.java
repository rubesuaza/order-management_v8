package com.example.management.application.services;

import com.example.management.application.ports.in.CreateOrderUseCase;
import com.example.management.application.ports.in.GetOrderUseCase;
import com.example.management.application.ports.in.PayOrderUseCase;
import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.Money;
import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;
import com.example.management.domain.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderApplicationService implements CreateOrderUseCase, GetOrderUseCase, PayOrderUseCase {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Order create(UUID customerId, List<CreateOrderUseCase.OrderItemCommand> items) {
        List<OrderItem> domainItems = items.stream()
            .map(cmd -> new OrderItem(cmd.productId(), cmd.quantity(), new Money(cmd.unitPrice())))
            .collect(Collectors.toList());
        Order order = Order.create(OrderId.generate(), customerId, domainItems);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getById(OrderId orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional
    public Optional<Order> pay(OrderId orderId) {
        return orderRepository.findById(orderId)
            .map(order -> {
                order.markAsPaid();
                return orderRepository.save(order);
            });
    }
}
