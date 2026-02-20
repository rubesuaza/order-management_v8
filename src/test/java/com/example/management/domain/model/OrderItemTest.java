package com.example.management.domain.model;

import com.example.management.domain.exception.InvalidItemException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    private static final UUID PRODUCT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    private static final Money VALID_PRICE = new Money(new BigDecimal("5.00"));

    @Test
    void create_validQuantityAndPrice_succeeds() {
        OrderItem item = new OrderItem(PRODUCT_ID, 2, VALID_PRICE);
        assertEquals(PRODUCT_ID, item.getProductId());
        assertEquals(2, item.getQuantity());
        assertEquals(VALID_PRICE, item.getUnitPrice());
    }

    @Test
    void create_quantityZero_throwsInvalidItemException() {
        assertThrows(InvalidItemException.class,
            () -> new OrderItem(PRODUCT_ID, 0, VALID_PRICE));
    }

    @Test
    void create_quantityNegative_throwsInvalidItemException() {
        assertThrows(InvalidItemException.class,
            () -> new OrderItem(PRODUCT_ID, -1, VALID_PRICE));
    }

    @Test
    void create_negativeUnitPrice_throwsInvalidItemException() {
        Money negativePrice = new Money(new BigDecimal("-1.00"));
        assertThrows(InvalidItemException.class,
            () -> new OrderItem(PRODUCT_ID, 1, negativePrice));
    }

    @Test
    void create_zeroUnitPrice_succeeds() {
        Money zeroPrice = new Money(BigDecimal.ZERO);
        OrderItem item = new OrderItem(PRODUCT_ID, 1, zeroPrice);
        assertEquals(zeroPrice, item.getUnitPrice());
    }

    @Test
    void lineTotal_returnsUnitPriceTimesQuantity() {
        OrderItem item = new OrderItem(PRODUCT_ID, 3, new Money(new BigDecimal("2.50")));
        Money total = item.lineTotal();
        assertEquals(new Money(new BigDecimal("7.50")), total);
    }
}
