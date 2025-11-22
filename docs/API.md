# Documentación de la API — SPIRITBLADE

## Índice
- [Visión general](#visión-general)
- [Acceso a la documentación interactiva de la API](#acceso-a-la-documentación-interactiva-de-la-api)
- [Uso de Swagger UI](#uso-de-swagger-ui)
- [Autenticación con JWT](#autenticación-con-jwt)
- [Endpoints disponibles](#endpoints-disponibles)
- [Recursos adicionales](#recursos-adicionales)

---

## Visión general

SPIRITBLADE expone una **API REST** construida con Spring Boot que ofrece análisis de datos de League of Legends y gestión de usuarios.

**URL base**:
- **HTTPS**: `https://localhost` (puerto 443)

⚠️ Importante: el servidor funciona **solo con HTTPS**. No hay acceso por HTTP. Acepta el certificado autofirmado en tu navegador la primera vez que te conectes.

Autenticación: la mayoría de endpoints requieren un token JWT Bearer en la cabecera `Authorization`.

Documentación interactiva: SPIRITBLADE incluye **Swagger UI** para exploración y pruebas interactivas de la API. Proporciona una interfaz dinámica y siempre actualizada.

---

## Acceso a la documentación interactiva de la API

### Swagger UI

El **Swagger UI** ofrece una interfaz completa e interactiva para explorar y probar todos los endpoints de la API directamente desde el navegador.

**URL de acceso**:
- **HTTPS**: [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html)

Primera vez: Tu navegador mostrará una advertencia de seguridad porque el certificado SSL está autofirmado. Haz clic en "Avanzado" → "Continuar a localhost (inseguro)" para aceptarlo.

Funciones:
- 📖 Catálogo completo de endpoints con descripciones, parámetros y respuestas
- 🔐 Soporte de autenticación JWT para probar endpoints protegidos
- 🧪 Funcionalidad "Try it out" para ejecutar solicitudes directamente
- 📊 Ejemplos de request/response con estructuras de datos reales
- 🔍 Definiciones de esquemas para todos los DTOs y modelos
- 🎨 Interfaz moderna e intuitiva con filtrado y búsqueda

### Especificación OpenAPI

La especificación OpenAPI 3.0 en bruto está disponible en:
- **JSON**: [https://localhost/v3/api-docs](https://localhost/v3/api-docs)
- **YAML**: [https://localhost/v3/api-docs.yaml](https://localhost/v3/api-docs.yaml)

---

## Uso de Swagger UI

### Paso 1: Iniciar la aplicación

```powershell
# Opción 1: Maven (Windows)
cd backend
.\dotenvtosystemargs.ps1

# Opción 2: Docker
docker-compose up
```

### Paso 2: Abrir Swagger UI

Navega a [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html) en tu navegador.

Acepta el certificado cuando se solicite (Avanzado → Continuar a localhost).

### Paso 3: Explorar la API

El Swagger UI organiza los endpoints en categorías:
- Autenticación - Inicio de sesión, registro, gestión de tokens
- Usuarios - Gestión de perfil y favoritos
- Summoners - Integración con Riot API, búsqueda de summoners, estadísticas
- Dashboard - Análisis personal, historial de partidas
- Admin - Administración de usuarios, estadísticas del sistema

Haz clic en cualquier endpoint para ver:
- Descripción: qué hace el endpoint
- Parámetros: entradas obligatorias/opcionales
- Cuerpo de la petición: esquema JSON con ejemplos
- Respuestas: códigos HTTP y estructuras de respuesta
- Try it out: botón para ejecutar la petición

### Paso 4: Probar un endpoint

1. Haz clic en "Try it out"
2. Rellena los parámetros obligatorios
3. Para endpoints protegidos, añade tu token JWT (ver sección de Autenticación)
4. Haz clic en "Execute"
5. Visualiza la respuesta abajo (código de estado, cuerpo, cabeceras)

---

## Autenticación con JWT

La mayoría de endpoints requieren autenticación. Autentícate en Swagger UI de la siguiente manera.

### Paso 1: Registrar o iniciar sesión

1. Expande la sección Autenticación
2. Usa `POST /auth/register` para crear una cuenta nueva, o
3. Usa `POST /auth/login` con credenciales existentes:
```json
{
   "username": "miusuario",
   "password": "micontraseña"
}
```
4. Haz clic en "Execute"
5. Copia el token de la respuesta:
```json
{
   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Paso 2: Autorizar Swagger UI

1. Haz clic en el botón "Authorize" (icono 🔓) en la esquina superior derecha
2. En el campo "Value", pega tu token
3. Haz clic en "Authorize" y luego en "Close"

Todas las solicitudes posteriores en la UI incluirán el token automáticamente.

### Paso 3: Probar endpoints protegidos

Ahora puedes probar endpoints marcados con un icono de candado. Ejemplos:
- `GET /users/me` - Ver tu perfil
- `GET /dashboard/stats` - Ver estadísticas personales
- `POST /users/me/favorites/{summonerId}` - Añadir summoner a favoritos

---

## Endpoints disponibles

### Categorías de endpoints

Swagger UI organiza la API en categorías lógicas (rutas base: `/api/v1/`):

| Categoría | Ruta base | Descripción |
|----------|-----------|-------------|
| Autenticación | `/api/v1/auth` | Autenticación de usuarios y gestión de tokens JWT |
| Usuarios | `/api/v1/users` | Gestión de perfil y favoritos de usuario |
| Summoners | `/api/v1/summoners` | Datos de summoners de League of Legends vía Riot API |
| Dashboard | `/api/v1/dashboard` | Análisis y estadísticas personales |
| Archivos | `/api/v1/files` | Subida/descarga de archivos (fotos de perfil, almacenamiento MinIO - solo PNG) |
| Admin | `/api/v1/admin` | Operaciones administrativas (requiere rol ADMIN) |

### Algunas Referencia rápida

Endpoints públicos (sin autenticación):
- `POST /api/v1/auth/login` - Inicio de sesión
- `POST /api/v1/auth/register` - Crear cuenta nueva

Endpoints autenticados (JWT requerido):
- `GET /api/v1/users/me` - Perfil del usuario actual
- `GET /api/v1/users/me/favorites` - Obtener summoners favoritos del usuario
- `POST /api/v1/users/me/favorites/{summonerId}` - Añadir favorito
- `DELETE /api/v1/users/me/favorites/{summonerId}` - Eliminar favorito
- `GET /api/v1/summoners/search` - Buscar summoner por Riot ID
- `GET /api/v1/summoners/{puuid}` - Obtener detalles del summoner
- `GET /api/v1/summoners/{puuid}/champion-mastery` - Obtener mastery por campeón
- `GET /api/v1/dashboard/me/stats` - Estadísticas personales
- `GET /api/v1/dashboard/me/favorites` - Análisis de rendimiento

Endpoints de administrador (requiere rol ADMIN):
- `GET /api/v1/admin/users` - Listar todos los usuarios
- `PUT /api/v1/admin/users/{id}` - Actualizar usuario (activar/desactivar)
- `DELETE /api/v1/admin/users/{id}` - Eliminar usuario

Para detalles completos, consulta Swagger UI que refleja el código en ejecución.

---

## Recursos adicionales

### Guías complementarias

- [README.md](../README.md) - Página principal del proyecto
- [Funcionalidades.md](Funcionalidades.md) - Descripción de características con capturas
- [Guia-Desarrollo.md](Guia-Desarrollo.md) - Configuración de desarrollo y guía de contribución
- [Ejecucion.md](Ejecucion.md) - Instrucciones de despliegue con Docker

### Herramientas de desarrollo

Pruebas de la API:
- Swagger UI (recomendado) - `https://localhost/swagger-ui.html`
- Postman - importar especificación OpenAPI desde `https://localhost/v3/api-docs`
- curl - peticiones desde línea de comandos (usar `-k` para omitir verificación del certificado)

Exportar la especificación:
```powershell
# Formato JSON
curl -k https://localhost/v3/api-docs > openapi.json

# Formato YAML
curl -k https://localhost/v3/api-docs.yaml > openapi.yaml
```

Nota: la opción `-k` en curl omite la verificación SSL (necesario para certificados autofirmados en desarrollo).

---

## Limitación de tasa (Rate Limiting)

Debido a las limitaciones del proyecto tanto la API de Riot Games como la API de Gemini tienen restricciones de uso. Por lo que el uso excesivo de la API puede provocar bloqueos temporales. Se recomienda usar la API de forma moderada y evitar múltiples solicitudes en un corto período de tiempo. Se tiene contemplado que en un escenario de producción, se usarian versiónes de pago de dichas APIs para evitar estas limitaciones tan restrictivas. Aunque actualmente se han implementado mecanismos de caché de sistema (no en el usuario) y optimización para minimizar el número de llamadas externas.

---

## Seguridad

Solo HTTPS: la API funciona únicamente sobre HTTPS (puerto 443).

Certificado SSL: en desarrollo se usa un keystore autofirmado (`keystore.jks`). Acepta la advertencia del navegador para continuar.

Deshabilitar Swagger UI en producción: establece `springdoc.swagger-ui.enabled=false` en `application.properties`.

---

## Enlaces

Repositorio: https://github.com/JorgeAndresEcheverria/2025-SPIRITBLADE

Swagger / OpenAPI (HTTPS):
- Swagger UI: https://localhost/swagger-ui.html
- OpenAPI JSON: https://localhost/v3/api-docs
- OpenAPI YAML: https://localhost/v3/api-docs.yaml

---

## Autoría

Desarrollador: Jorge Andrés Echevarría
Tutor: Iván Chicano Capelo
Universidad: Universidad Rey Juan Carlos (URJC)
Curso: 2024-2025
Contacto: j.andres.2022@alumnos.urjc.es

---

Última actualización: Noviembre 2025 (v1.0)