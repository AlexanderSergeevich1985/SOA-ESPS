# JetDAG Service

JetDAG Service is a high-performance, fully reactive microservice orchestrator designed to compose, validate, and execute dynamic **Directed Acyclic Graphs (DAG)** of tasks. Built on top of **Spring Boot 3 (WebFlux)** and **Temporal.io**, it provides a non-blocking framework to manage distributed workflows with complex topologies, automated error retries, and transactional fallback paths.

---

## Architecture & Request Lifecycle

The system utilizes an asynchronous handoff pattern, splitting responsibility between a reactive REST/DB ingest layer and an event-driven Temporal orchestrator cluster.

```mermaid
graph TD
    %% Styling
    classDef ingest fill:#232f3e,stroke:#333,stroke-width:2px,color:#fff;
    classDef service fill:#1f4e5b,stroke:#333,stroke-width:2px,color:#fff;
    classDef temporal fill:#2d3748,stroke:#333,stroke-width:2px,color:#fff;

    %% Ingestion Layer
    subgraph Ingestion_Layer [1. REST INGESTION LAYER]
        A["HTTP POST /api/v1/dag/run/{id}<br>(OAuth2 JWT Auth)"] --> B[DagTriggerController]
    end
    class B ingest;

    %% Service Layer
    subgraph Service_Layer [2. REACTIVE SERVICE LAYER]
        B --> C[PipelineOrchestratorService]
        C --> D[(PostgreSQL via R2DBC)]
        D -.->|Streams metadata| C
        C --> E{DagValidator<br>DFS Cycle Guard}
        E -->|Cycle Detected| F[Abort & Return HTTP 400]
        E -->|Safe Graph| G[Generate Trace ID & gRPC Handoff]
    end
    class C,E,F,G service;

    %% Temporal Layer
    subgraph Temporal_Cluster [3. TEMPORAL DISTRIBUTED CLUSTER]
        G ==> H[DynamicDagWorkflow<br>The Conductor]
        H --> I[Resolve Root Nodes]
        I --> J[HttpTaskActivities<br>The Executor]
        J --> K[WebClient Async HTTP Call]
        K --> L{doNotCompleteOnReturn}
        L -->|Callback 200 OK| M[complete<br>Advance Downstream]
        L -->|Callback Error/4xx/5xx| N[completeExceptionally<br>Trigger Fallback]
    end
    class H,J,K,L,M,N temporal;
```

---

## Tech Stack

* **Language Runtime:** Java 21+ LTS (or Java 24)
* **Core Framework:** Spring Boot 3.x, Spring WebFlux (Reactive Streams)
* **Security:** Spring Security OAuth2 Resource Server (JWT verification)
* **Orchestration Engine:** Temporal.io Java SDK
* **Database Layer:** Spring Data R2DBC (PostgreSQL asynchronous driver)
* **Utilities:** Project Lombok, Jackson ObjectMapper
* **Containerization:** Docker (Alpine Minimal Footprint Base Layer)

---

## Getting Started

### Prerequisites
* **JDK 21** or **JDK 24** installed locally
* **Docker** & **Docker Compose**
* Access to a running **Temporal Cluster** (`localhost:7233`) and a **PostgreSQL** database.

### Building the Container
To compile the application and packages natively within a standardized environment, run:
```bash
docker build -t jetdag-service:latest .
```

---

## API Reference

### Trigger DAG Execution

* **URL:** `/api/v1/dag/run/{pipelineId}`
* **Method:** `POST`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Parameters:** `pipelineId` (UUID) - The unique configuration key of the target pipeline.

#### Response: `200 OK`
```json
{
  "status": "TRIGGERED",
  "message": "DAG configuration loaded from DB and handed off to Temporal successfully",
  "temporal_workflow_id": "dag-Data_Sync_Pipeline-a1b2c3d4"
}
```

#### Response: `400 Bad Request` (Cycle Found)
```json
{
  "error": "Critical architectural violation: Circular dependency (infinite loop) detected in pipeline [Data_Sync_Pipeline]!"
}
```

---

## Structural Reliability & Fallbacks

1. **Cycle Guard:** The `DagValidator` engine runs a **Three-State Depth-First Search (DFS)** algorithm (`0=UNVISITED`, `1=VISITING`, `2=COMPLETED`). It acts as an immutable structural safeguard, detecting any back-edges or self-referential infinite loops in database configurations prior to workflow execution.
2. **Asynchronous Web Hooks:** Activities leverage `context.doNotCompleteOnReturn()`. This releases all underlying worker threads while waiting for long-running downstream REST service endpoints to reply, providing supreme memory efficiency.
3. **Database Fallbacks:** When a task node fails network retries, the orchestrator inspects the relational definition. If a valid `fallbackTaskCode` parameter is provided, it dynamically hot-swaps execution threads into a contingency recovery loop.