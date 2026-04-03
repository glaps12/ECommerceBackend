# Progress.md

Single source of truth for **what was done**, **what’s in progress**, and **what’s next** for this app.

## How to update
- Add a new entry under **Log** for each meaningful change/PR.
- Keep **Now / Next / Later** short (move items as they progress).
- Prefer checklists; link to files/paths when helpful.

## Now (in progress)
- [ ] **Baseline run**: confirm app starts against local MySQL `full-stack-ecommerce` and `GET /api/products`, `GET /api/product-category` work.
- [ ] **Stop committing real credentials**: move DB user/pass out of `src/main/resources/application.properties` (use env vars + a local-only profile file).
- [ ] **CORS**: replace hardcoded `@CrossOrigin("http://localhost:4200")` with a single config-driven CORS policy (keep `/api` base path).
- [ ] **API contract check**: keep `Product`/`ProductCategory` repositories **read-only** (POST/PUT/DELETE disabled) unless the feature explicitly needs writes.

## Next (planned soon)
- [ ] **Schema management**: add Flyway (or Liquibase) and create initial migration(s) for `product_category` + `product`.
- [ ] **Dev DB workflow**: add `docker-compose.yml` for MySQL (and optional phpMyAdmin) matching the expected schema/user.
- [ ] **DTO / projections** (if needed): avoid accidentally exposing entity internals via Spring Data REST; add projections for product listings/details.
- [ ] **Search/pagination UX**: confirm repository search endpoints for `findByCategoryId` and `findByNameContaining` are used by the frontend and documented.
- [ ] **Observability hardening**: configure actuator endpoints exposure for dev vs prod (don’t expose everything by default).

## Later (ideas / backlog)
- [ ] **Security**: add Spring Security (authn/authz) before enabling any write endpoints.
- [ ] **Checkout domain**: add customers, orders, order-items, addresses, and payment integration (only after catalog + schema migrations are stable).
- [ ] **API documentation**: add OpenAPI/Swagger for any custom endpoints (Spring Data REST links alone may not be enough).
- [ ] **Testing**: add repository tests with Testcontainers (MySQL) and a minimal API smoke test suite.
- [ ] **CI**: add a pipeline that runs `mvn test` (and optionally a formatting/static analysis step if adopted).

## Log (reverse chronological)

### 2026-04-03
- **Added**: `AGENTS.md` (repo-specific workflow constraints and validation commands).

