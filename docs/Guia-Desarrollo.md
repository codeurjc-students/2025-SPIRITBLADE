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
- **Base de datos**: MySQL para producción, Minio para almacenamiento de imágenes

### Resumen técnico

| Aspecto | Descripción |
|--------|-------------|
| **Tipo** | Aplicación web SPA con API REST |
| **Tecnologías** | Java 21, Spring Boot 3.4.3, Angular 17, MySQL 8.0, JWT, MinIO, Redis, Spring Cache |
| **Seguridad** | Solo HTTPS (puerto 443), SSL/TLS, autenticación JWT, control de acceso por roles |
| **Almacenamiento** | MinIO (compatibilidad S3) |
| **Documentación** | Swagger UI / OpenAPI 3.0 interactiva |
| **Herramientas** | VS Code, Maven, npm, Git, Docker |
| **Control de calidad** | JUnit 5, Jasmine/Karma, JaCoCo, SonarCloud, GitHub Actions |
| **Despliegue** | Docker, Docker Compose, configuración HTTPS-only |
| **Proceso de desarrollo** | Iterativo e incremental, Github workflows para CI/CD |

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

**Swagger UI** - Interfaz web para explorar y probar la API REST de forma interactiva.
- URL oficial: https://swagger.io/tools/swagger-ui/

### Frontend
**Angular 17** - Framework web con TypeScript para crear SPAs robustas. Usa componentes standalone para una arquitectura más modular.
- URL oficial: https://angular.io/

**TypeScript** - Superset de JavaScript con tipado estático, usado en todo el frontend para mejorar mantenibilidad.
- URL oficial: https://www.typescriptlang.org/

---

## Herramientas

**Visual Studio Code** - Editor recomendado con extensiones para Java, Angular y Git. Ofrece depuración y terminal integrados.
- URL oficial: https://code.visualstudio.com/

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
│ name: String     │   favoritos   │ riotId: String   │
│ email: String    │      N:M      │ puuid: String    │
│ encodedPwd: Str  │               │ name: String     │
│ roles[]: String  │               │ level: Integer   │
│ active: Boolean  │               │ profileIconId: I │
│ image: String    │               │ tier: String     │
│ avatarUrl: Str   │               │ rank: String     │
│ linkedSummoner.. │               │ lp: Integer      │
│ lastAiAnalysis.. │               │ wins: Integer    │
│ createdAt: Date  │               │ losses: Integer  │
└──────────────────┘               │ lastSearchedAt: D│
                                   └──────────────────┘
                                            │
                                            │ 1:N
                                            ▼
                                   ┌──────────────────┐
                                   │      Match       │
                                   ├──────────────────┤
                                   │ id: Long         │
                                   │ matchId: String  │
                                   │ timestamp: Date  │
                                   │ win: Boolean     │
                                   │ kills: Integer   │
                                   │ deaths: Integer  │
                                   │ assists: Integer │
                                   │ championName: S  │
                                   │ championId: Int  │
                                   │ role: String     │
                                   │ lane: String     │
                                   │ gameDuration: L  │
                                   │ gameMode: String │
                                   │ queueId: Integer │
                                   │ totalDamageDealt │
                                   │ goldEarned: Int  │
                                   │ champLevel: Int  │
                                   │ summonerName: S  │
                                   └──────────────────┘
                                            ▲
                                            │ 1:1
                                            │      
                                   ┌──────────────────┐
                                   │   RankHistory    │
                                   ├──────────────────┤
                                   │ id: Long         │
                                   │ timestamp: Date  │
                                   │ tier: String     │
                                   │ rank: String     │
                                   │ leaguePoints: I  │
                                   │ wins: Integer    │
                                   │ losses: Integer  │
                                   │ queueType: Str   │
                                   │ lpChange: Int    │
                                   └──────────────────┘
