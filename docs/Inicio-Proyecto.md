# Inicio del Proyecto - SPIRITBLADE

Este documento describe los objetivos iniciales, metodología y análisis del proyecto SPIRITBLADE tal como se definió en la Fase 1.

---

## 🎯 Objetivos

### Objetivo General

Desarrollar una aplicación web completa que permita a los jugadores de League of Legends **buscar, analizar y visualizar** estadísticas de invocadores y partidas utilizando datos obtenidos de la API pública de Riot Games, ofreciendo una plataforma intuitiva similar a OP.GG o Porofessor.

### Objetivos Funcionales

> **📝 Actualización Octubre 2025**: Los objetivos funcionales han sido reajustados para alinearse con los tiempos de desarrollo disponibles y priorizar las funcionalidades core del sistema. Ver [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para detalles completos.

SPIRITBLADE busca proporcionar diferentes niveles de funcionalidad según el tipo de usuario:

#### Para Usuarios Anónimos

**Funcionalidad Básica**:
- Búsqueda de invocadores y visualización de su perfil y rango
- Visualización del historial público de partidas con sistema de caché
- Acceso a estadísticas básicas de campeones incluyendo maestrías, campeones más jugados y datos de rendimiento general

**Funcionalidad Intermedia**:
- Visualización de estadísticas agregadas por invocadores, con información detallada de partidas almacenadas en caché

**Funcionalidad Avanzada**:
- Sistema inteligente de caché que minimiza los tiempos de carga mientras garantiza datos actualizados
- Estrategia híbrida de acceso a datos que balancea rendimiento y frescura de información

#### Para Usuarios Registrados

**Funcionalidad Básica**:
- Acceso a panel de control personalizable
- Consulta de datos detallados de partidas recientes con información enriquecida de la API de Riot
- Visualización de campeones con mayor maestría y rendimiento personal

**Funcionalidad Intermedia**:
- Acceso a datos detallados de rendimiento personal con campeones favoritos
- Visualización del historial de partidas con información contextual enriquecida

**Funcionalidad Avanzada**:
- Dashboard personalizado con indicadores clave de rendimiento calculados a partir del historial de partidas
- Sistema de caché inteligente que prioriza la base de datos antes de realizar costosas llamadas a APIs externas
- Validación automática de frescura de datos con mínimo impacto en tiempos de carga

#### Para Administradores
- Gestión completa de usuarios (activar, desactivar, eliminar)
- Panel de administración con estadísticas del sistema
- Moderación de contenido generado por usuarios
- Logs de auditoría

---

### Objetivos Técnicos

El proyecto se desarrolla con foco en **calidad del software** y **buenas prácticas de ingeniería**:

#### Arquitectura y Tecnologías
- **SPA (Single Page Application)**: Frontend Angular + Backend REST API
- **Stack tecnológico moderno**:
  - Frontend: Angular 17, TypeScript, SCSS
  - Backend: Spring Boot 3.4.3, Java 21
  - Base de datos: MySQL 8.0
- **Seguridad**: Spring Security + JWT para autenticación y autorización
- **API externa**: Integración con Riot Games API

#### Calidad y Testing
- **Cobertura de tests**: Mínimo 55% global
- **Tests multinivel**:
  - Unitarios: JUnit 5 + Mockito (backend), Jasmine + Karma (frontend)
  - Integración: Spring Boot Test
  - E2E: Selenium WebDriver
- **Análisis estático**: SonarCloud integrado en CI
- **Métricas objetivo**: 0 bugs críticos, 0 vulnerabilidades

#### DevOps y Despliegue
- **CI/CD automatizado**: GitHub Actions con workflows para:
  - Tests y quality gates en cada PR
  - Deploy automático de imagen `dev` en merge a main
  - Deploy de releases con versionado semántico
- **Containerización**: Docker con multi-stage build
- **Orquestación**: Docker Compose para app + MySQL
- **Publicación**: DockerHub con imágenes versionadas

#### Buenas Prácticas
- Control de versiones con Git y estrategia de branching
- Code reviews obligatorios mediante Pull Requests
- Conventional Commits para historial limpio
- Documentación completa y actualizada
- Responsive design para desktop y mobile

---

## 📅 Metodología

### Enfoque de Desarrollo

El proyecto sigue una metodología **ágil iterativa e incremental**:

- **Iteraciones cortas**: Ciclos de 2-3 semanas
- **Entregas frecuentes**: Versión desplegable al final de cada fase
- **Feedback continuo**: Revisiones periódicas y ajustes
- **Mejora continua**: Refactoring y optimización constante

### Fases Planificadas

#### Fase 1: Definición de Funcionalidades y Pantallas
**Duración**: Hasta 15 de septiembre  
**Estado**: ✅ Completada

**Entregables**:
- ✅ Definición de objetivos funcionales y técnicos
- ✅ Lista priorizada de funcionalidades por tipo de usuario
- ✅ Wireframes y mockups de pantallas principales
- ✅ Análisis de entidades del dominio
- ✅ Definición de permisos y roles
- ✅ Especificación de API REST preliminar

---

#### Fase 2: Configuración de Repositorio y CI
**Duración**: Hasta 15 de octubre  
**Estado**: ✅ Completada

**Entregables**:
- ✅ Repositorio GitHub con estructura de proyecto
- ✅ Configuración de GitHub Actions para CI
- ✅ Tests unitarios básicos (backend y frontend)
- ✅ Integración con SonarCloud
- ✅ Documentación de guía de desarrollo
- ✅ Branch protection rules en `main`

---

#### Fase 3: Versión 0.1 - Funcionalidades Básicas
**Duración**: Hasta 15 de diciembre  
**Estado**: ✅ Completada

**Entregables**:
- ✅ Backend REST API con:
  - Autenticación JWT
  - Endpoints de usuarios, invocadores, dashboard, admin
  - Integración con Riot API
  - Tests de integración
- ✅ Frontend Angular con:
  - Componentes: Home, Login, Dashboard, Summoner, Admin
  - Servicios y guards
  - Routing y navegación
  - Tests unitarios
- ✅ Base de datos MySQL con esquema completo
- ✅ Dockerfile multi-stage optimizado
- ✅ Docker Compose para despliegue
- ✅ CI/CD workflows:
  - Quality control en PRs
  - Deploy automático a DockerHub (dev + releases)
  - Build manual para testing
- ✅ Documentación actualizada

---

#### Fase 4: Versión 0.2 - Funcionalidades Intermedias
**Duración**: Hasta 1 de marzo  
**Estado**: 📋 Planificada

**Objetivos**:
- Análisis avanzado de rendimiento con gráficos (Chart.js)
- Sistema de notas en partidas
- Gestión completa de favoritos con notificaciones
- Dashboard de moderación para administradores
- Tests E2E completos con Selenium
- Mejoras de UI/UX basadas en feedback

---

#### Fase 5: Versión 1.0 - Funcionalidades Avanzadas
**Duración**: Hasta 15 de abril  
**Estado**: 📋 Planificada

**Objetivos**:
- Estadísticas globales de la comunidad
- Recomendaciones inteligentes basadas en ML
- Rankings personalizados entre usuarios
- Sistema de reportes por email (tentativo)
- Análisis predictivo de rendimiento
- Optimización de performance y escalabilidad

---

#### Fase 6: Redacción de Memoria del TFG
**Duración**: Hasta 15 de mayo  
**Estado**: 📋 Planificada

**Objetivos**:
- Memoria completa del proyecto
- Documentación técnica exhaustiva
- Análisis de resultados
- Conclusiones y trabajo futuro

---

#### Fase 7: Defensa del TFG
**Duración**: Hasta 15 de junio  
**Estado**: 📋 Planificada

**Objetivos**:
- Preparación de presentación
- Demostración en vivo
- Defensa ante tribunal

---

## 📐 Análisis Inicial

### Funcionalidades Iniciales

> **📝 Nota**: Esta sección documenta las funcionalidades tal como fueron definidas originalmente en la Fase 1. Para ver el estado actual de implementación y las funcionalidades actualizadas, consulta [Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md).

La lista completa de funcionalidades planificadas, con su estado de implementación (✅ implementado, 🚧 en desarrollo, 📋 planificado), se encuentra en el documento **[Funcionalidades Detalladas](Funcionalidades-Detalladas.md)**.

#### Resumen de Funcionalidades por Versión

**Versión 0.1 - Funcionalidades Básicas** (✅ Completada):
- Usuario anónimo: Búsqueda de invocadores, visualización de perfil y rango, historial de partidas con caché, estadísticas básicas de campeones
- Usuario registrado: Panel de control personalizable, datos detallados de partidas, visualización de campeones con maestría
- Administrador: Panel de administración, gestión de usuarios, estadísticas del sistema

**Versión 0.2 - Funcionalidades Intermedias** (📋 Planificada):
- Usuario anónimo: Estadísticas agregadas por invocadores con caché
- Usuario registrado: Datos detallados de rendimiento personal, historial enriquecido con contexto

**Versión 1.0 - Funcionalidades Avanzadas** (📋 Planificada):
- Usuario anónimo: Sistema inteligente de caché, estrategia híbrida de acceso a datos
- Usuario registrado: Dashboard con KPIs, sistema de caché inteligente priorizado, validación automática de frescura

Para más detalles sobre cada funcionalidad específica, consulta:
- **[Funcionalidades.md](Funcionalidades.md)** - Descripciones con capturas de pantalla
- **[Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)** - Lista completa con estados

#### Usuarios y Permisos (Análisis Inicial Fase 1)

**Tipos de usuario**:
1. **Anónimo**: Acceso de solo lectura a datos públicos
2. **Registrado**: Acceso a perfil personalizado y favoritos
3. **Administrador**: Control total del sistema

**Permisos por tipo**:
- Anónimo: Buscar, visualizar perfiles y partidas
- Registrado: Todo lo anterior + dashboard personal, favoritos, notas
- Admin: Todo lo anterior + gestión de usuarios, moderación, estadísticas del sistema

---

### Entidades del Dominio

**Diagrama de entidades** (modelo conceptual):

```
┌─────────────┐           ┌──────────────┐
│    User     │───────────│  Summoner    │
│             │  favoritos│              │
├─────────────┤           ├──────────────┤
│ id          │           │ id           │
│ name        │           │ puuid        │
│ email       │           │ riotId       │
│ encodedPwd  │           │ name         │
│ roles[]     │           │ level        │
│ active      │           │ tier         │
│ profilePic  │           │ rank         │
└─────────────┘           │ lp           │
                          │ wins         │
                          │ losses       │
                          └──────────────┘
                                 │
                                 │ 1:N
                                 ▼
                          ┌──────────────┐
                          │    Match     │
                          ├──────────────┤
                          │ matchId      │
                          │ championId   │
                          │ kills        │
                          │ deaths       │
                          │ assists      │
                          │ win          │
                          │ gameDuration │
                          │ timestamp    │
                          └──────────────┘
```

**Relaciones principales**:
- User N:M Summoner (favoritos)
- Summoner 1:N Match (historial)
- User 1:N Match (para notas en partidas - futuro)

---

### Imágenes y Recursos Estáticos

**Fuentes de imágenes**:
- **Avatares de usuario**: Blob almacenado en MySQL (campo `profilePic`)
- **Iconos de perfil LoL**: Data Dragon CDN de Riot Games
- **Imágenes de campeones**: Data Dragon CDN
- **Iconos de ítems y runas**: Data Dragon CDN (futuro)

**Gestión de imágenes**:
- Imágenes de usuario: upload con validación (tipo, tamaño máx 5MB)
- Imágenes externas: URLs generadas dinámicamente desde Data Dragon

---

### Gráficos y Visualización de Datos

**Biblioteca elegida**: Chart.js

**Tipos de gráficos previstos**:
- **Líneas**: Evolución de KDA, winrate a lo largo del tiempo
- **Barras**: Campeones más jugados, comparación de estadísticas
- **Pie**: Distribución de roles, tipos de partidas
- **Radar**: Perfil de habilidades (CS, vision score, kill participation)

**Implementación**: Planificado para v0.2

---

### Tecnología Complementaria

#### Integración con API Externa (Riot Games)

**Endpoints utilizados**:
- **Account-v1**: `/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}`
- **Summoner-v4**: `/lol/summoner/v4/summoners/by-puuid/{puuid}`
- **League-v4**: `/lol/league/v4/entries/by-puuid/{puuid}`
- **Champion-Mastery-v4**: `/lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}/top`
- **Match-v5**: `/lol/match/v5/matches/by-puuid/{puuid}/ids` y `/lol/match/v5/matches/{matchId}`

**Consideraciones**:
- Rate limits: 20 req/s, 100 req/2min (Development API Key)
- Región: EUW por defecto, configurable
- Caché local para reducir llamadas

---

#### Herramienta de Análisis de Código (SonarCloud)

**Configuración**:
- Integrado en GitHub Actions
- Quality Gate customizado con métricas estrictas
- Análisis de código Java, TypeScript, HTML, CSS

**Métricas**:
- Coverage: ≥55%
- Bugs: 0 críticos
- Vulnerabilidades: 0
- Code Smells: <50
- Duplicación: <5%

---

#### Algoritmo Avanzado (Futuro)

**Predicción de rendimiento**:
- Modelo de ML entrenado con datos históricos
- Features: composición de equipo, picks, bans, elo, estadísticas recientes
- Output: Probabilidad de victoria
- Framework: TensorFlow / scikit-learn (tentativo)

**Estado**: ⏸️ Tentativo para v1.0

---

### Mockups y Wireframes

Los wireframes iniciales se desarrollaron en HTML/CSS estático y están disponibles en `utils/wireframes/`:

- `index.html` - Página principal con búsqueda
- `summoner.html` - Perfil de invocador
- `dashboard.html` - Dashboard de usuario registrado
- `admin.html` - Panel de administración
- `login.html` - Pantallas de login y registro

Estos mockups sirvieron de referencia para el diseño del frontend Angular.

---

## 🔗 Referencias

- **Documentación de Riot API**: https://developer.riotgames.com/docs/lol
- **Data Dragon**: https://ddragon.leagueoflegends.com/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Angular**: https://angular.io/
- **Docker**: https://docs.docker.com/

---

**[← Volver al README principal](../README.md)** | **[Ver Metodología Completa →](Seguimiento.md)**
