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

# Terminal 2: Notification Service (runs on port 8083)
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

## Quick Reference

### Essential Commands
```bash
# Start all infrastructure
docker-compose up -d

# Build project (with SDKMAN)
source "$HOME/.sdkman/bin/sdkman-init.sh"
mvn clean install

# Run services (in separate terminals)
cd order-service && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
cd notification-service && mvn spring-boot:run

# Test API
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST001","customerEmail":"test@example.com","productName":"MacBook Pro","quantity":1,"totalAmount":2499.99}'

# View Kafka UI
open http://localhost:8080

# Stop everything
docker-compose down
```

### Monitoring
- **Kafka UI**: http://localhost:8080 - View topics, messages, consumer groups
- **H2 Console**: http://localhost:8082/h2-console - View order database
  - JDBC URL: `jdbc:h2:mem:orderdb`
  - Username: `sa`
  - Password: (empty)
- **Schema Registry**: http://localhost:8081/subjects - View registered Avro schemas

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

## Port Assignments

- **8080**: Kafka UI (monitoring and management)
- **8081**: Schema Registry
- **8082**: Order Service REST API
- **8083**: Notification Service
- **9092**: Kafka broker (external)
- **5432**: PostgreSQL

## Notes

- **Kafka KRaft Mode**: This tutorial uses KRaft mode (Kafka without Zookeeper), the modern recommended approach for Kafka 2.8+
- **H2 Database**: Order Service uses H2 in-memory database by default. Switch to PostgreSQL for production (config in `application.yml`)
- **Port Configuration**: Notification Service runs on 8083 (not 8081) to avoid conflicts with Schema Registry

## Troubleshooting

### Common Issues and Fixes

**1. Kafka Container Won't Start (macOS Docker)**

If Kafka fails with permission errors on volume writes:
```bash
# Symptom: "Permission denied" in Kafka logs
docker-compose logs kafka | grep "Permission denied"

# Fix: Add user configuration to docker-compose.yml
# Under the kafka service, add:
kafka:
  user: "0:0"  # Run as root for macOS Docker volume permissions

# Then restart:
docker-compose down
docker volume rm spring_kafka-data
docker-compose up -d
```

**2. Schema Registry Timeout on First Request**

The first API call may timeout when registering the Avro schema:
```bash
# Symptom: "Register operation timed out; error code: 50002"

# This is normal behavior - just retry the request
# Subsequent calls will succeed once schema is registered
curl -X POST http://localhost:8082/api/orders -H "Content-Type: application/json" -d '{...}'
```

**3. Service Won't Start - Port Already in Use**

```bash
# Check if ports are in use
lsof -i :8082  # Order Service
lsof -i :8083  # Notification Service
lsof -i :8080  # Kafka UI

# If order-service doesn't read port from application.yml:
cd order-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

**4. Maven Not Found**

```bash
# Ensure Maven is available (installed via SDKMAN)
source "$HOME/.sdkman/bin/sdkman-init.sh"
mvn -version

# Clean rebuild
mvn clean install -U
```

**5. Schema Registry Configuration Missing**

If you see "Could not resolve placeholder 'spring.kafka.properties.schema.registry.url'":
```yaml
# Ensure both services have this in application.yml:
spring:
  kafka:
    properties:
      schema.registry.url: ${SCHEMA_REGISTRY_URL:http://localhost:8081}
```

**6. Check Docker Services**

```bash
# View all running services
docker-compose ps

# Check logs for specific service
docker-compose logs kafka
docker-compose logs schema-registry

# Restart all services
docker-compose restart
```

## Next Steps

1. Explore the code in `order-service/` and `notification-service/`
2. Read `docs/SETUP.md` for detailed environment setup
3. Try the examples in `docs/TESTING.md`
4. Experiment with Kafka UI at http://localhost:8080
