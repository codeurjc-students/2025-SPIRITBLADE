# Características detalladas — SPIRITBLADE

Este documento contiene la lista completa de características previstas para el proyecto SPIRITBLADE, indicando su estado de implementación y una descripción detallada.

> 📝 Actualizado en octubre de 2025: Este documento fue revisado para reflejar un reajuste del alcance del proyecto basado en el tiempo de desarrollo disponible y la priorización de las características principales. Consulte [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para los detalles completos de los cambios.

---

## Estado de implementación

### Leyenda
- ✅ Implementado — Función completa en v1.0
- 🚧 En progreso — Iniciado pero no terminado
- 💤 Planificado — No iniciado, previsto para futuras versiones

---

## 1. Características principales (v1.0)

### 1.1 Usuario anónimo

#### Alcance (actualizado)
Búsqueda de invocador y vista de su perfil (Elo, nivel, icono, Numero de partidas, Clasificatorias ganadas...), Estadísticas de campeones (maestrías y campeones más jugados), Lista de summoners buscados recientemente.

| ID | Función | Estado | Descripción del comportamiento |
|----|---------|--------|-------------------------------|
| F1.1.1 | Buscar invocadores | ✅ | El usuario introduce un Riot ID (gameName#tagLine) en el campo de búsqueda. El sistema valida el formato, consulta la API de Riot Games y muestra el perfil completo. Si el invocador no existe, se muestra un mensaje de error claro. |
| F1.1.2 | Ver perfil de invocador y rango | ✅ | La página de perfil muestra avatar, nivel, Riot ID, rango actual (tier/división/LP), victorias/derrotas, tasa de victorias y partidas totales. Los datos se obtienen de la API de Riot. |
| F1.1.3 | Ver estadísticas básicas de campeones | ✅ | Acceso a maestría de campeón: top 3 campeones más jugados puntos. Muestra icono, nombre, nivel de maestría y puntos totales. Datos provenientes de Champion-Mastery-v4. |
| F1.1.4 | Búsquedas recientes | ✅ | La página de inicio lista las 9 búsquedas de invocador más recientes realizadas por cualquier usuario, ordenadas por fecha (más recientes primero). Cada elemento enlaza al perfil del invocador. |
| F1.1.5 | Muestreo de imagenes reales de LOL | ✅ | Con el objetivo de hacer una experiencia más cercana a los jugadores, se han añadido imágenes reales de posiciones y campeones extraidas de las APIs de Riot Games. |
---

### 1.2 Usuario registrado

#### Alcance (actualizado)
Acceso a un panel de control (dashboard), Enlazar tu summoner de lol con tu cuenta de usuario, Gestion de foto de perfil propia.

| ID | Función | Estado | Descripción del comportamiento |
|----|---------|--------|-------------------------------|
| F1.2.1 | Panel de control | ✅ | Dashboard con información de perfil, estadísticas básicas y acciones rápidas. |
| F1.2.2 | Enlazar cuenta de LoL | ✅ | Vincular cuenta de League of Legends usando Riot ID para análisis automático. |
| F1.2.3 | Gestión de foto de perfil | ✅ | Subir y gestionar avatar de usuario (PNG only, MinIO). |
| F1.2.4 | Editar información personal | ✅ v1.2.0 | Actualizar nombre, email y contraseña mediante modal de edición de perfil. Validación de email y fortaleza de contraseña. Cambios aplicados inmediatamente con confirmación visual. |
| F1.2.5 | Inicio de sesión y registro | ✅ | Registro con email y contraseña, inicio de sesión con JWT. Validación y manejo de errores. |

---

## 2. Funcionalidades intermedias (v1.0)

### 2.1 Usuario anónimo

#### Alcance (actualizado)
Winrate del invocador, usando datos detallados de partidas, Detalles completos de cada partida del historial.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F2.1.1 | Estadísticas agregadas de invocadores | ✅ | Motor de agregación que combina datos de múltiples invocadores buscados: tasa de victorias media por campeón, KDA medio por rol, tasas de selección de campeones populares. Dashboard público con gráficos. |
| F2.1.2 | Detalles completos de cada partida | ✅ v1.2.0 | Historial de partidas con detalles: fecha, modo, duración, resultado, KDA, campeones, roles y **Vision Score por participante** (puntuación de visión individual en cada partida). Datos obtenidos de Match-v5. |

---

### 2.2 Usuario registrado

#### Alcance (actualizado)
Estadisticas sobre el summoner vinculado (Rango actual, LP ganado, rol principal, campeon favorito), Sistema de permite marcar como favorito otros summoner para seguimiento rápido, Analisis por inteligencia artificial de rendimiento del summoner vinculado.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F2.2.1 | Estadísticas del summoner vinculado | ✅ v1.2.0 | Mostrar rango actual, LP ganado en los últimos 7 días, rol principal basado en historial, campeón favorito por maestría y **Average Vision Score** (promedio de puntuación de visión en partidas clasificatorias). |
| F2.2.2 | Sistema de favoritos | ✅ | Marcar summoners como favoritos para seguimiento rápido. Lista en el dashboard con acceso directo a perfiles. |
| F2.2.3 | Análisis por IA | ✅ | Integración con Gemini AI para análisis avanzado de rendimiento del summoner vinculado. Recomendaciones personalizadas. |


---

## 3. Funcionalidades avanzadas (v1.0)

### 3.1 Usuario anónimo

#### Alcance (actualizado)
Avanzado: historial público de partidas paginable.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F3.1.1 | Historial público paginable | ✅ | Historial de partidas públicas con paginación avanzada. |

---

### 3.2 Usuario registrado

#### Alcance (actualizado)
Grafico de evolución de rango del summoner vinculado por cada cola de clasificatoria.

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| F3.2.1 | Gráfico de evolución de rango | ✅ | Visualización gráfica de la evolución del rango a lo largo del tiempo para cada cola de clasificatoria (SoloQ, Flex). Basado en datos históricos almacenados. |

---

## 4 Funcionalidades de administración

Gestión de usuarios (habilitar, deshabilitar, eliminar, editar), Filtros de búsqueda avanzada en el panel de administración y paginación, Creación de usuarios con personalización de rol.

| ID | Función | Estado | Descripción del comportamiento |
|----|---------|--------|-------------------------------|
| F1.3.1 | Acceder al panel de administración | ✅ | Requiere rol ADMIN en el JWT. El panel muestra gestión de usuarios. Usuarios no autorizados son redirigidos con un mensaje de error. |
| F1.3.2 | Listar todos los usuarios | ✅ | Tabla de todos los usuarios registrados mostrando ID, nombre, email, roles, estado activo/inactivo y fecha de registro. |
| F1.3.3 | Habilitar/deshabilitar usuarios | ✅ | Interruptor para cambiar el flag `active`. Usuarios deshabilitados no pueden iniciar sesión (verificado en UserLoginService). El cambio se aplica inmediatamente vía llamada API. |
| F1.3.4 | Eliminar usuarios | ✅ | Botón de eliminar con confirmación. |
| F1.3.5 | Editar roles de usuario | ✅ | El admin puede cambiar roles (USER -> ADMIN) mediante select + botón guardar. |
| F1.3.6 | Filtros de búsqueda avanzada | ✅ | Filtros por nombre, email, rol, estado activo. Paginación para listas grandes. |
| F1.3.7 | Creación de usuarios | ✅ | Formulario para crear usuarios con asignación de rol inicial. Útil para testing o soporte. |

## 5. Características técnicas

### 5.1 Seguridad

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.1 | Autenticación JWT | ✅ | Tokens para seguridad y autenticación. |
| FT.2 | Autorización basada en roles | ✅ | Spring Security con `@PreAuthorize`. Roles: USER, ADMIN. Guards de Angular para rutas protegidas. |
| FT.3 | Hash de contraseñas | ✅ | BCryptPasswordEncoder (strength 10). Las contraseñas nunca se almacenan en texto plano. |
| FT.4 | HTTPS | ✅ | Certificado JKS autofirmado para desarrollo, TLS 1.3 en producción. |

---

### 5.2 Integración con APIs externas

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.6 | API de Riot Games | ✅ | Integración completa con Account-v1, Summoner-v4, League-v4, Champion-Mastery-v4, Match-v5. Usa RestTemplate con lógica de reintento. |
| FT.7 | Data Dragon CDN | ✅ | Assets estáticos (imágenes de campeones, objetos, runas) cargados desde Riot Data Dragon. Versión 14.1.1. |
| FT.8 | Limitación de tasa | ✅ | Implementar rate limiting para la API de Riot (20 req/s, 100 req/2min). |
| FT.9 | Gemini AI | ✅ | Integración con Gemini AI para análisis avanzado. |

---

### 5.3 Rendimiento y escalabilidad

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.10 | Caché de sistema | ✅ | El sistema se asegura de que los datos estén al día para evitar llamadas innecesarias a las APIs externas. Los datos de campeones se almacenan en base de datos y se precargan al iniciar la aplicación. |
| FT.11 | Caché distribuida con Redis | ✅ | Redis con Spring Cache implementado para respuestas de API (summoners: 10min, masteries: 1h, matches: 24h, champions: 24h), reduciendo significativamente la carga en BD y APIs externas. Mejora el rendimiento y la experiencia del usuario. |

---

### 5.4 Calidad y pruebas

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.13 | Tests unitarios backend | ✅ | JUnit 5 + Mockito. Meta de cobertura ≥80%. Tests para servicios, controladores y mappers. |
| FT.14 | Tests unitarios frontend | ✅ | Jasmine + Karma. Meta de cobertura ≥80%. Tests de componentes, servicios y guards. |
| FT.15 | Tests de integración | ✅ | Spring Boot Test con `@SpringBootTest`. Tests de endpoints usando MockMvc. |
| FT.16 | Tests E2E | ✅ | Selenium WebDriver para flujos end-to-end: login → búsqueda → perfil. En progreso. |
| FT.17 | Análisis estático | ✅ | SonarCloud en la pipeline de CI. Métricas: bugs, code smells, vulnerabilidades. Quality Gate configurado. |

---

### 5.5 Ci/CD

| ID | Función | Estado | Descripción |
|----|---------|--------|-------------|
| FT.18 | Docker multi-stage | ✅ | Dockerfile con 3 etapas: build de Node (Angular), build de Maven (Spring Boot), runtime JRE. Imagen objetivo <200MB. |
| FT.19 | Docker Compose | ✅ | Orquesta app + MySQL con healthchecks, depends_on y volúmenes persistentes. |
| FT.20 | CI/CD (GitHub Actions) | ✅ | Workflows: build (tests + calidad), deploy-dev (main), deploy-release (releases), manual-build. |
| FT.21 | Publicar en DockerHub | ✅ | Workflow automatizado publica imágenes con tags: dev, versión (0.1.0), latest... Artefactos OCI para compose. |

