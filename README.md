# EasyLink: Encurtador de URLs

<img src="https://raw.githubusercontent.com/linharesrocha/EasyLink/refs/heads/main/easylink-service/src/main/resources/static/logo.png" alt="Logo do EasyLink" width="300"/>

Bem-vindo ao EasyLink! Este projeto é um encurtador de URLs robusto e escalável ;)

## Tecnologias Chave

* **Backend:** Java 21, Spring Boot 3.x, Spring Cloud (Eureka, Gateway), Spring Security
* **Persistência:** PostgreSQL, MongoDB, Redis
* **Mensageria:** Apache Kafka
* **Versionamento de Banco de Dados:** Flyway
* **Containerização:** Docker, Docker Compose
* **Documentação API:** OpenAPI 3 (Swagger)
* **Testes:** JUnit 5, Mockito
* **CI/CD:** GitHub Actions
* **Monitoramento & Observabilidade:** Prometheus, Grafana, Micrometer

## Funcionalidades Principais

* **Encurtamento de URL:**
    * Cria links curtos e únicos.
    * Suporte a **URLs Personalizadas**, permitindo que os usuários escolham seus próprios apelidos para os links.
    * Opção de definir **Prazo de Validade** para os links.
    * Geração de QR Codes para os links encurtados.


* **Redirecionamento:**
    * Direciona de forma eficiente para a URL original.
    * Verifica o prazo de validade antes do redirecionamento.


* **Gerenciamento por Usuário:**
    * Registro e Login de usuários (autenticação via JWT).
    * Associação de links a usuários.
    * Edição e Deleção de links (apenas pelo proprietário).


* **Analytics de Cliques:** Coleta assíncrona de dados de cliques via Kafka e armazenamento em MongoDB.


* **Robustez e Otimizações:**
    * Cache com Redis para redirecionamentos rápidos.
    * Circuit Breaker no API Gateway para resiliência.
    * Rate Limiting no API Gateway baseado em IP.
    * Tratamento de erros aprimorado com exceções customizadas.
    * Logging aprimorado em toda a aplicação.


* **Monitoramento de Desempenho e Saúde:**
    * Coleta de métricas dos microsserviços via Actuator e Micrometer.
    * Visualização e alerta com Prometheus e Grafana.

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