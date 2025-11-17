# Características detalladas — SPIRITBLADE

Este documento contiene la lista completa de características previstas para el proyecto SPIRITBLADE, indicando su estado de implementación y una descripción detallada.

> 📝 Actualizado en octubre de 2025: Este documento fue revisado para reflejar un reajuste del alcance del proyecto basado en el tiempo de desarrollo disponible y la priorización de las características principales. Consulte [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para los detalles completos de los cambios.

---

## Estado de implementación

### Leyenda
- ✅ Implementado — Función completa en v0.1
- 🚧 En progreso — Iniciado pero no terminado
- 📋 Planificado — Programado para versiones futuras
- ⏸️ Tentativo — En consideración

---

## 1. Características principales (v0.1)

### 1.1 Usuario anónimo

#### Alcance (actualizado)
Funcionalidad básica: buscar invocadores y ver su perfil y rango. Historial público de partidas disponible con caché. Estadísticas básicas de campeones (maestrías, más jugados, métricas generales) expuestas.

| ID | Función | Estado | Descripción del comportamiento |
|----|---------|--------|-------------------------------|
| F1.1.1 | Buscar invocadores | ✅ | El usuario introduce un Riot ID (gameName#tagLine) en el campo de búsqueda. El sistema valida el formato, consulta la API de Riot Games y muestra el perfil completo. Si el invocador no existe, se muestra un mensaje de error claro. Se implementa caché para mejorar el rendimiento. |
| F1.1.2 | Ver perfil de invocador y rango | ✅ | La página de perfil muestra avatar, nivel, Riot ID, rango actual (tier/división/LP), victorias/derrotas, tasa de victorias y partidas totales. Los datos se obtienen de la API de Riot y se cachean localmente con una estrategia de actualización inteligente. |
| F1.1.3 | Ver historial público de partidas | ✅ | Lista las últimas partidas (paginadas, 5 por página). Cada partida muestra resultado (victoria/derrota), icono de campeón, KDA, duración y fecha. El caché reduce las llamadas a la API. Los usuarios pueden cargar más partidas con un botón "Cargar más". |
| F1.1.4 | Ver estadísticas básicas de campeones | ✅ | Acceso a maestría de campeón: top 3 campeones más jugados con nivel de maestría y puntos (1–7). Muestra icono, nombre, nivel de maestría y puntos totales. Datos provenientes de Champion-Mastery-v4. |
| F1.1.5 | Búsquedas recientes | ✅ | La página de inicio lista las 10 búsquedas de invocador más recientes realizadas por cualquier usuario, ordenadas por fecha (más recientes primero). Cada elemento enlaza al perfil del invocador. |

---

### 1.2 Usuario registrado

#### Alcance (actualizado)
Funcionalidad básica: panel personalizable, datos de partidas detallados enriquecidos desde la API de Riot y visualización de maestría/rendimiento personal por campeón.

| ID | Función | Estado | Descripción del comportamiento |
|----|---------|--------|-------------------------------|
| F1.2.1 | Registrar | ✅ | Formulario de registro con nombre de usuario, email, contraseña + confirmación. Validaciones: campos obligatorios, formato de email, contraseñas coincidentes, nombre de usuario único. En caso de éxito se crea la cuenta (contraseña hasheada con BCrypt) y el usuario se autentica automáticamente. |
| F1.2.2 | Iniciar sesión | ✅ | Formulario de inicio de sesión (usuario + contraseña). El backend valida credenciales (Spring Security), emite un JWT válido por 24 horas y lo almacena como cookie HttpOnly. Mensajes de error claros para credenciales inválidas, servidor no disponible o errores de red. |
| F1.2.3 | Cerrar sesión | ✅ | El usuario cierra sesión desde el menú. Los JWT (acceso y refresh) se invalidan eliminando las cookies, se limpia el contexto de seguridad y el usuario es redirigido al inicio. |
| F1.2.4 | Panel personalizable | ✅ | Panel personal con información de perfil (nombre, email), estadísticas básicas (búsquedas, favoritos), acciones rápidas (buscar invocador, ver favoritos, editar perfil). El panel se configura según preferencias del usuario y requiere un JWT válido. |
| F1.2.5 | Datos detallados de partidas recientes | ✅ | Historial de partidas enriquecido con estadísticas detalladas del campeón, build de objetos, participación en objetivos y daño realizado. Presentado de forma clara y accesible, alimentado por datos de la API de Riot. |
| F1.2.6 | Vista de maestría personal por campeón | ✅ | Panel que muestra los campeones favoritos del usuario con maestría y estadísticas de rendimiento: nivel de maestría, puntos acumulados, KDA promedio, tasa de victorias por campeón. Actualizado en cada búsqueda. |
| F1.2.7 | Guardar invocadores favoritos | 🚧 | Botón "Agregar a favoritos" en los perfiles de invocador. Favoritos almacenados en la BD (relación Usuario–Invocador). El panel muestra una lista de acceso rápido. Modelo de datos implementado, interfaz en progreso. |
| F1.2.8 | Vincular cuenta de LoL | 📋 | Los usuarios pueden vincular su cuenta de League of Legends usando Riot ID. El sistema verifica y asocia la cuenta para análisis automático de estadísticas personales. Planificado para v0.2. |

---

### 1.3 Admin

| ID | Función | Estado | Descripción del comportamiento |
|----|---------|--------|-------------------------------|
| F1.3.1 | Acceder al panel de administración | ✅ | Requiere rol ADMIN en el JWT. El panel muestra gestión de usuarios, métricas del sistema y logs. Usuarios no autorizados son redirigidos con un mensaje de error. |
| F1.3.2 | Listar todos los usuarios | ✅ | Tabla de todos los usuarios registrados mostrando ID, nombre, email, roles, estado activo/inactivo y fecha de registro. Endpoint `/admin/users` protegido con `@PreAuthorize("hasRole('ADMIN')")`. |
| F1.3.3 | Habilitar/deshabilitar usuarios | ✅ | Interruptor para cambiar el flag `active`. Usuarios deshabilitados no pueden iniciar sesión (verificado en UserLoginService). El cambio se aplica inmediatamente vía llamada API. |
| F1.3.4 | Eliminar usuarios | ✅ | Botón de eliminar con confirmación. DELETE `/admin/users/{id}` elimina el usuario de la BD (cascade para relaciones). Los admins no pueden eliminar su propia cuenta. |
| F1.3.5 | Editar roles de usuario | 🚧 | El admin puede cambiar roles (USER ↔ ADMIN) mediante select inline + botón guardar. PUT `/admin/users/{id}/roles`. En progreso. |
| F1.3.6 | Moderar contenido de usuarios | 📋 | Revisar y eliminar notas/comentarios inapropiados. Panel de moderación con flags automáticos. Planificado para v0.2. |

---

## 2. Funcionalidades intermedias (v0.2)

### 2.1 Usuario anónimo

#### Alcance (actualizado)
Intermedio: estadísticas agregadas para invocadores con datos de partidas cacheados para rendimiento.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F2.1.1 | Estadísticas agregadas de invocadores | 📋 | Motor de agregación que combina datos de múltiples invocadores buscados: tasa de victorias media por campeón, KDA medio por rol, tasas de selección de campeones populares. Dashboard público con gráficos, optimizado con detalles de partidas en caché para reducir tiempos de carga. |

---

### 2.2 Usuario registrado

#### Alcance (actualizado)
Intermedio: análisis de rendimiento personal más profundos y contexto enriquecido del historial de partidas.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F2.2.1 | Rendimiento personal por campeones favoritos | 📋 | Paneles de análisis detallado para campeones jugados con frecuencia: tendencias de rendimiento, comparaciones por temporada, fortalezas/debilidades usando datos históricos. |
| F2.2.2 | Historial de partidas enriquecido | 📋 | Línea de tiempo mejorada de la partida con eventos, análisis por fases (early/mid/late) y comparativas con otros jugadores de la partida. Integra múltiples fuentes de la API de Riot. |
| F2.2.3 | Añadir notas a partidas | 📋 | Notas de texto libre por partida vinculadas a Match + User. Las notas pueden editarse y eliminarse. |
| F2.2.4 | Recibir notificaciones | 📋 | Notificaciones en tiempo real (WebSocket) cuando: un favorito juega una partida, un favorito sube de rango o alcanza un nuevo hito de maestría. Panel de notificaciones en el dashboard. |

---

### 2.3 Admin

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F2.3.1 | Panel de moderación | 📋 | Interfaz para revisar informes de usuarios, contenido marcado automáticamente y logs de actividad sospechosa. |
| F2.3.2 | Métricas del sistema | 📋 | Métricas: usuarios activos, búsquedas por día, invocadores más buscados, uso de API (límites), errores HTTP. Gráficos impulsados por Chart.js. |

---

## 3. Funcionalidades avanzadas (v1.0)

### 3.1 Usuario anónimo

#### Alcance (actualizado)
Avanzado: sistema de caché inteligente que minimiza latencia manteniendo datos frescos; estrategia híbrida de acceso a datos para equilibrar rendimiento y actualidad.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F3.1.1 | Sistema de caché inteligente | 📋 | Caché multinivel con estrategias adaptativas: in-memory (Redis), caché persistente (MySQL) e invalidación inteligente basada en tiempo/eventos. Reduce latencia manteniendo datos actualizados. |
| F3.1.2 | Estrategia híbrida de acceso a datos | 📋 | Algoritmo que equilibra automáticamente rendimiento vs. frescura: prefiere datos cacheados recientes, refresca selectivamente datos críticos y usa prefetching. Mejora la experiencia de usuario. |
| F3.1.3 | Estadísticas globales de la comunidad | 📋 | Dashboard público con estadísticas agregadas a nivel aplicación: invocadores más buscados, campeones con mayor tasa de victorias, estadísticas por región — respaldado por caché inteligente. |

---

### 3.2 Usuario registrado

#### Alcance (actualizado)
Avanzado: panel KPI personalizado derivado del historial completo de partidas; estrategia cache-first para minimizar llamadas externas costosas; validación automática de frescura.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F3.2.1 | Panel KPI | 📋 | Panel avanzado que calcula y muestra indicadores clave de rendimiento a lo largo del tiempo, análisis de tendencias y detección de patrones basado en el historial completo de partidas del usuario almacenado en la BD. |
| F3.2.2 | Estrategia priorizada cache-first | 📋 | Estrategia de acceso a datos que prefiere DB/caché local antes de llamar APIs externas: verificación de frescura con timestamps, actualización selectiva de entradas obsoletas, minimizar llamadas a la API de Riot. |
| F3.2.3 | Validación automática de frescura | 📋 | Comprobaciones automáticas y actualizaciones en background para datos obsoletos: análisis por antigüedad, refresco asíncrono en segundo plano y notificaciones opcionales a usuarios. |
| F3.2.4 | Recomendaciones de builds | 📋 | Algoritmo que sugiere builds, runas y rutas de objetos óptimas según estilo de juego (campeones, rol, KDA). Integra tendencias de la comunidad. |
| F3.2.5 | Clasificaciones personalizadas | 📋 | Leaderboards privados entre amigos/favoritos. Comparar estadísticas, tasas de victoria y maestrías en tablas privadas. |

---

### 3.3 Admin

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F3.3.1 | Registros de auditoría | 📋 | Registros detallados de auditoría: quién cambió qué, cuándo y desde qué IP. Buscable, filtrable y exportable a CSV. |
| F3.3.2 | Gestión de claves API | 📋 | Herramientas para rotar claves de la API de Riot, monitorizar límites de tasa y gestionar múltiples claves para balanceo de carga. |

---

## 4. Características técnicas

### 4.1 Seguridad

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.1 | Autenticación JWT | ✅ | Tokens firmados HS256, expiración 24 h, refresh token 7 días, almacenados en cookies HttpOnly. |
| FT.2 | Autorización basada en roles | ✅ | Spring Security con `@PreAuthorize`. Roles: USER, ADMIN. Guards de Angular para rutas protegidas. |
| FT.3 | Hash de contraseñas | ✅ | BCryptPasswordEncoder (strength 10). Las contraseñas nunca se almacenan en texto plano. |
| FT.4 | HTTPS | ✅ | Certificado JKS autofirmado para desarrollo, TLS 1.3 en producción. Puerto 443. |
| FT.5 | Validación de entrada | ✅ | `@Valid` en DTOs con Hibernate Validator. Saneamiento de cadenas para prevenir XSS/SQL injection. |

---

### 4.2 Integración con APIs externas

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.6 | API de Riot Games | ✅ | Integración completa con Account-v1, Summoner-v4, League-v4, Champion-Mastery-v4, Match-v5. Usa RestTemplate con lógica de reintento. |
| FT.7 | Data Dragon CDN | ✅ | Assets estáticos (imágenes de campeones, objetos, runas) cargados desde Riot Data Dragon. Versión 14.1.1. |
| FT.8 | Limitación de tasa | 📋 | Implementar rate limiting para la API de Riot (20 req/s, 100 req/2min). Propuesta: Bucket4j + Redis para v0.2. |

---

### 4.3 Rendimiento y escalabilidad

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.9 | Caché de invocadores | ✅ | Campo MySQL `lastSearchedAt`. Los datos se refrescan solo si han pasado >5 minutos desde la última búsqueda. |
| FT.10 | Caché distribuida | 📋 | Redis para respuestas de API y sesiones para reducir carga en BD y APIs externas (v0.2). |
| FT.11 | Carga perezosa | 📋 | Lazy-loading de módulos Angular para reducir el tamaño del bundle inicial (v0.2). |

---

### 4.4 Calidad y pruebas

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.12 | Tests unitarios backend | ✅ | JUnit 5 + Mockito. Meta de cobertura ≥60%. Tests para servicios, controladores y mappers. |
| FT.13 | Tests unitarios frontend | ✅ | Jasmine + Karma. Meta de cobertura ≥50%. Tests de componentes, servicios y guards. |
| FT.14 | Tests de integración | ✅ | Spring Boot Test con `@SpringBootTest`. Tests de endpoints usando MockMvc. |
| FT.15 | Tests E2E | 🚧 | Selenium WebDriver para flujos end-to-end: login → búsqueda → perfil. En progreso. |
| FT.16 | Análisis estático | ✅ | SonarCloud en la pipeline de CI. Métricas: bugs, code smells, vulnerabilidades. Quality Gate configurado. |

---

### 4.5 Despliegue y DevOps

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.17 | Docker multi-stage | ✅ | Dockerfile con 3 etapas: build de Node (Angular), build de Maven (Spring Boot), runtime JRE. Imagen objetivo <200MB. |
| FT.18 | Docker Compose | ✅ | Orquesta app + MySQL con healthchecks, depends_on y volúmenes persistentes. |
| FT.19 | CI/CD (GitHub Actions) | ✅ | Workflows: build (tests + calidad), deploy-dev (main), deploy-release (releases), manual-build. |
| FT.20 | Publicar en DockerHub | ✅ | Workflow automatizado publica imágenes con tags: dev, versión (0.1.0), latest. Artefactos OCI para compose. |
| FT.21 | Manifiestos de Kubernetes | 📋 | Manifiestos K8s para deployments, services e ingress. HPA para escalado horizontal (v1.0). |

---

## Resumen de estado

| Estado | Cantidad | Aproximado % |
|--------|--------:|------------:|
| ✅ Implementado | 30 | ~50% |
| 🚧 En progreso | 4 | ~7% |
| 📋 Planificado | 24 | ~40% |
| ⏸️ Tentativo | 2 | ~3% |
| **TOTAL** | **60** | **100%** |

---

[← Volver al README principal](../README.md) | [Ver funcionalidades con capturas →](Funcionalidades.md)
