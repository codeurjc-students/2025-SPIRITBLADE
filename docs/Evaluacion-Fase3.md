# Evaluación — Fase 3: Versión 0.1 — Funcionalidad básica y Docker

**Fecha de evaluación:** 4 de noviembre de 2025  
**Proyecto:** SPIRITBLADE — Rastreador de estadísticas de League of Legends

---

## Backend

### ✅ Seguridad (Spring Security)
Estado: COMPLETADO

- Spring Security configurado en `SecurityConfiguration.java`
- JWT implementado mediante `JwtTokenProvider` y filtros de autenticación
- Protección por roles (USER, ADMIN) aplicada a endpoints
- Encriptado de contraseñas con BCrypt
- Protección CSRF configurada

Archivos clave: `SecurityConfiguration.java`, `JwtTokenProvider.java`, `UserLoginService.java`

### ✅ Comunicación segura (HTTPS, puerto 443)
Estado: COMPLETADO

- HTTPS configurado en `application.properties`
- Puerto del servidor establecido en 443 (`server.port=443`)
- SSL habilitado (`server.ssl.enabled=true`)
- Keystore incluido en recursos (`keystore.jks`)

Archivo de configuración: `backend/src/main/resources/application.properties`

### ✅ Almacenamiento de imágenes (MinIO vía AWS S3 SDK)
Estado: COMPLETADO

- Integración con MinIO implementada en `MinioStorageService.java`
- Ejemplo de configuración en `application.properties`:

```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.bucket-name=spiritblade-uploads
```

- Gestión de avatares proporcionada por `UserAvatarService`
- Dependencia: `aws-java-sdk-s3` v1.12.772

Archivos clave: `MinioStorageService.java`, `UserAvatarService.java`, `FileController.java`

### ✅ Arquitectura en capas
Estado: COMPLETADO

- Controladores (6 REST controllers): `LoginRestController`, `UserController`, `SummonerController`, `DashboardController`, `AdminController`, `FileController`
- Servicios (7): `UserService`, `RiotService`, `DataDragonService`, `MatchAnalysisService`, `UserAvatarService`, `MinioStorageService`, etc.
- Repositorios (4): `UserModelRepository`, `SummonerRepository`, `MatchRepository`, `MatchEntityRepository`

Separación clara de responsabilidades entre capas.

### ✅ Rutas API usan `/api/v1`
Estado: COMPLETADO

Ejemplos:

```java
@RequestMapping("/api/v1/auth")      // LoginRestController
@RequestMapping("/api/v1/users")     // UserController
@RequestMapping("/api/v1/summoners") // SummonerController
@RequestMapping("/api/v1/dashboard") // DashboardController
@RequestMapping("/api/v1/admin")     // AdminController
@RequestMapping("/api/v1/files")     // FileController
```

### ✅ Buenas prácticas REST
Estado: COMPLETADO

- Métodos HTTP correctos: GET, POST, PUT, DELETE, PATCH
- URLs orientadas a recursos (`/users/{id}`, `/summoners/{name}`)
- Códigos HTTP apropiados (200, 201, 204, 400, 401, 404)
- `Content-Type: application/json` usado de forma consistente
- `ResponseEntity` usado para respuestas

### ✅ Búsquedas parametrizadas
Estado: COMPLETADO

- Usuarios: `GET /api/v1/users?search={query}&page={n}&size={m}`
- Filtros: `role`, `active`, `search`
- Búsqueda de summoner: `GET /api/v1/summoners/search/{name}`

Referencia: `UserController.java` (líneas ~65–90)

### ✅ Paginación
Estado: COMPLETADO

Paginación implementada con Spring Data, por ejemplo:

```java
@GetMapping
public ResponseEntity<Page<UserDTO>> getAllUsers(
  @RequestParam(defaultValue = "0") int page,
  @RequestParam(defaultValue = "10") int size) {
  Pageable pageable = PageRequest.of(page, size);
  // ...
}
```

Endpoints paginados incluyen `/api/v1/users`, `/api/v1/summoners`, `/api/v1/dashboard/me/ranked-matches`.

Archivos clave: `UserController.java`, `SummonerController.java`, `DashboardController.java`

### ✅ Datos de ejemplo
Estado: COMPLETADO

- `DataInitializer.java` siembra usuarios de ejemplo al iniciar (admin y usuario regular)
- Datos cargados con `@PostConstruct`
- Contraseñas seguras generadas para cuentas sembradas

