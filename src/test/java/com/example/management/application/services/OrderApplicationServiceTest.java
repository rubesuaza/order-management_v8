package com.example.management.application.services;

import com.example.management.application.ports.in.CreateOrderUseCase;
import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderApplicationService service;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Money TEN_USD = new Money(new BigDecimal("10.00"));

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(orderRepository);
    }

    @Test
    void create_withValidItems_savesAndReturnsOrder() {
        // Arrange
        CreateOrderUseCase.OrderItemCommand itemCmd =
            new CreateOrderUseCase.OrderItemCommand(UUID.randomUUID(), 2, new BigDecimal("5.00"));
        Order savedOrder = Order.create(
            OrderId.generate(),
            CUSTOMER_ID,
            List.of(new OrderItem(itemCmd.productId(), itemCmd.quantity(), new Money(itemCmd.unitPrice())))
        );
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Order result = service.create(CUSTOMER_ID, List.of(itemCmd));

        // Assert
        assertNotNull(result);
        assertEquals(CUSTOMER_ID, result.getCustomerId());
        assertEquals(1, result.getItems().size());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getById_whenOrderExists_returnsOptionalWithOrder() {
        // Arrange
        OrderId orderId = OrderId.generate();
        Order order = Order.create(
            orderId,
            CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD))
        );
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<Order> result = service.getById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getById_whenOrderNotExists_returnsEmpty() {
        // Arrange
        OrderId orderId = OrderId.generate();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = service.getById(orderId);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void pay_whenOrderExists_marksAsPaidAndSaves() {
        // Arrange
        OrderId orderId = OrderId.generate();
        Order order = Order.create(
            orderId,
            CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD))
        );
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<Order> result = service.pay(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(OrderStatus.PAID, result.get().getStatus());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void pay_whenOrderNotExists_returnsEmpty() {
        // Arrange
        OrderId orderId = OrderId.generate();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = service.pay(orderId);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }
}
