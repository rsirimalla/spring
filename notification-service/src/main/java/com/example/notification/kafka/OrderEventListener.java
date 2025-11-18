package com.example.notification.kafka;

import com.example.avro.OrderEvent;
import com.example.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final NotificationService notificationService;
    
    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Kafka listener for order events
     * 
     * Key features:
     * - Manual acknowledgment for reliability
     * - Error handling with retry logic
     * - Logging for monitoring
     * - Dead Letter Queue (DLQ) for failed messages
     */
    @KafkaListener(
        topics = "${kafka.topics.order-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
            @Payload OrderEvent orderEvent,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            ConsumerRecord<String, OrderEvent> record,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received order event: orderId={}, eventType={}, partition={}, offset={}", 
                orderEvent.getOrderId(), 
                orderEvent.getEventType(),
                partition,
                offset);
            
            // Process the event
            notificationService.processOrderEvent(orderEvent);
            
            // Manually acknowledge successful processing
            acknowledgment.acknowledge();
            
            logger.info("Successfully processed order event: orderId={}", orderEvent.getOrderId());
            
        } catch (Exception e) {
            logger.error("Error processing order event: orderId={}, partition={}, offset={}", 
                orderEvent.getOrderId(), partition, offset, e);
            
            // Retry logic
            if (shouldRetry(record)) {
                logger.info("Retrying message: orderId={}", orderEvent.getOrderId());
                // Don't acknowledge - message will be redelivered
                // In production, you might want to add exponential backoff
                return;
            }
            
            // After max retries, send to DLQ
            logger.error("Max retries exceeded, sending to DLQ: orderId={}", orderEvent.getOrderId());
            sendToDeadLetterQueue(orderEvent, e);
            
            // Acknowledge to move on (message is in DLQ)
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Determine if message should be retried
     * This is a simple implementation - in production you'd want more sophisticated logic
     */
    private boolean shouldRetry(ConsumerRecord<String, OrderEvent> record) {
        // Check if this is a transient error (network issue, temporary service unavailability)
        // For now, we'll retry a few times based on headers or time
        
        // You could add retry count to headers:
        // Integer retryCount = (Integer) record.headers().lastHeader("retry-count");
        // return retryCount == null || retryCount < MAX_RETRIES;
        
        // For this example, we'll just log and not retry automatically
        return false;
    }
    
    /**
     * Send failed message to Dead Letter Queue
     * In production, you'd publish to a DLQ topic for later analysis
     */
    private void sendToDeadLetterQueue(OrderEvent event, Exception error) {
        logger.warn("Sending to DLQ: orderId={}, error={}", 
            event.getOrderId(), error.getMessage());
        
        // In production:
        // 1. Publish to DLQ topic with error metadata
        // 2. Store in database for manual review
        // 3. Send alert to monitoring system
        
        // Example DLQ publishing (requires KafkaTemplate<String, OrderEvent>):
        // DLQMessage dlqMessage = new DLQMessage(event, error.getMessage(), System.currentTimeMillis());
        // kafkaTemplate.send("order-events-dlq", event.getOrderId(), dlqMessage);
    }
    
    /**
     * Alternative listener implementation with automatic retry
     * (commented out - use either this or the above)
     */
    /*
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        autoCreateTopics = "false",
        include = {Exception.class}
    )
    @KafkaListener(
        topics = "${kafka.topics.order-events}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listenWithRetry(OrderEvent orderEvent) {
        logger.info("Processing: {}", orderEvent.getOrderId());
        notificationService.processOrderEvent(orderEvent);
    }
    
    @DltHandler
    public void handleDlt(OrderEvent event, Exception exception) {
        logger.error("DLT: Failed to process after retries: {}", event.getOrderId(), exception);
    }
    */
}
