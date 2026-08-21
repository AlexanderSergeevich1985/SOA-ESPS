### Infrastructure & Message Brokers
*   **Config Server (`config`)** — Centralized configuration management for all microservices using Spring Cloud Config.
*   **Eureka Service (`eureka-service`)** — Service discovery server for dynamic registration and routing.
*   **API Gateway (`api-gateway`)** — Single entry point handling routing, security, and request logging.
*   **Apache Kafka (`kafka-starter`)** — Core broker used for asynchronous event-driven system communication.
*   **RabbitMQ** — Message broker utilized by specific domain services (e.g., `notes-service`) for direct queue routing.

### Core Orchestration
*   **Camunda Engine (`camunda`)** — BPMN workflow engine that orchestrates complex distributed transactions and triggers compensating steps in case of failures (Saga Pattern).

### Business Microservices
*   **Auth Service (`auth-service`)** — Handles user authentication, authorization, and secure JWT token issuance (`auth.soa-esps.ru`).
*   **Payments Service (`payments-service`)** — Handles financial transactions and billing states within the distributed flow.
*   **Profile Service (`profile-service`)** — Manages user profiles, account metadata, and personal settings.
*   **Scheduler Service (`scheduler-service`)** — Handles cron tasks, time-based events, and automated routine triggers.
*   **Quotes Service (`quotes-service`)** — Service for fetching, storing, or processing data quotes.
*   **Data Process (`data-process`)** — Service responsible for processing, validating, and transforming incoming business data.
*   **Documents Service (`documents-service`)** — Manages related file workflows, storage, and report generation.
*   **Notes Service (`notes-service`)** — Global notification and system messaging service.
