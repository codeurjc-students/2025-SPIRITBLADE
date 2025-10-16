# Seguimiento - SPIRITBLADE

Este documento describe las estrategias de control de calidad, proceso de desarrollo, y métricas del proyecto SPIRITBLADE.

---

## 📊 Control de Calidad

### Estrategia de Testing

La aplicación implementa una pirámide de testing con múltiples niveles:

```
           /\
          /E2E\        ← Tests End-to-End (Sistema completo)
         /------\
        / Integr \     ← Tests de Integración (APIs + BD)
       /----------\
      /   Unitarios \   ← Tests Unitarios (Lógica aislada)
     /--------------\
```

#### Tests Unitarios

**Backend (JUnit 5 + Mockito)**
- Ubicación: `backend/src/test/java/.../unit/`
- Objetivo: Probar lógica de negocio aislada
- Mock de dependencias (repositories, external APIs)
- Cobertura objetivo: ≥60%

**Frontend (Jasmine + Karma)**
- Ubicación: `frontend/src/app/**/*.spec.ts`
- Objetivo: Probar componentes y servicios Angular
- Mock de HttpClient, servicios, routing
- Cobertura objetivo: ≥50%

**Ejemplos implementados**:
- `UserServiceSimpleUnitTest` - Lógica de creación de usuarios
- `SummonerMapperTest` - Transformación DTO ↔ Entity
- `AuthService.spec.ts` - Servicio de autenticación Angular
- `LoginComponent.spec.ts` - Componente de login

---

#### Tests de Integración

**Backend (Spring Boot Test)**
- Ubicación: `backend/src/test/java/.../integration/`
- Objetivo: Probar integración entre capas
- Context completo de Spring con `@SpringBootTest`
- Base de datos H2 in-memory
- MockMvc para simular peticiones HTTP

**Ejemplos implementados**:
- `SummonerIntegrationTest` - CRUD de invocadores + cache
- `AuthIntegrationTest` - Flujo de autenticación completo
- `AdminControllerIntegrationTest` - Endpoints administrativos

**Frontend (Angular Testing Utilities)**
- Ubicación: `frontend/src/app/integration/`
- Objetivo: Probar interacción entre componentes y servicios
- TestBed para configurar módulos
- HttpClientTestingModule para APIs simuladas

---

#### Tests de Sistema (E2E)

**Selenium WebDriver**
- Ubicación: `backend/src/test/java/.../e2e/`
- Objetivo: Validar flujos de usuario completos
- Chrome headless automatizado
- Verificación de UI + Backend + BD

**Escenarios implementados**:
- `SummonerE2ETest` - Búsqueda de invocador de punta a punta
- Verificación de navegación, carga de datos, rendimiento

**Estado**: 🚧 En desarrollo. E2E completo planificado para v0.2.

---

### Métricas de Cobertura

| Componente | Cobertura Actual | Objetivo | Estado |
|------------|------------------|----------|--------|
| Backend | ~55% | ≥60% | 🟡 Cercano |
| Frontend | ~48% | ≥50% | 🟡 Cercano |
| Global | ~52% | ≥55% | ✅ Cumplido |

**Herramientas**:
- Backend: JaCoCo (genera reportes HTML en `target/site/jacoco/`)
- Frontend: karma-coverage (genera reportes en `coverage/`)

---

### Análisis Estático (SonarCloud)

**Configuración**:
- Integrado en GitHub Actions (`.github/workflows/build.yml`)
- Análisis en cada PR a `main`
- Quality Gate configurado

**Métricas objetivo**:
- 🐛 Bugs: 0
- 🔒 Vulnerabilidades de Seguridad: 0
- 🧹 Code Smells: < 50
- 📊 Duplicación de Código: < 5%
- 📈 Cobertura: ≥55%
- ⏱️ Deuda Técnica: < 1 día

**Estado actual**: ✅ Quality Gate: PASSED

**Acceso**: [SonarCloud - SPIRITBLADE](https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE)

---

### Mejoras de Calidad Aplicadas (Code Smells Resueltos)

