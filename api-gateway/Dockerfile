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

COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

# Porta definida no application.yml do api-gateway
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]