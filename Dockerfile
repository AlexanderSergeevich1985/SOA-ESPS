# =============================================================================
# Stage 1: Build production-ready jar package
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source files and compile the artifact
COPY src ./src
RUN mvn clean package -DskipTests

# =============================================================================
# Stage 2: Ultra lightweight runtime production stage
# =============================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Secure container compliance: strictly avoid root execution privileges
RUN addgroup -S soagroup && adduser -S soauser -G soagroup
USER soauser

# Copy compiled artifact from the compilation builder container
COPY --from=builder /build/target/*.jar app.jar

# Explicitly set fallback system property for secure logging frameworks
ENTRYPOINT ["java", "-jar", "app.jar"]