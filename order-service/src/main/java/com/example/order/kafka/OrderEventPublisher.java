package com.example.order.kafka;

import com.example.avro.EventType;
import com.example.avro.OrderEvent;
import com.example.avro.OrderStatus;
import com.example.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);
    
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    @Value("${kafka.topics.order-events}")
    private String orderEventsTopic;
    
    public OrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publishes an order event to Kafka
     * @param order The order entity
     * @param eventType The type of event (CREATED, UPDATED, CANCELLED)
     */
    public void publishOrderEvent(Order order, EventType eventType) {
        try {
            // Convert Order entity to Avro OrderEvent
            OrderEvent orderEvent = buildOrderEvent(order, eventType);
            
            // Send to Kafka (key is orderId for partitioning)
            CompletableFuture<SendResult<String, OrderEvent>> future = 
                kafkaTemplate.send(orderEventsTopic, order.getId(), orderEvent);
            
            // Add callback for success/failure handling
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Order event published successfully: orderId={}, eventType={}, partition={}, offset={}", 
                        order.getId(), 
                        eventType,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish order event: orderId={}, eventType={}", 
                        order.getId(), eventType, ex);
                    // In production, you might want to:
                    // - Store failed events in a database for retry
                    // - Send alert to monitoring system
                    // - Implement circuit breaker pattern
                }
            });
            
        } catch (Exception e) {
            logger.error("Error creating order event: orderId={}", order.getId(), e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
    
    /**
     * Converts Order entity to Avro OrderEvent
     */
    private OrderEvent buildOrderEvent(Order order, EventType eventType) {
        return OrderEvent.newBuilder()
            .setOrderId(order.getId())
            .setCustomerId(order.getCustomerId())
            .setCustomerEmail(order.getCustomerEmail())
            .setProductName(order.getProductName())
            .setQuantity(order.getQuantity())
            .setTotalAmount(order.getTotalAmount())
            .setStatus(mapOrderStatus(order.getStatus()))
            .setEventTimestamp(System.currentTimeMillis())
            .setEventType(eventType)
            .build();
    }
    
    /**
     * Maps JPA OrderStatus to Avro OrderStatus
     */
    private OrderStatus mapOrderStatus(Order.OrderStatus status) {
        return switch (status) {
            case CREATED -> OrderStatus.CREATED;
            case CONFIRMED -> OrderStatus.CONFIRMED;
            case SHIPPED -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case CANCELLED -> OrderStatus.CANCELLED;
        };
    }
}
