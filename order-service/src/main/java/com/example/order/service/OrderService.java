package com.example.order.service;

import com.example.avro.EventType;
import com.example.order.kafka.OrderEventPublisher;
import com.example.order.model.CreateOrderRequest;
import com.example.order.model.Order;
import com.example.order.model.Order.OrderStatus;
import com.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    
    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Creates a new order and publishes event to Kafka
     * @Transactional ensures database consistency
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        logger.info("Creating order for customer: {}", request.getCustomerId());
        
        // Create and save order
        Order order = new Order(
            request.getCustomerId(),
            request.getCustomerEmail(),
            request.getProductName(),
            request.getQuantity(),
            request.getTotalAmount()
        );
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order saved with ID: {}", savedOrder.getId());
        
        // Publish event to Kafka (async)
        eventPublisher.publishOrderEvent(savedOrder, EventType.ORDER_CREATED);
        
        return savedOrder;
    }
    
    /**
     * Retrieves order by ID
     */
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }
    
    /**
     * Gets all orders for a customer
     */
    public List<Order> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
    
    /**
     * Gets all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * Updates order status and publishes event
     */
    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        OrderStatus oldStatus = order.getStatus();
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        logger.info("Order status updated: orderId={}, {} -> {}", 
            orderId, oldStatus, newStatus);
        
        // Publish update event
        EventType eventType = (newStatus == OrderStatus.CANCELLED) 
            ? EventType.ORDER_CANCELLED 
            : EventType.ORDER_UPDATED;
        
        eventPublisher.publishOrderEvent(updatedOrder, eventType);
        
        return updatedOrder;
    }
    
    /**
     * Cancels an order
     */
    @Transactional
    public Order cancelOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }
    
    // Custom exception for order not found
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }
}
