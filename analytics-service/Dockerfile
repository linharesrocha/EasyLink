# Estágio 1: Build da aplicação com Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

# Estágio 2: Criação da imagem final
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/analytics-service-0.0.1-SNAPSHOT.jar analytics-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "analytics-service.jar"]