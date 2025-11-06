# Evaluation — Phase 3: Version 0.1 — Basic functionality and Docker

**Evaluation date:** November 4, 2025  
**Project:** SPIRITBLADE — League of Legends Stats Tracker

---

## Backend

### ✅ Security (Spring Security)
Status: COMPLETED

- Spring Security configured in `SecurityConfiguration.java`
- JWT implemented via `JwtTokenProvider` and authentication filters
- Role-based protection (USER, ADMIN) applied to endpoints
- Password encoding with BCrypt
- CSRF protection configured

Key files: `SecurityConfiguration.java`, `JwtTokenProvider.java`, `UserLoginService.java`

### ✅ Secure communication (HTTPS, port 443)
Status: COMPLETED

- HTTPS configured in `application.properties`
- Server port set to 443 (`server.port=443`)
- SSL enabled (`server.ssl.enabled=true`)
- Keystore included in resources (`keystore.jks`)

Config file: `backend/src/main/resources/application.properties`

### ✅ Image storage (MinIO via AWS S3 SDK)
Status: COMPLETED

- MinIO integration implemented in `MinioStorageService.java`
- Configuration example in `application.properties`:

```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.bucket-name=spiritblade-uploads
```

- Avatar management provided by `UserAvatarService`
- Dependency: `aws-java-sdk-s3` v1.12.772

Key files: `MinioStorageService.java`, `UserAvatarService.java`, `FileController.java`

### ✅ Layered architecture
Status: COMPLETED

- Controllers (6 REST controllers): `LoginRestController`, `UserController`, `SummonerController`, `DashboardController`, `AdminController`, `FileController`
- Services (7): `UserService`, `RiotService`, `DataDragonService`, `MatchAnalysisService`, `UserAvatarService`, `MinioStorageService`, etc.
- Repositories (4): `UserModelRepository`, `SummonerRepository`, `MatchRepository`, `MatchEntityRepository`

Clear separation of responsibilities across layers.

### ✅ API paths use `/api/v1`
Status: COMPLETED

Examples:

```java
@RequestMapping("/api/v1/auth")      // LoginRestController
@RequestMapping("/api/v1/users")     // UserController
@RequestMapping("/api/v1/summoners") // SummonerController
@RequestMapping("/api/v1/dashboard") // DashboardController
@RequestMapping("/api/v1/admin")     // AdminController
@RequestMapping("/api/v1/files")     // FileController
```

### ✅ REST API good practices
Status: COMPLETED

- Correct HTTP methods: GET, POST, PUT, DELETE, PATCH
- Resource-oriented URLs (`/users/{id}`, `/summoners/{name}`)
- Proper HTTP status codes (200, 201, 204, 400, 401, 404)
- `Content-Type: application/json` used consistently
- `ResponseEntity` used for responses

### ✅ Parametrized searches
Status: COMPLETED

- Users: `GET /api/v1/users?search={query}&page={n}&size={m}`
- Filters: `role`, `active`, `search`
- Summoner search: `GET /api/v1/summoners/search/{name}`

Reference: `UserController.java` (lines ~65–90)

### ✅ Pagination
Status: COMPLETED

Pagination implemented with Spring Data, for example:

```java
@GetMapping
public ResponseEntity<Page<UserDTO>> getAllUsers(
  @RequestParam(defaultValue = "0") int page,
  @RequestParam(defaultValue = "10") int size) {
  Pageable pageable = PageRequest.of(page, size);
  // ...
}
```

Paginated endpoints include `/api/v1/users`, `/api/v1/summoners`, `/api/v1/dashboard/me/ranked-matches`.

Key files: `UserController.java`, `SummonerController.java`, `DashboardController.java`

### ✅ Sample data
Status: COMPLETED

- `DataInitializer.java` seeds example users at startup (admin and regular user)
- Data loaded with `@PostConstruct`
- Secure passwords generated for seeded accounts

File: `DataInitializer.java`

---

## Frontend

### ❌ UI component libraries
Status: TODO

- No integration with Angular Material or ng-bootstrap — UI uses custom CSS and components.
- Recommendation: integrate a component library to improve UX.

### ✅ Angular architecture (components + services)
Status: COMPLETED

- Components: `DashboardComponent`, `LoginComponent`, `HomeComponent`, `SummonerComponent`, `AdminComponent`, `HeaderComponent`, `FooterComponent`
- Services: `AuthService`, `UserService`, `DashboardService`, `SummonerService`, `AdminService`
- Uses Angular standalone components (Angular 17+)

### ❌ Error pages
Status: TODO

- No dedicated error components (404, 500) found.
- Recommendation: add an `ErrorComponent` and configure error routes.

### ⚠️ Frontend pagination
Status: PARTIAL

- Backend supports pagination, but the frontend uses hardcoded values (e.g., loads 30 matches by default)
- Admin user list lacks 'load more' or infinite scroll
- Recommendation: implement incremental loading UI (buttons or infinite scroll)

---

## Quality controls

### ⚠️ Automated tests
Status: PARTIAL — INSUFFICIENT

Existing tests:

- Unit tests: ~16 files under `/backend/src/test/java/unit/`
- System tests: only 1 (`SummonerSystemTest.java`)
- E2E tests: 1 file (possibly empty/incomplete)

Functionality coverage (system tests):

1. Authentication — unit tests present ✅
2. Personal dashboard — no system tests ❌
3. Summoner search — no system tests ❌
4. Favorites management — partial (unit tests) ⚠️
5. Match history — no system tests ❌
6. LP statistics — no system tests ❌
7. Admin panel — no system tests ❌
8. User management — no system tests ❌
9. Avatar upload — no system tests ❌

