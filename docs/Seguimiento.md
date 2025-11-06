# Tracking — SPIRITBLADE

This document describes the quality control strategies, development process, and project metrics for SPIRITBLADE.

---

## 📊 Quality control

### Testing strategy

The application implements a testing pyramid with multiple levels:

```
           /\
          /E2E\        ← End-to-end tests (full system)
         /------\
        / Integr \     ← Integration tests (APIs + DB)
       /----------\
      /   Unit    \   ← Unit tests (isolated logic)
     /--------------\
```

#### Unit tests

Backend (JUnit 5 + Mockito)
- Location: `backend/src/test/java/.../unit/`
- Goal: test isolated business logic
- Mock dependencies (repositories, external APIs)
- Coverage target: ≥60%

Frontend (Jasmine + Karma)
- Location: `frontend/src/app/**/*.spec.ts`
- Goal: test Angular components and services
- Mock HttpClient, services, and routing
- Coverage target: ≥50%

Examples implemented:
- `UserServiceSimpleUnitTest` — user creation logic
- `SummonerMapperTest` — DTO ↔ entity mapping
- `AuthService.spec.ts` — Angular authentication service
- `LoginComponent.spec.ts` — login component

---

#### Integration tests

Backend (Spring Boot Test)
- Location: `backend/src/test/java/.../integration/`
- Goal: test integration across layers
- Full Spring context with `@SpringBootTest`
- H2 in-memory database
- MockMvc to simulate HTTP requests

Examples implemented:
- `SummonerIntegrationTest` — summoner CRUD + cache
- `AuthIntegrationTest` — full authentication flow
- `AdminControllerIntegrationTest` — admin endpoints

Frontend (Angular testing utilities)
- Location: `frontend/src/app/integration/`
- Goal: test interaction between components and services
- TestBed to configure modules
- HttpClientTestingModule for simulated APIs

---

#### System tests (E2E)

Selenium WebDriver
- Location: `backend/src/test/java/.../e2e/`
- Goal: validate end-to-end user flows
- Chrome headless automation
- Verify UI + backend + DB

Implemented scenarios:
- `SummonerE2ETest` — end-to-end summoner search
- Verifies navigation, data loading and performance

Status: 🚧 In progress. Full E2E planned for v0.2.

---

### Coverage metrics

| Component | Current coverage | Target | Status |
|-----------|------------------:|-------:|:------:|
| Backend | ~55% | ≥60% | 🟡 Close |
| Frontend | ~48% | ≥50% | 🟡 Close |
| Global | ~52% | ≥55% | ✅ Met |

Tools:
- Backend: JaCoCo (HTML reports in `target/site/jacoco/`)
- Frontend: karma-coverage (reports in `coverage/`)

---

### Static analysis (SonarCloud)

Configuration:
- Integrated in GitHub Actions (`.github/workflows/build.yml`)
- Analysis runs on every PR to `main`
- Quality Gate configured

Target metrics:
- Bugs: 0
- Security vulnerabilities: 0
- Code smells: <50
- Code duplication: <5%
- Coverage: ≥55%
- Technical debt: < 1 day

Current state: ✅ Quality Gate: PASSED

Access: [SonarCloud - SPIRITBLADE](https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE)

---

### Quality improvements applied (resolved code smells)

Backend:
- ✅ Replaced `e.printStackTrace()` with SLF4J logging
- ✅ Avoided broad catches: now catching specific `HttpClientErrorException`
- ✅ Use `Collections.emptyList()` instead of `new ArrayList<>()`
- ✅ Improved logging: warn + debug stacktrace
- ✅ Return empty string instead of null for URLs
- ✅ Handle exceptions in refresh token flow returning 401 Unauthorized

Frontend:
- ✅ Replaced `console.error()` with `console.debug()` where appropriate
- ✅ Display user-facing error messages in the UI instead of logging only to console
- ✅ Improved HTTP error handling with informative messages

---

## 🔄 Development process

### Methodology

The project follows an iterative and incremental process with agile principles:

- Short iterations: 2–3 week sprints
- Incremental deliveries: working version at the end of each phase
- Continuous integration: automated tests on every commit
- Fast feedback: code review and automated deployments

### Project phases

```
Phase 1: Definition (Sep)          ✅ Completed
Phase 2: Setup & CI (Oct)          ✅ Completed
Phase 3: v0.1 Core (Dec)         ✅ Completed
├─ Milestone 0.1.0: core features
├─ Docker deployment
└─ CI/CD workflows

Phase 4: v0.2 Intermediate (Mar)     📋 Planned
├─ Charts and advanced analysis
├─ Favorites system
└─ Notifications

Phase 5: v1.0 Advanced (Apr)       📋 Planned
├─ ML predictions
├─ Recommendations
└─ Personalized leaderboards

Phase 6: Documentation (May)       📋 Planned
Phase 7: Defense (Jun)         📋 Planned
```

