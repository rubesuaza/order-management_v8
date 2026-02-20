package com.example.management.domain.model;

import com.example.management.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    private static final String USD = "USD";

    @Test
    void add_sameCurrency_returnsSum() {
        Money a = new Money(new BigDecimal("10.00"), USD);
        Money b = new Money(new BigDecimal("5.50"), USD);
        Money result = a.add(b);
        assertEquals(new BigDecimal("15.50"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    void add_differentCurrency_throwsCurrencyMismatchException() {
        Money usd = new Money(new BigDecimal("10.00"), "USD");
        Money eur = new Money(new BigDecimal("5.00"), "EUR");
        assertThrows(CurrencyMismatchException.class, () -> usd.add(eur));
    }

    @Test
    void subtract_sameCurrency_returnsDifference() {
        Money a = new Money(new BigDecimal("20.00"), USD);
        Money b = new Money(new BigDecimal("7.25"), USD);
        Money result = a.subtract(b);
        assertEquals(new BigDecimal("12.75"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    void subtract_differentCurrency_throwsCurrencyMismatchException() {
        Money usd = new Money(new BigDecimal("20.00"), "USD");
        Money eur = new Money(new BigDecimal("5.00"), "EUR");
        assertThrows(CurrencyMismatchException.class, () -> usd.subtract(eur));
    }

    @Test
    void multiply_returnsScaledAmount() {
        Money price = new Money(new BigDecimal("2.50"), USD);
        Money result = price.multiply(3);
        assertEquals(new BigDecimal("7.50"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    void defaultCurrency_isUsd() {
        Money m = new Money(new BigDecimal("10.00"));
        assertEquals("USD", m.getCurrency());
    }

    @Test
    void equals_sameAmountAndCurrency_areEqual() {
        Money a = new Money(new BigDecimal("10.00"), USD);
        Money b = new Money(new BigDecimal("10.00"), USD);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
