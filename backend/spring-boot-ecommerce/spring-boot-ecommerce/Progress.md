# Progress.md

Single source of truth for **what was done**, **what’s in progress**, and **what’s next** for this app.

## How to update
- Add a new entry under **Log** for each meaningful change/PR.
- Keep **Now / Next / Later** short (move items as they progress).
- Prefer checklists; link to files/paths when helpful.

## Now (in progress)
- [x] **CORS centralization**: Replace per-repository `@CrossOrigin("http://localhost:4200")` annotations on `ProductRepository` and `ProductCategoryRepository` with a single global CORS config in `MyDataRestConfig` (or a dedicated `WebMvcConfigurer`). Allows configurable allowed origins.
- [ ] **Externalize DB credentials**: Move `spring.datasource.username/password` out of `application.properties` into environment variables or a `application-local.properties` (gitignored) to avoid committing credentials.

## Next (planned soon)
- [ ] **Checkout domain**: Add `Customer`, `Address`, `Order`, `OrderItem` entities + repositories. Create a `CheckoutController` REST endpoint (`POST /api/checkout/purchase`) to receive orders from the frontend.
- [ ] **Schema management**: Add Flyway migrations for `product_category`, `product`, and the new checkout tables — ensures repeatable DB setup.
- [ ] **Dev DB workflow**: Add `docker-compose.yml` with MySQL 8 + optional phpMyAdmin matching the expected `full-stack-ecommerce` schema and `ecommerceapp` user.
- [ ] **Spring Data REST projections**: Add `@Projection` interfaces for `Product` (avoid leaking `ProductCategory.products` set back-reference in product listings).
- [ ] **Actuator hardening**: Configure `management.endpoints.web.exposure.include` to expose only `health` and `info` by default (currently exposes all via `spring-boot-starter-actuator`).
- [ ] **PATCH support for cart/order**: Once checkout exists, consider enabling `PATCH` for order status updates (admin use case).

## Later (ideas / backlog)
- [ ] **Spring Security (JWT)**: We have local Registration, Login, and Email Verification built (`AuthController`). Next, swap `.anyRequest().permitAll()` in `SecurityConfig` to require actual JWT tokens and implement role-based auth (ADMIN for product CRUD, USER for checkout).
- [ ] **Payment integration**: Integrate Stripe (or PayPal) for payment processing in the checkout flow.
- [ ] **Product image upload**: Admin endpoint to upload/update product images (store in S3 or local filesystem).
- [ ] **Product sorting API**: Expose `Sort` parameter support in `findByCategoryId` and `findByNameContaining` (frontend will send `sort=unitPrice,asc`).
- [ ] **API documentation**: Add `springdoc-openapi` for auto-generated Swagger UI at `/swagger-ui.html`.
- [ ] **Testing**: Repository integration tests with Testcontainers (MySQL) + controller smoke tests.
- [ ] **CI pipeline**: GitHub Actions or GitLab CI running `mvn test` + build on every push.
- [ ] **Rate limiting**: Add rate limiting on checkout/search endpoints to prevent abuse.
- [ ] **Caching**: Add Spring Cache (`@Cacheable`) on product categories (rarely change) for better performance.

## Log (reverse chronological)

### 2026-04-12
- **Added**: Extended `User` entity with `phoneNumber` and `birthDate` fields to support richer user profiles.
- **Added**: Implemented profile synchronization endpoint in `AuthController` to return full `User` details (including surname/lastName) for frontend initialization.
- **Improved**: Enhanced `updateUser` logic to handle optional password updates and correctly persist new profile fields.
- **Fixed**: Resolved data mapping issues where user attributes were inconsistent between login and registration responses.
- **Refined**: Synchronized frontend logic to restrict phone numbers to 10 digits starting with 5 (per +90 region conventions) and implemented validation to reject leading zeros.

### 2026-04-09
- **Added**: Enhanced `UpdateUserRequest` DTO and `AuthController.updateUser` logic to support partial profile updates (firstName, lastName) and secure password rotation.
- **Added**: Security-first password update flow requiring `currentPassword` verification via `passwordEncoder.matches()` before allowing a `newPassword` to be persisted. Returns `UNAUTHORIZED (401)` for incorrect current passwords.
- **Fixed**: Robust validation for profile names and email uniqueness checks during user setting updates.

### 2026-04-08
- **Added**: Centralized CORS config via `SecurityConfig` to replace scattered cross-origin annotations.
- **Added**: Initial `User` entity, `UserRepository`, and local `AuthController` to handle User Registration, Login, and Email Verification logic. 
- **Fixed**: Application startup failure due to missing SMTP configuration by injecting mock `spring.mail` properties in `application.properties`. Built `EmailService` to gracefully degrade and print verification codes to STDOUT instead of crashing.
- **Fixed**: Hardened `AuthController.java` to explicitly prevent duplicate email signups with reliable 409 responses and robust punctuation for all error/success texts. Protected initial `firstName` strings from being polluted with email name-splits.

### 2026-04-04
- **Fixed**: Replaced deprecated `org.hibernate.dialect.MySQL8Dialect` with `org.hibernate.dialect.MySQLDialect` in `application.properties` (deprecated in Hibernate 6.x / Spring Boot 3.3.x).
- **Fixed**: Raw generic types in `MyDataRestConfig.exposeIds()` — `List<Class>` → `List<Class<?>>`, `EntityType TempEntities` → `EntityType<?> tempEntity` (eliminates unchecked compiler warnings).

### 2026-04-03
- **Added**: `AGENTS.md` (repo-specific workflow constraints and validation commands).