```

Relaciones clave:
- **User ↔ Summoner**: Relación Many-to-Many para sistema de favoritos
- **Summoner → Match**: Relación One-to-Many para historial de partidas
- **Summoner → RankHistory**: Relación One-to-Many para seguimiento de progreso de rango
- **RankHistory → Match**: Relación One-to-One para match que activó el snapshot de rango

---

### Entidad Champion (JPA)

La entidad `Champion` representa los campeones estáticos de League of Legends y está persistida en la tabla **champions** en MySQL. Campos principales:
- `id` (Long): Identificador interno.
- `key` (String): Clave única del campeón.
- `name` (String): Nombre del campeón.
- `imageUrl` (String): URL de la imagen del campeón.

Los datos se precargan al iniciar la aplicación mediante `DataInitializer.updateChampionDatabase()`.

### Caché Redis

Se ha configurado **Redis** como caché distribuida usando **Spring Cache**. Configuración en `CacheConfig` con TTLs:
- `champions` – 24 h
- `summoners` – 10 min
- `masteries` – 1 h
- `matches` – 24 h

Los métodos de los servicios (`DataDragonService`, `RiotService`) están anotados con `@Cacheable` para aprovechar la caché y reducir llamadas externas.

#### ¿Cómo funciona Redis en SPIRITBLADE?

**Redis** es un almacén de datos en memoria (in-memory data store) de tipo clave-valor que actúa como caché distribuida. En SPIRITBLADE, se usa para almacenar temporalmente los resultados de llamadas costosas a APIs externas (Riot API, Data Dragon).

**Flujo de operación**:

1. **Primera petición**: Cuando un usuario solicita datos (ej: stats de un summoner):
   - El servicio verifica si los datos existen en Redis usando una clave única (ej: `summoners::EUW1#UserName`)
   - Si NO existe (cache miss), se llama a la API externa de Riot
   - La respuesta se serializa a JSON usando `GenericJackson2JsonRedisSerializer`
   - Se almacena en Redis con la clave y el TTL configurado
   - Se devuelve la respuesta al cliente

2. **Peticiones subsecuentes**: 
   - El servicio consulta Redis primero
   - Si existe (cache hit) y no ha expirado, se devuelve directamente desde Redis
   - **NO** se llama a la API de Riot → Mejora drástica en rendimiento y reduce cuota de API

3. **Expiración (TTL)**:
   - Cada tipo de dato tiene un TTL (Time-To-Live) específico
   - Datos estáticos (`champions`): 24h - cambian raramente
   - Datos dinámicos (`summoners`): 10min - pueden cambiar con frecuencia (subida de nivel, rank)
   - Al expirar, la siguiente petición refresca los datos

**Configuración técnica**:

```java
// Ejemplo de método cacheado
@Cacheable(value = "summoners", key = "#riotId")
public Summoner getSummonerByRiotId(String riotId) {
    // Solo se ejecuta si no está en caché
    return riotApiClient.fetchSummoner(riotId);
}
```

**Ventajas**:
- ⚡ **Reducción de latencia**: Respuestas en ~5ms vs ~200ms de API externa
- 🔄 **Menor carga en APIs externas**: Evita límites de rate limiting
- 💰 **Ahorro de cuota**: Las llamadas a Riot API son limitadas
- 📈 **Escalabilidad**: Redis puede compartirse entre múltiples instancias del backend

**Serialización y tipos polimórficos**:

Spring Cache utiliza `GenericJackson2JsonRedisSerializer` con un `ObjectMapper` configurado para:
- Manejar tipos polimórficos (clases heredadas, interfaces)
- Ignorar propiedades desconocidas en deserialización
- Almacenar metadatos de tipo para reconstruir objetos Java correctamente

### API REST

Revisar [API.md](API.md) para detalles completos de endpoints y ejemplos de uso.

---

### Arquitectura del Servidor

El backend sigue una arquitectura por capas con buenas prácticas de Spring Boot:

