# Spring Boot Microservices Tutorial - Getting Started

## 🎯 What You'll Learn

This tutorial teaches you to build **production-ready microservices** using Spring Boot with:

1. **REST API Service** (Order Service)
   - REST endpoints with Spring Web
   - Database integration with Spring Data JPA
   - Publishing events to Kafka
   - Avro serialization

2. **Kafka Consumer Service** (Notification Service)
   - Consuming Kafka messages
   - OAuth/SASL authentication
   - Avro deserialization with Schema Registry
   - Error handling and retry logic

## 📁 Project Structure

```
spring-kafka-tutorial/
├── README.md                    # Main documentation
├── pom.xml                      # Parent POM (multi-module)
├── docker-compose.yml           # Kafka, Schema Registry, PostgreSQL
│
├── docs/                        # Tutorial documentation
│   ├── GET_STARTED.md          # This file
│   ├── SETUP.md                # Setup with SDKMAN
│   └── TESTING.md              # API testing examples
│
├── order-service/              # REST API + Kafka Producer
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/order/
│       │   │   ├── OrderServiceApplication.java
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   ├── model/
│       │   │   └── kafka/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── avro/order-event.avsc
│       └── test/java/
│
└── notification-service/       # Kafka Consumer
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/example/notification/
        │   │   ├── NotificationServiceApplication.java
        │   │   ├── kafka/
        │   │   └── service/
        │   └── resources/
        │       ├── application.yml
        │       └── avro/order-event.avsc
        └── test/java/
```

## 🚀 Quick Start (5 minutes)

### Prerequisites
- Java 17+ and Maven installed (see [SETUP.md](SETUP.md) for SDKMAN installation)
- Docker running

### 1. Start Local Kafka (KRaft mode - no Zookeeper!)
```bash
docker-compose up -d
```

### 2. Build All Services
```bash
# From project root
mvn clean install
```

### 3. Start Order Service
```bash
cd order-service
mvn spring-boot:run
```
*Runs on port 8082*

### 4. Start Notification Service (new terminal)
```bash
cd notification-service
mvn spring-boot:run
```
*Runs on port 8081*

### 5. Create an Order
```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "customerEmail": "test@example.com",
    "productName": "MacBook Pro",
    "quantity": 1,
    "totalAmount": 2499.99
  }'
```

### 6. Check Logs
- **Order Service**: See "Order event published successfully"
- **Notification Service**: See "EMAIL: Order Confirmation"

**Congratulations!** You just ran a complete microservices flow with Kafka! 🎉

## 📚 Learning Path

### Day 1: Setup & REST API
1. Read [SETUP.md](SETUP.md) and install prerequisites with SDKMAN
2. Start Order Service locally
3. Test REST endpoints (see [TESTING.md](TESTING.md))
4. Understand the code flow:
   - Controller → Service → Repository → Database

### Day 2: Kafka Producer
1. Start local Kafka with Docker Compose
2. Study `order-service/src/main/java/com/example/order/kafka/KafkaProducerConfig.java`
3. Understand Avro schema in `order-service/src/main/resources/avro/order-event.avsc`
4. See how events are published in `OrderEventPublisher.java`
5. Monitor Kafka topics with Kafka UI at http://localhost:8080

### Day 3: Kafka Consumer
1. Start Notification Service
2. Study `notification-service/src/main/java/com/example/notification/kafka/KafkaConsumerConfig.java`
3. Understand consumer logic in `OrderEventListener.java`
4. Test manual acknowledgment
5. Trigger errors to see retry logic

### Day 4: Advanced Topics (Optional)
1. Try Confluent Cloud setup (see [SETUP.md](SETUP.md))
2. Configure OAuth/API keys
3. Test with cloud Kafka
4. Explore monitoring in Confluent Cloud console

## 🔧 What Makes This Production-Ready?

### Order Service Features
✅ Input validation with `@Valid`
✅ Error handling with `@ExceptionHandler`
✅ Transactional database operations
✅ Reliable Kafka publishing with callbacks
✅ Avro schema for message versioning
✅ Health checks (Spring Actuator)

### Notification Service Features
✅ Manual acknowledgment for message reliability
✅ Error handling and retry logic
✅ Dead Letter Queue (DLQ) support
✅ Consumer group for scalability
✅ OAuth/SASL authentication
✅ Avro deserialization with Schema Registry

## 💡 Tips for Success

1. **Start Simple**: Run locally with Docker first
2. **Check Logs**: Most issues show up in logs
3. **Test Incrementally**: Test each component separately
4. **Use Kafka UI**: Access at http://localhost:8080 to see topics and messages
5. **Clean Build**: If issues occur, run `mvn clean install`

## 🐛 Common Issues & Solutions

**Issue**: Avro classes not found
**Solution**: Run `mvn clean compile` to generate them

**Issue**: Kafka connection error
**Solution**: Check Docker: `docker-compose ps`

**Issue**: Port 8082 already in use
**Solution**: `lsof -i :8082` then `kill -9 <PID>`

**Issue**: Maven not found
**Solution**: Run `source "$HOME/.sdkman/bin/sdkman-init.sh"`

See [SETUP.md](SETUP.md) troubleshooting section for more!

## 📖 Documentation Files

- **GET_STARTED.md** ← You are here
- **[SETUP.md](SETUP.md)** - Installation and configuration with SDKMAN
- **[TESTING.md](TESTING.md)** - API examples and test scenarios

## 🎯 Next Steps After Tutorial

1. **Add Features**:
   - Update order endpoint
   - Add payment processing
   - Implement order history
   - Add more event types

2. **Add Tests**:
   - Unit tests with JUnit
   - Integration tests with TestContainers
   - Kafka tests with EmbeddedKafka

3. **Production Deployment**:
   - Containerize with Docker
   - Deploy to Kubernetes
   - Set up monitoring (Prometheus, Grafana)
   - Configure log aggregation (ELK stack)

4. **Advanced Topics**:
   - Circuit breakers (Resilience4j)
   - Service discovery (Eureka)
   - API Gateway (Spring Cloud Gateway)
   - Distributed tracing (Sleuth + Zipkin)

## 🌟 What You Built

By completing this tutorial, you've created:
1. A **REST API microservice** with database
2. An **event-driven architecture** with Kafka
3. **Schema versioning** with Avro
4. **Reliable message processing** with manual ack
5. **Production authentication** with OAuth/SASL
6. **Error handling** with DLQ pattern

This is the foundation for any microservices architecture! 🚀

---

**Ready to start?** → Check [SETUP.md](SETUP.md) for installation, then build and run!
