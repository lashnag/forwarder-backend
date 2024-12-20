# Этап сборки
FROM maven:3.9.5-eclipse-temurin-17 as build

WORKDIR /app

COPY . .

RUN sh -c "sleep 30 && mvn jooq-codegen:generate -Pprod"
RUN mvn clean package -DskipTests

# Этап выполнения
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/ForwarderBackend-2.0.0.jar ForwarderBackend.jar

ENTRYPOINT ["sh", "-c", "exec java -jar ForwarderBackend.jar"]