```
┌───────────────────────────────────────────────────────────────────┐
│                       SERVER ARCHITECTURE                          │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │  Auth    │  │Summoner  │  │Dashboard │  │  Files   │         │
│  │Controller│  │Controller│  │Controller│  │Controller│         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│  ┌──────────┐  ┌──────────┐                                     │
│  │  User    │  │  Admin   │                                     │
│  │Controller│  │Controller│                                     │
│  └──────────┘  └──────────┘                                     │
│         │              │              │              │          │
└─────────┼──────────────┼──────────────┼──────────────┼──────────┘
          │              │              │              │
          ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BUSINESS LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │UserLoginSvc  │  │   Riot       │  │  Dashboard   │           │
│  │UserAvatarSvc │  │   Service    │  │   Service    │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │MinIOStorage  │  │DataDragon    │                             │
│  │   Service    │  │   Service    │                             │
│  └──────────────┘  └──────────────┘                             │
│         │                  │                  │                 │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   User       │  │   Summoner   │  │    Match     │  │ RankHistory  │   │
│  │ Repository   │  │  Repository  │  │  Repository  │  │  Repository  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│  ┌──────────────┐                                                          │
│  │   Champion   │                                                          │
│  │ Repository   │                                                          │
│  └──────────────┘                                                          │
│         │                  │                  │                  │        │
└─────────┼──────────────────┼──────────────────┼──────────────────┼────────┘
          │                  │                  │                  │
          ▼                  ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   DATABASE (MySQL 8.0 ONLY)                         │ 
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  users   │  │summoners │  │ matches  │  │rank_hist │             │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘             │
│  ┌──────────┐                                                        │
│  │champions │                                                        │
│  └──────────┘                                                        │
└─────────────────────────────────────────────────────────────────────┘
                    │
                    │
                    │
┌─────────────────────────────────────────────────────────────────┐
│              EXTERNAL STORAGE (MinIO)                           │
│  ┌────────────────────────────────────────┐                     │
│  │  spiritblade-uploads bucket            │                     │
│  │  - Avatares de usuario (PNG only)      │                     │
│  │  - Validación PNG en 3 capas           │                     │
│  └────────────────────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘

CONCERNS TRANSVERSALES
┌─────────────────────────────────────────────────────────────────┐
│  Seguridad (HTTPS-only, Spring Security + JWT, SSL)             │
│  Manejo de Excepciones (Global @ControllerAdvice)               │
│  Logging de errores (SLF4J)                                     │
│  Integración API Externa (WebClient → Riot Games API)           │
│  Documentación API (Swagger UI / OpenAPI 3.0)                   │
│  Validación de ficheros (PNG-only enforcement)                  │
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
- **Integración Gemini AI**: `AiAnalysisService` para análisis avanzado 
- **Almacenamiento**: `MinioStorageService` + `UserAvatarService` para gestión de ficheros
- **Validación**: Enforce PNG-only en 3 capas (FileController, MinioStorageService, UserAvatarService)
- **Manejo de Excepciones**: `GlobalExceptionHandler` para respuestas de error consistentes
- **Documentación**: Springdoc OpenAPI con Swagger UI

---

### Arquitectura del Cliente

El frontend sigue arquitectura con **componentes standalone de Angular**:

```
┌───────────────────────────────────────────────────────────────────┐
│                       CLIENT ARCHITECTURE                         │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE VISTA (Componentes)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │   Home   │  │  Login   │  │Summoner  │  │Dashboard │         │
│  │Component │  │Component │  │Component │  │Component │  ...    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│       │              │              │              │            │
└───────┼──────────────┼──────────────┼──────────────┼────────────┘
        │              │              │              │
        ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CAPA DE SERVICIOS                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │   Auth   │  │Summoner  │  │Dashboard │  │  Admin   │         │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │  ...    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
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
│  ├── /             → HomeComponent                              │
│  ├── /login        → LoginComponent                             │
│  ├── /summoner/:id → SummonerComponent                          │
│  ├── /dashboard    → DashboardComponent (Auth Guard)            │
│  └── /admin        → AdminComponent (Admin Guard)               │
└─────────────────────────────────────────────────────────────────┘

MODELOS DE DATOS (DTOs)
┌─────────────────────────────────────────────────────────────────┐
│  SummonerDTO, UserDTO, MatchDTO, DashboardStatsDTO...           │
└─────────────────────────────────────────────────────────────────┘
```

Características clave:
- **Componentes Standalone**: Sin NgModule, mejor tree-shaking
- **Programación reactiva**: Observables de RxJS para operaciones asíncronas
- **Route Guards**: `AuthGuard` y `AdminGuard` para control de acceso
- **Interceptors**: `AuthInterceptor` añade token JWT a las solicitudes
- **Gestión de estado**: Servicios con BehaviorSubject para estado compartido

Comunicación entre componentes:
- Hermanos: Componentes hermanos (como HomeComponent y DashboardComponent) se suscriben a isAuthenticated para reaccionar a cambios de login/logout sin comunicación directa.

---

### Despliegue

La aplicación usa **build multi-stage en Docker** para imágenes optimizadas, combinando frontend y backend en un solo contenedor para simplificar el despliegue:

```
┌───────────────────────────────────────────────────────────────────┐
│                     DEPLOYMENT ARCHITECTURE                       │
└───────────────────────────────────────────────────────────────────┘

                  ┌─────────────────┐
                  │   Docker Host   │
                  └─────────────────┘
                          │
      ┌───────────────────┼───────────────────┐
      │                   │                   │
      ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   SPIRITBLADE│   │    MySQL     │   │    MinIO     │
