# Subscription Backend Monorepo

This repository contains backend services for the Subnex subscription platform. It is structured as a multi-service monorepo. At present, the implemented service is `subscription-service` (Spring Boot + MongoDB); other service folders are placeholders for future development.

## Services

- subscription-service: Manages subscription plans and user subscriptions. Exposes REST APIs with OpenAPI/Swagger docs. Uses MongoDB for persistence. Defaults to port 8082.
- auth-service: Placeholder.
- notification-service: Placeholder.
- payment-service: Placeholder.
- common-lib: Placeholder for shared code.

## Tech Stack

- Java 17 (toolchain)
- Spring Boot 3.2
- MongoDB (via `spring-boot-starter-data-mongodb`)
- Gradle (wrapper included)
- OpenAPI via `springdoc-openapi`
- Lombok (annotations)
- `.env` support via `dotenv-java`

## Prerequisites

- JDK 17
- A MongoDB instance/URI (local or cloud)
- Git

## Configuration

`subscription-service` reads configuration from environment variables and `application.yml`:

- `MONGODB_URI` (required): Mongo connection string, e.g. `mongodb://localhost:27017` or Atlas URI.
- `MONGODB_DB` (optional): Database name, defaults to `subscription_db`.
- `PORT` (optional): HTTP port, defaults to `8082`.

The service registers an `ApplicationContextInitializer` to load variables (e.g., from a `.env` file) early during startup: see [subscription-service/src/main/resources/META-INF/spring.factories](subscription-service/src/main/resources/META-INF/spring.factories) and [subscription-service/src/main/java/com/subnex/subscription/config/DotenvInitializer.java](subscription-service/src/main/java/com/subnex/subscription/config/DotenvInitializer.java).

## Run Locally (subscription-service)

1. Set configuration (example using a local MongoDB):

```bash
export MONGODB_URI="mongodb://localhost:27017"
export MONGODB_DB="subscription_db"
# export PORT=8082  # optional
```

Alternatively, you can create a `.env` file inside `subscription-service/` with the same keys; it will be loaded at startup.

2. Start the service:

```bash
cd subscription-service
./gradlew bootRun
```

3. Open API docs:

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/api-docs

## Build & Test

From inside `subscription-service/`:

```bash
./gradlew clean build
./gradlew test
```

The fat JAR will be located at `subscription-service/build/libs/`. To run it directly:

```bash
export MONGODB_URI="<your-uri>"
java -jar build/libs/subscription-service-0.0.1-SNAPSHOT.jar
```

## Logging

MongoDB operations logging is set to DEBUG for `MongoTemplate` in [subscription-service/src/main/resources/application.yml](subscription-service/src/main/resources/application.yml). Adjust `logging.level` there or via environment if needed.

## Project Structure (abridged)

```
subscription_be/
├─ auth-service/                 # placeholder
├─ common-lib/                   # placeholder
├─ notification-service/         # placeholder
├─ payment-service/              # placeholder
└─ subscription-service/
	├─ build.gradle
	├─ settings.gradle
	└─ src/main/
		├─ java/com/subnex/subscription/
		│  ├─ config/ (Dotenv, Security, OpenAPI)
		│  ├─ controller/ (REST endpoints)
		│  ├─ dto/, model/, repository/, service/
		│  └─ SubscriptionServiceApplication.java
		└─ resources/
			├─ application.yml
			└─ META-INF/spring.factories
```

## Contributing

- Use Java 17.
- Keep service boundaries clear; shared code goes in `common-lib` (when implemented).
- Follow Spring Boot conventions and prefer configuration via environment variables.

## License

Proprietary/private unless stated otherwise by the project owner.
