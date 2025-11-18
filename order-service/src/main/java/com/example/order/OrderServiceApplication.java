package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order Service - REST API Microservice with Kafka Producer
 * 
 * This application demonstrates:
 * - REST API endpoints for order management
 * - Database integration with Spring Data JPA
 * - Publishing events to Kafka with Avro serialization
 * - Confluent Schema Registry integration
 * - OAuth/SASL authentication support for Kafka
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