Archivo: `DataInitializer.java`

---

## Frontend

### ❌ Librerías de componentes UI
Estado: TODO

- No hay integración con Angular Material ni ng-bootstrap — la UI usa CSS y componentes personalizados.
- Recomendación: integrar una librería de componentes para mejorar la experiencia de usuario.

### ✅ Arquitectura Angular (componentes + servicios)
Estado: COMPLETADO

- Componentes: `DashboardComponent`, `LoginComponent`, `HomeComponent`, `SummonerComponent`, `AdminComponent`, `HeaderComponent`, `FooterComponent`
- Servicios: `AuthService`, `UserService`, `DashboardService`, `SummonerService`, `AdminService`
- Usa componentes standalone de Angular (Angular 17+)

### ❌ Páginas de error
Estado: TODO

- No se encontraron componentes dedicados de error (404, 500).
- Recomendación: añadir un `ErrorComponent` y configurar rutas de error.

### ⚠️ Paginación en frontend
Estado: PARCIAL

- El backend soporta paginación, pero el frontend usa valores hardcodeados (por ejemplo, carga 30 partidas por defecto)
- La lista de usuarios en admin carece de 'cargar más' o scroll infinito
- Recomendación: implementar carga incremental en la UI (botones o scroll infinito)

---

## Controles de calidad

### ⚠️ Pruebas automatizadas
Estado: PARCIAL — INSUFICIENTE

Pruebas existentes:

- Pruebas unitarias: ~16 archivos bajo `/backend/src/test/java/unit/`
- Pruebas de sistema: solo 1 (`SummonerSystemTest.java`)
- Pruebas E2E: 1 archivo (posiblemente vacío/incompleto)

Cobertura por funcionalidades (pruebas de sistema):

1. Autenticación — pruebas unitarias presentes ✅
2. Dashboard personal — sin pruebas de sistema ❌
3. Búsqueda de summoners — sin pruebas de sistema ❌
4. Gestión de favoritos — parcial (pruebas unitarias) ⚠️
5. Historial de partidas — sin pruebas de sistema ❌
6. Estadísticas de LP — sin pruebas de sistema ❌
7. Panel de administración — sin pruebas de sistema ❌
8. Gestión de usuarios — sin pruebas de sistema ❌
9. Subida de avatar — sin pruebas de sistema ❌

Cobertura de pruebas de sistema: ~11% (1 de 9 funcionalidades clave)

❌ Requisito no cumplido: >50% de cobertura de funcionalidades en pruebas de sistema.

Acción requerida: añadir pruebas de sistema para al menos 5 funcionalidades más, por ejemplo:

- `DashboardSystemTest.java` (estadísticas ranked, progresión de LP)
- `AuthSystemTest.java` (login, registro, logout)
- `SearchSystemTest.java` (búsqueda de summoner)
- `FavoritesSystemTest.java` (añadir/quitar favoritos)
- `AdminSystemTest.java` (gestión de usuarios)

Pruebas frontend:

- Specs de componentes (`.spec.ts`) existen ✅ pero pueden necesitar actualizaciones ⚠️

### ⚠️ Calidad de código
Estado: PARCIAL

- Logging (`Logger`) usado en servicios ✅
- Formato de código consistente ✅
- Algunos controladores carecen de comentarios inline; añadir comentarios aclaratorios donde sea útil ⚠️
- JaCoCo configurado para cobertura de pruebas ✅
- Algunos métodos (p. ej. en `DashboardController`) muestran complejidad ciclomática alta — se recomienda refactorizar (complejidad > 15)

---

## Empaquetado con Docker

### ✅ Dockerfile
Estado: COMPLETADO

- `docker/Dockerfile` presente y usa multi-stage build para optimización

### ✅ `docker-compose.yml`
Estado: COMPLETADO

- Archivo de compose ubicado en `/docker/docker-compose.yml`
- Servicios: MySQL 8.0 (con healthcheck), imagen de la app `spiritblade:0.1`
- Variables de entorno, volúmenes y health checks configurados

### ✅ `docker-compose-dev.yml`
Estado: COMPLETADO

- Archivo dev compose presente y configurado para la etiqueta de desarrollo `dev`

### ✅ Buenas prácticas Docker Compose
Estado: COMPLETADO

- Healthcheck de MySQL presente
- Orden de inicio gestionado con `depends_on` y condiciones de salud
- Variables de entorno con valores por defecto y sobrescribibles

