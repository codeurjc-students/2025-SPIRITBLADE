# API Documentation — SPIRITBLADE

## Index
- [Overview](#overview)
- [Accessing the Interactive API Documentation](#accessing-the-interactive-api-documentation)
- [Using Swagger UI](#using-swagger-ui)
- [Authentication with JWT](#authentication-with-jwt)
- [Available Endpoints](#available-endpoints)
- [Quick Start](#quick-start)
- [Additional Resources](#additional-resources)

---

## Overview

SPIRITBLADE exposes a **REST API** built with Spring Boot 3.4.3 that provides League of Legends data analysis and user management capabilities.

**Base URL**:
- **HTTPS**: `https://localhost` (port 443)

⚠️ Important: The server runs **HTTPS only**. There is no HTTP access. Accept the self-signed certificate in your browser the first time you connect.

Authentication: Most endpoints require a JWT Bearer token in the `Authorization` header.

Interactive Documentation: SPIRITBLADE includes **Swagger UI** for interactive API exploration and testing. This provides a dynamic, always up-to-date interface.

---

## Accessing the Interactive API Documentation

### Swagger UI

The **Swagger UI** provides a complete, interactive interface to explore and test all API endpoints directly from your browser.

**Access URL**:
- **HTTPS**: [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html)

First time: Your browser will show a security warning because the SSL certificate is self-signed. Click "Advanced" → "Proceed to localhost (unsafe)" to accept it.

Features:
- 📖 Complete endpoint catalog with descriptions, parameters, and responses
- 🔐 JWT authentication support for testing protected endpoints
- 🧪 "Try it out" functionality to execute requests directly
- 📊 Request/response examples with real data structures
- 🔍 Schema definitions for all DTOs and models
- 🎨 Modern, intuitive interface with filtering and search

### OpenAPI Specification

The raw OpenAPI 3.0 specification is available at:
- **JSON**: [https://localhost/v3/api-docs](https://localhost/v3/api-docs)
- **YAML**: [https://localhost/v3/api-docs.yaml](https://localhost/v3/api-docs.yaml)

Use these URLs to:
- Import into Postman or Insomnia
- Generate client SDKs with OpenAPI Generator
- Integrate with CI/CD pipelines
- Share with external developers

---

## Using Swagger UI

### Step 1: Start the Application

```powershell
# Option 1: Maven (Windows)
cd backend
.\mvnw.cmd spring-boot:run

# Option 2: Docker
docker-compose up

# Option 3: Run JAR
java -jar backend/target/tfg-0.1.0.jar
```

### Step 2: Open Swagger UI

Navigate to [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html) in your browser.

Accept the certificate when prompted (Advanced → Proceed to localhost).

### Step 3: Explore the API

The Swagger UI organizes endpoints into categories:
- Authentication - Login, registration, token management
- Users - Profile management, favorites
- Summoners - Riot API integration, summoner search, statistics
- Dashboard - Personal analytics, match history
- Admin - User administration, system statistics

Click any endpoint to see:
- Description: what the endpoint does
- Parameters: required/optional inputs
- Request body: JSON schema with examples
- Responses: HTTP status codes and response structures
- Try it out: button to execute the request

### Step 4: Test an Endpoint

1. Click "Try it out"
2. Fill in required parameters
3. For protected endpoints, add your JWT token (see Authentication section)
4. Click "Execute"
5. View the response below (status code, body, headers)

---

## Authentication with JWT

Most endpoints require authentication. Authenticate in Swagger UI as follows.

### Step 1: Register or Login

1. Expand the Authentication section
2. Use `POST /auth/register` to create a new account, or
3. Use `POST /auth/login` with existing credentials:
```json
{
   "username": "myuser",
   "password": "mypassword"
}
```
4. Click "Execute"
5. Copy the token from the response:
```json
{
   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Step 2: Authorize Swagger UI

1. Click the "Authorize" button (🔓 icon) at the top right
2. In the "Value" field, paste your token
3. Click "Authorize" then "Close"

All subsequent requests in the UI will include the token automatically.

### Step 3: Test Protected Endpoints

Now you can test endpoints marked with a lock icon. Examples:
- `GET /users/me` - View your profile
- `GET /dashboard/stats` - View personal stats
- `POST /users/me/favorites/{summonerId}` - Add favorite summoner

---

## Available Endpoints

### Endpoint Categories

Swagger UI organizes the API into logical categories (base routes: `/api/v1/`):

| Category | Base Path | Description |
|----------|-----------|-------------|
| Authentication | `/api/v1/auth` | User authentication and JWT token management |
| Users | `/api/v1/users` | User profile and favorites management |
| Summoners | `/api/v1/summoners` | League of Legends summoner data via Riot API |
| Dashboard | `/api/v1/dashboard` | Personal analytics and statistics |
| Files | `/api/v1/files` | File upload/download (profile pictures, MinIO storage - PNG only) |
| Admin | `/api/v1/admin` | Administrative operations (requires ADMIN role) |

### Quick Reference

Public Endpoints (no authentication):
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - Create new account

Authenticated Endpoints (JWT required):
- `GET /api/v1/users/me` - Current user profile
- `PUT /api/v1/users/me` - Update user profile
- `POST /api/v1/users/me/avatar` - Upload avatar (PNG only)
- `GET /api/v1/users/me/favorites` - Get user's favorite summoners
- `POST /api/v1/users/me/favorites/{summonerId}` - Add favorite
- `DELETE /api/v1/users/me/favorites/{summonerId}` - Remove favorite
- `GET /api/v1/summoners/search` - Search summoner by Riot ID
- `GET /api/v1/summoners/{puuid}` - Get summoner details
- `GET /api/v1/summoners/{puuid}/ranked-stats` - Get ranked statistics
- `GET /api/v1/summoners/{puuid}/champion-mastery` - Get champion mastery
- `GET /api/v1/dashboard/stats` - Personal statistics
- `GET /api/v1/dashboard/matches` - Match history
- `GET /api/v1/dashboard/performance` - Performance analytics

Admin Endpoints (ADMIN role required):
- `GET /api/v1/admin/users` - List all users
- `PUT /api/v1/admin/users/{id}` - Update user (activate/deactivate)
- `DELETE /api/v1/admin/users/{id}` - Delete user
- `GET /api/v1/admin/stats` - System statistics

For complete details, refer to the Swagger UI which reflects the running codebase.

---

## Quick Start

### Testing the API in 5 Minutes

1. Start the application:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

2. Open Swagger UI: [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html)

Accept the self-signed certificate when prompted.

3. Register a user:
- Expand Authentication → POST /auth/register
- Click "Try it out", enter username/email/password, then Execute

4. Login:
- Use POST /auth/login with your credentials and copy the returned token

5. Authorize Swagger:
- Click "Authorize" and paste your token

6. Test protected endpoints (e.g. GET /users/me, GET /summoners/search)

---

## Additional Resources

### Complete Guides

For detailed Swagger documentation, see:
- [SWAGGER.md](SWAGGER.md) - Complete Swagger guide with configuration and best practices
- [SWAGGER-QUICKSTART.md](SWAGGER-QUICKSTART.md) - Quickstart tutorial

### Other Documentation

- [README.md](../README.md) - Main project page
- [Funcionalidades.md](Funcionalidades.md) - Feature descriptions with screenshots
- [Guia-Desarrollo.md](Guia-Desarrollo.md) - Development setup and contributing guide
- [Ejecucion.md](Ejecucion.md) - Docker deployment instructions

### Development Tools

Testing the API:
- Swagger UI (recommended) - `https://localhost/swagger-ui.html`
- Postman - import OpenAPI spec from `https://localhost/v3/api-docs`
- Insomnia - import OpenAPI spec
- REST Client (VS Code) - use `.http` files with HTTPS URLs
- curl - command-line requests (use `-k` to skip certificate verification)

Exporting the specification:
```powershell
# JSON format
curl -k https://localhost/v3/api-docs > openapi.json

# YAML format
curl -k https://localhost/v3/api-docs.yaml > openapi.yaml
```

Note: the `-k` flag in curl skips SSL verification (needed for self-signed certs in development).

---

## Error Responses

All errors follow a consistent JSON format.

401 Unauthorized (invalid or expired token):
```json
{
   "timestamp": "2024-01-15T10:30:00",
   "status": 401,
   "error": "Unauthorized",
   "message": "Invalid or expired JWT token"
}
```

404 Not Found (resource doesn't exist):
```json
{
   "timestamp": "2024-01-15T10:30:00",
   "status": 404,
   "error": "Not Found",
   "message": "Summoner not found"
}
```

429 Too Many Requests (rate limit exceeded):
```json
{
   "timestamp": "2024-01-15T10:30:00",
   "status": 429,
   "error": "Too Many Requests",
   "message": "Riot API rate limit exceeded. Please retry after 60 seconds."
}
```

Refer to Swagger UI for complete error response schemas per endpoint.

---

## Rate Limiting

Riot API rate limits applied by the backend:
- Summoner Search: 20 requests per second
- Match History: 100 requests per 2 minutes

If you exceed limits you'll receive `429 Too Many Requests` with a `Retry-After` header.

---

## Security

HTTPS only: the API runs on HTTPS (port 443) only.

SSL certificate: development uses a self-signed keystore (`keystore.jks`). Accept the browser warning to proceed.

JWT expiration: tokens expire after 24 hours. Use `/auth/refresh` to renew tokens.

Disable Swagger UI in production by setting `springdoc.swagger-ui.enabled=false` in `application.properties`.

---

## Links

Repository: https://github.com/JorgeAndresEcheverria/2025-SPIRITBLADE

Swagger / OpenAPI (HTTPS):
- Swagger UI: https://localhost/swagger-ui.html
- OpenAPI JSON: https://localhost/v3/api-docs
- OpenAPI YAML: https://localhost/v3/api-docs.yaml

Documentation:
- [SWAGGER.md](SWAGGER.md)
- [SWAGGER-QUICKSTART.md](SWAGGER-QUICKSTART.md)
- [Guia-Desarrollo.md](Guia-Desarrollo.md)
- [Funcionalidades.md](Funcionalidades.md)

---

## Authorship

Developer: Jorge Andrés Echevarría
Advisor: Iván Chicano Capelo
University: Universidad Rey Juan Carlos (URJC)
Course: 2024-2025
Contact: j.echeverria.2021@alumnos.urjc.es

---

Last Updated: January 2025 (v0.1 - Swagger Integration)

[← Back to Main README](../README.md) | [View Swagger UI →](https://localhost/swagger-ui.html) | [View All Documentation →](../README.md#documentation)