│     App      │   │  Container   │   │  Container   │
│  Container   │   │              │   │              │
│ Angular +    │   │  mysql:8.0   │   │ minio/minio  │
│ Spring Boot  │   │  Port: 3306  │   │ Port: 9000   │
│ JRE 21       │   │              │   │ Port: 9001   │
│ Port: 443    │   └──────────────┘   │ (Console)    │
│ (HTTPS-only) │          │           └──────────────┘
└──────────────┘          │                   │
      │                   │                   │                
      │                   │                   │
      └───────────────────┼───────────────────┘
                          │
                          ▼
                Docker Compose Network
```

Características del despliegue:
- **Contenedor único para app**: Frontend (Angular) y backend (Spring Boot) combinados en un solo contenedor usando multi-stage build
- **Base de datos**: MySQL 8.0 separado para persistencia
- **Almacenamiento**: MinIO para archivos (avatares PNG)
- **Seguridad**: HTTPS obligatorio en puerto 443 con SSL/TLS
- **Red**: Docker Compose network para comunicación interna

---

Opciones de despliegue:

Ver [Ejecucion.md](Ejecucion.md) para instrucciones detalladas de despliegue.

---

## Control de Calidad

### Estrategia de pruebas

El proyecto sigue una aproximación de **pirámide de pruebas** con múltiples niveles de tests:

```              /\
                /  \
               /    \
              /  E2E \          ← Pocos, flujos críticos
             /--------\
            /          \
           / Integration\     ← Moderados, interacciones clave
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
- Ubicación: `backend/src/test/java/com/tfg/tfg/unit/`


**Integration Tests** - Spring Boot Test
- Propósito: Probar interacción de componentes con contexto Spring real
- Ámbito: Controller → Service → Repository con BD embebida
- Contexto: `@SpringBootTest` con `@AutoConfigureMockMvc`
- Ubicación: `backend/src/test/java/com/tfg/tfg/integration/`


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

**Integration Tests** - Angular Testing Utilities
- Propósito: Probar interacciones entre componentes hijo/padre
- Ámbito: Routing, formularios...
- Herramientas: `TestBed`, `ComponentFixture`, `RouterTestingModule`

---

### Cobertura de tests

Estado actual (v1.0):
- **Backend**: 80%> cobertura de líneas (JaCoCo)
- **Frontend**: 80%> cobertura de líneas (karma-coverage)

Informes de cobertura:
- Backend: `backend/target/site/jacoco/index.html`
- Frontend: `frontend/coverage/index.html`


---

### Análisis Estático de Código

**Integración SonarCloud**:
- URL: https://sonarcloud.io/project/overview?id=codeurjc-students_2025-SPIRITBLADE
- Trigger: Automático en cada PR a `main`
- Quality Gate: Requerido para merge

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
   - Imagen: `jae9104/2025-spiritblade:dev`

4. **deploy-release.yml** - Publicar release
   - Trigger: Creación de GitHub Release
   - Pasos: Build → Tag versión (ej. `v0.1.0`) → Tag `latest` → Push a DockerHub
   - Imágenes: `jae9104/2025-spiritblade:v0.1.0` + `latest`

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

7 fases planificadas:
1. ✅ Fase 1: Definición de funcionalidades y pantallas 
2. ✅ Fase 2: Repositorio y configuración CI 
3. ✅ Fase 3: Versión 0.1 - Funcionalidades básicas  
4. ✅ Fase 4: Versión 1.0 - Funcionalidades avanzadas y Funcionalidades intermedias
5. 📋 Fase 5: Redacción del TFG 
6. 📋 Fase 6: Defensa del TFG 

