# Guía de Desarrollo — SPIRITBLADE

## Índice
- [Introducción](#introducción)
- [Tecnologías](#tecnologías)
- [Herramientas](#herramientas)
- [Arquitectura](#arquitectura)
- [Control de Calidad](#control-de-calidad)
- [Proceso de Desarrollo](#proceso-de-desarrollo)
- [Ejecución y Edición de Código](#ejecución-y-edición-de-código)

---

## Introducción

SPIRITBLADE es una aplicación web con arquitectura **SPA (Single Page Application)**, diseñada para ofrecer análisis y visualización de estadísticas de League of Legends. La arquitectura SPA implica que la aplicación cliente se carga en una única página web y la navegación posterior se realiza de forma dinámica sin recargar toda la página, proporcionando una experiencia más fluida similar a aplicaciones de escritorio.

La aplicación está compuesta por tres componentes principales:
- **Cliente (Frontend)**: Angular 17 ejecutándose en el navegador del usuario
- **Servidor (Backend)**: API REST desarrollada en Spring Boot (Java 21)
- **Base de datos**: MySQL para producción, H2 en memoria en desarrollo (nota: actualmente MySQL es obligatorio)

### Resumen técnico

| Aspecto | Descripción |
|--------|-------------|
| **Tipo** | Aplicación web SPA con API REST |
| **Tecnologías** | Java 21, Spring Boot 3.4.3, Angular 17, MySQL 8.0, JWT, MinIO |
| **Seguridad** | Solo HTTPS (puerto 443), SSL/TLS, autenticación JWT, control de acceso por roles |
| **Almacenamiento** | MinIO (compatibilidad S3) (validación PNG aplicada) |
| **Documentación** | Swagger UI / OpenAPI 3.0 interactiva |
| **Herramientas** | VS Code, IntelliJ IDEA, Maven, npm, Git, Docker |
| **Control de calidad** | JUnit 5, Jasmine/Karma, JaCoCo, SonarCloud, GitHub Actions |
| **Despliegue** | Docker, Docker Compose, configuración HTTPS-only |
| **Proceso de desarrollo** | Iterativo e incremental, Git flow, DevOps con CI/CD |

---

## Tecnologías

### Backend
**Spring Boot 3.4.3** - Framework para desarrollar aplicaciones Java empresariales que simplifica configuración y despliegue. En el proyecto se usa para crear la API REST.
- URL oficial: https://spring.io/projects/spring-boot

**Java 21** - Lenguaje principal del backend, utilizando características LTS recientes.
- URL oficial: https://openjdk.org/projects/jdk/21/

**Spring Security** - Framework de seguridad que provee autenticación y autorización. Implementa autenticación basada en JWT y control por roles.
- URL oficial: https://spring.io/projects/spring-security

**MySQL 8.0** - Sistema de gestión de bases de datos relacional (REQUERIDO - H2 no es utilizado). Guarda usuarios, summoners, partidas y estadísticas.
- URL oficial: https://www.mysql.com/

**MinIO** - Almacenamiento de objetos compatible con S3 para ficheros de usuario (avatares). Se aplica validación PNG-only por seguridad.
- URL oficial: https://min.io/

**Springdoc OpenAPI** - Generación automática de documentación API con integración Swagger UI.
- URL oficial: https://springdoc.org/

### Frontend
**Angular 17** - Framework web con TypeScript para crear SPAs robustas. Usa componentes standalone para una arquitectura más modular.
- URL oficial: https://angular.io/

**TypeScript** - Superset de JavaScript con tipado estático, usado en todo el frontend para mejorar mantenibilidad.
- URL oficial: https://www.typescriptlang.org/

---

## Herramientas

**Visual Studio Code** - Editor recomendado con extensiones para Java, Angular y Git. Ofrece depuración y terminal integrados.
- URL oficial: https://code.visualstudio.com/

**IntelliJ IDEA** - IDE alternativo, especialmente recomendado para desarrollo de backend Java con excelente soporte Spring Boot.
- URL oficial: https://www.jetbrains.com/idea/

**Maven** - Gestión de dependencias y herramienta de build para el backend Java. Se usa el wrapper (`mvnw.cmd`).
- URL oficial: https://maven.apache.org/

**npm** - Gestor de paquetes de Node.js para las dependencias del frontend Angular.
- URL oficial: https://www.npmjs.com/

**Git** - Sistema de control de versiones distribuido para el seguimiento del código fuente.
- URL oficial: https://git-scm.com/

---

## Arquitectura

### Modelo de Dominio

El modelo de dominio representa las entidades principales de SPIRITBLADE y sus relaciones:

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

Relaciones clave:
- **User ↔ Summoner**: Relación Many-to-Many para sistema de favoritos
- **Summoner → Match**: Relación One-to-Many para historial de partidas

---

### API REST

La API REST sigue principios REST con autenticación JWT:

```
┌───────────────────────────────────────────────────────────────────┐
│                        REST API STRUCTURE                          │
└───────────────────────────────────────────────────────────────────┘

PUBLIC ENDPOINTS (Sin autenticación)
├── POST   /auth/login           → Login con usuario/contraseña
├── POST   /auth/signup          → Registrar nuevo usuario
├── POST   /auth/refresh         → Refrescar token JWT
├── GET    /summoners/search     → Buscar summoner por Riot ID
└── GET    /summoners/{id}       → Obtener detalles de summoner

ENDPOINTS AUTENTICADOS (JWT requerido)
├── ROL USER
│   ├── GET    /users/me         → Perfil del usuario actual
│   ├── PUT    /users/me         → Actualizar perfil
│   ├── POST   /users/me/favorites/{summonerId} → Añadir favorito
│   ├── DELETE /users/me/favorites/{summonerId} → Eliminar favorito
│   ├── GET    /dashboard/stats  → Estadísticas personales
│   └── GET    /dashboard/matches → Partidas recientes
│
└── ROL ADMIN
   ├── GET    /admin/users      → Listar todos los usuarios
   ├── PUT    /admin/users/{id} → Actualizar usuario (activar/desactivar)
   ├── DELETE /admin/users/{id} → Eliminar usuario
   └── GET    /admin/stats      → Estadísticas del sistema
```

Flujo de autenticación:
1. Cliente envía credenciales a `/auth/login`
2. Servidor valida y devuelve token JWT
3. Cliente incluye token en header `Authorization: Bearer <token>`
4. Servidor valida token en cada petición protegida
5. Token expira a las 24 horas (disponible refresh)

Documentación:
- Interactiva: Swagger UI en `/swagger-ui.html`
- OpenAPI: `/v3/api-docs` (JSON) y `/v3/api-docs.yaml` (YAML)
- Guía rápida: `docs/API.md`
- Guía completa: `docs/SWAGGER.md`
- Tutorial: `docs/SWAGGER-QUICKSTART.md`

---

### Arquitectura del Servidor

El backend sigue una arquitectura por capas con buenas prácticas de Spring Boot:

```
┌───────────────────────────────────────────────────────────────────┐
│                       SERVER ARCHITECTURE                          │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  Auth    │  │Summoner  │  │Dashboard │  │  Files   │       │
│  │Controller│  │Controller│  │Controller│  │Controller│       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│  ┌──────────┐  ┌──────────┐                                    │
│  │  User    │  │  Admin   │                                    │
│  │Controller│  │Controller│                                    │
│  └──────────┘  └──────────┘                                    │
│         │              │              │              │          │
└─────────┼──────────────┼──────────────┼──────────────┼──────────┘
        │              │              │              │
        ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BUSINESS LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │UserLoginSvc  │  │   Riot       │  │  Dashboard   │         │
│  │UserAvatarSvc │  │   Service    │  │   Service    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │MinIOStorage  │  │DataDragon    │                            │
│  │   Service    │  │   Service    │                            │
│  └──────────────┘  └──────────────┘                            │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
        │                  │                  │
        ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   User       │  │   Summoner   │  │    Match     │         │
│  │ Repository   │  │  Repository  │  │  Repository  │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
        │                  │                  │
        ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DATABASE (MySQL 8.0 ONLY)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  users   │  │summoners │  │ matches  │  │favorites │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────────────────────┘
                    │
                    │ (No H2, MySQL requerido)
                    │
┌─────────────────────────────────────────────────────────────────┐
│              EXTERNAL STORAGE (MinIO - S3 Compatible)            │
│  ┌────────────────────────────────────────┐                     │
│  │  spiritblade-uploads bucket            │                     │
│  │  - Avatares de usuario (PNG only)      │                     │
│  │  - Validación PNG en 3 capas           │                     │
│  └────────────────────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘

CONCERNS TRANSVERSALES
┌─────────────────────────────────────────────────────────────────┐
│  Seguridad (HTTPS-only, Spring Security + JWT, SSL/TLS)        │
│  Manejo de Excepciones (Global @ControllerAdvice)              │
│  Logging (SLF4J)                                               │
│  Integración API Externa (WebClient → Riot Games API)          │
│  Documentación API (Swagger UI / OpenAPI 3.0)                  │
│  Validación de ficheros (PNG-only enforcement)                 │
└─────────────────────────────────────────────────────────────────┘
```

Responsabilidades por capa:
- **Controllers** (6 en total): Manejan peticiones HTTP, validación y formateo de respuestas
  - `LoginRestController` - Endpoints de autenticación (`/api/v1/auth`)
  - `UserController` - Perfil y favoritos (`/api/v1/users`)
  - `SummonerController` - Integración con Riot API (`/api/v1/summoners`)
  - `DashboardController` - Analíticas y estadísticas (`/api/v1/dashboard`)
  - `FileController` - Subida/descarga de ficheros (`/api/v1/files`)
  - `AdminController` - Operaciones de administrador (`/api/v1/admin`)
- **Services**: Lógica de negocio, gestión de transacciones, integración con APIs
- **Repositories**: Acceso a datos con Spring Data JPA
- **Models/Entities**: Entidades JPA mapeadas a tablas MySQL

Componentes clave:
- **Seguridad**: HTTPS-only (puerto 443), autenticación JWT con `JwtTokenProvider` y `JwtAuthenticationFilter`
- **Integración Riot**: `RiotService` + `DataDragonService` para llamadas externas
- **Almacenamiento**: `MinioStorageService` + `UserAvatarService` para gestión de ficheros
- **Validación**: Enforce PNG-only en 3 capas (FileController, MinioStorageService, UserAvatarService)
- **Manejo de Excepciones**: `GlobalExceptionHandler` para respuestas de error consistentes
- **Documentación**: Springdoc OpenAPI con Swagger UI

---

### Arquitectura del Cliente

El frontend sigue arquitectura con **componentes standalone de Angular**:

```
┌───────────────────────────────────────────────────────────────────┐
│                       CLIENT ARCHITECTURE                          │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE VISTA (Componentes)               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │   Home   │  │  Login   │  │Summoner  │  │Dashboard │       │
│  │Component │  │Component │  │Component │  │Component │  ...  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│       │              │              │              │            │
└───────┼──────────────┼──────────────┼──────────────┼────────────┘
      │              │              │              │
      ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CAPA DE SERVICIOS                            │
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
           │  (Llamadas HTTP)│
           └─────────────────┘
                  │
                  ▼
           ┌─────────────────┐
           │  Backend API    │
           │  (Spring Boot)  │
           └─────────────────┘

RUTAS & GUARDS
┌─────────────────────────────────────────────────────────────────┐
│  app.routes.ts                                                  │
│  ├── /             → HomeComponent                             │
│  ├── /login        → LoginComponent                            │
│  ├── /summoner/:id → SummonerComponent                         │
│  ├── /dashboard    → DashboardComponent (Auth Guard)           │
│  └── /admin        → AdminComponent (Admin Guard)              │
└─────────────────────────────────────────────────────────────────┘

MODELOS DE DATOS (DTOs)
┌─────────────────────────────────────────────────────────────────┐
│  SummonerDTO, UserDTO, MatchDTO, DashboardStatsDTO...          │
└─────────────────────────────────────────────────────────────────┘
```

Características clave:
- **Componentes Standalone**: Sin NgModule, mejor tree-shaking
- **Programación reactiva**: Observables de RxJS para operaciones asíncronas
- **Route Guards**: `AuthGuard` y `AdminGuard` para control de acceso
- **Interceptors**: `AuthInterceptor` añade token JWT a las solicitudes
- **Gestión de estado**: Servicios con BehaviorSubject para estado compartido

Comunicación entre componentes:
- Padre → Hijo: `@Input()`
- Hijo → Padre: `@Output()` + EventEmitter
- Hermanos: Servicios compartidos con subjects de RxJS

---

### Despliegue

La aplicación usa **build multi-stage en Docker** para imágenes optimizadas:

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

Dockerfile multietapa de ejemplo:

1. Etapa de build frontend:
```dockerfile
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build --prod
```

2. Etapa de build backend:
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline
COPY backend/src ./src
RUN mvn clean package -DskipTests
```

3. Imagen final de runtime:
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

Docker Compose de ejemplo:
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

Opciones de despliegue:
- **Quick Start**: Pull desde DockerHub y ejecutar `docker-compose up`
- **Build desde código**: Construir localmente con `docker build` + `docker-compose up`
- **Cloud**: Desplegar en AWS ECS, Azure Container Instances o GCP Cloud Run

Ver [Ejecucion.md](Ejecucion.md) para instrucciones detalladas de despliegue.

---

## Control de Calidad

### Estrategia de pruebas

El proyecto sigue una aproximación de **pirámide de pruebas** con múltiples niveles de tests:

```
               /\
               /  \
              / E2E \          ← Pocos, flujos críticos
             /--------\
            /          \
            / Integration \     ← Moderados, interacciones clave
           /--------------\
          /                \
         /   Unit Tests     \   ← Muchos, rápidos, aislados
         /____________________\
```

### Pruebas automatizadas

#### Backend (Java)

**Unit Tests** - JUnit 5 + Mockito
- Propósito: Probar métodos individuales en aislamiento
- Ámbito: Lógica de negocio en servicios, utilidades
- Mocking: Dependencias externas (repositorios, APIs)
- Ubicación: `backend/src/test/java/com/tfg/tfg/service/`
- Ejemplo:
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
- Propósito: Probar interacción de componentes con contexto Spring real
- Ámbito: Controller → Service → Repository con BD embebida
- Contexto: `@SpringBootTest` con `@AutoConfigureMockMvc`
- Ubicación: `backend/src/test/java/com/tfg/tfg/`
- Ejemplo:
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
- Propósito: Probar flujos críticos de usuario en navegador real
- Ámbito: Stack completo desde UI hasta BD
- Ubicación: `backend/src/test/java/com/tfg/tfg/e2e/`
- Escenarios: Login, búsqueda de summoner, navegación del dashboard

---

#### Frontend (Angular)

**Unit Tests** - Jasmine + Karma
- Propósito: Probar componentes y servicios en aislamiento
- Ámbito: Lógica de componentes, servicios, pipes
- Mocking: HttpClient, Router, dependencias
- Ubicación: `frontend/src/app/**/*.spec.ts`
- Ejemplo:
```typescript
it('should display summoner name after search', () => {
  component.summoner = mockSummoner;
  fixture.detectChanges();
  const compiled = fixture.nativeElement;
  expect(compiled.querySelector('h2').textContent).toContain('Player#EUW');
});
```

**Integration Tests** - Angular Testing Utilities
- Propósito: Probar interacciones entre componentes hijo/padre
- Ámbito: Comunicación padre-hijo, routing, formularios
- Herramientas: `TestBed`, `ComponentFixture`, `RouterTestingModule`

---

### Cobertura de tests

Estado actual (v0.1):
- **Backend**: 55% cobertura de líneas (JaCoCo)
- **Frontend**: 48% cobertura de líneas (karma-coverage)

Objetivos:
- **Backend**: ≥ 60% para v0.2
- **Frontend**: ≥ 50% para v0.2

Informes de cobertura:
- Backend: `backend/target/site/jacoco/index.html`
- Frontend: `frontend/coverage/index.html`

Áreas no cubiertas (plan v0.2):
- Casos límite en manejo de excepciones
- Funcionalidades avanzadas del panel admin
- Escenarios de recuperación de errores
- Transformaciones de datos complejas

---

### Funcionalidades probadas (v0.1)

✅ **Autenticación & Autorización**
- Login con generación de JWT
- Validación de token en endpoints protegidos
- Control por roles (USER vs ADMIN)
- Mecanismo de refresh de token
- Seguridad HTTPS-only (puerto 443)

✅ **Gestión de Usuarios**
- Registro con validación
- Recuperación y actualización de perfil
- Encriptación de contraseñas (BCrypt)
- Subida de foto de perfil (PNG only, MinIO)
- Validación de avatar en 3 capas
- Gestión de favoritos (añadir/quitar)

✅ **Operaciones de Summoner**
- Búsqueda por Riot ID (gameName + tagLine)
- Obtención de datos desde Riot API
- Recuperar stats ranked (tier, rank, LP, W/L)
- Mostrar mastery top 3 de campeones
- Historial de partidas con estadísticas detalladas
- Sistema de caché para rendimiento

✅ **Dashboard**
- Agregación de estadísticas personales
- Partidas recientes con métricas
- Gestión de summoners favoritos
- Analíticas de rendimiento y KDA

✅ **Gestión de ficheros**
- Integración con MinIO
- Validación PNG-only (header + extensión + content type)
- Subida/descarga segura de ficheros
- Gestión de avatares de usuario

✅ **Panel Admin**
- Listado de usuarios con filtros
- Activar/desactivar usuarios
- Borrado con cascada
- Estadísticas del sistema

✅ **Integración con APIs externas**
- Autenticación con Riot Games API
- Manejo de rate limits (20 req/s, 100 req/2min)
- Recuperación ante errores (retries, fallbacks)
- Data Dragon CDN para imágenes

✅ **Documentación API**
- Swagger UI interactiva
- OpenAPI 3.0
- Autenticación JWT en Swagger
- Documentación completa de endpoints

✅ **Componentes Frontend**
- Renderizado con linkado de datos correcto
- Rutas con guards (Auth, Admin)
- Validación de formularios reactivos
- Muestra de errores amigables
- Comunicación HTTPS-only

---

### Análisis Estático de Código

**Integración SonarCloud**:
- URL: https://sonarcloud.io/project/overview?id=JorgeAndresEcheverria_2025-SPIRITBLADE
- Trigger: Automático en cada PR a `main`
- Quality Gate: Requerido para merge

Métricas analizadas:
- Bugs: 0 (objetivo: 0 críticos, 0 mayores)
- Vulnerabilidades: 0 (objetivo: 0)
- Code Smells: <50 (objetivo: <50)
- Security Hotspots: Revisados y resueltos
- Cobertura: Integrado con JaCoCo + karma-coverage
- Duplicaciones: <5%
- Mantenimiento: Rating A

Configuración:
- Archivo: `sonar-project.properties`
- Lenguajes: Java, TypeScript, HTML, CSS
- Exclusiones: Tests, código generado, librerías externas

Mejoras de calidad (v0.1):
- Reemplazo de `printStackTrace()` por logging SLF4J
- Reemplazo de `console.error()` por `console.debug()` en frontend
- Capturas de excepciones específicas en lugar de `Exception` genérico
- Colecciones vacías inmutables (`Collections.emptyList()`)
- Uso de strings vacíos en vez de `null` para URLs opcionales

---

### Integración Continua (CI)

Workflows de GitHub Actions:

1. **build.yml** - Control de calidad básico
   - Trigger: Push a ramas feature
   - Pasos: Checkout → Setup JDK/Node → Build backend → Build frontend → Ejecutar unit tests
   - Artefactos: Ninguno
   - Duración: ~5 min

2. **build-with-quality.yml** - Control completo (PR a main)
   - Trigger: Pull Request a `main`
   - Pasos: Todo lo anterior + Integration tests → Reportes de cobertura → Análisis SonarCloud
   - Quality Gate: Requerido para merge
   - Artefactos: Reportes de cobertura, resultados de tests
   - Duración: ~10 min

3. **deploy-dev.yml** - Publicar imagen de desarrollo
   - Trigger: Push a `main`
   - Pasos: Build → Tag `dev` → Push a DockerHub
   - Imagen: `jorgeandresecheverriagarcia/2025-spiritblade:dev`

4. **deploy-release.yml** - Publicar release
   - Trigger: Creación de GitHub Release
   - Pasos: Build → Tag versión (ej. `v0.1.0`) → Tag `latest` → Push a DockerHub
   - Imágenes: `jorgeandresecheverriagarcia/2025-spiritblade:v0.1.0` + `latest`

5. **manual-build.yml** - Build manual
   - Trigger: Workflow dispatch manual
   - Propósito: Builds bajo demanda para testing

Protecciones de rama (`main`):
- ✅ Requerir PR antes de merge
- ✅ Requerir checks de estado (build-with-quality.yml)
- ✅ Requerir aprobación de revisión de código
- ❌ No permitir pushes directos a `main`

Ver [Seguimiento.md](Seguimiento.md) para detalles de CI/CD y métricas.

---

## Proceso de Desarrollo

### Metodología

El proyecto sigue una metodología ágil **iterativa e incremental**:

Principios:
- 🔄 Iteraciones cortas: ciclos de 2-3 semanas
- 📦 Entregables incrementales: versión desplegable al final de cada fase
- 🔍 Feedback continuo: revisiones y ajustes regulares
- 🚀 Cultura DevOps: automatización, CI/CD, monitorización

7 fases planificadas:
1. ✅ Fase 1: Definición de funcionalidades y pantallas (15 Sep 2024)
2. ✅ Fase 2: Repositorio y configuración CI (15 Oct 2024)
3. ✅ Fase 3: Versión 0.1 - Funcionalidades básicas (15 Dic 2024)
4. 📋 Fase 4: Versión 0.2 - Funcionalidades intermedias (01 Mar 2025)
5. 📋 Fase 5: Versión 1.0 - Funcionalidades avanzadas (15 Abr 2025)
6. 📋 Fase 6: Redacción del TFG (15 May 2025)
7. 📋 Fase 7: Defensa del TFG (15 Jun 2025)

Ver [Inicio-Proyecto.md](Inicio-Proyecto.md) para descripciones detalladas de fases.

---

### Gestión de tareas

**GitHub Issues**:
- Seguimiento de bugs con etiqueta `bug`
- Features con `enhancement`
- Documentación con `documentation`
- Prioridades: `priority: high|medium|low`

**GitHub Projects (Kanban)**:
- Backlog, To Do, In Progress, In Review, Done

**Hitos**:
- v0.1 (15 Dic 2024) - ✅ Completado
- v0.2 (01 Mar 2025) - 📋 Planificado
- v1.0 (15 Apr 2025) - 📋 Planificado

---

### Control de versiones (Git)

Estrategia de ramas:
```
main (protegida)
  │
  ├── feature/user-authentication
  ├── feature/summoner-search
  ├── feature/admin-panel
  ├── bugfix/login-error
  └── docs/update-readme
```

Convenciones de nombres:
- `feature/<descripción>` - Nuevas funcionalidades
- `bugfix/<descripción>` - Correcciones
- `docs/<descripción>` - Documentación
- `refactor/<descripción>` - Refactorizaciones
- `test/<descripción>` - Mejoras en tests

Mensajes de commit (Conventional Commits):
```
type(scope): description

feat(auth): add JWT token refresh endpoint
fix(summoner): handle 404 when summoner not found
docs(readme): update installation instructions
test(service): add unit tests for RiotService
refactor(controller): simplify error handling
```

Tipos: `feat`, `fix`, `docs`, `test`, `refactor`, `style`, `chore`

Métricas actuales (v0.1):
- 📊 Comits totales: ~80
- 🌿 Ramas activas: 2-3 típicamente
- 🔒 `main` protegida con revisiones obligatorias

---

### Flujo de Pull Request

1. Crear rama desde `main`:
```bash
git checkout -b feature/new-feature
```

2. Desarrollar con commits frecuentes:
```bash
git add .
git commit -m "feat(scope): description"
```

3. Push a remoto:
```bash
git push origin feature/new-feature
```

4. Crear PR en GitHub:
- Título claro
- Descripción: qué, por qué, cómo + capturas si UI
- Enlazar issues relacionados
- Solicitar reviewers

5. Checks CI automáticos:
- ✅ Build OK
- ✅ Tests pasan
- ✅ Cobertura cumplida
- ✅ SonarCloud quality gate pasado

6. Revisión de código:
- Reviewer comenta
- Developer corrige
- Aprobar cuando esté satisfecho

7. Merge a `main`:
- Squash and merge (historial limpio)
- Eliminar rama feature
- CI despliega imagen `dev` automáticamente

---

### Guía de revisión de código

Checklist para reviewers:
- ✅ Código sigue convenciones del proyecto
- ✅ Tests incluidos y pasan
- ✅ Sin bugs o problemas de seguridad evidentes
- ✅ Documentación actualizada
- ✅ Consideraciones de rendimiento
- ✅ Manejo de errores adecuado

Feedback común:
- "Extraer en método separado"
- "Agregar unit tests para este edge case"
- "Posible NPE, añadir null check"
- "Actualizar documentación API"

---

### Versionado y Releases

SPIRITBLADE usa **Semantic Versioning** (SemVer).

Formato: `MAJOR.MINOR.PATCH` (ej., `0.1.0`)

- MAJOR: Cambios incompatibles
- MINOR: Nuevas features compatibles
- PATCH: Correcciones y parches

Versiones de desarrollo usan sufijo `-SNAPSHOT` (ej., `0.2.0-SNAPSHOT`)

---

#### Historial de releases

| Versión | Fecha | Descripción | DockerHub |
|---------|-------|-------------|-----------|
| **0.1.0** | 15 Dic 2024 | ✅ Funcionalidad básica: autenticación, búsqueda de summoner, historial, panel admin, despliegue Docker | [spiritblade:0.1.0](https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade/tags) |
| **0.2.0** | 01 Mar 2025 | 📋 Funcionalidades intermedias (planificado): analíticas con Chart.js, sistema de notas, notificaciones en favoritos, moderación admin | - |
| **1.0.0** | 15 Apr 2025 | 📋 Funcionalidades avanzadas (planificado): estadísticas globales, recomendaciones inteligentes, rankings personalizados | - |

Estado actual: v0.1.0 liberado, v0.2.0 en planificación

---

#### Proceso de release

Prerequisitos:
- Todos los tests pasando (CI green)
- SonarCloud quality gate pasado
- Documentación actualizada
- CHANGELOG preparado

Pasos para crear un release:

1. Pre-release: actualizar versión
```powershell
# PowerShell (Windows)
.\scripts\update-version.ps1 0.2.0
```
o
```bash
# Bash (Linux/Mac)
bash scripts/update-version.sh 0.2.0
```
Actualiza:
- `backend/pom.xml`
- `frontend/package.json`
- `docker/docker-compose.yml`

2. Commit del bump de versión:
```bash
git add .
git commit -m "chore: bump version to 0.2.0"
git push origin main
```

3. Crear tag git:
```bash
git tag -a 0.2.0 -m "Release v0.2.0: Intermediate features"
git push origin 0.2.0
```

4. Crear GitHub Release:
- Ir a: `https://github.com/codeurjc-students/2025-SPIRITBLADE/releases/new`
- Seleccionar tag `0.2.0`
- Título: `SPIRITBLADE v0.2.0 - Intermediate Features`
- Descripción (Changelog):
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
- Publicar release

5. Despliegue automático:
- Workflow `deploy-release.yml` se ejecuta
- Construye y publica imágenes Docker:
  - `spiritblade:0.2.0`
  - `spiritblade:latest`

6. Post-release: preparar siguiente iteración
```powershell
# Actualizar a siguiente SNAPSHOT
.\scripts\update-version.ps1 0.3.0-SNAPSHOT

git add .
git commit -m "chore: prepare for next development iteration 0.3.0-SNAPSHOT"
git push origin main
```

7. Anunciar release:
- Actualizar blog del proyecto
- Notificar usuarios
- Actualizar documentación de despliegue

---

#### Workflows de entrega continua

1. Deploy Dev (CD a Dev):
- Trigger: Merge a `main`
- Workflow: `.github/workflows/deploy-dev.yml`
- Artefactos: Imagen Docker `spiritblade:dev`, OCI compose `spiritblade-compose:dev`
- Propósito: Builds de desarrollo automáticos

2. Deploy Release (Producción):
- Trigger: GitHub Release creada
- Workflow: `.github/workflows/deploy-release.yml`
- Artefactos: Imagen `spiritblade:<version>`, `spiritblade:latest`, compose OCI versionado
- Propósito: Releases oficiales

3. Manual Build:
- Trigger: manual (workflow_dispatch)
- Workflow: `.github/workflows/manual-build.yml`
- Artefactos: Imagen con tag `<branch>-<timestamp>-<commit>`
- Propósito: Pruebas de ramas feature, hotfixes

---

#### Scripts de gestión de versiones

En `scripts/`:

PowerShell: `update-version.ps1`
```powershell
# Uso
.\scripts\update-version.ps1 <new-version>

# Ejemplos
.\scripts\update-version.ps1 0.2.0
.\scripts\update-version.ps1 0.2.0-SNAPSHOT
```

Bash: `update-version.sh`
```bash
# Uso
bash scripts/update-version.sh <new-version>

# Ejemplos
bash scripts/update-version.sh 0.2.0
bash scripts/update-version.sh 0.2.0-SNAPSHOT
```

Actualizan:
- `backend/pom.xml`
- `frontend/package.json`
- `docker/docker-compose.yml`

---

#### Artefactos en DockerHub

Todos los releases se publican en DockerHub:

Repositorio: [`jorgeandresecheverriagarcia/2025-spiritblade`](https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade)

Tags disponibles:
- `latest` - Último release estable (actualmente 0.1.0)
- `0.1.0` - Versión específica
- `dev` - Último build de desarrollo desde `main`
- Tags personalizados para builds manuales

Pull image:
```bash
docker pull jorgeandresecheverriagarcia/2025-spiritblade:latest
docker pull jorgeandresecheverriagarcia/2025-spiritblade:0.1.0
docker pull jorgeandresecheverriagarcia/2025-spiritblade:dev
```

---

#### Checklist de release

Antes de crear un release, asegurar:
- [ ] Todas las features del milestone completadas
- [ ] Tests pasando local y en CI
- [ ] Cobertura de tests en umbrales (≥55% backend, ≥50% frontend)
- [ ] SonarCloud quality gate pasado
- [ ] Documentación actualizada (README, Funcionalidades.md, API.md)
- [ ] CHANGELOG preparado
- [ ] Pruebas manuales completadas
- [ ] Vulnerabilidades resueltas
- [ ] Versiones actualizadas en todos los archivos
- [ ] Tag git creado y push
- [ ] GitHub Release creado con notas detalladas
- [ ] Imágenes Docker publicadas en DockerHub
- [ ] Post-release version bump (`-SNAPSHOT`) commiteado
- [ ] Release anunciado (blog, notificaciones)

---

### Entorno de desarrollo

Herramientas requeridas:
- **Java 21 JDK**
- **Node.js 18+**
- **Git**
- **Maven** (wrapper incluido)
- **Docker** (opcional)
- **MySQL 8.0+** (OBLIGATORIO)
- **MinIO** (opcional)

IDE recomendados:
- VS Code (extensiones: Java, Angular Language Service, REST Client, Docker, GitLens, SonarLint)
- IntelliJ IDEA (para backend)

Opcionales:
- Postman, MySQL Workbench, GUI de Git

---

## Ejecución y Edición de Código

### Requisitos previos

Herramientas mínimas:

| Herramienta | Versión | Propósito | Descarga |
|------|---------|---------|----------|
| **Java JDK** | 21+ | Runtime backend | [Eclipse Temurin](https://adoptium.net/) |
| **Node.js** | 18+ | Build frontend | [nodejs.org](https://nodejs.org/) |
| **Git** | Reciente | Control de versiones | [git-scm.com](https://git-scm.com/) |
| **Maven** | 3.9+ | Build backend | [maven.apache.org](https://maven.apache.org/) |
| **MySQL** | 8.0+ | Base de datos (REQUERIDO) | [mysql.com](https://www.mysql.com/) |
| **Docker** | Reciente | Contenerización (opcional) | [docker.com](https://www.docker.com/) |
| **MinIO** | Reciente | Almacenamiento objetos (opcional) | [min.io](https://min.io/) |

Verificar instalaciones:
```powershell
# PowerShell
java -version      # Debe mostrar 21.x
node -v            # Debe mostrar v18.x o superior
git --version
mvn -version       # O usar mvnw
mysql --version    # Debe mostrar 8.0.x (REQUERIDO)
docker --version   # Si está instalado
```

⚠️ Importante: MySQL es obligatorio. El proyecto ya no soporta H2.

---

### Clonar el repositorio

```bash
# HTTPS (recomendado para solo lectura)
git clone https://github.com/JorgeAndresEcheverria/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE

# SSH (si tienes llaves configuradas)
git clone git@github.com:JorgeAndresEcheverria/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE
```

Verificar estructura:
```powershell
ls
# Debe verse: backend/, frontend/, docs/, .github/, README.md
```

---

### Configuración local de desarrollo

#### Opción 1: Desarrollo con MySQL (Recomendado)

⚠️ Importante: SPIRITBLADE ya **NO usa H2**. MySQL es obligatorio para desarrollo y producción.

Ventajas:
- Persistencia de datos
- Identico a producción
- Mejor para pruebas

Configurar MySQL:

1. Instalar MySQL 8.0
2. Crear base de datos:
```sql
CREATE DATABASE spiritblade_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'spiritblade'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON spiritblade_db.* TO 'spiritblade'@'localhost';
FLUSH PRIVILEGES;
```

3. Configurar backend:
Archivo por defecto `backend/src/main/resources/application.properties` usa MySQL. Ajustar `username` y `password` según tu instalación.

4. Añadir Riot API Key:
```properties
riot.api.key=RGAPI-YOUR-KEY-HERE
riot.api.region=euw1
```

5. Iniciar backend:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

6. Iniciar frontend (otra terminal):
```bash
cd frontend
npm install    # primera vez
npm start
```

7. Acceso:
- Backend API: https://localhost (puerto 443)
- Swagger UI: https://localhost/swagger-ui.html
- Frontend: http://localhost:4200 (desarrollo)

⚠️ Certificado SSL: Aceptar certificado autofirmado en el navegador la primera vez.

---

#### Opción 2: Docker Compose (Full Stack)

Ventajas:
- Un comando para levantar todo
- Entorno aislado
- Similar a producción

Prerequisitos: Docker y Docker Compose instalados

Setup:

1. Crear archivo `.env` en la raíz desde ejemplo:
```bash
cp .env.example .env
# editar .env y rellenar valores
```
Variables ejemplo:
```
MYSQL_ROOT_PASSWORD=your-db-password
MYSQL_DATABASE=spiritblade
MYSQL_USER=spiritblade
MYSQL_PASSWORD=spiritbladepass

RIOT_API_KEY=RGAPI-YOUR-KEY-HERE
JWT_SECRET=your-secret-key
```

2. Levantar servicios:
```bash
docker-compose up
```

3. Acceso:
- App: https://localhost (puerto 443)
- MySQL: localhost:3306 (usuario: spiritblade, password: spiritbladepass)

⚠️ Certificado SSL: Aceptar certificado autofirmado al acceder por primera vez.

Ver [Ejecucion.md](Ejecucion.md) para guía completa de Docker.

---

### Configuración IDE

#### VS Code (Recomendado para frontend + backend)

Extensiones recomendadas:
1. Extension Pack for Java (Microsoft)
2. Angular Language Service (Angular)
3. REST Client (Huachao Mao)
4. Docker (Microsoft)
5. GitLens (GitKraken)
6. SonarLint (SonarSource)

Abrir workspace:
```powershell
code .
```

Depuración:
- Backend: `spring-boot:run` en modo debug
- Frontend: `npm start` + Chrome DevTools

---

#### IntelliJ IDEA (Recomendado para backend)

1. Abrir `backend/pom.xml` como proyecto
2. IDEA detecta Spring Boot automáticamente
3. Configurar JDK 21 en Project Structure
4. Run configuration creada automáticamente

Ventajas: mejores herramientas de refactorización y soporte Spring

---

### Testing

#### Backend

Ejecutar todos los tests:
```powershell
cd backend
.\mvnw.cmd test
```

Ejecutar clase de test específica:
```powershell
.\mvnw.cmd test -Dtest=RiotServiceTest
```

Ejecutar con cobertura:
```powershell
.\mvnw.cmd test jacoco:report
```
Reporte en: `backend/target/site/jacoco/index.html`

Ejecutar solo integration tests:
```powershell
.\mvnw.cmd verify -P integration-tests
```

---

#### Frontend

Ejecutar tests (watch):
```bash
cd frontend
npm test
```

Ejecutar tests una vez (CI):
```bash
npm run test:ci
```

Con cobertura:
```bash
npm run test:coverage
```
Reporte en: `frontend/coverage/index.html`

Ejecutar archivo de test específico:
```bash
npm test -- --include='**/summoner.component.spec.ts'
```

---

### Pruebas de API

#### Usando Swagger UI (Recomendado)

1. Iniciar la aplicación:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

2. Abrir Swagger UI: [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html)

⚠️ Primera vez: aceptar certificado autofirmado en el navegador

3. Autenticarse:
- Usar `POST /auth/login` o `POST /auth/register`
- Copiar token de la respuesta
- Click en "Authorize" y pegar `Bearer <token>`

4. Probar endpoints con "Try it out"

Ventajas: no requiere herramientas externas y está siempre actualizada.

Ver [SWAGGER-QUICKSTART.md](SWAGGER-QUICKSTART.md) para más detalles.

---

#### Usando Postman

Importar OpenAPI:
1. Abrir Postman
2. Import → Link → `https://localhost/v3/api-docs`
3. Postman genera colección
4. Establecer env variable `baseUrl` = `https://localhost`
5. Desactivar verificación SSL en Settings para desarrollo

O exportar spec:
```bash
curl -k https://localhost/v3/api-docs > openapi.json
```
Importar `openapi.json` en Postman.

Pruebas manuales:
1. POST `/auth/login` con credenciales
2. Copiar token y usar Bearer en Authorization
3. Probar endpoints según Swagger

---

#### Usando REST Client (extensión VS Code)

Crear `test.http`:
```http
### Login
POST https://localhost/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}

### Search Summoner (requires token from login)
GET https://localhost/summoners/search?gameName=Hide on bush&tagLine=KR
Authorization: Bearer {{token}}
```

Enviar requests desde VS Code.

⚠️ VS Code REST Client puede requerir configuración para aceptar certificados autofirmados.

---

#### Usando curl (PowerShell)

Login:
```powershell
# Deshabilitar verificación SSL para certificados autofirmados
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}

$response = Invoke-RestMethod -Uri "https://localhost/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin"}'

$token = $response.token
```

Buscar summoner:
```powershell
Invoke-RestMethod -Uri "https://localhost/summoners/search?gameName=Hide on bush&tagLine=KR" `
  -Headers @{"Authorization"="Bearer $token"}
```

Nota: En desarrollo con certificados autofirmados, deshabilitar verificación SSL como arriba.

---

### Build para producción

#### JAR backend
```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
```
Salida: `backend/target/tfg-0.0.1-SNAPSHOT.jar`

Ejecutar JAR:
```bash
java -jar backend/target/tfg-0.0.1-SNAPSHOT.jar
```

---

#### Build frontend
```bash
cd frontend
npm run build --prod
```
Salida: `frontend/dist/frontend/`

Servir localmente (para pruebas):
```bash
npx http-server dist/frontend -p 8081
```

---

#### Imagen Docker

Construir imagen multi-stage:
```bash
docker build -t spiritblade:local .
```

Ejecutar contenedor:
```bash
docker run -p 8080:8080 \
  -e RIOT_API_KEY=your-key \
  -e JWT_SECRET=your-secret \
  spiritblade:local
```

---

### Resolución de problemas

#### HTTPS/SSL

Error `ERR_CERT_AUTHORITY_INVALID`:
- Normal con certificados autofirmados
- En navegador: Advanced → Proceed to localhost (unsafe)
- curl: usar `-k`
- PowerShell: deshabilitar verificación SSL (ver ejemplos)

Error `Connection refused` en `http://localhost:8080`:
- Solución: El servidor funciona solo con HTTPS en puerto 443
- Usar `https://localhost`
- Revisar `server.ssl.enabled=true` en `application.properties`

---

#### Backend no arranca

Error `Port 443 already in use`:
- Matar proceso que usa el puerto (requiere permisos admin):
```powershell
netstat -ano | findstr :443
taskkill /PID <PID> /F
```

Error `Could not find or load main class`:
- Limpiar y rebuild:
```powershell
.\mvnw.cmd clean install
```

Error `401 Unauthorized from Riot API`:
- Revisar `riot.api.key` en `application.properties` y renovar clave en https://developer.riotgames.com/

---

#### Frontend no arranca

Error `npm: command not found`:
- Instalar Node.js

Error `Cannot find module '@angular/core'`:
- Reinstalar dependencias:
```bash
rm -rf node_modules package-lock.json
npm install
```

Error `Port 4200 already in use`:
- Usar otro puerto:
```bash
npm start -- --port 4201
```

---

#### Problemas de base de datos

Error `Access denied for user`:
- Revisar credenciales MySQL en `application.properties`

Error `Unknown database 'spiritblade'`:
- Crear base de datos (nota: nombre por defecto `spiritblade`, no `spiritblade_db`):
```sql
CREATE DATABASE spiritblade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Error `Table doesn't exist`:
- Habilitar auto schema creation:
```properties
spring.jpa.hibernate.ddl-auto=update
```

---

### Hot Reload / Live Reload

Backend (Spring Boot DevTools):
- Incluido en `pom.xml`
- Reinicio automático en cambios de classpath

Frontend (Angular CLI):
- Auto con `npm start`
- Refresh automático al guardar
- HMR habilitado

---

### Formateo de código

Backend (Java):
- IntelliJ: `Ctrl+Alt+L`
- VS Code: `Shift+Alt+F`

Frontend (TypeScript):
- Configurado en `tsconfig.json` y `.editorconfig`
- Auto-format on save en VS Code:
```json
"editor.formatOnSave": true
```

---

### Próximos pasos

1. ✅ Configurar entorno de desarrollo local
2. ✅ Ejecutar backend y frontend
3. ✅ Probar API con Postman o REST Client
4. ✅ Ejecutar tests para verificar setup
5. 📖 Leer [API.md](API.md) para documentación de endpoints
6. 🚀 Comenzar desarrollo de nuevas features

Recursos adicionales:
- [Inicio-Proyecto.md](Inicio-Proyecto.md)
- [Funcionalidades.md](Funcionalidades.md)
- [Seguimiento.md](Seguimiento.md)
- [Ejecucion.md](Ejecucion.md)

---

## Resumen

Esta guía cubre:
- ✅ **Tecnologías**: Spring Boot 3.4.3, Angular 17, MySQL 8.0
- ✅ **Arquitectura**: Modelo de dominio, API REST, backend por capas, cliente Angular, despliegue Docker
- ✅ **Control de calidad**: Pirámide de pruebas, métricas de cobertura, SonarCloud, CI/CD
- ✅ **Proceso de desarrollo**: Metodología ágil, flujo Git, PRs, gestión de releases
- ✅ **Ejecución**: Setup local (MySQL/Docker), IDE, pruebas, resolución de problemas

---

## Enlaces y recursos

Enlaces del proyecto:
- 🐙 **Repositorio**: https://github.com/JorgeAndresEcheverria/2025-SPIRITBLADE
- 📝 **Blog**: https://jorgeandrescheverria.blogspot.com/search/label/tfg
- 🔍 **SonarCloud**: https://sonarcloud.io/project/overview?id=JorgeAndresEcheverria_2025-SPIRITBLADE
- 🐳 **DockerHub**: https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade

Documentación:
- [README.md](../README.md)
- [API.md](API.md)
- [SWAGGER.md](SWAGGER.md)
- [SWAGGER-QUICKSTART.md](SWAGGER-QUICKSTART.md)
- [Funcionalidades.md](Funcionalidades.md)
- [Ejecucion.md](Ejecucion.md)
- [Seguimiento.md](Seguimiento.md)
- [Inicio-Proyecto.md](Inicio-Proyecto.md)
- [Autores.md](Autores.md)

Documentación externa:
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Springdoc OpenAPI (Swagger)](https://springdoc.org/)
- [Angular Documentation](https://angular.io/docs)
- [Riot Games API](https://developer.riotgames.com/docs/lol)

---

## Autoresía

**Desarrollador**: Jorge Andrés Echevarría  
**Tutor**: Iván Chicano Capelo  
**Universidad**: Universidad Rey Juan Carlos (URJC)  
**Curso**: 2024-2025

Contacto: j.echeverria.2021@alumnos.urjc.es

Ver [Autores.md](Autores.md) para información completa de autoría.

---

**Última actualización**: Enero 2025 (v0.1)

**[← Volver al README principal](../README.md)** | **[Ver toda la documentación →](../README.md#documentación)**
