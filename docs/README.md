# SPIRITBLADE — Documentation (Phase 1)

Resumen
SPIRITBLADE es una aplicación web para analizar y visualizar estadísticas de jugadores de League of Legends. Está diseñada como Trabajo de Fin de Grado (TFG) y sigue una arquitectura SPA: frontend en Angular y backend en Spring Boot (Java). Esta carpeta contiene la documentación de la Fase 1 (definición de funcionalidades, guías de desarrollo y control de calidad).

Estado del proyecto
- Backend: API REST en Spring Boot (Java 21). Implementación parcial (autenticación, summoners, dashboard).
- Frontend: SPA en Angular 17 (componentes principales implementados: home, login, dashboard, summoner, admin).
- Base de datos objetivo: MySQL (configuración para desarrollo puede usar H2/in-memory).
- CI: GitHub Actions con controles de calidad básicos y completos (ver `docs/CI-CD.md`).

Índice (archivos incluidos)
- `Guia-Desarrollo.md` — Guía práctica para montar el entorno, compilar, ejecutar y probar localmente.
- `Control-Calidad.md` — Estrategia de pruebas (unitarias, integración, sistema, E2E) y métricas objetivo.
- `API.md` — Resumen de los endpoints más relevantes y ejemplos de uso (curl).
- `CI-CD.md` — Descripción del pipeline en `.github/workflows/build.yml`, secretos requeridos y cómo reproducir los pasos localmente.
- `BRANCH-PROTECTION.md` — Recomendaciones de reglas para proteger `main` y flujos de PR.

Wireframes y mockups
- Los wireframes están incluidos en el repositorio en `utils/wireframes/` (HTML estático y CSS de referencia).

Proyecto y autoría
- Repositorio: https://github.com/codeurjc-students/2025-SPIRITBLADE
- Autor: Jorge Andrés Echevarría
- Tutor: Iván Chicano Capelo

Metodología (resumen)
- Fase 1: definición de funcionalidades y pantallas
- Fase 2: repositorio, pruebas y CI (actual)
- Fase 3..7: desarrollo incremental (ver `README.md` raíz para el plan completo)

Notas
- Los comandos del backend en los documentos están orientados a Windows PowerShell (uso de `mvnw.cmd`).
- Estos son borradores; ampliar con capturas, colecciones Postman y OpenAPI cuando estén disponibles.
