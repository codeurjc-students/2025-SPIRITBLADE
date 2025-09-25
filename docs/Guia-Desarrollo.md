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

### Deployment (To Be Specified)
The application follows a three-tier architecture deployed independently:

```
[Angular Client] <--HTTP/HTTPS--> [Spring Boot Server] <--JDBC--> [MySQL Database]
     Port 4200                        Port 8080                    Port 3306
```

- **Frontend**: Angular application served as static files
- **Backend**: Spring Boot server that exposes REST API
- **Database**: MySQL Server with relational schema

### REST API
The REST API is documented and can be viewed through:
- Manual documentation in `docs/API.md`
- Planned: OpenAPI/Swagger specification exported as static HTML

Main endpoints:
- `/auth/*` - Authentication and authorization
- `/users/*` - User management
- `/summoners/*` - Summoner information
- `/dashboard/*` - Personalized statistics
- `/admin/*` - Administrative functions

---

## Quality Control

### Automated Tests

**Backend (Java)**
- **Unit**: JUnit 5 + Mockito for isolated business logic
- **Integration**: Spring Boot Test for tests with full context
- **System**: E2E tests of critical endpoints
- Location: `backend/src/test/java/`

**Frontend (Angular)**
- **Unit**: Jasmine + Karma for components and services
- **Integration**: Angular Testing Utilities for component interactions
- Location: `frontend/src/app/**/*.spec.ts`

### Tested Functionalities
- JWT Authentication (login, logout, token validation)
- Role-based access control (USER/ADMIN)
- CRUD operations for users and summoners
- Integration with Riot Games external API
- Correct rendering of Angular components

### Test Statistics
- **Backend coverage target**: ≥ 60%
- **Frontend coverage target**: ≥ 50%
- **Tools**: JaCoCo (backend), karma-coverage (frontend)

### Static Analysis
- **SonarCloud** for detecting code smells, bugs, and vulnerabilities
- **Target metrics**: 0 critical bugs, 0 security vulnerabilities

---

## Development Process

### Methodology
Iterative and incremental process following agile principles:
- **Planned phases**: 7 phases from definition to TFG defense
- **Short iterations** with incremental deliverables
- **Continuous integration** for rapid feedback

### Task Management
- **GitHub Issues** for bug and feature tracking
- **GitHub Projects** with Kanban board for progress visualization
- **Milestones** associated with development phases

### Version Control (Git)
- **Branch strategy**:
  - `main`: protected branch for production
  - `feature/*`: feature development branches
- **Current metrics** (in development):
  - Commits: >50
  - Active branches: 2-3
  - `main` protection with mandatory PR reviews

### Continuous Integration (GitHub Actions)
Automated workflows in `.github/workflows/build.yml`:
- **Basic Quality Control**: compilation + unit tests (feature branches)
- **Complete Quality Control**: all tests + coverage + static analysis (PRs to main)
- **Artifacts**: coverage reports automatically uploaded

---

## Code Execution and Editing (To Be Specified)

### Prerequisites
- Java 21 JDK
- Node.js >= 18
- Git
- MySQL Server (optional for development, H2 can be used)

### Clone the Repository
```bash
git clone https://github.com/codeurjc-students/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE
```

### Execution

#### Database (Development)
For development, you can use H2 in-memory (default configuration) or local MySQL:

**Option 1: H2 (no additional configuration)**
- Starts automatically with the backend

**Option 2: Local MySQL**
```sql
CREATE DATABASE spiritblade_db;
```
Modify `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/spiritblade_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

#### Backend (Windows PowerShell)
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
The server will be available at: http://localhost:8080

#### Frontend
```bash
cd frontend
npm ci
npm start
```
The application will be available at: http://localhost:4200

### Tool Usage

#### VS Code Development
1. Install recommended extensions:
   - Extension Pack for Java
   - Angular Language Service
   - REST Client

2. Open the complete workspace or separate folders for backend/frontend

#### REST API Interaction
- **Recommended tool**: Postman
- **Included collection**: `docs/postman/SPIRITBLADE.postman_collection.json` (pending creation)
- **Usage examples**: See `docs/API.md`

Login request example:
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

### Test Execution

#### Backend
```powershell
cd backend
# All tests
.\mvnw.cmd test

# Unit tests only
.\mvnw.cmd test -Dtest="**/*Test"

# With coverage
.\mvnw.cmd test jacoco:report
```

#### Frontend
```bash
cd frontend
# Unit tests (watch mode)
npm test

# Tests in CI mode (single execution)
npm run test:ci

# Tests with coverage
npm run test:coverage
```

### Creating a Release
1. **Complete all tests**: Ensure CI passes on `main`
2. **Create tag**: `git tag -a v1.0.0 -m "Release v1.0.0"`
3. **Push tag**: `git push origin v1.0.0`
4. **GitHub Release**: Create release from web interface with changelog

---

## Links and Authorship
- **Repository**: https://github.com/codeurjc-students/2025-SPIRITBLADE
- **Author**: Jorge Andrés Echevarría
- **Advisor**: Iván Chicano Capelo
