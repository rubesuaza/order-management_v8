package com.example.management.infrastructure.adapters.out;

import com.example.management.domain.model.*;
import com.example.management.infrastructure.persistence.OrderJpaEntity;
import com.example.management.infrastructure.persistence.OrderJpaRepository;
import com.example.management.infrastructure.persistence.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterTest {

    @Mock
    private OrderJpaRepository jpaRepository;

    @Mock
    private OrderMapper mapper;

    private OrderPersistenceAdapter adapter;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Money TEN_USD = new Money(new BigDecimal("10.00"));

    @BeforeEach
    void setUp() {
        adapter = new OrderPersistenceAdapter(jpaRepository, mapper);
    }

    @Test
    void save_newOrder_callsMapperAndRepository() {
        // Arrange
        Order order = Order.create(
            new OrderId(ORDER_ID),
            CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("5.00"))))
        );
        OrderJpaEntity entity = new OrderJpaEntity(
            ORDER_ID, CUSTOMER_ID, "PENDING",
            new BigDecimal("10.00"), "USD", LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        );
        when(jpaRepository.findById(ORDER_ID)).thenReturn(Optional.empty());
        when(mapper.toEntity(order)).thenReturn(entity);

        // Act
        Order result = adapter.save(order);

        // Assert
        assertSame(order, result);
        verify(mapper).toEntity(order);
        verify(jpaRepository).save(entity);
    }

    @Test
    void save_existingOrder_updatesEntityAndSaves() {
        // Arrange
        Order order = Order.create(
            new OrderId(ORDER_ID),
            CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD))
        );
        order.markAsPaid();
        OrderJpaEntity existingEntity = new OrderJpaEntity(
            ORDER_ID, CUSTOMER_ID, "PENDING",
            new BigDecimal("20.00"), "USD", LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        );
        when(jpaRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingEntity));

        // Act
        Order result = adapter.save(order);

        // Assert
        assertSame(order, result);
        assertEquals("PAID", existingEntity.getStatus());
        assertEquals(new BigDecimal("20.00"), existingEntity.getTotalAmount());
        verify(jpaRepository).save(existingEntity);
        verify(mapper, never()).toEntity(any());
    }

    @Test
    void findById_whenExists_returnsMappedOrder() {
        // Arrange
        OrderId orderId = new OrderId(ORDER_ID);
        Order order = Order.reconstitute(
            orderId,
            CUSTOMER_ID,
            LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)),
            TEN_USD.multiply(2),
            OrderStatus.PENDING
        );
        OrderJpaEntity entity = new OrderJpaEntity(
            ORDER_ID, CUSTOMER_ID, "PENDING",
            new BigDecimal("20.00"), "USD", LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        );
        when(jpaRepository.findById(ORDER_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(order);

        // Act
        Optional<Order> result = adapter.findById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertSame(order, result.get());
        verify(jpaRepository).findById(ORDER_ID);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_whenNotExists_returnsEmpty() {
        OrderId orderId = new OrderId(ORDER_ID);
        when(jpaRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        Optional<Order> result = adapter.findById(orderId);

        assertTrue(result.isEmpty());
        verify(jpaRepository).findById(ORDER_ID);
        verify(mapper, never()).toDomain(any());
    }
}
