package com.example.notification.kafka;

import com.example.avro.OrderEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;
    
    @Value("${spring.kafka.properties.basic.auth.user.info:}")
    private String schemaRegistryAuth;
    
    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Basic Kafka Configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        
        // Consumer behavior
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        
        // Schema Registry Configuration
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        
        // If using Confluent Cloud with basic auth
        if (!schemaRegistryAuth.isEmpty()) {
            props.put("basic.auth.credentials.source", "USER_INFO");
            props.put("basic.auth.user.info", schemaRegistryAuth);
        }
        
        // Add OAuth configuration if needed (read from environment variables)
        String securityProtocol = System.getenv("KAFKA_SECURITY_PROTOCOL");
        if (securityProtocol != null && !securityProtocol.isEmpty()) {
            props.put("security.protocol", securityProtocol);
            props.put("sasl.mechanism", System.getenv("KAFKA_SASL_MECHANISM"));
            props.put("sasl.jaas.config", System.getenv("KAFKA_SASL_JAAS_CONFIG"));
            
            // For OAuth Bearer specifically
            if ("OAUTHBEARER".equals(System.getenv("KAFKA_SASL_MECHANISM"))) {
                props.put("sasl.login.callback.handler.class", 
                    "org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler");
            }
        }
        
        // Consumer reliability settings
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // Concurrency (number of consumer threads)
        factory.setConcurrency(3);
        
        // Error handling - will be handled in the listener
        
        return factory;
    }
}
