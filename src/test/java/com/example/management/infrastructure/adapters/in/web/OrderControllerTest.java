package com.example.management.infrastructure.adapters.in.web;

import com.example.management.application.ports.in.CreateOrderUseCase;
import com.example.management.application.ports.in.GetOrderUseCase;
import com.example.management.application.ports.in.PayOrderUseCase;
import com.example.management.domain.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private GetOrderUseCase getOrderUseCase;

    @MockBean
    private PayOrderUseCase payOrderUseCase;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final Money TEN_USD = new Money(new BigDecimal("10.00"));

    @Test
    void createOrder_validRequest_returns201AndBody() throws Exception {
        // Arrange
        Order order = Order.create(
            new OrderId(ORDER_ID),
            CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, new Money(new BigDecimal("5.00"))))
        );
        when(createOrderUseCase.create(eq(CUSTOMER_ID), any())).thenReturn(order);

        String body = """
            {
              "customerId": "%s",
              "items": [
                { "productId": "%s", "quantity": 2, "unitPrice": 5.00 }
              ]
            }
            """.formatted(CUSTOMER_ID, UUID.randomUUID());

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(10.0));
    }

    @Test
    void getOrder_whenExists_returns200AndBody() throws Exception {
        // Arrange
        Order order = Order.reconstitute(
            new OrderId(ORDER_ID),
            CUSTOMER_ID,
            LocalDateTime.now(),
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD)),
            TEN_USD.multiply(2),
            OrderStatus.PENDING
        );
        when(getOrderUseCase.getById(new OrderId(ORDER_ID))).thenReturn(Optional.of(order));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{orderId}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
            .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_whenNotExists_returns404() throws Exception {
        when(getOrderUseCase.getById(new OrderId(ORDER_ID))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/{orderId}", ORDER_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    void payOrder_whenExists_returns200AndBody() throws Exception {
        Order order = Order.create(
            new OrderId(ORDER_ID),
            CUSTOMER_ID,
            List.of(new OrderItem(UUID.randomUUID(), 2, TEN_USD))
        );
        order.markAsPaid();
        when(payOrderUseCase.pay(new OrderId(ORDER_ID))).thenReturn(Optional.of(order));

        mockMvc.perform(post("/api/v1/orders/{orderId}/pay", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void payOrder_whenNotExists_returns404() throws Exception {
        when(payOrderUseCase.pay(new OrderId(ORDER_ID))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/orders/{orderId}/pay", ORDER_ID))
            .andExpect(status().isNotFound());
    }
}