**Backend**:
- ✅ Reemplazado `e.printStackTrace()` por logging SLF4J
- ✅ Evitado catch genéricos: captura específica de `HttpClientErrorException`
- ✅ Uso de `Collections.emptyList()` en lugar de `new ArrayList<>()`
- ✅ Logging mejorado: warn + debug stacktrace
- ✅ Return empty string en lugar de null para URLs
- ✅ Manejo de excepciones en refresh token con 401 Unauthorized

**Frontend**:
- ✅ Reemplazado `console.error()` por `console.debug()`
- ✅ Mensajes de error mostrados en UI en lugar de console
- ✅ Manejo de errores HTTP con mensajes informativos

---

## 🔄 Proceso de Desarrollo

### Metodología

El proyecto sigue un proceso **iterativo e incremental** con principios ágiles:

- **Iteraciones cortas**: Sprints de 2-3 semanas
- **Entregas incrementales**: Versión funcional al final de cada fase
- **Integración continua**: Tests automáticos en cada commit
- **Feedback rápido**: Revisión de código y deployment automático

### Fases del Proyecto

```
Fase 1: Definición (Sep)          ✅ Completada
Fase 2: Setup & CI (Oct)          ✅ Completada
Fase 3: v0.1 Básica (Dic)         ✅ Completada
├─ Milestone 0.1.0: Funcionalidades básicas
├─ Docker deployment
└─ CI/CD workflows

Fase 4: v0.2 Intermedia (Mar)     📋 Planificada
├─ Gráficos y análisis avanzado
├─ Sistema de favoritos completo
└─ Notificaciones

Fase 5: v1.0 Avanzada (Abr)       📋 Planificada
├─ ML predictions
├─ Recomendaciones
└─ Rankings personalizados

Fase 6: Documentación (May)       📋 Planificada
Fase 7: Defensa TFG (Jun)         📋 Planificada
```

---

### Gestión de Tareas (GitHub)

**GitHub Issues**:
- Labels: `bug`, `enhancement`, `documentation`, `good first issue`
- Templates para bugs y features
- Asignación de responsables

**GitHub Projects**:
- Tablero Kanban con columnas:
  - 📋 Backlog
  - 🚧 In Progress
  - 👀 In Review
  - ✅ Done

**Milestones**:
- v0.1.0 - Funcionalidades básicas (✅ Completado)
- v0.2.0 - Funcionalidades intermedias (📋 Planificado)
- v1.0.0 - Funcionalidades avanzadas (📋 Planificado)

