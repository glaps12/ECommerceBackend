# AGENTS.md

## Must-follow constraints
- **Build/runtime**: this module is **Maven + Maven Wrapper** (use `mvnw` / `mvnw.cmd`), **Java 17**, Spring Boot **3.3.x**.
- **API base path**: Spring Data REST is mounted at **`/api`** (`spring.data.rest.base-path=/api`). Do not change unless the task explicitly requires an API-breaking change.
- **Read-only REST for core catalog**: `Product` and `ProductCategory` repositories have **POST/PUT/DELETE disabled** via `MyDataRestConfig`. Do not re-enable write methods unless explicitly requested.

## Validation before finishing
- **Run tests**: `.\mvnw.cmd test` (or `./mvnw test` on non-Windows).
- **Optional fast sanity** (when you changed only wiring/config): `.\mvnw.cmd -DskipTests package`

## Repo-specific conventions
- **Local DB expectation**: default config points at local MySQL `full-stack-ecommerce` with user/pass `ecommerceapp` in `src/main/resources/application.properties`. Treat these as **dev defaults**; do not introduce real credentials into git.

## Important locations (only non-obvious)
- **Spring Data REST exposure + ID exposure**: `src/main/java/com/glaps12/ecommerce/config/MyDataRestConfig.java`