Ver [Inicio-Proyecto.md](Inicio-Proyecto.md) para descripciones detalladas de fases.

---

### Gestión de tareas

**GitHub Issues**:
- Seguimiento de tasks, bugs, mejoras

**GitHub Projects (Kanban)**:
- Backlog, To Do, In Progress, In Review, Done, Discarded

---

### Control de versiones (Git)

Estrategia de ramas:
```
main (protegida)
  │
  ├── feat<NumTask>--(Branch feature)
```

Mensajes de commit (Conventional Commits):

```
Formato: feat<NumTask>: Descripción breve del commit
```


Métricas actuales (v1.0):
- 📊 Comits totales: ~80
- 🌿 Ramas activas: 1-3 típicamente
- 🔒 `main` protegida con revisiones obligatorias

---

### Flujo de Pull Request

1. Crear rama desde `main`

2. Desarrollar con commits frecuentes

3. Push a remoto

4. Crear PR en GitHub

5. Checks CI automáticos:
- ✅ Build OK
- ✅ Tests pasan
- ✅ Cobertura cumplida
- ✅ SonarCloud quality gate pasado

6. Revisión de código por Github Copilot

7. Merge a `main`:
- Eliminar rama feature
- CI despliega imagen `dev` automáticamente

---

### Guía de revisión de código

