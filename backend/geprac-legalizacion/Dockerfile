# ── Etapa 1: compilar ──────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Las dependencias primero: si solo cambia el código,
# Docker reutiliza esta capa y la construcción es mucho más rápida
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ── Etapa 2: ejecutar ──────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Nunca ejecutar como root
RUN addgroup -S geprac && adduser -S geprac -G geprac
USER geprac

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]