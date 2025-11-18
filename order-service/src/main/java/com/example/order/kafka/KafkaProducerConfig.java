package com.example.order.kafka;

import com.example.avro.OrderEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;
    
    // Optional: For Confluent Cloud with basic auth
    @Value("${spring.kafka.properties.basic.auth.user.info:}")
    private String schemaRegistryAuth;
    
    @Bean
    public ProducerFactory<String, OrderEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic Kafka Configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        
        // Schema Registry Configuration
        configProps.put("schema.registry.url", schemaRegistryUrl);
        
        // If using Confluent Cloud with basic auth
        if (!schemaRegistryAuth.isEmpty()) {
            configProps.put("basic.auth.credentials.source", "USER_INFO");
            configProps.put("basic.auth.user.info", schemaRegistryAuth);
        }
        
        // Producer reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Compression for better network utilization
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Add OAuth configuration if needed (read from environment variables)
        String securityProtocol = System.getenv("KAFKA_SECURITY_PROTOCOL");
        if (securityProtocol != null && !securityProtocol.isEmpty()) {
            configProps.put("security.protocol", securityProtocol);
            configProps.put("sasl.mechanism", System.getenv("KAFKA_SASL_MECHANISM"));
            configProps.put("sasl.jaas.config", System.getenv("KAFKA_SASL_JAAS_CONFIG"));
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
