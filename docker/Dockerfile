# ── Build stage ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache dependencies first
COPY pom.xml .
COPY core/pom.xml core/
COPY scheduler/pom.xml scheduler/
COPY notification/pom.xml notification/
COPY api/pom.xml api/
RUN mvn dependency:go-offline -B --no-transfer-progress

# Build the project
COPY . .
RUN mvn clean package -pl api -am -DskipTests --no-transfer-progress

# ── Runtime stage ──────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S aethertrack && adduser -S aethertrack -G aethertrack
USER aethertrack

COPY --from=builder /build/api/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
