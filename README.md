# SOA-ESPS — Modular Enterprise Platform

**SOA-ESPS** is a service-oriented enterprise suite that unifies core business
operations — identity, financial transactions, quotations, document management,
communications and process orchestration — on a single event-driven backbone.
On top of the operational core, an IoT-telemetry and GenAI **intelligence layer**
turns raw device data into proactive, user-facing recommendations delivered
through the platform's own communication channels.

The goal is an open, modular alternative to monolithic ERP systems: every
business capability is an independently deployable microservice with its own
data store, joined by shared infrastructure contracts rather than a shared
database.

## Platform Domains

| Domain | Services | Capabilities |
|---|---|---|
| Identity & Access | `auth-service`, `profile-service` | Authentication/authorization, JWT & OAuth2 token lifecycle, user accounts, device registry |
| Financial Operations | `payments-service`, `quotes-service` | Payment transactions, billing states, quotations and pricing data |
| Documents & Records | `documents-service` | File workflows, storage, report generation |
| Communications | `notes-service` | Omnichannel notifications (e-mail, push), per-user channel preferences; the delivery endpoint for system events and GenAI recommendations |
| Process Orchestration | `camunda`, `scheduler-service` | BPMN orchestration of cross-domain transactions (Saga pattern with compensating actions), cron and time-based triggers |
| Intelligence & Recommendations | `msg-process`, `aggregator` | Streaming ML scoring (Triton gRPC), real-time anomaly triage (Pekko Cluster Sharding), time-series analytics (TimescaleDB), GenAI diagnostic reports (Temporal + LangChain4j) |
| Platform Core | `config`, `eureka-service`, `api-gateway`, `kafka-starter`, `core`, `common-functional`, `certs` | Centralized configuration, service discovery, unified entry point, shared Kafka auto-configuration, shared domain models, TLS material |

## Architecture Backbone

Three principles hold the platform together:

1. **Event-driven backbone.** Apache Kafka is the nervous system: domain
   services publish business events, and cross-domain consumers react without
   synchronous coupling.
2. **Durable orchestration.** Cross-service transactions run as Camunda BPMN
   sagas with compensating actions; long-running analytical batches run on
   Temporal.io with crash-safe progress and retry policies.
3. **Intelligence as a first-class citizen.** Operational and telemetry data
   feed ML scoring and GenAI pipelines; the resulting recommendations re-enter
   the platform through the same communication channels as regular business
   events — the user experiences one coherent conversation with the platform.

### Intelligence layer data flow

```
IoT devices
   |  raw telemetry (Kafka: telemetry-raw)
   v
[msg-process] -- Triton Inference Server (gRPC, ML scoring)
   |  scored events (Kafka: ml-metrics)     malformed/failed -> (Kafka: frames-dlq)
   +-------------------------------+
   v hot path                      v cold path
[Pekko Cluster Sharding]       [batch inserts]
 DeviceActor xN (in-memory        |
 50-event window,                 v
 anomaly >= 0.9 / rising trend) [TimescaleDB: ml_metrics + ml_metrics_hourly]
   |                               |
   v                               v
[DeviceAdvisor - LangChain4j]   [Temporal: ReportFanOutWorkflow, every 6h]
   |                               |
   +-----------+-------------------+
               v  recommendations (Kafka: user-advice)
        [notes-service] -> push / e-mail -> user
```

## Kafka Topic Map (intelligence layer)

| Topic | Producer | Consumers | Payload |
|---|---|---|---|
| `telemetry-raw` | device fleet / edge | `msg-process` | raw sensor frames |
| `ml-metrics` | `msg-process` | `aggregator` | `MlMetricEvent` (value, anomalyScore, predictedState) |
| `frames-dlq` | `msg-process` router | ops / replay tooling | malformed or failed frames |
| `user-advice` | `aggregator` | `notes-service` | `UserAdviceEvent` (anomaly alert / periodic summary) |

Business domains publish their own domain events following the shared
`kafka-starter` conventions; the full event catalog lives in `docs/`.

## Repository Layout

