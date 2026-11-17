# ==============================================================================
# Build Stage
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy build descriptor and wrapper first to leverage Docker layer caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Make wrapper executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy project source code
COPY src src

# Build the application artifact
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Runtime Stage
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Security: Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose standard application port
EXPOSE 8080

# Healthcheck to verify service availability
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/certifai/v3/api-docs || exit 1

# Container-optimized JVM execution
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "/app/app.jar"]
