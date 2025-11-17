# Seguimiento — SPIRITBLADE

Este documento describe las estrategias de control de calidad, el proceso de desarrollo y las métricas del proyecto SPIRITBLADE.

---

## 📊 Control de calidad

### Estrategia de pruebas

La aplicación implementa una pirámide de pruebas con varios niveles:

```
       /\
      /E2E\        ← Pruebas de extremo a extremo (sistema completo)
     /------\
    / Integr \     ← Pruebas de integración (APIs + BD)
     /----------\
    /   Unit    \   ← Pruebas unitarias (lógica aislada)
   /--------------\
```

#### Pruebas unitarias

Backend (JUnit 5 + Mockito)
- Ubicación: `backend/src/test/java/.../unit/`
- Objetivo: probar lógica de negocio aislada
- Mockear dependencias (repositorios, APIs externas)
- Objetivo de cobertura: ≥60%

Frontend (Jasmine + Karma)
- Ubicación: `frontend/src/app/**/*.spec.ts`
- Objetivo: probar componentes y servicios Angular
- Mockear HttpClient, servicios y enrutamiento
- Objetivo de cobertura: ≥50%

Ejemplos implementados:
- `UserServiceSimpleUnitTest` — lógica de creación de usuario
- `SummonerMapperTest` — mapeo DTO ↔ entidad
- `AuthService.spec.ts` — servicio de autenticación Angular
- `LoginComponent.spec.ts` — componente de inicio de sesión

---

#### Pruebas de integración

Backend (Spring Boot Test)
- Ubicación: `backend/src/test/java/.../integration/`
- Objetivo: probar la integración entre capas
- Contexto completo de Spring con `@SpringBootTest`
- Base de datos en memoria H2
- MockMvc para simular peticiones HTTP

Ejemplos implementados:
- `SummonerIntegrationTest` — CRUD de summoner + caché
- `AuthIntegrationTest` — flujo completo de autenticación
- `AdminControllerIntegrationTest` — endpoints de administración

Frontend (utilidades de testing de Angular)
- Ubicación: `frontend/src/app/integration/`
- Objetivo: probar la interacción entre componentes y servicios
- TestBed para configurar módulos
- HttpClientTestingModule para APIs simuladas

---

#### Pruebas de sistema (E2E)

Selenium WebDriver
- Ubicación: `backend/src/test/java/.../e2e/`
- Objetivo: validar flujos de usuario de extremo a extremo
- Automatización con Chrome en modo headless
- Verificar UI + backend + BD

Escenarios implementados:
- `SummonerE2ETest` — búsqueda de summoner de extremo a extremo
- Verifica navegación, carga de datos y rendimiento

Estado: 🚧 En progreso. E2E completo planificado para v0.2.

---

### Métricas de cobertura

| Componente | Cobertura actual | Objetivo | Estado |
|-----------:|-----------------:|--------:|:------:|
| Backend | ~55% | ≥60% | 🟡 Casi |
| Frontend | ~48% | ≥50% | 🟡 Casi |
| Global | ~52% | ≥55% | ✅ Cumplido |

Herramientas:
- Backend: JaCoCo (informes HTML en `target/site/jacoco/`)
- Frontend: karma-coverage (informes en `coverage/`)

---

### Análisis estático (SonarCloud)

Configuración:
- Integrado en GitHub Actions (`.github/workflows/build.yml`)
- El análisis se ejecuta en cada PR a `main`
- Quality Gate configurada

Métricas objetivo:
- Bugs: 0
- Vulnerabilidades de seguridad: 0
- Code smells: <50
- Duplicación de código: <5%
- Cobertura: ≥55%
- Deuda técnica: < 1 día

Estado actual: ✅ Quality Gate: APROBADA

Acceso: [SonarCloud - SPIRITBLADE](https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE)

---

### Mejoras de calidad aplicadas (code smells resueltos)

Backend:
- ✅ Reemplazado `e.printStackTrace()` por logging SLF4J
- ✅ Evitar capturas genéricas: ahora se captura `HttpClientErrorException` específico
- ✅ Usar `Collections.emptyList()` en lugar de `new ArrayList<>()`
- ✅ Mejora de logs: warn + debug con stacktrace
- ✅ Devolver cadena vacía en lugar de null para URLs
- ✅ Manejar excepciones en el flujo de refresh token devolviendo 401 Unauthorized

Frontend:
- ✅ Reemplazado `console.error()` por `console.debug()` donde procede
- ✅ Mostrar mensajes de error amigables al usuario en la UI en lugar de solo loguear en consola
- ✅ Mejor manejo de errores HTTP con mensajes informativos

---

## 🔄 Proceso de desarrollo

### Metodología

El proyecto sigue un proceso iterativo e incremental con principios ágiles:

- Iteraciones cortas: sprints de 2–3 semanas
- Entregas incrementales: versión funcional al final de cada fase
- Integración continua: tests automatizados en cada commit
- Feedback rápido: revisión de código y despliegues automatizados

### Fases del proyecto

```
Fase 1: Definición (Sep)          ✅ Completada
Fase 2: Configuración & CI (Oct)  ✅ Completada
Fase 3: v0.1 Core (Dec)           ✅ Completada
├─ Hito 0.1.0: funcionalidades core
├─ Despliegue con Docker
└─ Workflows de CI/CD

Fase 4: v0.2 Intermedia (Mar)     📋 Planificada
├─ Gráficas y análisis avanzado
├─ Sistema de favoritos
└─ Notificaciones

Fase 5: v1.0 Avanzada (Abr)       📋 Planificada
├─ Predicciones ML
├─ Recomendaciones
└─ Clasificaciones personalizadas

Fase 6: Documentación (May)       📋 Planificada
Fase 7: Defensa (Jun)             📋 Planificada
```

