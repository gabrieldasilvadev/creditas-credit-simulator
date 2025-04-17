FROM gradle:8.7.0-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/container/build/libs/container-*-all.jar app.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "app.jar"]
