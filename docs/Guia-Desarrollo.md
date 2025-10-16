# Development Guide — SPIRITBLADE

## Index
- [Introduction](#introduction)
- [Technologies](#technologies)
- [Tools](#tools)
- [Architecture](#architecture)
- [Quality Control](#quality-control)
- [Development Process](#development-process)
- [Code Execution and Editing](#code-execution-and-editing)

---

## Introduction

SPIRITBLADE is a web application with **SPA (Single Page Application)** architecture, designed to provide analysis and visualization of League of Legends statistics. An SPA architecture means that the entire client application is loaded in a single web page, and subsequent navigation is performed dynamically without reloading the complete page, offering a more fluid experience similar to desktop applications.

The application is composed of three main components:
- **Client (Frontend)**: Angular 17 running in the user's browser
- **Server (Backend)**: REST API developed in Spring Boot (Java 21)
- **Database**: MySQL for production, H2 in-memory for development

### Technical Summary

| Aspect | Description |
|--------|-------------|
| **Type** | SPA web application with REST API |
| **Technologies** | Java 21, Spring Boot 3.4.3, Angular 17, MySQL, JWT |
| **Tools** | VS Code, IntelliJ IDEA, Maven, npm, Git |
| **Quality control** | JUnit 5, Jasmine/Karma, JaCoCo, SonarCloud, GitHub Actions |
| **Deployment** | Docker, Docker Compose (planned for Phase 3) |
| **Development process** | Iterative and incremental, Git flow, DevOps with CI/CD |

---

## Technologies

### Backend
**Spring Boot 3.4.3** - Framework for developing enterprise Java applications that simplifies configuration and deployment. In the project, it's used to create the REST API that serves data to the frontend.
- Official URL: https://spring.io/projects/spring-boot

**Java 21** - Main programming language for the backend, using the latest LTS features.
- Official URL: https://openjdk.org/projects/jdk/21/

**Spring Security** - Security framework that provides authentication and authorization. In the project, it implements JWT-based authentication and role-based access control.
- Official URL: https://spring.io/projects/spring-security

**MySQL** - Relational database management system used in production to store users, summoners, matches, and statistics.
- Official URL: https://www.mysql.com/

### Frontend
**Angular 17** - Web development framework with TypeScript that allows creating robust SPA applications. Uses standalone components for a more modular architecture.
- Official URL: https://angular.io/

**TypeScript** - JavaScript superset that adds static typing, used throughout the frontend to improve code maintainability.
- Official URL: https://www.typescriptlang.org/

---

## Tools

**Visual Studio Code** - Recommended primary editor with extensions for Java, Angular, and Git. Provides integrated debugging and terminal.
- Official URL: https://code.visualstudio.com/

**IntelliJ IDEA** - Alternative IDE especially recommended for Java backend development with excellent Spring Boot support.
- Official URL: https://www.jetbrains.com/idea/

**Maven** - Dependency management and build tool for the Java backend. The included wrapper (`mvnw.cmd`) is used.
- Official URL: https://maven.apache.org/

**npm** - Node.js package manager used to handle Angular frontend dependencies.
- Official URL: https://www.npmjs.com/

**Git** - Distributed version control system for tracking changes in source code.
- Official URL: https://git-scm.com/

---

## Architecture

### Domain Model

The domain model represents the main entities of the SPIRITBLADE application and their relationships:

```
┌────────────────────────────────────────────────────────────────────┐
│                         DOMAIN MODEL                                │
└────────────────────────────────────────────────────────────────────┘

┌──────────────────┐               ┌──────────────────┐
│      User        │               │    Summoner      │
├──────────────────┤               ├──────────────────┤
│ id: Long         │──────────────→│ id: Long         │
│ name: String     │   favoritos   │ puuid: String    │
│ email: String    │      N:M      │ riotId: String   │
│ encodedPwd: Str  │               │ gameName: String │
│ roles[]: String  │               │ tagLine: String  │
│ active: Boolean  │               │ summonerLevel: I │
│ profilePic: Blob │               │ tier: String     │
│ createdAt: Date  │               │ rank: String     │
└──────────────────┘               │ leaguePoints: I  │
                                   │ wins: Integer    │
                                   │ losses: Integer  │
                                   │ updatedAt: Date  │
                                   └──────────────────┘
                                            │
                                            │ 1:N
                                            ▼
                                   ┌──────────────────┐
                                   │      Match       │
                                   ├──────────────────┤
                                   │ id: Long         │
                                   │ matchId: String  │
                                   │ championId: Int  │
                                   │ championName: S  │
                                   │ kills: Integer   │
                                   │ deaths: Integer  │
                                   │ assists: Integer │
                                   │ win: Boolean     │
                                   │ gameDuration: I  │
                                   │ timestamp: Date  │
                                   │ gameMode: String │
                                   └──────────────────┘
```

**Key Relationships**:
- **User ↔ Summoner**: Many-to-Many relationship for favorites system
- **Summoner → Match**: One-to-Many relationship for match history

---

### REST API

The REST API follows REST architectural principles with JWT authentication:

```
┌───────────────────────────────────────────────────────────────────┐
│                        REST API STRUCTURE                          │
└───────────────────────────────────────────────────────────────────┘

PUBLIC ENDPOINTS (No authentication)
├── POST   /auth/login           → Login with username/password
├── POST   /auth/signup          → Register new user
├── POST   /auth/refresh         → Refresh JWT token
├── GET    /summoners/search     → Search summoner by Riot ID
└── GET    /summoners/{id}       → Get summoner details

AUTHENTICATED ENDPOINTS (JWT required)
├── USER ROLE
│   ├── GET    /users/me         → Current user profile
│   ├── PUT    /users/me         → Update profile
│   ├── POST   /users/me/favorites/{summonerId} → Add favorite
│   ├── DELETE /users/me/favorites/{summonerId} → Remove favorite
│   ├── GET    /dashboard/stats  → Personal statistics
│   └── GET    /dashboard/matches → Recent matches
│
└── ADMIN ROLE
    ├── GET    /admin/users      → List all users
    ├── PUT    /admin/users/{id} → Update user (activate/deactivate)
    ├── DELETE /admin/users/{id} → Delete user
    └── GET    /admin/stats      → System statistics
```

**Authentication Flow**:
1. Client sends credentials to `/auth/login`
2. Server validates and returns JWT token
3. Client includes token in `Authorization: Bearer <token>` header
4. Server validates token on each protected request
5. Token expires after 24 hours (refresh available)

**Documentation**:
- Manual: `docs/API.md`
- Planned: OpenAPI/Swagger specification

---

### Server Architecture

The backend follows **layered architecture** with Spring Boot best practices:

```
┌───────────────────────────────────────────────────────────────────┐
│                       SERVER ARCHITECTURE                          │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Auth       │  │   Summoner   │  │    Admin     │         │
│  │ Controller   │  │  Controller  │  │  Controller  │  ...    │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BUSINESS LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │UserLoginSvc  │  │   Riot       │  │  Dashboard   │         │
│  │              │  │   Service    │  │   Service    │  ...    │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   User       │  │   Summoner   │  │    Match     │         │
│  │ Repository   │  │  Repository  │  │  Repository  │  ...    │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE (MySQL)                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  users   │  │summoners │  │ matches  │  │favorites │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────────────────────┘

CROSS-CUTTING CONCERNS
┌─────────────────────────────────────────────────────────────────┐
│  Security (Spring Security + JWT)                               │
│  Exception Handling (Global @ControllerAdvice)                  │
│  Logging (SLF4J)                                                │
│  External API Integration (RestTemplate → Riot Games API)       │
└─────────────────────────────────────────────────────────────────┘
```

**Layer Responsibilities**:
- **Controllers**: Handle HTTP requests, validation, response formatting
- **Services**: Business logic, transaction management, API integration
- **Repositories**: Data access using Spring Data JPA
- **Models/Entities**: JPA entities mapping to database tables

**Key Components**:
- **Security**: JWT-based authentication with `JwtTokenProvider` and `JwtAuthenticationFilter`
- **Riot Integration**: `RiotService` + `DataDragonService` for external API calls
- **Exception Handling**: `GlobalExceptionHandler` for consistent error responses

---

### Client Architecture

The frontend follows **Angular standalone components** architecture:

```
┌───────────────────────────────────────────────────────────────────┐
│                       CLIENT ARCHITECTURE                          │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      VIEW LAYER (Components)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │   Home   │  │  Login   │  │Summoner  │  │Dashboard │       │
│  │Component │  │Component │  │Component │  │Component │  ...  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│       │              │              │              │            │
└───────┼──────────────┼──────────────┼──────────────┼────────────┘
        │              │              │              │
        ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │   Auth   │  │Summoner  │  │Dashboard │  │  Admin   │       │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │  ...  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│       │              │              │              │            │
└───────┼──────────────┼──────────────┼──────────────┼────────────┘
        │              │              │              │
        └──────────────┴──────────────┴──────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │  HttpClient     │
              │  (HTTP Calls)   │
              └─────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │  Backend API    │
              │  (Spring Boot)  │
              └─────────────────┘

ROUTING & GUARDS
┌─────────────────────────────────────────────────────────────────┐
│  app.routes.ts                                                  │
│  ├── /             → HomeComponent                             │
│  ├── /login        → LoginComponent                            │
│  ├── /summoner/:id → SummonerComponent                         │
│  ├── /dashboard    → DashboardComponent (Auth Guard)           │
│  └── /admin        → AdminComponent (Admin Guard)              │
└─────────────────────────────────────────────────────────────────┘

DATA MODELS (DTOs)
┌─────────────────────────────────────────────────────────────────┐
│  SummonerDTO, UserDTO, MatchDTO, DashboardStatsDTO...          │
└─────────────────────────────────────────────────────────────────┘
```

**Key Features**:
- **Standalone Components**: No NgModule needed, better tree-shaking
- **Reactive Programming**: RxJS Observables for async operations
- **Route Guards**: `AuthGuard` and `AdminGuard` for access control
- **Interceptors**: `AuthInterceptor` adds JWT token to requests
- **State Management**: Services with BehaviorSubject for shared state

**Component Communication**:
- Parent → Child: `@Input()`
- Child → Parent: `@Output()` + EventEmitter
- Sibling: Shared services with RxJS subjects

---

### Deployment

The application uses **Docker multi-stage build** for optimized production images:

```
┌───────────────────────────────────────────────────────────────────┐
│                     DEPLOYMENT ARCHITECTURE                        │
└───────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │   Docker Host   │
                    └─────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Angular    │   │ Spring Boot  │   │    MySQL     │
│  Container   │   │  Container   │   │  Container   │
│              │   │              │   │              │
│ nginx:alpine │   │  JRE 21      │   │  mysql:8.0   │
│ Port: 80     │   │  Port: 8080  │   │  Port: 3306  │
└──────────────┘   └──────────────┘   └──────────────┘
        │                   │                   │
        │                   │                   │
        └───────────────────┴───────────────────┘
                            │
                  Docker Compose Network
```

**Docker Multi-Stage Build**:

1. **Frontend Build Stage**:
   ```dockerfile
   FROM node:18-alpine AS frontend-build
   WORKDIR /app/frontend
   COPY frontend/package*.json ./
   RUN npm ci
   COPY frontend/ ./
   RUN npm run build --prod
   ```

2. **Backend Build Stage**:
   ```dockerfile
   FROM maven:3.9-eclipse-temurin-21 AS backend-build
   WORKDIR /app/backend
   COPY backend/pom.xml ./
   RUN mvn dependency:go-offline
   COPY backend/src ./src
   RUN mvn clean package -DskipTests
   ```

3. **Final Runtime Image**:
   ```dockerfile
   FROM eclipse-temurin:21-jre-alpine
   WORKDIR /app
   
   # Copy frontend static files
   COPY --from=frontend-build /app/frontend/dist/frontend /app/static
   
   # Copy backend jar
   COPY --from=backend-build /app/backend/target/*.jar app.jar
   
   EXPOSE 8080
   CMD ["java", "-jar", "app.jar"]
   ```

**Docker Compose**:
```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: spiritblade_db
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    image: jorgeandresecheverriagarcia/2025-spiritblade:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/spiritblade_db
      RIOT_API_KEY: ${RIOT_API_KEY}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - mysql
    ports:
      - "8080:8080"

volumes:
  mysql-data:
```

**Deployment Options**:
- **Quick Start**: Pull from DockerHub and run with `docker-compose up`
- **Source Build**: Build locally with `docker build` + `docker-compose up`
- **Cloud**: Deploy to AWS ECS, Azure Container Instances, or GCP Cloud Run

See [Ejecucion.md](Ejecucion.md) for detailed deployment instructions.

---

## Quality Control

### Testing Strategy

The project follows a **testing pyramid** approach with multiple levels of automated tests:

```
                    /\
                   /  \
                  / E2E \          ← Few, critical user flows
                 /--------\
                /          \
               / Integration \     ← Moderate, key interactions
              /--------------\
             /                \
            /   Unit Tests     \   ← Many, fast, isolated
           /____________________\
```

### Automated Tests

#### Backend (Java)

**Unit Tests** - JUnit 5 + Mockito
- **Purpose**: Test individual methods in isolation
- **Scope**: Service layer business logic, utility methods
- **Mocking**: External dependencies (repositories, APIs)
- **Location**: `backend/src/test/java/com/tfg/tfg/service/`
- **Example**:
  ```java
  @Test
  void testGetSummonerByRiotId_Success() {
      when(restTemplate.exchange(...)).thenReturn(mockResponse);
      SummonerDTO result = riotService.getSummonerByRiotId("Player", "EUW");
      assertNotNull(result);
      assertEquals("Player", result.getGameName());
  }
  ```

**Integration Tests** - Spring Boot Test
- **Purpose**: Test component interactions with real Spring context
- **Scope**: Controller → Service → Repository with embedded DB
- **Context**: `@SpringBootTest` with `@AutoConfigureMockMvc`
- **Location**: `backend/src/test/java/com/tfg/tfg/`
- **Example**:
  ```java
  @Test
  void testLoginEndpoint_ValidCredentials() throws Exception {
      mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(loginJson))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").exists());
  }
  ```

**E2E Tests** - Selenium WebDriver
- **Purpose**: Test critical user flows in real browser
- **Scope**: Full stack from UI to database
- **Location**: `backend/src/test/java/com/tfg/tfg/e2e/`
- **Scenarios**: Login, summoner search, dashboard navigation

---

#### Frontend (Angular)

**Unit Tests** - Jasmine + Karma
- **Purpose**: Test components and services in isolation
- **Scope**: Component logic, service methods, pipes
- **Mocking**: HttpClient, Router, dependencies
- **Location**: `frontend/src/app/**/*.spec.ts`
- **Example**:
  ```typescript
  it('should display summoner name after search', () => {
    component.summoner = mockSummoner;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('h2').textContent).toContain('Player#EUW');
  });
  ```

**Integration Tests** - Angular Testing Utilities
- **Purpose**: Test component interactions with child components
- **Scope**: Parent-child communication, routing, forms
- **Tools**: `TestBed`, `ComponentFixture`, `RouterTestingModule`

---

### Test Coverage

**Current Status (v0.1)**:
- **Backend**: 55% line coverage (JaCoCo)
- **Frontend**: 48% line coverage (karma-coverage)

**Target Goals**:
- **Backend**: ≥ 60% for v0.2
- **Frontend**: ≥ 50% for v0.2

**Coverage Reports**:
- Backend: `backend/target/site/jacoco/index.html`
- Frontend: `frontend/coverage/index.html`

**Uncovered Areas** (planned for v0.2):
- Exception handling edge cases
- Admin panel advanced features
- Error recovery scenarios
- Complex data transformations

---

### Tested Functionalities (v0.1)

✅ **Authentication & Authorization**
- User login with JWT token generation
- Token validation on protected endpoints
- Role-based access control (USER vs ADMIN)
- Token refresh mechanism

✅ **User Management**
- User registration with validation
- Profile retrieval and updates
- Password encryption (BCrypt)
- Profile picture upload

✅ **Summoner Operations**
- Search by Riot ID (gameName + tagLine)
- Fetch summoner data from Riot API
- Retrieve ranked stats (tier, rank, LP, W/L)
- Display champion mastery top 3
- Match history with pagination

✅ **Dashboard**
- Personal statistics aggregation
- Recent matches with details
- Favorite summoners management

✅ **Admin Panel**
- List all users with filters
- Activate/deactivate users
- Delete users with cascade
- System statistics (user count, active users)

✅ **External API Integration**
- Riot Games API authentication
- Rate limit handling
- Error recovery (retries, fallbacks)
- Data Dragon CDN for images

✅ **Frontend Components**
- Component rendering with proper data binding
- Routing with guards (Auth, Admin)
- Form validation (reactive forms)
- Error display with user-friendly messages

---

### Static Code Analysis

**SonarCloud Integration**:
- **URL**: https://sonarcloud.io/project/overview?id=JorgeAndresEcheverria_2025-SPIRITBLADE
- **Trigger**: Automatic on every PR to `main`
- **Quality Gate**: Must pass for merge approval

**Analyzed Metrics**:
- **Bugs**: 0 (target: 0 critical, 0 major)
- **Vulnerabilities**: 0 (target: 0)
- **Code Smells**: <50 (target: <50)
- **Security Hotspots**: Reviewed and resolved
- **Coverage**: Integrated with JaCoCo + karma-coverage
- **Duplications**: <5%
- **Maintainability Rating**: A

**Configuration**:
- File: `sonar-project.properties`
- Languages: Java, TypeScript, HTML, CSS
- Exclusions: Tests, generated code, third-party libraries

**Code Quality Improvements (v0.1)**:
- Replaced `printStackTrace()` with SLF4J logging
- Replaced `console.error()` with `console.debug()` in frontend
- Specific exception catches instead of generic `Exception`
- Immutable empty collections (`Collections.emptyList()`)
- Empty strings instead of `null` for optional URLs

---

### Continuous Integration (CI)

**GitHub Actions Workflows**:

1. **build.yml** - Basic Quality Control
   - **Trigger**: Push to feature branches
   - **Steps**: Checkout → Setup JDK/Node → Build backend → Build frontend → Run unit tests
   - **Artifacts**: None
   - **Duration**: ~5 minutes

2. **build-with-quality.yml** - Complete Quality Control (on PR to main)
   - **Trigger**: Pull Request to `main`
   - **Steps**: All above + Integration tests → Coverage reports → SonarCloud analysis
   - **Quality Gate**: Must pass for merge
   - **Artifacts**: Coverage reports, test results
   - **Duration**: ~10 minutes

3. **deploy-dev.yml** - Deploy Development Image
   - **Trigger**: Push to `main`
   - **Steps**: Build → Tag as `dev` → Push to DockerHub
   - **Image**: `jorgeandresecheverriagarcia/2025-spiritblade:dev`

4. **deploy-release.yml** - Deploy Release Image
   - **Trigger**: GitHub Release created
   - **Steps**: Build → Tag with version (e.g., `v0.1.0`) → Tag as `latest` → Push to DockerHub
   - **Images**: `jorgeandresecheverriagarcia/2025-spiritblade:v0.1.0` + `latest`

5. **manual-build.yml** - Manual Build Trigger
   - **Trigger**: Manual workflow dispatch
   - **Purpose**: On-demand builds for testing

**Branch Protection** (`main`):
- ✅ Require PR before merge
- ✅ Require status checks (build-with-quality.yml)
- ✅ Require code review approval
- ❌ No direct pushes to `main`

See [Seguimiento.md](Seguimiento.md) for detailed CI/CD workflows and metrics.

---

## Development Process

### Methodology

The project follows an **iterative and incremental** agile methodology:

**Principles**:
- 🔄 **Short iterations**: 2-3 week cycles
- 📦 **Incremental deliverables**: Deployable version at each phase end
- 🔍 **Continuous feedback**: Regular reviews and adjustments
- 🚀 **DevOps culture**: Automation, CI/CD, monitoring

**7 Planned Phases**:
1. ✅ **Phase 1**: Functionality and screen definition (Sep 15, 2024)
2. ✅ **Phase 2**: Repository and CI setup (Oct 15, 2024)
3. ✅ **Phase 3**: Version 0.1 - Basic features (Dec 15, 2024)
4. 📋 **Phase 4**: Version 0.2 - Intermediate features (Mar 1, 2025)
5. 📋 **Phase 5**: Version 1.0 - Advanced features (Apr 15, 2025)
6. 📋 **Phase 6**: TFG report writing (May 15, 2025)
7. 📋 **Phase 7**: TFG defense (Jun 15, 2025)

See [Inicio-Proyecto.md](Inicio-Proyecto.md) for detailed phase descriptions.

---

### Task Management

**GitHub Issues**:
- Bug tracking with `bug` label
- Feature requests with `enhancement` label
- Documentation updates with `documentation` label
- Priority levels: `priority: high`, `priority: medium`, `priority: low`

**GitHub Projects** (Kanban):
- **Backlog**: Planned features for future versions
- **To Do**: Selected for current iteration
- **In Progress**: Active development
- **In Review**: Pull Request created, awaiting approval
- **Done**: Merged to `main` and deployed

**Milestones**:
- Milestone: **v0.1** (Dec 15, 2024) - ✅ Completed
- Milestone: **v0.2** (Mar 1, 2025) - 📋 Planned
- Milestone: **v1.0** (Apr 15, 2025) - 📋 Planned

---

### Version Control (Git)

**Branch Strategy**:
```
main (protected)
  │
  ├── feature/user-authentication     ← Feature branches
  ├── feature/summoner-search
  ├── feature/admin-panel
  ├── bugfix/login-error
  └── docs/update-readme
```

**Naming Conventions**:
- `feature/<description>` - New features
- `bugfix/<description>` - Bug fixes
- `docs/<description>` - Documentation updates
- `refactor/<description>` - Code refactoring
- `test/<description>` - Test improvements

**Commit Messages** (Conventional Commits):
```
type(scope): description

feat(auth): add JWT token refresh endpoint
fix(summoner): handle 404 when summoner not found
docs(readme): update installation instructions
test(service): add unit tests for RiotService
refactor(controller): simplify error handling
```

**Types**: `feat`, `fix`, `docs`, `test`, `refactor`, `style`, `chore`

**Current Metrics (v0.1)**:
- 📊 Total commits: ~80
- 🌿 Active branches: 2-3 at any time
- 🔒 `main` protected with mandatory PR reviews

---

### Pull Request Workflow

1. **Create branch** from `main`:
   ```bash
   git checkout -b feature/new-feature
   ```

2. **Develop** with frequent commits:
   ```bash
   git add .
   git commit -m "feat(scope): description"
   ```

3. **Push** to remote:
   ```bash
   git push origin feature/new-feature
   ```

4. **Create PR** on GitHub:
   - Title: Clear description of changes
   - Description: What, why, how + screenshots if UI
   - Link related issues
   - Request reviewers

5. **CI Checks** run automatically:
   - ✅ Build successful
   - ✅ All tests pass
   - ✅ Coverage thresholds met
   - ✅ SonarCloud quality gate passed

6. **Code Review**:
   - Reviewer comments on code
   - Developer addresses feedback
   - Approve when satisfied

7. **Merge** to `main`:
   - Squash and merge (clean history)
   - Delete feature branch
   - CI deploys `dev` image automatically

---

### Code Review Guidelines

**Reviewer Checklist**:
- ✅ Code follows project conventions
- ✅ Tests are included and pass
- ✅ No obvious bugs or security issues
- ✅ Documentation is updated
- ✅ Performance considerations addressed
- ✅ Error handling is appropriate

**Common Feedback**:
- "Consider extracting this into a separate method"
- "Add unit tests for this edge case"
- "This could throw NPE, add null check"
- "Update API documentation"

---

### Versioning and Releases

SPIRITBLADE follows **Semantic Versioning** (SemVer) for all releases.

#### Semantic Versioning

Format: `MAJOR.MINOR.PATCH` (e.g., `0.1.0`)

- **MAJOR** (0 → 1): Breaking API changes, major architectural changes
- **MINOR** (0.1 → 0.2): New features, backwards compatible
- **PATCH** (0.1.0 → 0.1.1): Bug fixes, security patches

**Development Versions**: Use `-SNAPSHOT` suffix (e.g., `0.2.0-SNAPSHOT`)

---

#### Release History

| Version | Release Date | Description | DockerHub |
|---------|--------------|-------------|-----------|
| **0.1.0** | Dec 15, 2024 | ✅ **Basic Functionality**: User authentication, summoner search, match history, admin panel, Docker deployment | [spiritblade:0.1.0](https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade/tags) |
| **0.2.0** | Mar 1, 2025 | 📋 **Intermediate Features** (Planned): Advanced analytics with Chart.js, notes system, favorites with notifications, admin moderation dashboard | - |
| **1.0.0** | Apr 15, 2025 | 📋 **Advanced Features** (Planned): Global community statistics, intelligent recommendations, custom rankings, predictive analysis (tentative) | - |

**Current Status**: v0.1.0 released, v0.2.0 in planning

---

#### Release Process

**Prerequisites**:
- All tests passing (CI green)
- SonarCloud quality gate passed
- Documentation updated
- CHANGELOG prepared

**Steps to Create a Release**:

1. **Pre-Release: Update Version**
   
   Use the provided scripts to update version across all files:
   
   ```powershell
   # PowerShell (Windows)
   .\scripts\update-version.ps1 0.2.0
   ```
   
   ```bash
   # Bash (Linux/Mac)
   bash scripts/update-version.sh 0.2.0
   ```
   
   This updates:
   - `backend/pom.xml`
   - `frontend/package.json`
   - `docker/docker-compose.yml`

2. **Commit Version Bump**:
   ```bash
   git add .
   git commit -m "chore: bump version to 0.2.0"
   git push origin main
   ```

3. **Create Git Tag**:
   ```bash
   git tag -a 0.2.0 -m "Release v0.2.0: Intermediate features"
   git push origin 0.2.0
   ```

4. **Create GitHub Release**:
   - Navigate to: `https://github.com/codeurjc-students/2025-SPIRITBLADE/releases/new`
   - Select tag: `0.2.0`
   - Title: `SPIRITBLADE v0.2.0 - Intermediate Features`
   - Description (Changelog):
     ```markdown
     ## ✨ New Features
     - Advanced performance analytics with Chart.js graphs
     - Personal notes system for matches
     - Enhanced favorites management with notifications
     
     ## 🐛 Bug Fixes
     - Fixed summoner search caching issues
     - Corrected JWT token expiration handling
     
     ## 📦 Deployment
     Docker images:
     - `jorgeandresecheverriagarcia/2025-spiritblade:0.2.0`
     - `jorgeandresecheverriagarcia/2025-spiritblade:latest`
     ```
   - Publish release

5. **Automatic Deployment**:
   - `deploy-release.yml` workflow triggers automatically
   - Builds and pushes Docker images:
     - `spiritblade:0.2.0`
     - `spiritblade:latest` (updated)
   - Publishes Docker Compose OCI artifacts

6. **Post-Release: Prepare Next Iteration**:
   ```powershell
   # Update to next SNAPSHOT version
   .\scripts\update-version.ps1 0.3.0-SNAPSHOT
   
   git add .
   git commit -m "chore: prepare for next development iteration 0.3.0-SNAPSHOT"
   git push origin main
   ```

7. **Announce Release**:
   - Update project blog
   - Notify users
   - Update deployment documentation

---

#### Continuous Delivery Workflows

The project uses **GitHub Actions** for automated deployment:

**1. Deploy Dev (Continuous Delivery to Dev)**:
- **Trigger**: Merge to `main` branch
- **Workflow**: `.github/workflows/deploy-dev.yml`
- **Artifacts**:
  - Docker image: `spiritblade:dev`
  - Compose OCI: `spiritblade-compose:dev`
- **Purpose**: Automatic development builds for testing

**2. Deploy Release (Production Releases)**:
- **Trigger**: GitHub Release created
- **Workflow**: `.github/workflows/deploy-release.yml`
- **Artifacts**:
  - Docker image: `spiritblade:<version>` (e.g., `0.1.0`)
  - Docker image: `spiritblade:latest` (updated)
  - Compose OCI with version tags
- **Purpose**: Official versioned releases

**3. Manual Build (Ad-hoc Builds)**:
- **Trigger**: Manual execution via workflow_dispatch
- **Workflow**: `.github/workflows/manual-build.yml`
- **Parameters**: Branch/commit to build
- **Artifacts**: Docker image with custom tag `<branch>-<timestamp>-<commit>`
- **Purpose**: Feature branch testing, hotfixes

---

#### Version Management Scripts

Located in `scripts/`:

**PowerShell (Windows)**: `update-version.ps1`
```powershell
# Usage
.\scripts\update-version.ps1 <new-version>

# Examples
.\scripts\update-version.ps1 0.2.0
.\scripts\update-version.ps1 0.2.0-SNAPSHOT
```

**Bash (Linux/Mac)**: `update-version.sh`
```bash
# Usage
bash scripts/update-version.sh <new-version>

# Examples
bash scripts/update-version.sh 0.2.0
bash scripts/update-version.sh 0.2.0-SNAPSHOT
```

These scripts automatically update version numbers in:
- Maven POM (`backend/pom.xml`)
- NPM package (`frontend/package.json`)
- Docker Compose (`docker/docker-compose.yml`)

---

#### DockerHub Artifacts

All releases are published to DockerHub:

**Repository**: [`jorgeandresecheverriagarcia/2025-spiritblade`](https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade)

**Available Tags**:
- `latest` - Latest stable release (currently 0.1.0)
- `0.1.0` - Specific version (immutable)
- `dev` - Latest development build from `main`
- Custom tags for manual builds

**Pull Image**:
```bash
docker pull jorgeandresecheverriagarcia/2025-spiritblade:latest
docker pull jorgeandresecheverriagarcia/2025-spiritblade:0.1.0
docker pull jorgeandresecheverriagarcia/2025-spiritblade:dev
```

---

#### Release Checklist

Before creating a release, ensure:

- [ ] All features for the milestone are completed
- [ ] All tests pass locally and in CI
- [ ] Code coverage meets thresholds (≥55% backend, ≥50% frontend)
- [ ] SonarCloud quality gate passed
- [ ] Documentation updated (README, Funcionalidades.md, API.md)
- [ ] CHANGELOG prepared with features, fixes, breaking changes
- [ ] Manual testing completed
- [ ] Security vulnerabilities resolved
- [ ] Version numbers updated across all files
- [ ] Git tag created and pushed
- [ ] GitHub Release created with detailed notes
- [ ] Docker images published to DockerHub
- [ ] Post-release version bump committed (`-SNAPSHOT`)
- [ ] Release announced (blog, notifications)

---

### Development Environment Setup

**Required Tools**:
- **Java 21 JDK** - AdoptOpenJDK, Oracle JDK, or Eclipse Temurin
- **Node.js 18+** - For Angular development
- **Git** - Version control
- **Maven** - Included wrapper (`mvnw.cmd`)
- **Docker** - For containerized deployment (optional for dev)
- **MySQL** - Production database (optional for dev, H2 can be used)

**Recommended IDEs**:
- **VS Code** with extensions:
  - Extension Pack for Java
  - Angular Language Service
  - REST Client
  - Docker
- **IntelliJ IDEA** (especially for backend)

**Optional Tools**:
- **Postman** - API testing
- **MySQL Workbench** - Database management
- **Git GUI** - GitKraken, Sourcetree

---

## Code Execution and Editing

### Prerequisites

Before starting development, ensure you have these tools installed:

| Tool | Version | Purpose | Download |
|------|---------|---------|----------|
| **Java JDK** | 21+ | Backend runtime | [Eclipse Temurin](https://adoptium.net/) |
| **Node.js** | 18+ | Frontend build | [nodejs.org](https://nodejs.org/) |
| **Git** | Latest | Version control | [git-scm.com](https://git-scm.com/) |
| **Maven** | 3.9+ | Backend build (included wrapper) | [maven.apache.org](https://maven.apache.org/) |
| **MySQL** | 8.0+ | Database (optional for dev) | [mysql.com](https://www.mysql.com/) |
| **Docker** | Latest | Containerization (optional) | [docker.com](https://www.docker.com/) |

**Verify installations**:
```powershell
# PowerShell commands
java -version      # Should show 21.x
node -v            # Should show v18.x or higher
git --version      # Any recent version
mvn -version       # Should show 3.9.x (or use mvnw)
mysql --version    # Should show 8.0.x (if installed)
docker --version   # Should show 20.x or higher (if installed)
```

---

### Clone the Repository

```bash
# HTTPS (recommended for read-only)
git clone https://github.com/JorgeAndresEcheverria/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE

# SSH (if you have SSH keys configured)
git clone git@github.com:JorgeAndresEcheverria/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE
```

**Verify structure**:
```powershell
ls
# Should see: backend/, frontend/, docs/, .github/, README.md
```

---

### Local Development Setup

#### Option 1: Development with H2 In-Memory Database (Simplest)

**Advantages**:
- ✅ No database installation needed
- ✅ Fast startup
- ✅ Automatic schema creation

**Limitations**:
- ⚠️ Data lost on restart
- ⚠️ Not identical to production (MySQL)

**Configuration**:
No configuration needed! Backend uses H2 by default.

**Start Backend**:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Start Frontend**:
```bash
cd frontend
npm ci              # Install dependencies (first time only)
npm start           # Start dev server with hot reload
```

**Access**:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:testdb`)

---

#### Option 2: Development with MySQL (Production-like)

**Advantages**:
- ✅ Persistent data
- ✅ Identical to production
- ✅ Better for testing

**Setup MySQL**:

1. **Install MySQL 8.0** (if not installed)
2. **Create database**:
   ```sql
   CREATE DATABASE spiritblade_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'spiritblade'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON spiritblade_db.* TO 'spiritblade'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure Backend**:
   
   Edit `backend/src/main/resources/application.properties`:
   ```properties
   # Comment out H2 configuration
   # spring.datasource.url=jdbc:h2:mem:testdb
   
   # Add MySQL configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/spiritblade_db
   spring.datasource.username=spiritblade
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
   spring.jpa.hibernate.ddl-auto=update
   ```

4. **Add Riot API Key** (required for summoner search):
   ```properties
   # Get your key from https://developer.riotgames.com/
   riot.api.key=RGAPI-YOUR-KEY-HERE
   riot.api.region=euw1
   ```

5. **Start Backend**:
   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

---

#### Option 3: Docker Compose (Full Stack)

**Advantages**:
- ✅ One command setup
- ✅ Isolated environment
- ✅ Identical to production

**Prerequisites**: Docker and Docker Compose installed

**Setup**:

1. **Create `.env` file** in project root:
   ```env
   MYSQL_ROOT_PASSWORD=rootpassword
   MYSQL_DATABASE=spiritblade_db
   MYSQL_USER=spiritblade
   MYSQL_PASSWORD=spiritbladepass
   
   RIOT_API_KEY=RGAPI-YOUR-KEY-HERE
   JWT_SECRET=your-secret-key-min-256-bits
   ```

2. **Start all services**:
   ```bash
   docker-compose up
   ```

3. **Access**:
   - Application: http://localhost:8080
   - MySQL: localhost:3306 (username: spiritblade, password: spiritbladepass)

See [Ejecucion.md](Ejecucion.md) for complete Docker deployment guide.

---

### IDE Setup

#### VS Code (Recommended for Frontend + Backend)

**Install Extensions**:
1. **Extension Pack for Java** (Microsoft) - Java language support, debugging, Maven
2. **Angular Language Service** (Angular) - TypeScript autocomplete, templates
3. **REST Client** (Huachao Mao) - Test API endpoints without Postman
4. **Docker** (Microsoft) - Dockerfile syntax, container management
5. **GitLens** (GitKraken) - Advanced Git features
6. **SonarLint** (SonarSource) - Real-time code quality

**Open Workspace**:
```powershell
code .
```

**Debugging**:
- Backend: Use Maven lifecycle → `spring-boot:run` in debug mode
- Frontend: `npm start` + Chrome DevTools

---

#### IntelliJ IDEA (Recommended for Backend)

**Setup**:
1. Open `backend/pom.xml` as project
2. IDEA auto-detects Spring Boot
3. Configure JDK 21 in Project Structure
4. Run configuration created automatically

**Advantages**:
- Better Java refactoring
- Superior Spring Boot support
- Database tools included

---

### Testing

#### Backend Tests

**Run all tests**:
```powershell
cd backend
.\mvnw.cmd test
```

**Run specific test class**:
```powershell
.\mvnw.cmd test -Dtest=RiotServiceTest
```

**Run with coverage**:
```powershell
.\mvnw.cmd test jacoco:report
```
Report: `backend/target/site/jacoco/index.html`

**Run integration tests only**:
```powershell
.\mvnw.cmd verify -P integration-tests
```

---

#### Frontend Tests

**Run tests (watch mode)**:
```bash
cd frontend
npm test
```
Tests re-run automatically on file changes.

**Run tests once (CI mode)**:
```bash
npm run test:ci
```

**Run with coverage**:
```bash
npm run test:coverage
```
Report: `frontend/coverage/index.html`

**Run specific test file**:
```bash
npm test -- --include='**/summoner.component.spec.ts'
```

---

### API Testing

#### Using REST Client (VS Code Extension)

Create `test.http` file:
```http
### Login
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}

### Search Summoner (requires token from login)
GET http://localhost:8080/summoners/search?gameName=Hide on bush&tagLine=KR
Authorization: Bearer {{token}}
```

Click "Send Request" above each request.

---

#### Using Postman

**Import Collection** (when available):
1. Open Postman
2. Import → `docs/postman/SPIRITBLADE.postman_collection.json`
3. Set environment variable `baseUrl` = `http://localhost:8080`

**Manual Testing**:
1. **Login**: POST `/auth/login` with credentials
2. **Copy token** from response
3. **Set Authorization**: Bearer Token in subsequent requests
4. **Test endpoints**: See `docs/API.md` for all endpoints

---

#### Using curl (PowerShell)

**Login**:
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin"}'

$token = $response.token
```

**Search Summoner**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/summoners/search?gameName=Hide on bush&tagLine=KR" `
  -Headers @{"Authorization"="Bearer $token"}
```

---

### Build for Production

#### Backend JAR

```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
```
Output: `backend/target/tfg-0.0.1-SNAPSHOT.jar`

**Run JAR**:
```powershell
java -jar backend/target/tfg-0.0.1-SNAPSHOT.jar
```

---

#### Frontend Build

```bash
cd frontend
npm run build --prod
```
Output: `frontend/dist/frontend/`

**Serve Locally** (testing):
```bash
npx http-server dist/frontend -p 8081
```

---

#### Docker Image

**Build multi-stage image**:
```bash
docker build -t spiritblade:local .
```

**Run container**:
```bash
docker run -p 8080:8080 \
  -e RIOT_API_KEY=your-key \
  -e JWT_SECRET=your-secret \
  spiritblade:local
```

---

### Troubleshooting

#### Backend won't start

**Error**: `Port 8080 already in use`
- **Solution**: Kill process using port 8080:
  ```powershell
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  ```

**Error**: `Could not find or load main class`
- **Solution**: Clean and rebuild:
  ```powershell
  .\mvnw.cmd clean install
  ```

**Error**: `401 Unauthorized from Riot API`
- **Solution**: Check `riot.api.key` in `application.properties`, get new key from https://developer.riotgames.com/

---

#### Frontend won't start

**Error**: `npm: command not found`
- **Solution**: Install Node.js from https://nodejs.org/

**Error**: `Cannot find module '@angular/core'`
- **Solution**: Reinstall dependencies:
  ```bash
  rm -rf node_modules package-lock.json
  npm install
  ```

**Error**: `Port 4200 already in use`
- **Solution**: Use different port:
  ```bash
  npm start -- --port 4201
  ```

---

#### Database Issues

**Error**: `Access denied for user`
- **Solution**: Check MySQL credentials in `application.properties`

**Error**: `Unknown database 'spiritblade_db'`
- **Solution**: Create database:
  ```sql
  CREATE DATABASE spiritblade_db;
  ```

**Error**: `Table doesn't exist`
- **Solution**: Enable auto schema creation:
  ```properties
  spring.jpa.hibernate.ddl-auto=update
  ```

---

### Hot Reload / Live Reload

**Backend** (Spring Boot DevTools):
- Automatically included in `pom.xml`
- Restart on classpath changes
- No manual restart needed

**Frontend** (Angular CLI):
- Automatic with `npm start`
- Browser refreshes on file save
- Hot Module Replacement (HMR) enabled

---

### Code Formatting

**Backend** (Java):
- Use IntelliJ IDEA auto-format: `Ctrl+Alt+L`
- Or VS Code format: `Shift+Alt+F`

**Frontend** (TypeScript):
- Configured in `tsconfig.json` and `.editorconfig`
- Auto-format on save in VS Code:
  ```json
  "editor.formatOnSave": true
  ```

---

### Next Steps

1. ✅ Set up local development environment
2. ✅ Run backend and frontend
3. ✅ Test API with Postman or REST Client
4. ✅ Run tests to verify setup
5. 📖 Read [API.md](API.md) for endpoint documentation
6. 🚀 Start developing features!

**Additional Resources**:
- [Inicio-Proyecto.md](Inicio-Proyecto.md) - Project objectives and phases
- [Funcionalidades.md](Funcionalidades.md) - Feature descriptions
- [Seguimiento.md](Seguimiento.md) - Development process and metrics
- [Ejecucion.md](Ejecucion.md) - Docker deployment guide

---

## Summary

This development guide covers:
- ✅ **Technologies**: Spring Boot 3.4.3, Angular 17, MySQL 8.0
- ✅ **Architecture**: Domain model, REST API, layered backend, Angular client, Docker deployment
- ✅ **Quality Control**: Testing pyramid, coverage metrics, SonarCloud, CI/CD
- ✅ **Development Process**: Agile methodology, Git workflow, PR process, release management
- ✅ **Execution**: Local setup (H2/MySQL/Docker), IDE configuration, testing, troubleshooting

---

## Links and Resources

**Project Links**:
- 🐙 **Repository**: https://github.com/JorgeAndresEcheverria/2025-SPIRITBLADE
- 📝 **Blog**: https://jorgeandrescheverria.blogspot.com/search/label/tfg
- 🔍 **SonarCloud**: https://sonarcloud.io/project/overview?id=JorgeAndresEcheverria_2025-SPIRITBLADE
- 🐳 **DockerHub**: https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade

**Documentation**:
- [README.md](../README.md) - Main project page
- [API.md](API.md) - REST API documentation
- [Funcionalidades.md](Funcionalidades.md) - Feature descriptions
- [Ejecucion.md](Ejecucion.md) - Docker deployment guide
- [Seguimiento.md](Seguimiento.md) - Quality control and metrics
- [Inicio-Proyecto.md](Inicio-Proyecto.md) - Project objectives and phases
- [Autores.md](Autores.md) - Team information

**External Documentation**:
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Angular Documentation](https://angular.io/docs)
- [Riot Games API](https://developer.riotgames.com/docs/lol)

---

## Authorship

**Developer**: Jorge Andrés Echevarría  
**Advisor**: Iván Chicano Capelo  
**University**: Universidad Rey Juan Carlos (URJC)  
**Course**: 2024-2025

**Contact**: j.echeverria.2021@alumnos.urjc.es

See [Autores.md](Autores.md) for full authorship information.

---

**Last Updated**: January 2025 (v0.1)

**[← Back to Main README](../README.md)** | **[View All Documentation →](../README.md#documentación)**
