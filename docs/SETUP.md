# Setup Guide

## Prerequisites Installation

### 1. Install Java 17+
```bash
# Check Java version
java -version

# Install Java 17 (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-17-jdk

# Install Java 17 (macOS with Homebrew)
brew install openjdk@17
```

### 2. Install Maven
```bash
# Check Maven version
mvn -version

# Install Maven (Ubuntu/Debian)
sudo apt install maven

# Install Maven (macOS with Homebrew)
brew install maven
```

### 3. Install using SDKMAN (Cross-platform - Recommended for Java version management)

SDKMAN is a tool for managing parallel versions of multiple Software Development Kits on Unix systems.

#### Install SDKMAN
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash

# Initialize SDKMAN in current shell
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify SDKMAN installation
sdk version
```

#### Install Java 17 with SDKMAN
```bash
# List available Java versions
sdk list java

# Install Java 17 (Temurin - Eclipse Adoptium, recommended)
sdk install java 17.0.9-tem

# Or install Oracle Java 17
# sdk install java 17.0.9-oracle

# Set Java 17 as default version
sdk default java 17.0.9-tem

# Verify Java installation
java -version
```

#### Install Maven with SDKMAN
```bash
# Install latest Maven
sdk install maven

# Or install specific version
# sdk install maven 3.9.6

# Verify Maven installation
mvn -version
```

**Benefits of SDKMAN:**
- Easy switching between Java versions: `sdk use java 17.0.9-tem`
- Clean uninstall: `sdk uninstall java 17.0.9-tem`
- Works on macOS, Linux, and WSL on Windows
- Manages multiple JVM tools (Gradle, Kotlin, Scala, etc.)

## Local Development Setup

### Option 1: Local Kafka with Docker

#### Start Kafka with Docker Compose

Create `docker-compose.yml` in the project root:

```yaml
version: '3.8'

services:
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    hostname: kafka
    container_name: kafka
    ports:
      - "9092:9092"
      - "9093:9093"
    environment:
      # KRaft settings (Kafka without Zookeeper)
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:9093'
      KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER'
      
      # Listeners
      KAFKA_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://0.0.0.0:9092,CONTROLLER://kafka:9093'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
      
      # Other settings
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_LOG_DIRS: '/tmp/kraft-combined-logs'
      CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'

  schema-registry:
    image: confluentinc/cp-schema-registry:7.5.0
    hostname: schema-registry
    container_name: schema-registry
    depends_on:
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: 'kafka:29092'
      SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081
```

**Note**: This uses KRaft mode (Kafka without Zookeeper), which is the modern approach in Kafka 2.8+

Start the services:
```bash
docker-compose up -d
```

Verify services are running:
```bash
docker-compose ps
```

### Option 2: Confluent Cloud (Production-like)

#### Create Confluent Cloud Account
1. Go to https://confluent.cloud
2. Sign up for free trial (gets $400 credit)
3. Create a Kafka cluster

#### Get Credentials
1. Create API keys for Kafka cluster
2. Create API keys for Schema Registry
3. Note down:
   - Bootstrap servers URL
   - API Key & Secret for Kafka
   - Schema Registry URL
   - API Key & Secret for Schema Registry

#### Configure OAuth (Optional, for advanced use)
1. In Confluent Cloud Console, go to Security → OAuth
2. Configure OAuth provider (Auth0, Okta, etc.)
3. Get Client ID, Client Secret, and Token URL

## Configuration

### For Local Kafka (Docker)

No changes needed - services use default configuration:
- Kafka: `localhost:9092`
- Schema Registry: `http://localhost:8081`

### For Confluent Cloud

#### Order Service Configuration

Edit `order-service/src/main/resources/application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: pkc-xxxxx.us-east-1.aws.confluent.cloud:9092
    
    producer:
      properties:
        schema.registry.url: https://psrc-xxxxx.us-east-2.aws.confluent.cloud
        basic.auth.credentials.source: USER_INFO
        basic.auth.user.info: ${SCHEMA_REGISTRY_API_KEY}:${SCHEMA_REGISTRY_API_SECRET}
    
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: PLAIN
      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username='${KAFKA_API_KEY}' password='${KAFKA_API_SECRET}';
```

Or set environment variables:
```bash
export KAFKA_BOOTSTRAP_SERVERS="pkc-xxxxx.us-east-1.aws.confluent.cloud:9092"
export SCHEMA_REGISTRY_URL="https://psrc-xxxxx.us-east-2.aws.confluent.cloud"
export KAFKA_API_KEY="your-kafka-api-key"
export KAFKA_API_SECRET="your-kafka-api-secret"
export SCHEMA_REGISTRY_API_KEY="your-sr-api-key"
export SCHEMA_REGISTRY_API_SECRET="your-sr-api-secret"
```

#### Notification Service Configuration

Same changes apply to `notification-service/src/main/resources/application.yml`

### For OAuth Authentication (Advanced)

Set environment variables:
```bash
export KAFKA_SECURITY_PROTOCOL="SASL_SSL"
export KAFKA_SASL_MECHANISM="OAUTHBEARER"
export KAFKA_SASL_JAAS_CONFIG="org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId='your-client-id' clientSecret='your-client-secret' scope='your-scope';"
export OAUTH_CLIENT_ID="your-oauth-client-id"
export OAUTH_CLIENT_SECRET="your-oauth-client-secret"
```

## Building the Projects

### Generate Avro Classes

Both services need to generate Java classes from Avro schemas:

```bash
# Order Service
cd order-service
mvn clean compile

# Notification Service
cd ../notification-service
mvn clean compile
```

This generates Java classes in `target/generated-sources/avro/`

### Build JARs

```bash
# Order Service
cd order-service
mvn clean package

# Notification Service
cd ../notification-service
mvn clean package
```

## Running the Services

### Order Service
```bash
cd order-service
./mvnw spring-boot:run

# Or run the JAR
java -jar target/order-service-1.0.0.jar
```

Service starts on: http://localhost:8080

### Notification Service
```bash
cd notification-service
./mvnw spring-boot:run

# Or run the JAR
java -jar target/notification-service-1.0.0.jar
```

Service starts on: http://localhost:8081

## Verify Setup

### Check Order Service
```bash
curl http://localhost:8080/actuator/health
```

### Check Notification Service
```bash
curl http://localhost:8081/actuator/health
```

### Create Test Topic (if using local Kafka)
```bash
# Enter Kafka container
docker exec -it <kafka-container-id> bash

# Create topic
kafka-topics --create \
  --topic order-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# List topics
kafka-topics --list --bootstrap-server localhost:9092
```

## Troubleshooting

### Issue: Cannot connect to Kafka
- Check if Kafka is running: `docker-compose ps`
- Verify bootstrap servers configuration
- Check firewall rules

### Issue: Schema Registry errors
- Verify Schema Registry URL
- Check authentication credentials
- Ensure schema is registered

### Issue: Avro classes not generated
- Run `mvn clean compile` again
- Check Avro schema syntax
- Verify Maven plugin configuration

### Issue: Consumer not receiving messages
- Check consumer group ID
- Verify topic name matches
- Check Kafka logs: `docker-compose logs kafka`
- Ensure consumer is subscribed to correct topic

## Next Steps

1. Review [TESTING.md](TESTING.md) for testing guidelines
2. Check service logs for any errors
3. Try the example API calls in TESTING.md
4. Monitor Kafka topics and consumer groups