System test coverage: ~11% (1 of 9 key functionalities)

❌ Requirement not met: >50% coverage of functionalities in system tests.

Action required: add system tests for at least 5 more functionalities, e.g.:

- `DashboardSystemTest.java` (ranked stats, LP progression)
- `AuthSystemTest.java` (login, registration, logout)
- `SearchSystemTest.java` (summoner search)
- `FavoritesSystemTest.java` (add/remove favorites)
- `AdminSystemTest.java` (user management)

Frontend tests:

- Component specs (`.spec.ts`) exist for components ✅ but may need updates ⚠️

### ⚠️ Code quality
Status: PARTIAL

- Logging (`Logger`) is used across services ✅
- Code formatting is consistent ✅
- Some controllers lack inline comments; add clarifying comments where helpful ⚠️
- JaCoCo configured for test coverage ✅
- Some methods (e.g., in `DashboardController`) show high cyclomatic complexity — refactor recommended (complexity > 15)

---

## Docker packaging

### ✅ Dockerfile
Status: COMPLETED

- `docker/Dockerfile` present and uses a multi-stage build for optimization

### ✅ `docker-compose.yml` (v0.1.0)
Status: COMPLETED

- Compose file located at `/docker/docker-compose.yml`
- Services: MySQL 8.0 (with healthcheck), app image `spiritblade:0.1`
- Environment variables, volumes, and health checks configured

### ✅ `docker-compose-dev.yml`
Status: COMPLETED

- Dev compose file present and configured for development tag `dev`

### ✅ Docker Compose best practices
Status: COMPLETED

- MySQL healthcheck present
- Startup ordering handled with `depends_on` and health conditions
- Environment variables with defaults and overridable values

---

## CI / Delivery

### ✅ Dev pipeline (merge → dev tag)
Status: COMPLETED

- Workflow: `.github/workflows/deploy-dev.yml` triggers on pushes to `main` and publishes a `dev` tag image to Docker Hub

### ✅ Release pipeline (GitHub release → version tag)
Status: COMPLETED

- Workflow: `.github/workflows/deploy-release.yml` builds and publishes a versioned image and the compose artifact as OCI

### ✅ Manual build workflow
Status: COMPLETED

- Workflow: `.github/workflows/manual-build.yml`

### ✅ Reusable workflows and DRY
Status: COMPLETED

- `build-push.yml` is reusable and other workflows call it with parameters

### ❌ Release 0.1.0
Status: TODO

- No `0.1.0` tag found in the repository
- No GitHub release exists
- Action required: create release `0.1.0` to trigger the release pipeline

### ❌ Docker images for 0.1.0 / latest
Status: TODO

- Since no release exists the `0.1.0` image and `latest` tag were not published

---

## Documentation

### ⚠️ Documentation status
Status: PARTIAL

- Docs present: `API.md`, `Funcionalidades.md`, `Guia-Desarrollo.md`, `Ejecucion.md`, `Inicio-Proyecto.md`, `Seguimiento.md`
- Some docs may be outdated relative to Phase 3 changes

Recommendation: update docs to reflect new dashboard features, LP tracking, and testing status.

### ⚠️ Blog post
Status: PARTIAL

- Existing Medium post covers Phase 1 only: https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3
- Action required: publish a Phase 3 post describing the dashboard, Riot API integration, Docker and CI/CD

---

## Summary

### Completed (17/27)
1. Spring Security implemented
2. HTTPS on port 443
3. MinIO/S3 for images
4. Layered architecture
5. `/api/v1` endpoints
6. REST best practices
7. Parametrized searches
8. Backend pagination
9. Sample data
10. Angular architecture
11. Dockerfile
12. `docker-compose.yml`
13. `docker-compose-dev.yml`
14. Docker best practices
15. Dev pipeline
16. Release pipeline
17. Reusable workflows

### Partial (4/27)
1. Frontend pagination (backend OK)
2. System tests (only 1; need 5+)
3. Code quality (improvements needed)
4. Documentation (update needed)

### To do (6/27)
1. Add UI component library (ng-bootstrap/Material)
2. Error pages (ErrorComponent)
3. Create release 0.1.0
4. Publish Docker image tagged 0.1.0
5. Publish compose artifact (OCI) for 0.1.0
6. Publish Phase 3 blog post

---

## Priority actions

### High priority (blocking)
1. Create system tests for at least 5 additional functionalities
2. Publish release `0.1.0` on GitHub to trigger pipelines
3. Publish a Phase 3 blog post

### Medium priority
4. Implement error pages and UX improvements
5. Integrate Angular Material or ng-bootstrap
6. Improve frontend pagination UX
7. Update technical documentation

### Low priority
8. Refactor high complexity methods
9. Improve code comments and documentation
10. Improve frontend test coverage

---

## Conclusion

Overall compliance: ~63% (17/27 completed)

The project has a solid base:

- Backend is well-structured with Spring Security and HTTPS
- REST API follows good practices
- Docker and CI/CD infrastructure are in place
- Frontend architecture is sound

Main deficiencies:

- System tests are insufficient (11% vs required 50%) — CRITICAL
- No `0.1.0` release published — straightforward to fix
- Frontend lacks a component library — UX improvement

Recommendation: prioritize system tests and publishing the `0.1.0` release to meet Phase 3 requirements.