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

# 4. Copy the target notes service module maintaining identical directory tree structure
COPY notes-service/pom.xml ./notes-service/pom.xml
COPY notes-service/src ./notes-service/src

# FIXED: Removed custom repo mapping to prevent dependency mismatch errors
RUN mvn -f ./notes-service/pom.xml clean package -DskipTests -B -Djar.finalName=notes-service


# Step 2: Optimal runtime stage using JRE 24 on alpine for minimal attack surface
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Add a dedicated non-root security group and user for bulletproof container isolation
RUN addgroup -S soaesps && adduser -S notes -G soaesps

# CORRECT PATH MAPPING: Copy the jar from the notes-service module subdirectory
COPY --from=builder /app/notes-service/target/notes-service.jar /app/notes-service.jar

# Enforce secure file ownership before switching user context
RUN chown notes:soaesps /app/notes-service.jar
USER notes

# Expose Tomcat / Web MVC embedded server port
EXPOSE 8083

# Optimize JVM configurations (Using absolute path for stable container run)
ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app/notes-service.jar"]

#docker run --rm -u root --entrypoint sh notes-service:latest -c "apk add --no-cache tree && tree -la /app"


#docker run -d -p 8082:8082 -e APP_RABBITMQ_ENABLED=false -e DB_HOST=host.docker.internal -e DB_PORT=5432 -e POSTGRES_DB=profile_db -e POSTGRES_USER=postgres -e POSTGRES_PWD=******* --name profile-app notes-service:latest

#docker rm -f notes-app; docker run -d -p 8083:8083 -e SERVER_SSL_ENABLED=false -e APP_RABBITMQ_ENABLED=false --name notes-app notes-service:latest --spring.kafka.bootstrap-servers=host.docker.internal:9094 --server.port=8082 --spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/profile_db --spring.datasource.username=postgres --spring.datasource.password=*