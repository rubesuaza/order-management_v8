package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidItemException;

import java.util.Objects;
import java.util.UUID;

/**
 * An item within an order. Identity within the aggregate is effectively (order + productId).
 * When reconstituted from persistence, {@code id} is set to preserve the entity ID on save.
 */
public final class OrderItem {

    private final UUID id;
    private final UUID productId;
    private final int quantity;
    private final Money unitPrice;

    /** Creates a new item (no persistence id yet). */
    public OrderItem(UUID productId, int quantity, Money unitPrice) {
        this(null, productId, quantity, unitPrice);
    }

    /** Reconstitutes an item, optionally with its persistence id for updates. */
    public OrderItem(UUID id, UUID productId, int quantity, Money unitPrice) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (quantity <= 0) {
            throw new InvalidItemException("Quantity must be strictly greater than zero");
        }
        if (unitPrice.getAmount().signum() < 0) {
            throw new InvalidItemException("Unit price cannot be negative");
        }
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** Persistence id when loaded from DB; null for new items. */
    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    /**
     * Line total: unitPrice * quantity.
     */
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity
            && Objects.equals(id, orderItem.id)
            && Objects.equals(productId, orderItem.productId)
            && Objects.equals(unitPrice, orderItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return "OrderItem{" + "productId=" + productId + ", quantity=" + quantity + ", unitPrice=" + unitPrice + '}';
    }
}
