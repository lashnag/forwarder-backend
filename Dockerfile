# Этап сборки
FROM maven:3.9.5-eclipse-temurin-17 as build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Этап выполнения
FROM openjdk:17-jdk-slim

WORKDIR /app
RUN apt-get update && apt-get install -y python3 python3-pip && apt-get clean

COPY --from=build /app/target/ForwarderBackend-2.0.0.jar ForwarderBackend.jar

ENTRYPOINT ["sh", "-c", "sleep 30; exec java -jar ForwarderBackend.jar"]