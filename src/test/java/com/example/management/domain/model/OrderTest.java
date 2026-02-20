package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final Money TEN_USD = new Money(new BigDecimal("10.00"));
    private static final Money FIVE_USD = new Money(new BigDecimal("5.00"));

    @Test
    void create_withOneItem_setsTotalAndStatusPending() {
        OrderItem item = new OrderItem(UUID.randomUUID(), 2, FIVE_USD);
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID, List.of(item));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new Money(new BigDecimal("10.00")), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
    }

    @Test
    void create_emptyItems_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> Order.create(OrderId.generate(), CUSTOMER_ID, List.of()));
    }

    @Test
    void create_totalIsSumOfLineTotals() {
        OrderItem a = new OrderItem(UUID.randomUUID(), 1, new Money(new BigDecimal("3.00")));
        OrderItem b = new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("4.00")));
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID, List.of(a, b));
        assertEquals(new Money(new BigDecimal("11.00")), order.getTotalAmount());
    }

    @Test
    void markAsPaid_totalAtLeast10Usd_succeeds() {
        OrderItem item = new OrderItem(UUID.randomUUID(), 2, TEN_USD);
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID, List.of(item));
        order.markAsPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void markAsPaid_totalLessThan10Usd_throwsInvalidOrderStateException() {
        OrderItem item = new OrderItem(UUID.randomUUID(), 1, FIVE_USD);
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID, List.of(item));
        assertThrows(InvalidOrderStateException.class, order::markAsPaid);
    }

    @Test
    void cancel_whenPending_succeeds() {
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)));
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancel_whenPaid_succeeds() {
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)));
        order.markAsPaid();
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancel_whenShipped_throwsInvalidOrderStateException() {
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)));
        order.markAsPaid();
        order.markAsShipped();
        assertThrows(InvalidOrderStateException.class, order::cancel);
    }

    @Test
    void markAsShipped_whenPaid_succeeds() {
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)));
        order.markAsPaid();
        order.markAsShipped();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void markAsShipped_whenPending_throwsInvalidOrderStateException() {
        Order order = Order.create(OrderId.generate(), CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)));
        assertThrows(InvalidOrderStateException.class, order::markAsShipped);
    }
}
