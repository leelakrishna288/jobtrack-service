# jobtrack-service

A Spring Boot REST service for tracking job applications through their funnel. Small on purpose: it exists so the backend claims on my CV have a public implementation behind them, and it is built the way I would build a production service rather than the way a tutorial would.

**Stack:** Java 21 · Spring Boot 3.3 · Spring Data JPA · Spring Security (OAuth2 resource server) · Flyway · PostgreSQL · JUnit 5 · Docker · GitHub Actions

## Run it

```bash
docker compose up --build        # app on :8080, Postgres on :5432
```

Locally without Docker, point it at a Postgres instance and start it:

```bash
JOBTRACK_DB_URL=jdbc:postgresql://localhost:5432/jobtrack \
JOBTRACK_DB_USER=jobtrack JOBTRACK_DB_PASSWORD=jobtrack \
mvn spring-boot:run
```

OpenAPI UI: `/swagger-ui.html`. Health: `/actuator/health`.

## API

| Method | Path | Notes |
|---|---|---|
| POST | `/api/v1/applications` | 201 + `Location`; 409 on a duplicate canonical URL |
| GET | `/api/v1/applications/{id}` | 404 as an RFC 7807 problem |
| GET | `/api/v1/applications?status=SUBMITTED&page=0&size=20` | paged, optional status filter |
| PATCH | `/api/v1/applications/{id}/status` | 409 when the funnel forbids the transition |

Reads need the `jobtrack.read` scope, writes need `jobtrack.write`.

## The decisions worth asking me about

**The status machine lives in the domain, not the controller.** `ApplicationStatus.allowedNext()` owns the funnel, and `JobApplication.moveTo()` enforces it. An invalid transition is refused identically whichever entry point asks — an HTTP call, a batch import, a future message consumer. Putting that rule in the controller would mean re-implementing it for every new caller and eventually disagreeing with itself.

**Invalid transitions are 409, not 500.** `ApiExceptionHandler` maps each domain failure to an RFC 7807 `ProblemDetail`, and the body names the refused transition. A caller should never have to read server logs to learn why a request failed. Validation failures additionally carry a `fields` map.

**Optimistic locking with `@Version`.** Two concurrent status changes on one row would otherwise silently overwrite each other. The loser gets a 409 telling it to re-read and retry, rather than a lost update.

**The unique constraint is the real duplicate guard.** The service also checks `existsByCanonicalUrl` first, but only so the caller gets a useful 409 instead of a constraint-violation stack trace. The database, not the application, is what makes it true under concurrency.

**Flyway with `ddl-auto: validate`.** Schema changes are reviewed migrations. Hibernate is allowed to verify that the entity matches the schema and to do nothing else — `ddl-auto: update` against a real database is how schemas drift.

**Bearer-token only, CSRF disabled deliberately.** There is no session and no cookie to forge. `open-in-view` is off so lazy loading cannot leak into the view layer, and `server.error.include-message: never` keeps internals out of error bodies.

**Tests are slices, not one big context.** The domain machine is a plain unit test, the service uses Mockito, the web layer is `@WebMvcTest` including the 401/403 paths, and persistence is `@DataJpaTest` against H2 so the mapping and the unique constraint are genuinely exercised.

## Scope, honestly

This is a single-aggregate service. It has no event streaming, no service mesh and no multi-region story, and it does not pretend to. What it does show is how I handle the things that actually break enterprise services: transaction boundaries, concurrent updates, schema evolution, error contracts, and authorisation.
