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

# 4. Copy the target profile service module maintaining identical directory tree structure
COPY profile-service/pom.xml ./profile-service/pom.xml
COPY profile-service/src ./profile-service/src
RUN mvn -f ./profile-service/pom.xml clean package -DskipTests -B -Djar.finalName=profile-service


# Step 2: Optimal runtime stage using JRE 24 on alpine for minimal attack surface
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Add a dedicated non-root security group and user for bulletproof container isolation
RUN addgroup -S soaesps && adduser -S profile -G soaesps

# Copy the fat compiled executable from the builder layer
#COPY --from=builder /app/target/profile-service/profile-service-*.jar profile-service.jar
COPY --from=builder /app/profile-service/target/profile-service.jar /app/profile-service.jar
RUN chown profile:soaesps /app/profile-service.jar
USER profile

# Expose Tomcat / Web MVC embedded server port
EXPOSE 8082

# Optimize JVM configurations for cloud environments and virtual thread allocations
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "profile-service.jar"]

#docker run -d -p 8082:8082 -e APP_RABBITMQ_ENABLED=false -e DB_HOST=host.docker.internal -e DB_PORT=5432 -e POSTGRES_DB=profile_db -e POSTGRES_USER=postgres -e POSTGRES_PWD=******* --name profile-app profile-service:latest

#docker rm -f profile-app; docker run -d -p 8082:8082 -e SERVER_SSL_ENABLED=false -e APP_RABBITMQ_ENABLED=false --name profile-app profile-service:latest --spring.kafka.bootstrap-servers=host.docker.internal:9094 --server.port=8082 --spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/profile_db --spring.datasource.username=postgres --spring.datasource.password=*****