# Testing Guide

## Testing Order Service (REST API)

### 1. Create an Order

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerEmail": "customer@example.com",
    "productName": "MacBook Pro",
    "quantity": 1,
    "totalAmount": 2499.99
  }'
```

Expected Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST001",
  "customerEmail": "customer@example.com",
  "productName": "MacBook Pro",
  "quantity": 1,
  "totalAmount": 2499.99,
  "status": "CREATED",
  "createdAt": "2025-09-15T10:30:00",
  "updatedAt": null
}
```

### 2. Get All Orders

```bash
curl http://localhost:8082/api/orders
```

### 3. Get Order by ID

```bash
curl http://localhost:8082/api/orders/{orderId}
```

### 4. Get Orders by Customer

```bash
curl http://localhost:8082/api/orders?customerId=CUST001
```

### 5. Update Order Status

```bash
curl -X PUT "http://localhost:8082/api/orders/{orderId}/status?status=CONFIRMED"
```

Possible status values:
- `CREATED`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

### 6. Cancel an Order

```bash
curl -X POST http://localhost:8082/api/orders/{orderId}/cancel
```

### 7. Test Validation Errors

```bash
# Missing required fields
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Test Product"
  }'
```

Expected Response (400 Bad Request):
```json
{
  "customerId": "Customer ID is required",
  "customerEmail": "Customer email is required",
  "quantity": "Quantity is required",
  "totalAmount": "Total amount is required"
}
```

## Monitoring Kafka

### View Kafka Topics

```bash
# Using local Kafka
docker exec -it <kafka-container-id> kafka-topics \
  --list \
  --bootstrap-server localhost:9092

# View topic details
docker exec -it <kafka-container-id> kafka-topics \
  --describe \
  --topic order-events \
  --bootstrap-server localhost:9092
```

### Consume Messages (Debug)

```bash
# Console consumer to see messages
docker exec -it <kafka-container-id> kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

### View Consumer Groups

```bash
docker exec -it <kafka-container-id> kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# View consumer group details
docker exec -it <kafka-container-id> kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe
```

## Testing the Complete Flow

### End-to-End Test

1. **Start both services**
   ```bash
   # Terminal 1: Order Service
   cd order-service && ./mvnw spring-boot:run
   
   # Terminal 2: Notification Service
   cd notification-service && ./mvnw spring-boot:run
   ```

2. **Create an order**
   ```bash
   curl -X POST http://localhost:8082/api/orders \
     -H "Content-Type: application/json" \
     -d '{
       "customerId": "CUST001",
       "customerEmail": "test@example.com",
       "productName": "iPhone 15 Pro",
       "quantity": 2,
       "totalAmount": 1999.98
     }'
   ```

3. **Check Order Service logs**
   Look for:
   ```
   Order saved with ID: xxx-xxx-xxx
   Order event published successfully: orderId=xxx, eventType=ORDER_CREATED
   ```

4. **Check Notification Service logs**
   Look for:
   ```
   Received order event: orderId=xxx, eventType=ORDER_CREATED
   EMAIL: To=test@example.com, Subject=Order Confirmation
   Successfully processed order event: orderId=xxx
   ```

5. **Update order status**
   ```bash
   curl -X PUT "http://localhost:8082/api/orders/{orderId}/status?status=SHIPPED"
   ```

6. **Verify notification for update**
   Check Notification Service logs for:
   ```
   Received order event: orderId=xxx, eventType=ORDER_UPDATED
   EMAIL: To=test@example.com, Subject=Order Update
   ```

## Testing with Confluent Cloud

### Configure for Confluent Cloud

1. Set environment variables:
   ```bash
   export KAFKA_BOOTSTRAP_SERVERS="your-confluent-cloud-bootstrap"
   export KAFKA_API_KEY="your-api-key"
   export KAFKA_API_SECRET="your-api-secret"
   export SCHEMA_REGISTRY_URL="your-schema-registry-url"
   export SCHEMA_REGISTRY_API_KEY="your-sr-key"
   export SCHEMA_REGISTRY_API_SECRET="your-sr-secret"
   ```

2. Create topic in Confluent Cloud:
   - Go to Topics → Add Topic
   - Name: `order-events`
   - Partitions: 3
   - Retention: 7 days

3. Run the same tests as above

### Monitor in Confluent Cloud Console

1. **Messages**: Topics → order-events → Messages
2. **Consumer Lag**: Consumers → notification-service-group
3. **Schema Registry**: Schema Registry → View registered schemas

## Load Testing

### Generate Multiple Orders

```bash
#!/bin/bash
# create-orders.sh

for i in {1..10}
do
  curl -X POST http://localhost:8082/api/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"customerId\": \"CUST$(printf %03d $i)\",
      \"customerEmail\": \"customer$i@example.com\",
      \"productName\": \"Product $i\",
      \"quantity\": $((RANDOM % 5 + 1)),
      \"totalAmount\": $((RANDOM % 1000 + 100))
    }"
  
  echo "Created order $i"
  sleep 0.5
done
```

Run it:
```bash
chmod +x create-orders.sh
./create-orders.sh
```

### Monitor Consumer Performance

Watch consumer lag:
```bash
watch -n 5 'docker exec <kafka-container> kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group notification-service-group \
  --describe'
```

## Integration Tests

### Order Service Test

Create `OrderServiceIntegrationTest.java` in test directory:

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateOrder() throws Exception {
        String orderJson = """
            {
                "customerId": "TEST001",
                "customerEmail": "test@example.com",
                "productName": "Test Product",
                "quantity": 1,
                "totalAmount": 99.99
            }
        """;
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
```

Run tests:
```bash
cd order-service
./mvnw test
```

## Troubleshooting Tests

### Order Service not responding
- Check if service started: `curl http://localhost:8082/actuator/health`
- View logs: Check console output
- Verify H2 database: http://localhost:8082/h2-console

### Messages not being consumed
- Check consumer group lag
- Verify topic name in configuration
- Check Notification Service logs for errors
- Ensure consumer is running

### Schema Registry errors
- Verify Schema Registry is running: `curl http://localhost:8081`
- Check schema compatibility settings
- Ensure Avro schema is valid

### Performance issues
- Increase consumer concurrency in `application.yml`
- Adjust `max-poll-records` for batch processing
- Monitor JVM memory usage
- Check network latency to Kafka cluster

## Example Test Scenarios

### Scenario 1: Order Lifecycle
```bash
# 1. Create order
ORDER_ID=$(curl -s -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST001","customerEmail":"test@example.com","productName":"iPhone","quantity":1,"totalAmount":999}' \
  | jq -r '.id')

# 2. Confirm order
curl -X PUT "http://localhost:8082/api/orders/$ORDER_ID/status?status=CONFIRMED"

# 3. Ship order
curl -X PUT "http://localhost:8082/api/orders/$ORDER_ID/status?status=SHIPPED"

# 4. Deliver order
curl -X PUT "http://localhost:8082/api/orders/$ORDER_ID/status?status=DELIVERED"
```

### Scenario 2: Order Cancellation
```bash
# Create and immediately cancel
ORDER_ID=$(curl -s -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST002","customerEmail":"test2@example.com","productName":"Laptop","quantity":1,"totalAmount":1499}' \
  | jq -r '.id')

curl -X POST "http://localhost:8082/api/orders/$ORDER_ID/cancel"
```

## Health Checks

### Order Service Health
```bash
curl http://localhost:8082/actuator/health
```

### Notification Service Health
```bash
curl http://localhost:8081/actuator/health
```

### Kafka Health (Local)
```bash
docker-compose ps
```

All services should show "Up" status.
