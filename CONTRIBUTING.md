# Contributing to SOA-ESPS

Thanks for investing your time in the platform. This document defines how we
build, test, review, and merge — so that a suite of 20+ services stays
coherent instead of decaying into a distributed monolith.

## 1. Ground Rules

* Be respectful and pragmatic; critique code, not people.
* **English only inside the repository**: comments, javadoc, identifiers,
  log messages, SQL aliases, test names and `@DisplayName` values, docs.
  Commit messages follow the same rule.
* No drive-by refactors: if you see unrelated mess, open an issue instead of
  bundling it into your PR.
* Big changes (new module, new broker, contract change, new orchestration
  engine) start as an issue or an ADR discussion, not as code.

## 2. Getting Started

Prerequisites: JDK 25, Maven 3.9+, Docker & Compose. Optional: local Ollama
for GenAI features without cloud keys.

```bash
# 1. TLS material for local mTLS
./generate-certs.bat

# 2. Infrastructure: Kafka, TimescaleDB, Redis, MongoDB, Temporal
docker compose up -d kafka timescaledb redis mongo temporal

# 3. Build everything
mvn clean install -DskipTests

# 4. Bootstrap order: config -> eureka -> gateway -> your service
mvn -pl config,eureka-service spring-boot:run

# 5. GenAI secrets (any OpenAI-compatible endpoint, or local Ollama)
export LLM_API_KEY=***
export LLM_BASE_URL=http://localhost:11434/v1
export LLM_MODEL=qwen2.5-7b-instruct
```

Verify your setup by running the tests of the module you touched:

```bash
mvn test -pl aggregator
```

The module/domain map lives in the root `README.md`; read it before your
first change.

## 3. Branches, Commits, Pull Requests

* Branches: `feature/<module>-<short-desc>`, `fix/<module>-<short-desc>`,
  `chore/...`, `docs/...`.
* Commits: Conventional Commits with module scope —
  `feat(aggregator): add warm-up from TimescaleDB on actor activation`.
* PRs: small and focused; one logical change per PR. The description must
  answer: what, why, how it was tested. Link the issue.
* Every PR needs a green build, green tests for touched modules, and at
  least one approval from a module owner.

## 4. Coding Conventions

### General

* Java 25, Spring Boot 3.4.x. Prefer `record` for DTOs and Kafka contracts.
* Constructor injection only; field `@Autowired` in production code is banned.
* Configuration via properties (`@Value`/`@ConfigurationProperties`);
  no hardcoded hosts, topics, or model names.
* SLF4J logging; `System.out` is banned.

### Persistence

* Business domains may use Spring Data JPA; shared entities live in `core`
  (`com.soaesps.core.DataModels.*`), service-private entities stay in-module.
* The intelligence layer (`aggregator`) uses **pure JDBC** (`JdbcTemplate`,
  HikariCP, batch inserts) against TimescaleDB. No ORM on the ingest path.
* Reactive paths use R2DBC; never block reactor threads — blocking work goes
  to `Schedulers.boundedElastic()`.

### Messaging

* Kafka is the backbone. Topic names live in `Topics` constants; payloads are
  immutable records with explicit Jackson annotations (`@JsonFormat` for
  `Instant`).
* Schema evolution is **additive only**: never rename or remove fields of a
  published contract without an ADR and a consumer migration plan.
* Spring Cloud Stream: functional beans only (`Consumer`, `Supplier`,
  `StreamBridge`). `@EnableBinding` / `@Input` / `@Output` are banned
  (removed in SCS 4.x).
* Consumers commit offsets only after successful persistence
  (at-least-once) and must be idempotent.

### Actors & Orchestration

* Apache Pekko **typed Java DSL** only. Akka imports and classic actors
  (`UntypedAbstractActor`, `akka.*`) are banned.
* Java DSL getters: `ctx.getSystem()`, `ctx.getSelf()`, `ctx.getLog()`.
  Scala-style `ctx.system()` does not compile here — do not paste it.
* Actors never block: JDBC/LLM calls go through injected deps
  (e.g. `DeviceDeps`) on dedicated schedulers; hot state stays in memory and
  must be reconstructible from the DB (passivation-safe).
* Cross-service transactions: Camunda sagas with compensation.
  Long-running analytical batches: Temporal workflows with retry policies.
  Bare `@Scheduled` is acceptable only behind a property gate
  (`aggregator.report.mode`).

### GenAI

* All LLM access goes through LangChain4j AI-services behind interfaces;
  provider switching is a properties-only change (auto-detected
  `TokenCountEstimator` included).
* Every LLM call must have a timeout, bounded concurrency, and a
  deterministic fallback. An LLM outage must never kill a scheduler,
  an actor, or the ingest loop.

## 5. Testing

* JUnit 5 + AssertJ + Mockito. JUnit 4 API is banned
  (no `@RunWith`, no `org.junit.Test`).
* Use `@MockitoBean` / `@MockitoSpyBean`; `@MockBean` is deprecated since
  Boot 3.4 and must not appear in new code.
* Prefer slice tests (`@DataJpaTest`, `@DataR2dbcTest`, `@WebMvcTest`);
  full `@SpringBootTest` only when integration is the point of the test.
* JPA slices that touch `core` entities require
  `@EntityScan("com.soaesps.core.DataModels")`.
* The `TEST` profile provides H2 and embedded Kafka with
  `spring.kafka.listener.auto-startup=false`; tests must never require live
  brokers, Triton, or LLM endpoints — mock transformers and AI-services.
* JDK 25 note: Mockito's inline mock-maker needs the agent attached
  explicitly. CI passes `-javaagent:<repo>/org/mockito/mockito-core/<ver>/mockito-core-<ver>.jar`
  via Surefire `argLine`; configure the same in your IDE run configurations.
* Naming: `*Test` for unit tests, `*IntegrationTest` for tests with
  infrastructure (embedded Kafka, H2, GreenMail).
* New behavior ships with tests in the same PR.

## 6. Contracts & Documentation

* The Kafka topic map in the root `README.md` and the event catalog in
  `docs/` are the source of truth; update both when you touch a contract.
* Architectural decisions are recorded as ADRs in `docs/adr/NNNN-title.md`.
* The module list in `README.md` must always match the `<modules>` section
  of the root `pom.xml`.

## 7. Pull Request Checklist

* [ ] `mvn clean install` passes on JDK 25
* [ ] Tests green for all touched modules
* [ ] No non-English text in code, comments, logs, or test names
* [ ] No new Akka / SCS-imperative / JUnit 4 / `@MockBean` usage
* [ ] Kafka contracts changed additively; README/docs updated if so
* [ ] LLM calls have timeouts and fallbacks; no blocking on reactor or
  actor threads
* [ ] Commit messages follow Conventional Commits

## 8. Issues & Feature Proposals

* Bug reports: module, expected vs actual, minimal reproduction steps,
  relevant logs (stack traces as text, not screenshots).
* Feature proposals: start with an issue describing the business capability
  and the domain it belongs to; maintainers will route it to the right
  module or to an ADR discussion.

## 9. License

By contributing you agree that your contributions are licensed under the
same license as the project.

## 10. How to Help the Project? ⭐

The best support for the author is your attention! If this project saved you
time or turned out to be useful:

* **Give it a Star** at the top of this page — it boosts the project in
  rankings and helps other developers discover it.
* **Tell your friends** or colleagues who might find this tool useful too.