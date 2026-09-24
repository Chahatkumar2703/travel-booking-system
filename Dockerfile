# ==========================================================
# VoyageQuest - Online Travel Booking System Dockerfile
# Optimized for cloud deployment on Render, Railway, or VPS
# ==========================================================

# Stage 1: Builder stage using Eclipse Temurin JDK
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy JAR dependencies and source code
COPY lib/ lib/
COPY src/ src/

# Create output directory and compile all source files
RUN mkdir -p bin && \
    javac -d bin -cp "lib/mysql-connector-j-8.3.0.jar:src" \
    src/util/*.java \
    src/model/*.java \
    src/dao/*.java \
    src/service/*.java \
    src/view/*.java \
    src/Main.java \
    src/WebServer.java

# Copy configuration properties into classpath
RUN cp src/db.properties bin/ || true

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy compiled bytecode and runtime library from builder
COPY --from=builder /app/bin bin/
COPY --from=builder /app/lib lib/

# Copy web assets and database scripts
COPY web/ web/
COPY database/ database/

# Default port (Render & Railway inject PORT automatically)
ENV PORT=8080
EXPOSE 8080

# Launch embedded WebServer
CMD ["java", "-cp", "bin:lib/mysql-connector-j-8.3.0.jar", "WebServer"]
