# Spring Boot Microservices Tutorial
## Production-Ready Microservices with Kafka

This tutorial demonstrates building production-ready microservices using Spring Boot 3.2 and Apache Kafka:

1. **Order Service** - REST API microservice with database and Kafka producer
2. **Notification Service** - Kafka consumer with OAuth & Avro serialization

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT / API USER                        │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                │ HTTP REST API
                                │ (POST /api/orders)
                                │
                    ┌───────────▼────────────┐
                    │   ORDER SERVICE        │
                    │   (Spring Boot)        │
                    │                        │
                    │  • REST Controller     │
                    │  • Order Service       │
                    │  • JPA Repository      │
                    │  • Kafka Producer      │
                    └───────┬───────┬────────┘
                            │       │
                     Save   │       │ Publish Event
                            │       │ (Avro)
                            │       │
                    ┌───────▼───────▼────────┐
                    │     PostgreSQL/H2      │
                    └────────────────────────┘
                            │
                            │
                    ┌───────▼────────────────┐
                    │  CONFLUENT KAFKA       │
                    │                        │
                    │  Topic: order-events   │
                    │  • Avro Serialization  │
                    │  • Schema Registry     │
                    │  • OAuth/SASL Auth     │
                    └───────┬────────────────┘
                            │
                            │ Consume Event
                            │ (Avro)
                            │
                ┌───────────▼─────────────┐
                │ NOTIFICATION SERVICE    │
                │ (Spring Boot)           │
                │                         │
                │  • Kafka Consumer       │
                │  • Manual Ack           │
                │  • Retry Logic          │
                │  • DLQ Handler          │
                └───────────┬─────────────┘
                            │
                            │ Send Notification
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
  ┌─────▼─────┐      ┌──────▼──────┐     ┌─────▼─────┐
  │   Email   │      │     SMS     │     │   Push    │
  │  Service  │      │   Service   │     │  Notif.   │
  └───────────┘      └─────────────┘     └───────────┘
```

## Technologies Used

- **Spring Boot 3.2.x** (Java 17+)
- **Spring Data JPA** (Database layer)
- **Spring Kafka** (Kafka integration)
- **Confluent Kafka 7.5.0** with KRaft mode (no Zookeeper!)
- **Schema Registry** (Avro schema management)
- **Apache Avro** (Message serialization)
- **OAuth 2.0 / SASL** (Kafka authentication)
- **PostgreSQL / H2** (Database)
- **Maven** (Build tool)

## Project Structure

```
spring-kafka-tutorial/
├── README.md                    # This file
├── pom.xml                      # Parent POM (multi-module)
├── docker-compose.yml           # Kafka, Schema Registry, PostgreSQL
│
├── docs/                        # Tutorial documentation
│   ├── GET_STARTED.md          # Quick start guide
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

## Prerequisites

- **Java 17+** - Install via SDKMAN (recommended) or Homebrew/apt
- **Maven 3.6+** - Install via SDKMAN (recommended) or Homebrew/apt
- **Docker** - For running Kafka locally
- **Confluent Cloud account** (optional - for cloud setup)

See [docs/SETUP.md](docs/SETUP.md) for detailed installation instructions with SDKMAN.

## Quick Start

### 1. Start Infrastructure
```bash
# Start Kafka, Schema Registry, PostgreSQL
docker-compose up -d
```

### 2. Build Services
```bash
# Build all services
mvn clean install

# Or build individually
cd order-service && mvn clean package
cd notification-service && mvn clean package
```

### 3. Run Services
```bash
# Terminal 1: Order Service (runs on port 8082)
cd order-service
mvn spring-boot:run

# Terminal 2: Notification Service (runs on port 8081)
cd notification-service
mvn spring-boot:run
```

### 4. Test the API
```bash
# Create an order
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

Check the logs - you should see:
- **Order Service**: "Order event published successfully"
- **Notification Service**: "EMAIL: Order Confirmation"

## What You'll Learn

1. **REST API Design** with Spring Boot
2. **Database Operations** with Spring Data JPA
3. **Kafka Producer** configuration and event publishing
4. **Kafka Consumer** with consumer groups
5. **Avro Schema Registry** integration
6. **OAuth 2.0** for Kafka authentication
7. **Error handling** and retry mechanisms with DLQ
8. **Production patterns** for microservices

## Documentation

- **[docs/GET_STARTED.md](docs/GET_STARTED.md)** - Complete getting started guide
- **[docs/SETUP.md](docs/SETUP.md)** - Detailed setup with SDKMAN
- **[docs/TESTING.md](docs/TESTING.md)** - API testing examples and scenarios

## Production Features

### Order Service
✅ Input validation with `@Valid`
✅ Error handling with `@ExceptionHandler`
✅ Transactional database operations
✅ Async Kafka publishing with callbacks
✅ Avro schema for message versioning

### Notification Service
✅ Manual acknowledgment for reliability
✅ Error handling and retry logic
✅ Dead Letter Queue (DLQ) support
✅ Consumer groups for scalability
✅ OAuth/SASL authentication

## Notes

- **Kafka KRaft Mode**: This tutorial uses KRaft mode (Kafka without Zookeeper), the modern recommended approach for Kafka 2.8+
- **H2 Database**: Order Service uses H2 in-memory database by default. Switch to PostgreSQL for production (config in `application.yml`)
- **Ports**: Order Service runs on 8082, Notification Service on 8081, Kafka UI on 8080

## Troubleshooting

**Service won't start?**
```bash
# Check if port is in use
lsof -i :8082
lsof -i :8081

# Check Docker services
docker-compose ps
docker-compose logs kafka
```

**Maven build fails?**
```bash
# Ensure Maven is available
source "$HOME/.sdkman/bin/sdkman-init.sh"
mvn -version

# Clean rebuild
mvn clean install -U
```

## Next Steps

1. Explore the code in `order-service/` and `notification-service/`
2. Read `docs/SETUP.md` for detailed environment setup
3. Try the examples in `docs/TESTING.md`
4. Experiment with Kafka UI at http://localhost:8080
