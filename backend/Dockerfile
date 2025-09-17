# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create user for running application
RUN addgroup -g 1000 spring && \
    adduser -D -u 1000 -G spring spring

# Install curl for health check
RUN apk add --no-cache curl

# Copy JAR from builder
COPY --from=builder /app/target/markdown-notes-*.jar app.jar

# Create directories for uploads and logs
RUN mkdir -p /data/markdown-notes/uploads /logs && \
    chown -R spring:spring /data /logs

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]