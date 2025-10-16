# Funcionalidades Detalladas - SPIRITBLADE

Este documento contiene la lista completa de funcionalidades del proyecto SPIRITBLADE, indicando su estado de implementación y descripción detallada.

> **📝 Actualización Octubre 2025**: Este documento ha sido actualizado para reflejar el reajuste de enfoque del proyecto basado en los tiempos de desarrollo disponibles y priorización de funcionalidades core. Ver [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para detalles completos de los cambios.

---

## 📊 Estado de Implementación

### Leyenda
- ✅ **Implementado** - Funcionalidad completa en v0.1
- 🚧 **En desarrollo** - Iniciado pero no completado
- 📋 **Planificado** - Definido para versiones futuras
- ⏸️ **Tentativo** - Bajo consideración

---

## 1. Funcionalidades Básicas (v0.1)

### 1.1 Usuario Anónimo

#### Alcance Actualizado
**Funcionalidad básica**: Búsqueda de invocadores y visualización de su perfil y rango. Visualización del historial público de partidas con sistema de caché. Acceso a estadísticas básicas de campeones incluyendo maestrías, campeones más jugados y datos de rendimiento general.

| ID | Funcionalidad | Estado | Descripción del Comportamiento |
|----|---------------|--------|-------------------------------|
| F1.1.1 | Buscar invocadores | ✅ | El usuario introduce un Riot ID (gameName#tagLine) en el campo de búsqueda. El sistema valida el formato, consulta la API de Riot Games y muestra el perfil completo con datos en tiempo real. Si el invocador no existe, se muestra un mensaje de error informativo. Sistema de caché implementado para optimizar rendimiento. |
| F1.1.2 | Ver perfil de invocador y rango | ✅ | Se muestra una página con toda la información del invocador: icono de perfil, nivel, Riot ID, rango actual (tier, división, LP), victorias/derrotas, tasa de victorias y total de partidas. Los datos se obtienen de Riot API y se cachean localmente con estrategia de actualización inteligente. |
| F1.1.3 | Ver historial público de partidas | ✅ | Lista de las últimas partidas del invocador, paginadas de 5 en 5. Para cada partida se muestra: resultado (victoria/derrota), campeón jugado con icono, KDA (kills/deaths/assists), duración y fecha. Sistema de caché implementado para reducir llamadas a API. El usuario puede cargar más partidas con el botón "Cargar más". |
| F1.1.4 | Ver estadísticas básicas de campeones | ✅ | Acceso a maestrías de campeones: Top 3 campeones más jugados del invocador con nivel de maestría y puntos. Se muestra el icono, nombre, nivel de maestría (1-7) y puntos de maestría totales. Incluye datos de rendimiento general obtenidos de la API Champion-Mastery-v4. |
| F1.1.5 | Ver búsquedas recientes | ✅ | En la página de inicio se listan los 10 últimos invocadores buscados por cualquier usuario, ordenados por fecha descendente. Cada elemento es clickeable y redirige al perfil del invocador. |

---

### 1.2 Usuario Registrado

#### Alcance Actualizado
**Funcionalidad básica**: Acceso a panel de control personalizable. Consulta de datos detallados de partidas recientes con información enriquecida de la API de Riot. Visualización de campeones con mayor maestría y rendimiento personal.

| ID | Funcionalidad | Estado | Descripción del Comportamiento |
|----|---------------|--------|-------------------------------|
| F1.2.1 | Registrarse en la aplicación | ✅ | Formulario de registro con campos: nombre de usuario, email, contraseña y confirmación de contraseña. Validaciones: campos requeridos, formato de email correcto, contraseñas coincidentes, usuario no duplicado. Al completar con éxito, se crea la cuenta encriptando la contraseña con BCrypt y se hace login automático. |
| F1.2.2 | Iniciar sesión | ✅ | Formulario de login con usuario y contraseña. El backend valida credenciales con Spring Security, genera un token JWT válido por 24 horas y lo almacena en cookies HttpOnly. Mensajes de error específicos: credenciales inválidas, servidor no disponible, error de red. |
| F1.2.3 | Cerrar sesión | ✅ | El usuario cierra sesión desde el menú. Se invalidan los tokens JWT (access y refresh) eliminando las cookies. El contexto de seguridad se limpia y se redirige al home. |
| F1.2.4 | Panel de control personalizable | ✅ | Dashboard personalizado del usuario con: información de perfil (nombre, email), estadísticas básicas (búsquedas realizadas, favoritos), accesos rápidos (buscar invocador, ver favoritos, editar perfil). Panel configurable según preferencias del usuario. Requiere autenticación con JWT válido. |
| F1.2.5 | Consulta de datos detallados de partidas recientes | ✅ | Visualización enriquecida del historial de partidas con información ampliada obtenida de la API de Riot: estadísticas detalladas de campeón, build utilizada, participación en objetivos, daño infligido. Los datos se presentan de forma clara y accesible. |
| F1.2.6 | Visualización de campeones con mayor maestría | ✅ | Dashboard que muestra los campeones favoritos del usuario con estadísticas de maestría y rendimiento personal: nivel de maestría, puntos acumulados, KDA promedio, winrate por campeón. Información actualizada con cada búsqueda. |
| F1.2.7 | Guardar invocadores favoritos | 🚧 | Botón "Añadir a favoritos" en el perfil de invocador. Los favoritos se guardan en BD (relación User-Summoner). El dashboard muestra lista de favoritos con acceso rápido. Modelo implementado, interfaz en desarrollo. |
| F1.2.8 | Asociar cuenta de LoL | 📋 | El usuario puede vincular su cuenta de League of Legends mediante Riot ID. El sistema verifica la cuenta y la asocia al perfil, permitiendo análisis automático de estadísticas propias. **Planificado para v0.2**. |

---

### 1.3 Administrador

| ID | Funcionalidad | Estado | Descripción del Comportamiento |
|----|---------------|--------|-------------------------------|
| F1.3.1 | Acceder al panel de administración | ✅ | Requiere rol ADMIN en el token JWT. El panel muestra menú con opciones: gestión de usuarios, estadísticas del sistema, logs. Si un usuario sin permisos intenta acceder, se redirige con mensaje de error. |
| F1.3.2 | Listar todos los usuarios | ✅ | Tabla con todos los usuarios registrados mostrando: ID, nombre, email, roles, estado (activo/inactivo), fecha de registro. Endpoint `/admin/users` protegido por Spring Security con preAuthorize("hasRole('ADMIN')"). |
| F1.3.3 | Activar/desactivar usuarios | ✅ | Toggle para cambiar el campo `active` de un usuario. Usuarios desactivados no pueden hacer login (se valida en UserLoginService). Cambio inmediato sin recarga de página (llamada API). |
| F1.3.4 | Eliminar usuarios | ✅ | Botón "Eliminar" con confirmación. Hace DELETE a `/admin/users/{id}` que borra el usuario de BD (cascade para relaciones). No se puede eliminar el propio usuario admin. |
| F1.3.5 | Editar roles de usuarios | 🚧 | El admin puede cambiar roles USER ↔ ADMIN. Formulario inline con select y botón "Guardar". Endpoint PUT `/admin/users/{id}/roles`. En desarrollo. |
| F1.3.6 | Moderar contenido de usuarios | 📋 | Revisar y eliminar notas/comentarios inapropiados de usuarios. Dashboard de moderación con flags automáticos. **Planificado para v0.2**. |

---

## 2. Funcionalidades Intermedias (v0.2)

### 2.1 Usuario Anónimo

#### Alcance Actualizado
**Funcionalidad intermedia**: Visualización de estadísticas agregadas por invocadores, con información detallada de partidas almacenadas en caché.

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| F2.1.1 | Estadísticas agregadas por invocadores | 📋 | Sistema de agregación de datos de múltiples invocadores buscados: winrate promedio por campeón, KDA medio por rol, pick rate de campeones populares. Dashboard público con gráficos. Optimizado con información detallada de partidas almacenadas en caché para minimizar tiempos de carga. |

---

### 2.2 Usuario Registrado

#### Alcance Actualizado
**Funcionalidad intermedia**: Acceso a datos detallados de rendimiento personal con campeones favoritos. Visualización del historial de partidas con información contextual enriquecida.

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| F2.2.1 | Rendimiento personal con campeones favoritos | 📋 | Panel de análisis detallado mostrando estadísticas avanzadas de los campeones más jugados: tendencias de rendimiento, comparativas por temporada, análisis de fortalezas y debilidades. Integración con datos históricos almacenados. |
| F2.2.2 | Historial de partidas enriquecido | 📋 | Visualización mejorada del historial con información contextual adicional: timeline de eventos, análisis de fase de juego (early/mid/late), comparativa con otros jugadores de la partida. Integración de datos de múltiples fuentes de la API de Riot. |
| F2.2.3 | Añadir notas en partidas | 📋 | Campo de texto en cada partida para añadir notas personales. Las notas se asocian a Match + User. Edición y eliminación permitidas. |
| F2.2.4 | Recibir notificaciones | 📋 | Sistema de notificaciones en tiempo real (WebSocket) cuando: un favorito juega una partida, un favorito sube de rango, nuevos logros de maestría. Panel de notificaciones en dashboard. |

---

### 2.3 Administrador

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| F2.3.1 | Dashboard de moderación | 📋 | Interfaz para revisar reportes de usuarios, contenido flaggeado automáticamente, logs de actividad sospechosa. |
| F2.3.2 | Estadísticas del sistema | 📋 | Métricas: usuarios activos, búsquedas por día, invocadores más populares, uso de API (rate limit), errores HTTP. Gráficos con Chart.js. |

---

## 3. Funcionalidades Avanzadas (v1.0)

### 3.1 Usuario Anónimo

#### Alcance Actualizado
**Funcionalidad avanzada**: Sistema inteligente de caché que minimiza los tiempos de carga mientras garantiza datos actualizados. Estrategia híbrida de acceso a datos que balancea rendimiento y frescura de información.

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| F3.1.1 | Sistema inteligente de caché | 📋 | Implementación de sistema de caché multinivel con estrategias adaptativas: caché en memoria (Redis), caché persistente (MySQL), invalidación inteligente basada en tiempo y eventos. Minimiza tiempos de carga mientras garantiza datos actualizados. |
| F3.1.2 | Estrategia híbrida de acceso a datos | 📋 | Algoritmo que balancea automáticamente entre rendimiento y frescura de información: prioriza datos en caché cuando son recientes, realiza actualización selectiva de datos críticos, implementa prefetching inteligente. Optimiza la experiencia del usuario final. |
| F3.1.3 | Estadísticas globales de comunidad | 📋 | Dashboard público con datos agregados de toda la aplicación: top invocadores más buscados, campeones con mayor winrate en la app, estadísticas por región. Datos actualizados mediante sistema de caché inteligente. |

---

### 3.2 Usuario Registrado

#### Alcance Actualizado
**Funcionalidad avanzada**: Dashboard personalizado con indicadores clave de rendimiento calculados a partir del historial de partidas. Sistema de caché inteligente que prioriza la base de datos antes de realizar costosas llamadas a APIs externas. Validación automática de frescura de datos con mínimo impacto en tiempos de carga.

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| F3.2.1 | Dashboard con KPIs personalizados | 📋 | Dashboard avanzado que calcula y presenta indicadores clave de rendimiento: evolución temporal de estadísticas, análisis de tendencias, identificación de patrones de juego. Cálculos realizados a partir del historial completo de partidas almacenado en BD. |
| F3.2.2 | Sistema de caché inteligente priorizado | 📋 | Implementación de estrategia de acceso a datos que prioriza la base de datos local antes de realizar llamadas a APIs externas: validación de frescura con timestamps, actualización selectiva de datos obsoletos, minimización de llamadas a API de Riot. Reduce costes y mejora rendimiento. |
| F3.2.3 | Validación automática de frescura de datos | 📋 | Sistema automático que verifica y actualiza datos cuando es necesario: análisis de antiguedad de datos, actualización asíncrona en segundo plano, notificaciones de actualización. Mínimo impacto en tiempos de carga percibidos por el usuario. |
| F3.2.4 | Recomendaciones de builds | 📋 | Algoritmo que analiza el estilo de juego (campeones jugados, rol preferido, KDA) y recomienda builds óptimas, runas, ítem paths. Integración con datos de comunidad. |
| F3.2.5 | Rankings personalizados | 📋 | Crear rankings personalizados entre amigos/favoritos. Comparar estadísticas, winrates, maestrías. Tablas de clasificación privadas. |

---

### 3.3 Administrador

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| F3.3.1 | Logs de auditoría | 📋 | Registro detallado de acciones administrativas: quién modificó qué, cuándo, desde qué IP. Búsqueda y filtrado de logs. Exportar a CSV. |
| F3.3.2 | Gestión de API keys | 📋 | Sistema para rotar la API key de Riot, monitorizar rate limits, gestionar múltiples keys para balanceo de carga. |

---

## 4. Funcionalidades Técnicas

### 4.1 Seguridad

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| FT.1 | Autenticación JWT | ✅ | Tokens firmados con HS256, expiración 24h, refresh token 7 días, almacenados en cookies HttpOnly. |
| FT.2 | Autorización por roles | ✅ | Spring Security con @PreAuthorize. Roles: USER, ADMIN. Guards en Angular para rutas protegidas. |
| FT.3 | Cifrado de contraseñas | ✅ | BCryptPasswordEncoder con strength 10. Nunca se almacenan contraseñas en texto plano. |
| FT.4 | HTTPS | ✅ | Certificado SSL autofirmado en desarrollo (JKS), TLS 1.3 en producción. Puerto 443. |
| FT.5 | Validación de entrada | ✅ | @Valid en DTOs con Hibernate Validator. Sanitización de strings para prevenir XSS/SQL injection. |

---

### 4.2 Integración con APIs Externas

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| FT.6 | Riot Games API | ✅ | Integración completa con Account-v1, Summoner-v4, League-v4, Champion-Mastery-v4, Match-v5. RestTemplate con retry logic. |
| FT.7 | Data Dragon CDN | ✅ | Carga de imágenes estáticas (campeones, ítems, runas) desde Riot Data Dragon. Versión 14.1.1. |
| FT.8 | Rate limiting | 📋 | Implementar control de rate limit para API de Riot (20 req/s, 100 req/2m). Bucket4j con Redis. **v0.2**. |

---

### 4.3 Rendimiento y Escalabilidad

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| FT.9 | Caché de invocadores | ✅ | MySQL con campo `lastSearchedAt`. Datos se actualizan solo si > 5 minutos desde última búsqueda. |
| FT.10 | Caché distribuida | 📋 | Redis para caché de respuestas API, sesiones. Reducir carga en BD y API externa. **v0.2**. |
| FT.11 | Lazy loading | 📋 | Carga diferida de módulos Angular para reducir bundle inicial. **v0.2**. |

---

### 4.4 Calidad y Testing

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| FT.12 | Tests unitarios backend | ✅ | JUnit 5 + Mockito. Cobertura objetivo: ≥60%. Tests de servicios, controladores, mappers. |
| FT.13 | Tests unitarios frontend | ✅ | Jasmine + Karma. Cobertura objetivo: ≥50%. Tests de componentes, servicios, guards. |
| FT.14 | Tests de integración | ✅ | Spring Boot Test con @SpringBootTest. Tests de endpoints con MockMvc. |
| FT.15 | Tests E2E | 🚧 | Selenium WebDriver para flujos completos: login → buscar → ver perfil. En desarrollo. |
| FT.16 | Análisis estático | ✅ | SonarCloud en pipeline CI. Métricas: bugs, code smells, vulnerabilities. Quality Gate configurado. |

---

### 4.5 Despliegue y DevOps

| ID | Funcionalidad | Estado | Descripción |
|----|---------------|--------|-------------|
| FT.17 | Docker multi-stage | ✅ | Dockerfile con 3 stages: Node build (Angular), Maven build (Spring Boot), JRE runtime. Imagen optimizada <200MB. |
| FT.18 | Docker Compose | ✅ | Orquestación de app + MySQL. Healthchecks, depends_on, volumes para persistencia. |
| FT.19 | CI/CD con GitHub Actions | ✅ | Workflows: build.yml (tests + quality), deploy-dev.yml (main), deploy-release.yml (releases), manual-build.yml. |
| FT.20 | Publicación en DockerHub | ✅ | Workflow automatizado que publica imágenes con tags: dev, version (0.1.0), latest. OCI artifacts para compose. |
| FT.21 | Kubernetes deployment | 📋 | Manifests K8s para deployment, services, ingress. Escalado horizontal con HPA. **v1.0**. |

---

## Resumen de Estados

| Estado | Cantidad | Porcentaje |
|--------|----------|------------|
| ✅ Implementado | 30 | ~50% |
| 🚧 En desarrollo | 4 | ~7% |
| 📋 Planificado | 24 | ~40% |
| ⏸️ Tentativo | 2 | ~3% |
| **TOTAL** | **60** | **100%** |

---

**[← Volver al README principal](../README.md)** | **[Ver Funcionalidades con capturas →](Funcionalidades.md)**
