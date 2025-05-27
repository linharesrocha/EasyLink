# EasyLink: Encurtador de URLs com Microsserviços

![Logo do EasyLink](https://raw.githubusercontent.com/linharesrocha/EasyLink/refs/heads/main/easylink-service/src/main/resources/static/logo.png

Bem-vindo ao EasyLink! Este projeto é um encurtador de URLs robusto e escalável, construído com Java, Spring Boot e uma arquitetura de microsserviços. Ele foi desenvolvido como um projeto de portfólio para demonstrar a aplicação de diversas tecnologias e boas práticas de desenvolvimento backend.

## Funcionalidades Principais

* **Encurtamento de URL:** Cria links curtos e únicos.
* **Redirecionamento:** Direciona de forma eficiente para a URL original.
* **Gerenciamento por Usuário:**
    * Registro e Login de usuários (autenticação via JWT).
    * Associação de links a usuários.
    * Edição e Deleção de links (apenas pelo proprietário).
* **QR Code:** Geração de QR Codes para os links encurtados.
* **Analytics de Cliques:** Coleta assíncrona de dados de cliques via Kafka e MongoDB.
* **Otimizações:** Cache com Redis para redirecionamentos rápidos e Circuit Breaker para resiliência.

## Arquitetura do Sistema

O EasyLink utiliza uma arquitetura de microsserviços para modularidade e escalabilidade:

1.  **`discovery-service` (Netflix Eureka):** Para registro e descoberta dos outros serviços.
2.  **`api-gateway` (Spring Cloud Gateway):** Ponto de entrada único, roteamento e validação de JWT.
3.  **`user-service`:** Responsável pelo registro, login e gerenciamento de usuários (usa PostgreSQL).
4.  **`easylink-service`:** Núcleo do encurtador, lida com URLs, QR Codes e eventos de clique (usa PostgreSQL, Redis, Kafka).
5.  **`analytics-service`:** Consome eventos de clique do Kafka e armazena no MongoDB.

**Infraestrutura de Suporte (via Docker Compose):**
* PostgreSQL (para `user-service` e `easylink-service`)
* MongoDB (para `analytics-service`)
* Apache Kafka & Zookeeper (para analytics)
* Redis (para cache)

## Tecnologias Chave

* **Backend:** Java 21, Spring Boot 3.x, Spring Cloud, Spring Security
* **Persistência:** PostgreSQL, MongoDB, Redis
* **Mensageria:** Apache Kafka
* **Containerização:** Docker, Docker Compose
* **Documentação API:** OpenAPI 3 (Swagger)
* **Testes:** JUnit 5, Mockito

## Como Executar Localmente

1.  **Pré-requisitos:**
    * Git
    * Docker e Docker Compose instalados e rodando.

2.  **Clone o Repositório:**
    ```bash
    git clone https://github.com/linharesrocha/EasyLink.git
    cd EasyLink
    ```

3.  **Inicie a Aplicação:**
    Na raiz do projeto, onde está o `docker-compose.yml`, execute:
    ```bash
    docker-compose up --build -d
    ```
    * O comando `--build` é importante na primeira vez para construir as imagens Docker dos seus serviços.
    * Aguarde alguns minutos para todos os serviços iniciarem e se registrarem.

4.  **Acessando os Serviços:**
    * **API Gateway (Ponto de Entrada Principal):** `http://localhost:8080`
    * **Eureka Dashboard:** `http://localhost:8761` (para ver os serviços registrados)
    * **Swagger UI (Documentação das APIs):**
        * User Service: `http://localhost:8083/swagger-ui.html`
        * EasyLink Service: `http://localhost:8081/swagger-ui.html`

5.  **Principais Endpoints (via API Gateway `http://localhost:8080`):**
    * **Registro:** `POST /api/v1/auth/register`
        * Corpo: `{"username": "seu_usuario", "password": "sua_senha"}`
    * **Login:** `POST /api/v1/auth/login`
        * Corpo: `{"username": "seu_usuario", "password": "sua_senha"}`
        * Resposta: Retorna um token JWT.
    * **Encurtar URL:** `POST /api/v1/urls`
        * Header: `Authorization: Bearer SEU_TOKEN_JWT`
        * Corpo: `{"originalUrl": "https://sua-url-longa.com"}`
    * **Redirecionar:** `GET /{shortKey}` (Ex: `http://localhost:8080/aBc1DeFg`)
    * **QR Code:** `GET /api/v1/urls/{shortKey}/qr`
    * **Editar URL:** `PUT /api/v1/urls/{shortKey}` (Header de autorização + corpo com `newOriginalUrl`)
    * **Deletar URL:** `DELETE /api/v1/urls/{shortKey}` (Header de autorização)

## Próximos Passos e Evoluções

Este projeto é uma base sólida. Algumas evoluções futuras planejadas incluem:
* Implementação de URLs customizadas.
* Links com prazo de validade e proteção por senha.
* Geração de `shortKey` mais robusta (ex: Base62).
* Versionamento de banco de dados com Flyway.
* Pipeline de CI/CD e implantação na nuvem.