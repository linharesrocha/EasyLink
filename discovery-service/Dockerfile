FROM maven:3.9-eclipse-temurin-21 AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o pom.xml primeiro para aproveitar o cache de dependências do Docker
COPY pom.xml .

# Baixa todas as dependências (se o pom.xml não mudou, esta camada será cacheada)
RUN mvn dependency:go-offline

# Copia todo o código fonte do projeto
COPY src ./src

# Compila a aplicação e gera o .jar
RUN mvn package -DskipTests

# Estágio 2: Criação da imagem final, menor e otimizada para execução
FROM eclipse-temurin:21-jre-jammy

# Define o diretório de trabalho
WORKDIR /app

# Copia o .jar gerado no estágio de build para a imagem final
# O .jar estará em target/discovery-service-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/discovery-service-0.0.1-SNAPSHOT.jar discovery-service.jar

# Expõe a porta
EXPOSE 8761

# Comando para executar a aplicação quando o contêiner iniciar
ENTRYPOINT ["java", "-jar", "discovery-service.jar"]