**Enlace**: [GitHub Projects](https://github.com/codeurjc-students/2025-SPIRITBLADE/projects)

---

### Control de Versiones (Git)

**Estrategia de Branching**:

```
main (producción)
  ├─ feature/summoner-search     ✅ Merged
  ├─ feature/auth-jwt            ✅ Merged
  ├─ feature/admin-panel         ✅ Merged
  ├─ feature/docker-deployment   ✅ Merged
  ├─ hotfix/fix-api-timeout      ✅ Merged
  └─ CodeSmells-&-Tests          🚧 En desarrollo
```

**Reglas**:
- `main` es protegida: requiere Pull Request
- Commits deben pasar CI antes de merge
- Revisión de código obligatoria
- Squash commits al mergear

**Convenciones de commit (Conventional Commits)**:
```
feat: añadir búsqueda de invocadores
fix: corregir error en cálculo de winrate
docs: actualizar README con instrucciones Docker
test: añadir tests unitarios para UserService
refactor: mejorar manejo de excepciones en RiotService
chore: bump version to 0.1.0
```

**Métricas**:
- Total de commits: ~80
- Branches activas: 2-3
- PRs merged: ~15
- Contributors: 1

---

### Integración y Entrega Continua (CI/CD)

**GitHub Actions Workflows**:

#### 1. `build.yml` - Control de Calidad
**Trigger**: Push a cualquier rama, PR a main
**Acciones**:
- ✅ Compilación backend (Maven)
- ✅ Compilación frontend (npm)
- ✅ Tests unitarios (JUnit + Jasmine)
- ✅ Tests de integración
- ✅ Coverage con JaCoCo + karma-coverage
- ✅ SonarCloud analysis (solo en PR a main)

#### 2. `deploy-dev.yml` - Deploy Automático
**Trigger**: Push a `main`
**Acciones**:
- ✅ Build de imagen Docker multistage
- ✅ Push a DockerHub con tag `dev`
- ✅ Publicación de docker-compose como OCI artifact

#### 3. `deploy-release.yml` - Deploy de Releases
**Trigger**: Publicación de GitHub Release
**Acciones**:
- ✅ Build de imagen Docker
- ✅ Push con tag de versión (ej: `0.1.0`)
- ✅ Actualizar tag `latest`
- ✅ Publicar docker-compose con versión

#### 4. `manual-build.yml` - Build Manual
**Trigger**: workflow_dispatch (manual)
**Acciones**:
- ✅ Build de imagen con tag custom: `<branch>-<timestamp>-<commit>`
- ✅ Push a DockerHub
- ✅ Útil para testing de features

**Reusabilidad**: Los workflows `deploy-dev` y `deploy-release` llaman a `build-push.yml` (reusable workflow) para evitar duplicación de código.

**Secretos configurados**:
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `SONAR_TOKEN`

**Estado de CI**: [![Build Status](https://github.com/codeurjc-students/2025-SPIRITBLADE/workflows/CI%2FCD%20-%20Quality%20Control%20%26%20Testing/badge.svg)](https://github.com/codeurjc-students/2025-SPIRITBLADE/actions)

---

### Versionado

**Estrategia**: Semantic Versioning (`MAJOR.MINOR.PATCH`)

- **MAJOR**: Cambios incompatibles de API
- **MINOR**: Nuevas funcionalidades compatibles
- **PATCH**: Bug fixes

**Versiones publicadas**:
- v0.1.0 (Dic 2024) - Primera versión funcional con Docker

**Próximas versiones**:
- v0.2.0 (Mar 2025) - Funcionalidades intermedias
- v1.0.0 (Abr 2025) - Funcionalidades avanzadas

**Proceso de Release**:
1. **Pre-release**: Actualizar versiones con script `update-version.ps1/sh`
2. **Commit & Tag**: `git commit -m "chore: bump version" && git tag 0.1.0`
3. **Push**: `git push && git push --tags`
4. **GitHub Release**: Crear release desde UI con changelog
5. **Post-release**: Actualizar a siguiente SNAPSHOT (`0.2.0-SNAPSHOT`)

**Documentación completa**: [RELEASE-PROCESS.md](RELEASE-PROCESS.md)

---

## 📈 Métricas del Proyecto

### Líneas de Código

| Componente | Lenguaje | Archivos | Líneas |
|------------|----------|----------|--------|
| Backend | Java | ~40 | ~3,500 |
| Frontend | TypeScript | ~30 | ~2,500 |
| Tests | Java/TS | ~25 | ~2,000 |
| Configuración | YAML/JSON/XML | ~15 | ~800 |
| **TOTAL** | - | **~110** | **~8,800** |

### Estadísticas de Desarrollo

- **Duración**: ~4 meses (Sep - Dic 2024)
- **Commits**: ~80
- **Pull Requests**: ~15
- **Issues cerrados**: ~25
- **Releases**: 1 (v0.1.0)

### Performance

- **Build time**: ~3 minutos (CI)
- **Docker image size**: ~180MB
- **Tiempo de arranque**: ~30 segundos
- **API response time**: <500ms (p95)

---

## 🔗 Enlaces de Seguimiento

- **GitHub Repository**: [SPIRITBLADE](https://github.com/codeurjc-students/2025-SPIRITBLADE)
- **GitHub Actions**: [CI/CD Pipelines](https://github.com/codeurjc-students/2025-SPIRITBLADE/actions)
- **SonarCloud**: [Quality Dashboard](https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE)
- **DockerHub**: [spiritblade](https://hub.docker.com/r/codeurjcstudents/spiritblade)
- **Blog del Proyecto**: [Medium](https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3)

---

**[← Volver al README principal](../README.md)**
