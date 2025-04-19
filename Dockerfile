# Etapa de build
FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /app

COPY . .
RUN gradle :container:bootJar --no-daemon -x test

# Etapa de runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/container/build/libs/app.jar app.jar

EXPOSE 7000
CMD ["java", "-jar", "app.jar"]
