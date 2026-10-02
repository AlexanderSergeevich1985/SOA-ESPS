# Step 1: Build stage using Eclipse Temurin JDK 21 on Alpine for lightweight footprint
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven manually to ensure crisp isolation
RUN apk add --no-cache maven

# Copy the parent pom from the root directory to satisfy multi-module parent resolution
COPY ../pom.xml ./parent-pom.xml

# Copy the module configuration and source code
COPY pom.xml .
COPY src ./src

# Build the specific module using the parent definition if required, skipping tests for fast deploy
RUN mvn clean package -DskipTests -B

# Step 2: Optimal runtime stage using JRE 21 on alpine for minimal attack surface
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a dedicated non-root security group and user for bulletproof container isolation
RUN addgroup -S jetdag && adduser -S jetdag -G jetdag
USER jetdag

# Copy the fat compiled executable from the builder layer
COPY --from=builder /app/target/jetdag-service-*.jar jetdag-service.jar

# Expose WebFlux reactive Netty port
EXPOSE 8181

# Optimize JVM configurations for cloud environments and virtual thread allocations
ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "jetdag-service.jar"]