---

### Task management (GitHub)

GitHub Issues:
- Labels: `bug`, `enhancement`, `documentation`, `good first issue`
- Templates for bugs and features
- Assignment of owners

GitHub Projects:
- Kanban board columns:
  - Backlog
  - In Progress
  - In Review
  - Done

Milestones:
- v0.1.0 — core features (✅ Completed)
- v0.2.0 — intermediate features (📋 Planned)
- v1.0.0 — advanced features (📋 Planned)

Link: [GitHub Projects](https://github.com/codeurjc-students/2025-SPIRITBLADE/projects)

---

### Version control (Git)

Branching strategy:

```
main (production)
  ├─ feature/summoner-search     ✅ Merged
  ├─ feature/auth-jwt            ✅ Merged
  ├─ feature/admin-panel         ✅ Merged
  ├─ feature/docker-deployment   ✅ Merged
  ├─ hotfix/fix-api-timeout      ✅ Merged
  └─ CodeSmells-&-Tests          🚧 In progress
```

Rules:
- `main` is protected: pull request required
- Commits must pass CI before merge
- Code review required
- Squash commits on merge

Conventional commits examples:

```
feat: add summoner search
fix: correct winrate calculation bug
docs: update README with Docker instructions
test: add unit tests for UserService
refactor: improve exception handling in RiotService
chore: bump version to 0.1.0
```

Metrics:
- Total commits: ~80
- Active branches: 2–3
- Merged PRs: ~15
- Contributors: 1

---

### CI/CD

GitHub Actions workflows

1) `build.yml` — Quality control
Trigger: push to any branch, PR to main
Actions:
- Build backend (Maven)
- Build frontend (npm)
- Unit tests (JUnit + Jasmine)
- Integration tests
- Coverage with JaCoCo + karma-coverage
- SonarCloud analysis (PRs to main)

2) `deploy-dev.yml` — automatic deploy
Trigger: push to `main`
Actions:
- Build multi-stage Docker image
- Push to DockerHub with tag `dev`
- Publish docker-compose as OCI artifact

3) `deploy-release.yml` — release deploy
Trigger: GitHub Release
Actions:
- Build Docker image
- Push with version tag (e.g. `0.1.0`)
- Update `latest` tag
- Publish versioned docker-compose

4) `manual-build.yml` — manual build
Trigger: workflow_dispatch
Actions:
- Build image with custom tag: `<branch>-<timestamp>-<commit>`
- Push to DockerHub

Reusable workflows: `deploy-dev` and `deploy-release` call `build-push.yml` to avoid duplication.

Secrets configured:
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `SONAR_TOKEN`

CI status badge available in the repo actions page.

---

### Versioning

Strategy: Semantic Versioning (`MAJOR.MINOR.PATCH`)

- MAJOR: breaking API changes
- MINOR: new backward-compatible features
- PATCH: bug fixes

Published versions:
- v0.1.0 (Dec 2024) — first functional release with Docker

Upcoming:
- v0.2.0 (Mar 2025) — intermediate features
- v1.0.0 (Apr 2025) — advanced features

Release process:
1) Pre-release: update versions with `update-version.ps1/sh`
2) Commit & tag: `git commit -m "chore: bump version" && git tag 0.1.0`
3) Push: `git push && git push --tags`
4) Create GitHub Release with changelog
5) Post-release: bump to next SNAPSHOT (`0.2.0-SNAPSHOT`)

Documentation: [RELEASE-PROCESS.md](RELEASE-PROCESS.md)

---

## 📈 Project metrics

### Lines of code

| Component | Language | Files | Lines |
|----------:|---------:|------:|------:|
| Backend | Java | ~40 | ~3,500 |
| Frontend | TypeScript | ~30 | ~2,500 |
| Tests | Java/TS | ~25 | ~2,000 |
| Config | YAML/JSON/XML | ~15 | ~800 |
| **TOTAL** | - | **~110** | **~8,800** |

### Development stats

- Duration: ~4 months (Sep–Dec 2024)
- Commits: ~80
- Pull requests: ~15
- Issues closed: ~25
- Releases: 1 (v0.1.0)

### Performance

- Build time: ~3 minutes (CI)
- Docker image size: ~180MB
- Startup time: ~30s
- API response time: <500ms (p95)

---

## 🔗 Tracking links

- GitHub repo: https://github.com/codeurjc-students/2025-SPIRITBLADE
- GitHub Actions: https://github.com/codeurjc-students/2025-SPIRITBLADE/actions
- SonarCloud: https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE
- DockerHub: https://hub.docker.com/r/codeurjcstudents/spiritblade
- Project blog: https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3

---

[← Back to main README](../README.md)