---

## CI / Entrega

### ✅ Pipeline de dev (merge → tag dev)
Estado: COMPLETADO

- Flujo: `.github/workflows/deploy-dev.yml` se dispara en pushes a `main` y publica una etiqueta `dev` en Docker Hub

### ✅ Pipeline de release (GitHub release → tag versión)
Estado: COMPLETADO

- Flujo: `.github/workflows/deploy-release.yml` construye y publica una imagen versionada y el artifact compose como OCI

### ✅ Workflow de build manual
Estado: COMPLETADO

- Workflow: `.github/workflows/manual-build.yml`

### ✅ Workflows reutilizables y DRY
Estado: COMPLETADO

- `build-push.yml` es reutilizable y otros workflows lo invocan con parámetros

### ❌ Release 0.1.0
Estado: TODO

- No se encontró la etiqueta `0.1.0` en el repositorio
- No existe un release en GitHub
- Acción requerida: crear el release `0.1.0` para activar el pipeline de release

### ❌ Imágenes Docker para 0.1.0 / latest
Estado: TODO

- Al no existir release, las imágenes `0.1.0` y `latest` no fueron publicadas

---

## Documentación

### ⚠️ Estado de la documentación
Estado: PARCIAL

- Docs presentes: `API.md`, `Funcionalidades.md`, `Guia-Desarrollo.md`, `Ejecucion.md`, `Inicio-Proyecto.md`, `Seguimiento.md`
- Algunos documentos pueden estar desactualizados respecto a cambios de la Fase 3

Recomendación: actualizar la documentación para reflejar nuevas funciones del dashboard, seguimiento de LP y estado de pruebas.

### ⚠️ Entrada de blog
Estado: PARCIAL

- Post de Medium existente cubre solo la Fase 1: https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3
- Acción requerida: publicar un post de la Fase 3 describiendo el dashboard, integración con la API de Riot, Docker y CI/CD

---

## Resumen

### Completado (17/27)
1. Spring Security implementado
2. HTTPS en puerto 443
3. MinIO/S3 para imágenes
4. Arquitectura en capas
5. Endpoints `/api/v1`
6. Buenas prácticas REST
7. Búsquedas parametrizadas
8. Paginación backend
9. Datos de ejemplo
10. Arquitectura Angular
11. Dockerfile
12. `docker-compose.yml`
13. `docker-compose-dev.yml`
14. Buenas prácticas en Docker
15. Pipeline de dev
16. Pipeline de release
17. Workflows reutilizables

### Parcial (4/27)
1. Paginación frontend (backend OK)
2. Pruebas de sistema (solo 1; se necesitan 5+)
3. Calidad de código (mejoras necesarias)
4. Documentación (actualizar necesaria)

### Pendiente (6/27)
1. Añadir librería UI (ng-bootstrap/Material)
2. Páginas de error (ErrorComponent)
3. Crear release 0.1.0
4. Publicar imagen Docker etiquetada 0.1.0
5. Publicar artifact compose (OCI) para 0.1.0
6. Publicar post de la Fase 3

---

## Acciones prioritarias

### Alta prioridad (bloqueantes)
1. Crear pruebas de sistema para al menos 5 funcionalidades adicionales
2. Publicar release `0.1.0` en GitHub para activar pipelines
3. Publicar un post de la Fase 3

### Prioridad media
4. Implementar páginas de error y mejoras UX
5. Integrar Angular Material o ng-bootstrap
6. Mejorar la UX de paginación en frontend
7. Actualizar documentación técnica

### Baja prioridad
8. Refactorizar métodos de alta complejidad
9. Mejorar comentarios en el código y documentación
10. Incrementar cobertura de pruebas frontend

---

## Conclusión

Cumplimiento general: ~63% (17/27 completados)

La base del proyecto es sólida:

- Backend bien estructurado con Spring Security y HTTPS
- API REST sigue buenas prácticas
- Infraestructura Docker y CI/CD en su lugar
- Arquitectura frontend razonable

Deficiencias principales:

- Pruebas de sistema insuficientes (11% vs requisito 50%) — CRÍTICO
- No existe release `0.1.0` publicado — solución directa
- Frontend carece de una librería de componentes — mejora de UX

Recomendación: priorizar las pruebas de sistema y la publicación del release `0.1.0` para cumplir los requisitos de la Fase 3.