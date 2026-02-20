package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidOrderStateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for Order. Encapsulates status transitions and invariants.
 */
public final class Order {

    private static final Money MINIMUM_ORDER_AMOUNT = new Money(new java.math.BigDecimal("10.00"));

    private final OrderId id;
    private final UUID customerId;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;
    private final Money totalAmount;
    private OrderStatus status;

    private Order(OrderId id, UUID customerId, LocalDateTime createdAt,
                  List<OrderItem> items, Money totalAmount, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.totalAmount = totalAmount;
        this.status = status;
    }

    /**
     * Reconstitutes an order from persistence. Use when loading an existing aggregate.
     */
    public static Order reconstitute(OrderId id, UUID customerId, LocalDateTime createdAt,
                                    List<OrderItem> items, Money totalAmount, OrderStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Order(id, customerId, createdAt, items, totalAmount, status);
    }

    /**
     * Creates a new order with at least one item. Total is computed from line totals.
     */
    public static Order create(OrderId id, UUID customerId, List<OrderItem> items) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        Money total = items.stream()
            .map(OrderItem::lineTotal)
            .reduce(Money::add)
            .orElseThrow(() -> new IllegalArgumentException("Order must have at least one item"));
        return new Order(id, customerId, LocalDateTime.now(), items, total, OrderStatus.PENDING);
    }

    public OrderId getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Marks the order as paid. Allowed only when total >= 10.00 USD and status is PENDING.
     */
    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only PENDING orders can be marked as PAID");
        }
        if (!meetsMinimumOrderAmount()) {
            throw new InvalidOrderStateException(
                "Order total must be at least 10.00 " + totalAmount.getCurrency() + " to be placed");
        }
        this.status = OrderStatus.PAID;
    }

    private boolean meetsMinimumOrderAmount() {
        return !totalAmount.isLessThan(MINIMUM_ORDER_AMOUNT);
    }

    /**
     * Marks the order as shipped. Allowed only when status is PAID.
     */
    public void markAsShipped() {
        if (status != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Only PAID orders can be marked as SHIPPED");
        }
        this.status = OrderStatus.SHIPPED;
    }

    /**
     * Cancels the order. Allowed only when status is PENDING or PAID.
     */
    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw new InvalidOrderStateException(
                "Only PENDING or PAID orders can be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
