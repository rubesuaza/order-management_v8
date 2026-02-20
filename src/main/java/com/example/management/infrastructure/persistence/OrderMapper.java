package com.example.management.infrastructure.persistence;

import com.example.management.domain.model.Money;
import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;
import com.example.management.domain.model.OrderItem;
import com.example.management.domain.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public Order toDomain(OrderJpaEntity entity) {
        String currency = entity.getCurrency();
        List<OrderItem> items = entity.getItems().stream()
            .map(e -> new OrderItem(e.getId(), e.getProductId(), e.getQuantity(), new Money(e.getUnitPrice(), currency)))
            .toList();
        Money totalAmount = new Money(entity.getTotalAmount(), currency);
        return Order.reconstitute(
            new OrderId(entity.getId()),
            entity.getCustomerId(),
            entity.getCreatedAt(),
            items,
            totalAmount,
            OrderStatus.valueOf(entity.getStatus())
        );
    }

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
            order.getId().getValue(),
            order.getCustomerId(),
            order.getStatus().name(),
            order.getTotalAmount().getAmount(),
            order.getTotalAmount().getCurrency(),
            order.getCreatedAt()
        );
        for (com.example.management.domain.model.OrderItem item : order.getItems()) {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                item.getId(),
                null,
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice().getAmount()
            );
            entity.addOrderItem(itemEntity);
        }
        return entity;
    }
}
