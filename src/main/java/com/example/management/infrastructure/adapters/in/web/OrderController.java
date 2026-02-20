package com.example.management.infrastructure.adapters.in.web;

import com.example.management.application.ports.in.CreateOrderUseCase;
import com.example.management.application.ports.in.GetOrderUseCase;
import com.example.management.application.ports.in.PayOrderUseCase;
import com.example.management.domain.model.OrderId;
import com.example.management.infrastructure.adapters.in.web.dto.CreateOrderRequest;
import com.example.management.infrastructure.adapters.in.web.dto.CreateOrderResponse;
import com.example.management.infrastructure.adapters.in.web.dto.OrderResponse;
import com.example.management.infrastructure.adapters.in.web.dto.PayOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final PayOrderUseCase payOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase, GetOrderUseCase getOrderUseCase, PayOrderUseCase payOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.payOrderUseCase = payOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        List<CreateOrderUseCase.OrderItemCommand> items = request.items().stream()
            .map(i -> new CreateOrderUseCase.OrderItemCommand(i.productId(), i.quantity(), i.unitPrice()))
            .collect(Collectors.toList());
        var order = createOrderUseCase.create(request.customerId(), items);
        CreateOrderResponse body = new CreateOrderResponse(
            order.getId().getValue(),
            order.getStatus().name(),
            order.getTotalAmount().getAmount(),
            order.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return getOrderUseCase.getById(new OrderId(orderId))
            .map(order -> ResponseEntity.ok(toOrderResponse(order)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<PayOrderResponse> payOrder(@PathVariable UUID orderId) {
        return payOrderUseCase.pay(new OrderId(orderId))
            .map(order -> ResponseEntity.ok(new PayOrderResponse(order.getId().getValue(), order.getStatus().name())))
            .orElse(ResponseEntity.notFound().build());
    }

    private static OrderResponse toOrderResponse(com.example.management.domain.model.Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
            .map(i -> new OrderResponse.OrderItemResponse(
                i.getProductId(),
                i.getQuantity(),
                i.getUnitPrice().getAmount()
            ))
            .collect(Collectors.toList());
        return new OrderResponse(
            order.getId().getValue(),
            order.getCustomerId(),
            order.getStatus().name(),
            items,
            order.getTotalAmount().getAmount(),
            order.getTotalAmount().getCurrency(),
            order.getCreatedAt()
        );
    }
}
