# spring-crud-api

A RESTful CRUD API for managing a product catalog. Built with Spring Boot 3.5.2 and Java 21, with full Swagger/OpenAPI documentation, profile-based datasource configuration, and full-stack integration tests.

---

## Tech Stack

| Component       | Technology                                      |
|-----------------|-------------------------------------------------|
| Language        | Java 21                                         |
| Framework       | Spring Boot 3.5.2                               |
| Web             | Spring MVC (`spring-boot-starter-web`)          |
| ORM             | Spring Data JPA + Hibernate 6.6                 |
| Validation      | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Dev DB          | H2 in-memory                                    |
| Prod DB         | Oracle (ojdbc11) via `devint` profile           |
| API Docs        | SpringDoc OpenAPI 2.8.8 (Swagger UI)            |
| Build           | Gradle 8.12.1 (Gradle Wrapper)                  |
| Testing         | JUnit 5 + Spring Boot Test + MockMvc            |
| Coverage        | JaCoCo — 80% minimum enforced                   |
| Hot Reload      | Spring DevTools                                 |

---

## Getting Started

### Prerequisites

- JDK 21 (the Gradle wrapper will provision a matching toolchain automatically if needed)
- No local database setup required for development — the default `local` profile uses an in-memory H2 database

### Run the application

```powershell
.\gradlew.bat bootRun
```

The app starts on `http://localhost:8080` with the `local` profile active (H2 in-memory database, seeded with 3 sample products on startup).

To run against the Oracle-backed `devint` profile instead:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=devint"
```

### Run tests and generate a coverage report

```powershell
.\gradlew.bat test
```

This runs the full JUnit test suite and generates a JaCoCo coverage report at:

```
build/reports/jacoco/test/html/index.html
```

A minimum of 80% coverage is enforced via `jacocoTestCoverageVerification`. As of the latest test run, `ProductController`, `ProductService`, and `Product` are all at **100%** instructions/lines/methods coverage.

### Build a JAR

```powershell
.\gradlew.bat build
```

---

## API Endpoints

Base path: `/api/products`

| Method | Path           | Description           | Success Status |
|--------|----------------|-----------------------|----------------|
| GET    | `/`            | Get all products       | 200            |
| GET    | `/{id}`        | Get product by ID      | 200 / 404      |
| POST   | `/`            | Create product          | 201            |
| PUT    | `/{id}`        | Update product          | 200 / 404      |
| DELETE | `/{id}`        | Delete product          | 204 / 404      |

### Product fields

| Field         | Type    | Validation        |
|---------------|---------|--------------------|
| `id`          | Long    | Auto-generated      |
| `name`        | String  | Required (not blank) |
| `description` | String  | Optional            |
| `price`       | Double  | Required, positive  |
| `quantity`    | Integer | Required, positive  |

Validation failures return HTTP 400 with field-level error messages. Requests for a non-existent product ID return HTTP 404.

---

## API Documentation (Swagger UI)

Once the application is running, interactive API documentation is available at:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`

---

## Configuration Profiles

| Profile   | Database           | DDL auto    | H2 Console |
|-----------|--------------------|-------------|------------|
| `local`   | H2 in-memory       | create-drop | enabled    |
| `devint`  | Oracle (port 1521) | update      | disabled   |

The `local` profile is active by default. The H2 console (when enabled) is available at `http://localhost:8080/h2-console`.

---

## Project Structure

```
src/main/java/com/example/crud/
├── CrudApplication.java
├── config/
│   └── OpenApiConfig.java
├── controller/
│   └── ProductController.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model/
│   └── Product.java
├── repository/
│   └── ProductRepository.java
└── service/
    └── ProductService.java

src/main/resources/
├── application.properties          ← common config, activates 'local' profile
├── application-local.properties    ← H2 in-memory, H2 console enabled
├── application-devint.properties   ← Oracle DB, H2 console disabled
└── data.sql                        ← seed data (3 products on startup)

src/test/java/com/example/crud/
└── ProductControllerTest.java
```

The application follows a standard 3-layer architecture: `ProductController` (web layer) → `ProductService` (business logic) → `ProductRepository` (data access) → Database (H2 or Oracle).
