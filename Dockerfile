# Этап сборки
FROM maven:3.9.5-eclipse-temurin-17 as build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Этап выполнения
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/ForwarderBackend-1.0.1.jar ForwarderBackend.jar

ENTRYPOINT ["sh", "-c", "sleep 30; exec java -jar ForwarderBackend.jar"]