```
SOA-ESPS/
├── api-gateway/          # Platform core: unified entry point, routing, security
├── config/               # Platform core: Spring Cloud Config server
├── eureka-service/       # Platform core: service discovery
├── kafka-starter/        # Platform core: shared Kafka auto-configuration
├── core/                 # Platform core: shared domain models (com.soaesps.core.DataModels.*)
├── common-functional/    # Platform core: shared libraries and contracts
├── certs/                # Platform core: TLS material, generate-certs.bat
├── auth-service/         # Identity: authentication, authorization, JWT issuance
├── profile-service/      # Identity: user accounts, device registry metadata
├── payments-service/     # Finance: payment transactions, billing states
├── quotes-service/       # Finance: quotations and pricing data
├── documents-service/    # Records: file workflows, storage, report generation
├── notes-service/        # Communications: e-mail, push, channel preferences
├── scheduler-service/    # Orchestration: cron tasks, time-based triggers
├── camunda/              # Orchestration: BPMN sagas with compensation
├── msg-process/          # Intelligence: streaming ML scoring (Spring Integration + Triton)
├── aggregator/           # Intelligence: Pekko sharding + TimescaleDB + Temporal + LangChain4j
├── data-process/         # Business data validation and transformation
├── ClientApps/           # Client applications
├── docs/                 # Design docs, ADRs, event catalog
├── docker-compose.yml    # Local infrastructure stack
└── pom.xml
```

## Technology Stack

* **Runtime:** Java 25, Spring Boot 3.4.x, Spring Cloud (Config, Eureka, Gateway, Stream)
* **Messaging:** Apache Kafka (primary backbone), RabbitMQ (legacy integration contours)
* **Real-time state:** Apache Pekko Cluster Sharding (in-memory device actors)
* **Time-series storage:** TimescaleDB (hypertables + continuous aggregates)
* **Orchestration:** Camunda (business sagas), Temporal.io (durable analytical batches)
* **GenAI:** LangChain4j with OpenAI-compatible endpoints (Groq / OpenAI) or local Ollama;
  RAG over domain knowledge bases; pluggable token estimators per provider
* **ML inference:** Triton Inference Server (gRPC) — matrix math stays off the JVM
* **Polyglot persistence:** PostgreSQL/TimescaleDB, Redis, MongoDB (+ R2DBC reactive paths)

## Vision & Roadmap

* Unified billing & ledger module closing the financial contour.
* Cross-domain reporting suite on top of operational and time-series stores.
* Procurement / marketplace module completing the ERP-class feature set.
* Public module API & SDK so third-party verticals can plug in the way SAP
  modules plug into an ERP core.

## Getting Started

Prerequisites: JDK 25, Maven 3.9+, Docker & Compose.

```bash
# 1. TLS material for local mTLS
./generate-certs.bat

# 2. Infrastructure: Kafka, TimescaleDB, Redis, MongoDB, Temporal
docker compose up -d kafka timescaledb redis mongo temporal

# 3. Bootstrap order: config -> eureka -> gateway -> domain services
mvn -pl config,eureka-service spring-boot:run

# 4. GenAI secrets (any OpenAI-compatible endpoint; or local Ollama)
export LLM_API_KEY=***
export LLM_BASE_URL=https://api.groq.com/openai/v1
export LLM_MODEL=qwen2.5-32b
```

Build:

```bash
mvn clean install -DskipTests
```

## Testing

* Service slices run on the `TEST` profile: H2 for JPA slices, embedded Kafka
  (listeners auto-startup disabled), GreenMail for e-mail, `@DataR2dbcTest` for
  reactive repositories.
* Intelligence-layer integration tests use `@EmbeddedKafka` and mock
  transformers — no Triton or external brokers required.
* On JDK 25, Mockito's inline mock-maker needs the agent attached explicitly:
  pass `-javaagent:<path-to>/mockito-core-<version>.jar` via Surefire `argLine`
  or IDE run configuration, per Mockito documentation.

## Documentation

Design notes, ADRs, the domain event catalog and per-service deep dives live
in [`docs/`](docs/).