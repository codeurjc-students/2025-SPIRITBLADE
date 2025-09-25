
API — SPIRITBLADE (Resumen rápido)

Este archivo resume los endpoints más relevantes implementados en la Fase 1. Para más detalles del proyecto, ver `README.md` en la raíz del repositorio.

Autenticación
- `POST /auth/login` — Request: `{ "username": "user", "password": "pass" }` — Response: JWT token.
- `GET /auth/me` — Devuelve datos del usuario actual (require Authorization: Bearer <token>).

Usuarios
- `POST /users` — Registrar nuevo usuario.
- `GET /users/{id}` — Obtener usuario por id.

Summoners
- `GET /summoners/{name}` — Buscar summoner por nombre (consulta externa y/o BD local).
- `POST /summoners` — Añadir summoner al sistema.

Dashboard
- `GET /dashboard/personal` — Estadísticas personales (requerido auth).
- `GET /dashboard/favorites` — Resumen de favoritos.

Administración (requiere rol ADMIN)
- `GET /admin/users` — Listar todos los usuarios.
- `DELETE /admin/users/{id}` — Borrar usuario.

Ejemplo de uso con `curl` (login + petición protegida)
1) Login
```
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```
2) Solicitar `/auth/me` usando el token devuelto (reemplazar TOKEN)
```
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/auth/me
```

Notas
- Base de datos objetivo: MySQL (configurable en `application.properties`). Para desarrollo se puede usar H2.
- Si se añade OpenAPI/Swagger, exportar el `swagger.json` a `docs/openapi.yaml` y enlazar desde aquí.

Enlaces y autoría
- Repositorio: https://github.com/codeurjc-students/2025-SPIRITBLADE
- Autor: Jorge Andrés Echevarría
