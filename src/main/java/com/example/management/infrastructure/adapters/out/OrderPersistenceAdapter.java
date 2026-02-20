package com.example.management.infrastructure.adapters.out;

import com.example.management.application.ports.out.OrderRepository;
import com.example.management.domain.model.Order;
import com.example.management.domain.model.OrderId;
import com.example.management.infrastructure.persistence.OrderJpaEntity;
import com.example.management.infrastructure.persistence.OrderJpaRepository;
import com.example.management.infrastructure.persistence.OrderMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        if (jpaRepository.existsById(order.getId().getValue())) {
            OrderJpaEntity existing = jpaRepository.getReferenceById(order.getId().getValue());
            existing.setStatus(order.getStatus().name());
            existing.setTotalAmount(order.getTotalAmount().getAmount());
            existing.setCurrency(order.getTotalAmount().getCurrency());
            jpaRepository.save(existing);
            return order;
        }
        OrderJpaEntity entity = mapper.toEntity(order);
        jpaRepository.save(entity);
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
            .map(mapper::toDomain);
    }
}
