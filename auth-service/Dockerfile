# Step 1: Build stage using Eclipse Temurin JDK 24 on Alpine
FROM eclipse-temurin:24-jdk-alpine AS builder
WORKDIR /app

# Install Maven manually to ensure crisp isolation
RUN apk add --no-cache maven

# 1. Copy the ROOT parent pom.xml
COPY pom.xml ./pom.xml

# 2. Crucial: Install parent POM to local .m2 cache WITHOUT checking child modules (-N)
RUN mvn clean install -N -B

# 3. Copy the shared library module maintaining identical directory tree structure
COPY common-functional/pom.xml ./common-functional/pom.xml
COPY common-functional/src ./common-functional/src
RUN mvn -f ./common-functional/pom.xml clean install -DskipTests -B

# 4. Copy the target auth service module maintaining identical directory tree structure
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY auth-service/src ./auth-service/src

# FIXED: Removed custom repo mapping to prevent dependency mismatch errors
RUN mvn -f ./auth-service/pom.xml clean package -DskipTests -B -Djar.finalName=auth-service


# Step 2: Optimal runtime stage using JRE 24 on alpine for minimal attack surface
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Add a dedicated non-root security group and user for bulletproof container isolation
RUN addgroup -S soaesps && adduser -S auth -G soaesps

# CORRECT PATH MAPPING: Copy the jar from the auth-service module subdirectory
COPY --from=builder /app/auth-service/target/auth-service.jar /app/auth-service.jar

# Enforce secure file ownership before switching user context
RUN chown auth:soaesps /app/auth-service.jar
USER auth

# Expose Tomcat / Web MVC embedded server port
EXPOSE 8083

# Optimize JVM configurations (Using absolute path for stable container run)
ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app/auth-service.jar"]

#docker rm -f auth-app ; docker run -d -p 8083:8083 -e SERVER_PORT=8083 -e EUREKA_ENABLED=false -e SERVER_SSL_ENABLED=false -e APP_RABBITMQ_ENABLED=false -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9094 -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal -e SPRING_R2DBC_URL=r2dbc:postgresql://host.docker.internal:5432/auth_db -e SPRING_R2DBC_USERNAME=postgres -e SPRING_R2DBC_PASSWORD=1 -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/auth_db -e DB_HOST=host.docker.internal -e DB_PORT=5432 -e POSTGRES_DB=auth_db -e POSTGRES_USER=postgres -e POSTGRES_PWD=1 -e CONSUL_HOST=host.docker.internal --name auth-app auth-service:latest --spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration