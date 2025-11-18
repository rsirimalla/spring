package com.example.order.repository;

import com.example.order.model.Order;
import com.example.order.model.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    // Spring Data JPA automatically implements these methods
    // No need to write SQL queries!
    
    List<Order> findByCustomerId(String customerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
    
    // Custom query methods are also possible:
    // @Query("SELECT o FROM Order o WHERE o.totalAmount > :amount")
    // List<Order> findOrdersAboveAmount(@Param("amount") Double amount);
}
