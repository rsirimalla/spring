package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service - Kafka Consumer Microservice
 * 
 * This application demonstrates:
 * - Consuming messages from Confluent Kafka
 * - OAuth 2.0 / SASL authentication with Kafka
 * - Avro deserialization with Schema Registry
 * - Manual acknowledgment for reliability
 * - Error handling and Dead Letter Queue pattern
 * - Processing events and sending notifications
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