---

### Gestión de tareas (GitHub)

Issues de GitHub:
- Etiquetas: `bug`, `enhancement`, `documentation`, `good first issue`
- Plantillas para bugs y features
- Asignación de responsables

GitHub Projects:
- Columnas del tablero Kanban:
  - Backlog
  - In Progress
  - In Review
  - Done

Hitos:
- v0.1.0 — funcionalidades core (✅ Completado)
- v0.2.0 — funcionalidades intermedias (📋 Planificado)
- v1.0.0 — funcionalidades avanzadas (📋 Planificado)

Enlace: [GitHub Projects](https://github.com/codeurjc-students/2025-SPIRITBLADE/projects)

---

### Control de versiones (Git)

Estrategia de ramas:

```
main (producción)
  ├─ feature/summoner-search     ✅ Merged
  ├─ feature/auth-jwt            ✅ Merged
  ├─ feature/admin-panel         ✅ Merged
  ├─ feature/docker-deployment   ✅ Merged
  ├─ hotfix/fix-api-timeout      ✅ Merged
  └─ CodeSmells-&-Tests          🚧 En progreso
```

Reglas:
- `main` está protegida: requiere pull request
- Los commits deben pasar CI antes de merge
- Revisión de código obligatoria
- Squash de commits al merge

Ejemplos de commits convencionales:

```
feat: add summoner search
fix: correct winrate calculation bug
docs: update README with Docker instructions
test: add unit tests for UserService
refactor: improve exception handling in RiotService
chore: bump version to 0.1.0
```

Métricas:
- Commits totales: ~80
- Ramas activas: 2–3
- PRs mergeados: ~15
- Colaboradores: 1

---

### CI/CD

Workflows de GitHub Actions

1) `build.yml` — Control de calidad
Trigger: push a cualquier rama, PR a main
Acciones:
- Build del backend (Maven)
- Build del frontend (npm)
- Tests unitarios (JUnit + Jasmine)
- Pruebas de integración
- Cobertura con JaCoCo + karma-coverage
- Análisis en SonarCloud (PRs a main)

2) `deploy-dev.yml` — despliegue automático
Trigger: push a `main`
Acciones:
- Construcción de imagen Docker multi-stage
- Push a DockerHub con tag `dev`
- Publicar docker-compose como artefacto OCI

3) `deploy-release.yml` — despliegue de release
Trigger: GitHub Release
Acciones:
- Construcción de imagen Docker
- Push con tag de versión (p.ej. `0.1.0`)
- Actualizar tag `latest`
- Publicar docker-compose versionado

4) `manual-build.yml` — build manual
Trigger: workflow_dispatch
Acciones:
- Construir imagen con tag personalizado: `<branch>-<timestamp>-<commit>`
- Push a DockerHub

Workflows reutilizables: `deploy-dev` y `deploy-release` llaman a `build-push.yml` para evitar duplicación.

Secrets configurados:
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `SONAR_TOKEN`

Badge de estado de CI disponible en la página de actions del repo.

---

### Versionado

Estrategia: Semantic Versioning (`MAJOR.MINOR.PATCH`)

- MAJOR: cambios incompatibles en la API
- MINOR: nuevas funcionalidades compatibles
- PATCH: correcciones de bugs

Versiones publicadas:
- v0.1.0 (Dic 2024) — primera release funcional con Docker

Próximas:
- v0.2.0 (Mar 2025) — funcionalidades intermedias
- v1.0.0 (Abr 2025) — funcionalidades avanzadas

Proceso de release:
1) Pre-release: actualizar versiones con `update-version.ps1/sh`
2) Commit & tag: `git commit -m "chore: bump version" && git tag 0.1.0`
3) Push: `git push && git push --tags`
4) Crear GitHub Release con changelog
5) Post-release: subir a siguiente SNAPSHOT (`0.2.0-SNAPSHOT`)

Documentación: [RELEASE-PROCESS.md](RELEASE-PROCESS.md)

---

## 📈 Métricas del proyecto

### Líneas de código

| Componente | Lenguaje | Archivos | Líneas |
|----------:|---------:|--------:|------:|
| Backend | Java | ~40 | ~3,500 |
| Frontend | TypeScript | ~30 | ~2,500 |
| Tests | Java/TS | ~25 | ~2,000 |
| Config | YAML/JSON/XML | ~15 | ~800 |
| **TOTAL** | - | **~110** | **~8,800** |

### Estadísticas de desarrollo

- Duración: ~4 meses (Sep–Dic 2024)
- Commits: ~80
- Pull requests: ~15
- Issues cerrados: ~25
- Releases: 1 (v0.1.0)

### Rendimiento

- Tiempo de build: ~3 minutos (CI)
- Tamaño imagen Docker: ~180MB
- Tiempo de arranque: ~30s
- Tiempo de respuesta API: <500ms (p95)

---

## 🔗 Enlaces de seguimiento

- Repo GitHub: https://github.com/codeurjc-students/2025-SPIRITBLADE
- GitHub Actions: https://github.com/codeurjc-students/2025-SPIRITBLADE/actions
- SonarCloud: https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE
- DockerHub: https://hub.docker.com/r/codeurjcstudents/spiritblade
- Blog del proyecto: https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3

---

[← Volver al README principal](../README.md)
