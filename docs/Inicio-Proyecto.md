# Inicio del proyecto — SPIRITBLADE

Este documento describe los objetivos iniciales, la metodología y el análisis del proyecto SPIRITBLADE tal como se definió en la Fase 1.

---

## 🎯 Objetivos

### Objetivo general

Desarrollar una aplicación web completa que permita a jugadores de League of Legends buscar, analizar y visualizar estadísticas de invocadores y partidas usando datos de la API pública de Riot Games, ofreciendo una plataforma intuitiva similar a OP.GG o Porofessor.

### Objetivos funcionales

> 📝 Actualizado octubre de 2025: Los objetivos funcionales se re-ajustaron para alinearse con el tiempo de desarrollo disponible y priorizar las funcionalidades esenciales del sistema. Ver [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para detalles completos.

SPIRITBLADE pretende ofrecer distintos niveles de funcionalidad según el tipo de usuario:

#### Usuarios anónimos

Funciones básicas:
- Buscar invocadores y ver su perfil y clasificación
- Ver historial de partidas público con una capa de caché
- Acceder a estadísticas básicas de campeones incluyendo maestrías, campeones más jugados y datos de rendimiento generales

Funciones intermedias:
- Estadísticas agregadas de invocadores usando datos de partidas cacheadas

Funciones avanzadas:
- Sistema de caché inteligente que minimiza tiempos de carga garantizando datos frescos
- Estrategia híbrida de acceso a datos que equilibra rendimiento y frescura

#### Usuarios registrados

Funciones básicas:
- Acceso a un panel de control personalizable (dashboard)
- Ver datos detallados de partidas recientes enriquecidos desde la API de Riot
- Ver maestría de campeones y rendimiento personal

Funciones intermedias:
- Acceso a datos detallados de rendimiento personal por campeones favoritos
- Historial de partidas enriquecido con información contextual

Funciones avanzadas:
- Dashboard personalizado con KPIs calculados a partir del historial de partidas
- Estrategia cache-first priorizando la base de datos antes de llamadas externas costosas
- Validación automática de frescura con impacto mínimo en tiempos percibidos de carga

#### Administradores
- Gestión completa de usuarios (habilitar, deshabilitar, eliminar)
- Panel de administración con métricas del sistema
- Moderación de contenido generado por usuarios
- Logs de auditoría

---

### Objetivos técnicos

El proyecto se enfoca en calidad de software y buenas prácticas de ingeniería:

#### Arquitectura y tecnologías
- SPA (Single Page Application): frontend en Angular + backend con API REST
- Stack moderno:
  - Frontend: Angular 17, TypeScript, SCSS
  - Backend: Spring Boot 3.4.3, Java 21
  - Base de datos: MySQL 8.0
- Seguridad: Spring Security + JWT para autenticación y autorización
- API externa: Integración con la API de Riot Games

#### Calidad y pruebas
- Objetivo de cobertura: mínimo 55% global
- Pruebas multinivel:
  - Unitarias: JUnit 5 + Mockito (backend), Jasmine + Karma (frontend)
  - Integración: Spring Boot Test
  - E2E: Selenium WebDriver
- Análisis estático: SonarCloud integrado en CI
- Objetivos de calidad: 0 bugs críticos, 0 vulnerabilidades

#### DevOps y despliegue
- CI/CD automatizado: workflows de GitHub Actions para:
  - Tests y gates de calidad en cada PR
  - Despliegue automático de la imagen `dev` al hacer merge en main
  - Despliegues de release con versionado semántico
- Contenerización: Docker multi-stage builds
- Orquestación: Docker Compose para app + MySQL
- Publicación: DockerHub con imágenes versionadas

#### Buenas prácticas
- Control de versiones con Git y estrategia de ramas
- Revisiones de código obligatorias mediante pull requests
- Commits con Conventional Commits para un historial limpio
- Documentación actualizada
- Diseño responsive para escritorio y móvil

---

## 📅 Metodología

### Enfoque de desarrollo

El proyecto sigue una metodología ágil iterativa e incremental:

- Iteraciones cortas: ciclos de 2–3 semanas
- Entregas frecuentes: versión desplegable al final de cada fase
- Feedback continuo: revisiones y ajustes regulares
- Mejora continua: refactorización y optimización

### Fases planificadas

#### Fase 1: Definición de funcionalidades y pantallas
Duración: hasta 15 de septiembre  
Estado: ✅ Completado

Entregables:
- ✅ Definición de objetivos funcionales y técnicos
- ✅ Lista priorizada de funcionalidades por tipo de usuario
- ✅ Wireframes y mockups de pantallas principales
- ✅ Análisis de entidades del dominio
- ✅ Definición de permisos y roles
- ✅ Especificación REST API preliminar

---

#### Fase 2: Configuración de repositorio y CI
Duración: hasta 15 de octubre  
Estado: ✅ Completado

Entregables:
- ✅ Repositorio en GitHub con estructura del proyecto
- ✅ Configuración de GitHub Actions CI
- ✅ Pruebas unitarias básicas (backend y frontend)
- ✅ Integración con SonarCloud
- ✅ Documentación de guía de desarrollo
- ✅ Reglas de protección de ramas en `main`

---

#### Fase 3: Versión 0.1 — Funcionalidades núcleo
Duración: hasta 15 de diciembre  
Estado: ✅ Completado

Entregables:
- ✅ API REST backend con:
  - Autenticación JWT
  - Endpoints para usuarios, invocadores, dashboard, admin
  - Integración con la API de Riot
  - Tests de integración
- ✅ Frontend en Angular con:
  - Componentes: Home, Login, Dashboard, Summoner, Admin
  - Servicios y guards
  - Routing y navegación
  - Tests unitarios
- ✅ Esquema MySQL
- ✅ Dockerfile optimizado multi-stage
- ✅ Despliegue con Docker Compose
- ✅ Workflows CI/CD:
  - Control de calidad en PRs
  - Publicación automática en DockerHub (dev + releases)
  - Build manual para pruebas
- ✅ Documentación actualizada

---

#### Fase 4: Versión 0.2 — Funcionalidades intermedias
Duración: hasta 1 de marzo  
Estado: 📋 Planificado

Objetivos:
- Análisis de rendimiento avanzado con gráficas (Chart.js)
- Sistema de notas de partidas
- Gestión completa de favoritos con notificaciones
- Dashboard de moderación para admins
- Tests E2E completos con Selenium
- Mejoras de UI/UX basadas en feedback

---

#### Fase 5: Versión 1.0 — Funcionalidades avanzadas
Duración: hasta 15 de abril  
Estado: 📋 Planificado

Objetivos:
- Estadísticas globales de la comunidad
- Recomendaciones inteligentes basadas en ML
- Clasificaciones personalizadas
- Sistema de informes por email (tentativo)
- Análisis predictivo de rendimiento
- Optimización de rendimiento y escalabilidad

---

#### Fase 6: Memoria del proyecto (TFG)
Duración: hasta 15 de mayo  
Estado: 📋 Planificado

Objetivos:
- Memoria completa del proyecto
- Documentación técnica exhaustiva
- Análisis de resultados
- Conclusiones y trabajo futuro

---

#### Fase 7: Defensa
Duración: hasta 15 de junio  
Estado: 📋 Planificado

Objetivos:
- Preparación de la presentación
- Demostración en vivo
- Defensa ante el tribunal

---

## 📐 Análisis inicial

### Funcionalidades iniciales

> 📝 Nota: Esta sección documenta las funcionalidades tal como se definieron originalmente en la Fase 1. Para el estado de implementación actual y las funcionalidades actualizadas, ver [Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md).

La lista completa de funcionalidades con estado (✅ implementado, 🚧 en progreso, 📋 planificado) está disponible en **[Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)**.

#### Resumen de funcionalidades por versión

**Versión 0.1 — Funcionalidades núcleo** (✅ Completado):
- Usuarios anónimos: búsqueda de invocador, vista de perfil y clasificación, historial de partidas cacheado, estadísticas básicas de campeones
- Usuarios registrados: dashboard personalizable, datos detallados de partidas, vistas de maestría de campeones
- Admin: panel de administración, gestión de usuarios, métricas del sistema

**Versión 0.2 — Funcionalidades intermedias** (📋 Planificado):
- Usuarios anónimos: estadísticas agregadas de invocadores con caché
- Usuarios registrados: rendimiento personal detallado, historial de partidas enriquecido

**Versión 1.0 — Funcionalidades avanzadas** (📋 Planificado):
- Usuarios anónimos: caché inteligente, estrategia híbrida de acceso a datos
- Usuarios registrados: dashboards con KPIs, estrategia priorizada de caché, validación automática de frescura

Para más detalles:
- **[Funcionalidades.md](Funcionalidades.md)** — Descripciones de UI con capturas
- **[Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)** — matriz completa de funcionalidades

#### Usuarios y permisos (análisis Fase 1)

Tipos de usuario:
1. Anónimo: acceso de solo lectura a datos públicos
2. Registrado: acceso a perfil personal y favoritos
3. Administrador: control total del sistema

Permisos por tipo:
- Anónimo: buscar y ver perfiles y partidas
- Registrado: lo anterior + dashboard personal, favoritos, notas
- Admin: lo anterior + gestión de usuarios, moderación, métricas del sistema

---

### Entidades del dominio

Diagrama conceptual de entidades:

```
┌─────────────┐           ┌──────────────┐
│   Usuario   │───────────│  Invocador   │
│             │  favoritos│              │
├─────────────┤           ├──────────────┤
│ id          │           │ id           │
│ nombre      │           │ puuid        │
│ email       │           │ riotId       │
│ pwdCodificada│          │ nombre       │
│ roles[]     │           │ nivel        │
│ activo      │           │ tier         │
│ fotoPerfil  │           │ rank         │
└─────────────┘           │ lp           │
                          │ victorias    │
                          │ derrotas     │
                          └──────────────┘
                                 │
                                 │ 1:N
                                 ▼
                          ┌──────────────┐
                          │   Partida    │
                          ├──────────────┤
                          │ matchId      │
                          │ championId   │
                          │ kills        │
                          │ deaths       │
                          │ assists      │
                          │ win          │
                          │ duracionJuego│
                          │ timestamp    │
                          └──────────────┘
```

Relaciones principales:
- Usuario N:M Invocador (favoritos)
- Invocador 1:N Partida (historial)
- Usuario 1:N Partida (notas sobre partidas — futuro)

---

### Imágenes y assets estáticos

Fuentes de imágenes:
- Avatares de usuario: blob almacenado en MySQL (campo `profilePic`)
- Iconos de perfil LoL: CDN Data Dragon de Riot
- Imágenes de campeones: CDN Data Dragon de Riot
- Iconos de objetos y runas: CDN Data Dragon (futuro)

Gestión de imágenes:
- Subidas de usuarios: validadas por tipo y tamaño máximo (5MB)
- Imágenes externas: URL generadas dinámicamente desde Data Dragon

---

### Gráficas y visualización de datos

Librería elegida: Chart.js

Tipos de gráficas planificadas:
- Línea: evolución de KDA, tasa de victorias a lo largo del tiempo
- Barras: campeones más jugados, comparativas de estadísticas
- Pastel: distribución de roles, tipos de partida
- Radar: perfil de habilidades (CS, visión, participación en kills)

Implementación planificada para v0.2

---

### Tecnología complementaria

#### Integración con la API de Riot Games

Endpoints utilizados:
- Account-v1: `/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}`
- Summoner-v4: `/lol/summoner/v4/summoners/by-puuid/{puuid}`
- League-v4: `/lol/league/v4/entries/by-puuid/{puuid}`
- Champion-Mastery-v4: `/lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}/top`
- Match-v5: `/lol/match/v5/matches/by-puuid/{puuid}/ids` y `/lol/match/v5/matches/{matchId}`

Consideraciones:
- Límites de tasa: 20 req/s, 100 req/2min (clave de desarrollo)
- Región: EUW por defecto, configurable
- Caché local para reducir llamadas

---

#### Análisis estático (SonarCloud)

Configuración:
- Integrado en GitHub Actions
- Quality Gate personalizado con métricas estrictas
- Análisis para Java, TypeScript, HTML, CSS

Métricas:
- Cobertura: ≥55%
- Bugs: 0 críticos
- Vulnerabilidades: 0
- Olores de código: <50
- Duplicación: <5%

---

#### Algoritmo avanzado (futuro)

Predicción de rendimiento:
- Modelo ML entrenado con datos históricos
- Features: composición de equipo, picks, bans, elo, estadísticas recientes
- Salida: probabilidad de victoria
- Framework: TensorFlow / scikit-learn (tentativo)

Estado: ⏸️ Tentativo para v1.0

---

### Mockups y wireframes

Los wireframes iniciales se desarrollaron en HTML/CSS estático y están disponibles en `utils/wireframes/`:

- `index.html` — Página principal de búsqueda
- `summoner.html` — Perfil de invocador
- `dashboard.html` — Dashboard de usuario registrado
- `admin.html` — Panel de administración
- `login.html` — Pantallas de login y registro

Estos mockups sirvieron como referencia para el diseño del frontend en Angular.

---

## 🔗 Referencias

- Riot API docs: https://developer.riotgames.com/docs/lol
- Data Dragon: https://ddragon.leagueoflegends.com/
- Spring Boot: https://spring.io/projects/spring-boot
- Angular: https://angular.io/
- Docker: https://docs.docker.com/

---

[← Volver al README principal](../README.md) | [Ver Metodología →](Seguimiento.md)