Checklist para reviewers:
- ✅ Código sigue convenciones del proyecto
- ✅ Tests incluidos y pasan
- ✅ Sin bugs o problemas de seguridad evidentes
- ✅ Consideraciones de rendimiento
- ✅ Manejo de errores adecuado


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
| **0.1.0** | Octubre 2025 | ✅ Funcionalidad básica: autenticación, búsqueda de summoner, historial, panel admin, despliegue Docker | [spiritblade:0.1.0](https://hub.docker.com/r/jae9104/spiritblade/tags) |
| **1.0.0** | Diciembre 2025 | ✅ Funcionalidades intermedias y avanzadas (planificado): estadísticas, recomendaciones inteligentes, rankings personalizados | [spiritblade:1.0.0](https://hub.docker.com/r/jae9104/spiritblade/tags) |

Estado actual: v1.0.0 acabado

---

#### Proceso de release

Prerequisitos:
- Todos los tests pasando (CI green)
- SonarCloud quality gate pasado
- Documentación actualizada

Pasos para crear un release:

1. Commit del bump de versión:
```bash
git add .
git commit -m "chore: bump version to 1.0.0"
git push origin main
```

2. Crear tag git:
```bash
git tag -a 1.0.0 -m "Release v1.0.0: Advanced features"
git push origin 1.0.0
```

3. Crear GitHub Release:
- Seleccionar tag `1.0.0`
- Título: `SPIRITBLADE v1.0.0 - Advanced Features`
- Publicar release

4. Despliegue automático:
- Workflow `deploy-release.yml` se ejecuta
- Construye y publica imágenes Docker:
  - `spiritblade:1.0.0`
  - `spiritblade:latest`

5. Post-release: preparar siguiente iteración
```powershell
# Actualizar a siguiente SNAPSHOT
.\scripts\update-version.ps1 1.1.0-SNAPSHOT

git add .
git commit -m "chore: prepare for next development iteration 1.1.0-SNAPSHOT"
git push origin main
```

6. Anunciar release:
- Actualizar blog del proyecto
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

#### Artefactos en DockerHub

Todos los releases se publican en DockerHub:

Repositorio: [`jae9104/2025-spiritblade`](https://hub.docker.com/r/jae9104/spiritblade/tags)

Tags disponibles:
- `latest` - Último release estable (actualmente 1.0.0)
- `0.1.0` - Versión específica
- `1.0.0` - Versión específica
- `dev` - Último build de desarrollo desde `main`
- Tags personalizados para builds manuales

Pull image:
```bash
docker pull jae9104/2025-spiritblade:latest
docker pull jae9104/2025-spiritblade:0.1.0
docker pull jae9104/2025-spiritblade:dev
```

---

#### Checklist de release

Antes de crear un release, asegurar:
- [ ] Todas las features del milestone completadas
- [ ] Tests pasando local y en CI
- [ ] Cobertura de tests en umbrales (≥80% backend, ≥80% frontend)
- [ ] SonarCloud quality gate pasado
- [ ] Documentación actualizada (README, Funcionalidades.md, API.md)
- [ ] Pruebas manuales completadas
- [ ] Vulnerabilidades resueltas
- [ ] Versiones actualizadas en todos los archivos
- [ ] Tag git creado y push
- [ ] GitHub Release creado
- [ ] Imágenes Docker publicadas en DockerHub
- [ ] Post-release version bump (`-SNAPSHOT`) commiteado
- [ ] Release anunciado (blog, notificaciones)

---

### Entorno de desarrollo

Herramientas requeridas:
- **Java 21 JDK**
- **Node.js 18+**
- **Git**
- **Maven** 
- **Docker** 
- **MySQL 8.0+** 
- **MinIO** 

IDE recomendados:
- VS Code con extensiones 

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
| **MySQL** | 8.0+ | Base de datos | [mysql.com](https://www.mysql.com/) |
| **Docker** | Reciente | Contenerización | [docker.com](https://www.docker.com/) |
| **MinIO** | Reciente | Almacenamiento objetos | [min.io](https://min.io/) |

Verificar instalaciones:
```powershell
# PowerShell
java -version      
node -v            
git --version
mvn -version       # O usar mvnw
mysql --version    
docker --version   # Si está instalado
```

---

### Clonar el repositorio

```bash
# HTTPS (recomendado para solo lectura)
git clone https://github.com/codeurjc-students/2025-SPIRITBLADE
cd 2025-SPIRITBLADE

# SSH (si tienes llaves configuradas)
git clone git@github.com:codeurjc-students/2025-SPIRITBLADE
cd 2025-SPIRITBLADE
```

Verificar estructura:
```powershell
ls
# Debe verse: backend/, frontend/, docs/, .github/, README.md
```

---

### Configuración local de desarrollo

#### Opción 1: Desarrollo con MySQL

Configurar MySQL:

1. Instalar MySQL 8.0
2. Crear base de datos:
```sql
CREATE DATABASE spiritblade_db;
```

3. Configurar backend:
Archivo por defecto `backend/docker/.env` usa MySQL. Ajustar las variables de entorno según tu instalación.

4. Añadir Riot API Key y Gemini AI Key en variables de entorno

5. Iniciar backend:
```powershell
cd backend
.\dotenvtosystemargs.ps1
```

6. Iniciar frontend (otra terminal):
```bash
cd frontend
npm install    # primera vez
ng serve --ssl
```

7. Acceso:
- Backend API: https://localhost (puerto 443)
- Swagger UI: https://localhost/swagger-ui.html
- Frontend: https://localhost:4200

⚠️ Certificado SSL: Aceptar certificado autofirmado en el navegador la primera vez.

---

#### Opción 2: Docker Compose (Full Stack)

Ventajas:
- Un comando para levantar todo
- Entorno aislado
- Similar a producción

Prerequisitos: Docker y Docker Compose instalados

Ver [Ejecucion.md](Ejecucion.md) para guía completa de Docker.

---

### Testing

#### Backend

Ejecutar todos los tests:
```powershell
cd backend
.\dotenvtosystemargstests.ps1
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

1. Iniciar la aplicación

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

---

### Build para producción

#### JAR backend
```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
```
Salida: `backend/target/tfg-1.0.0-SNAPSHOT.jar`

Ejecutar JAR:
```bash
java -jar backend/target/tfg-1.0.0-SNAPSHOT.jar
```

---

#### Build frontend

```bash
cd frontend
npm run build --prod
```


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
- Repositorio: https://hub.docker.com/repository/docker/jae9104/spiritblade/general
- Blog del proyecto: https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3
- SonarCloud: https://sonarcloud.io/project/overview?id=codeurjc-students_2025-SPIRITBLADE
- DockerHub: https://hub.docker.com/r/jorgeandresecheverriagarcia/2025-spiritblade

Documentación:
- [README.md](../README.md)
- [API.md](API.md)
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

Contacto: j.andres.2022@alumnos.urjc.es

Ver [Autores.md](Autores.md) para información completa de autoría.

---

**Última actualización**: Noviembre 2025 (v1.0)
