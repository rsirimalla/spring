package com.example.notification.service;

import com.example.avro.EventType;
import com.example.avro.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Value("${notification.email.enabled}")
    private boolean emailEnabled;
    
    @Value("${notification.email.from}")
    private String emailFrom;
    
    @Value("${notification.sms.enabled}")
    private boolean smsEnabled;
    
    /**
     * Process order event and send appropriate notifications
     */
    public void processOrderEvent(OrderEvent event) {
        logger.info("Processing order event: orderId={}, eventType={}, status={}", 
            event.getOrderId(), event.getEventType(), event.getStatus());
        
        switch (event.getEventType()) {
            case ORDER_CREATED:
                sendOrderConfirmation(event);
                break;
            case ORDER_UPDATED:
                sendOrderUpdate(event);
                break;
            case ORDER_CANCELLED:
                sendCancellationNotification(event);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }
    
    /**
     * Send order confirmation notification
     */
    private void sendOrderConfirmation(OrderEvent event) {
        String message = String.format(
            "Order Confirmed! Order #%s for %s (Qty: %d) - Total: $%.2f",
            event.getOrderId(),
            event.getProductName(),
            event.getQuantity(),
            event.getTotalAmount()
        );
        
        sendNotification(event.getCustomerEmail(), "Order Confirmation", message);
    }
    
    /**
     * Send order update notification
     */
    private void sendOrderUpdate(OrderEvent event) {
        String message = String.format(
            "Order Update: Order #%s status changed to %s",
            event.getOrderId(),
            event.getStatus()
        );
        
        sendNotification(event.getCustomerEmail(), "Order Update", message);
    }
    
    /**
     * Send cancellation notification
     */
    private void sendCancellationNotification(OrderEvent event) {
        String message = String.format(
            "Order Cancelled: Order #%s has been cancelled. Refund will be processed.",
            event.getOrderId()
        );
        
        sendNotification(event.getCustomerEmail(), "Order Cancelled", message);
    }
    
    /**
     * Generic notification sender
     * In production, this would integrate with:
     * - SendGrid/AWS SES for email
     * - Twilio/AWS SNS for SMS
     * - Firebase for push notifications
     */
    private void sendNotification(String recipient, String subject, String message) {
        if (emailEnabled) {
            sendEmail(recipient, subject, message);
        }
        
        if (smsEnabled) {
            sendSMS(recipient, message);
        }
    }
    
    /**
     * Send email notification
     * TODO: Integrate with actual email service
     */
    private void sendEmail(String to, String subject, String body) {
        // In production, use JavaMailSender or external service
        logger.info("EMAIL: To={}, Subject={}, Body={}", to, subject, body);
        
        // Example with JavaMailSender (not implemented):
        // MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message);
        // helper.setFrom(emailFrom);
        // helper.setTo(to);
        // helper.setSubject(subject);
        // helper.setText(body);
        // mailSender.send(message);
    }
    
    /**
     * Send SMS notification
     * TODO: Integrate with actual SMS service
     */
    private void sendSMS(String phoneNumber, String message) {
        // In production, use Twilio or AWS SNS
        logger.info("SMS: To={}, Message={}", phoneNumber, message);
        
        // Example with Twilio (not implemented):
        // Message.creator(
        //     new PhoneNumber(phoneNumber),
        //     new PhoneNumber(twilioPhoneNumber),
        //     message
        // ).create();
    }
}
