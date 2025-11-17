# Funcionalidades - SPIRITBLADE v0.1

Este documento describe las funcionalidades implementadas en SPIRITBLADE v0.1, ilustradas con capturas y descripciones detalladas del comportamiento.

---

## 📑 Contenido
1. [Usuario anónimo](#usuario-anónimo)
2. [Usuario registrado](#usuario-registrado)
3. [Administrador](#administrador)

---

## Usuario anónimo

Los visitantes anónimos pueden usar la búsqueda y ver contenidos sin registrarse.

> **Nota de actualización (octubre 2025)**: Las funcionalidades en este documento se ajustaron para coincidir con el nuevo alcance del proyecto y el tiempo de desarrollo disponible. Ver [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para más detalles.

### Funciones principales
- Buscar invocadores y ver su perfil y clasificación
- Ver historial público de partidas con capa de caché
- Acceder a estadísticas básicas de campeones (maestría, campeones más jugados, métricas básicas de rendimiento)

### 1.1 Búsqueda de invocador

Descripción: Los usuarios pueden buscar cualquier invocador de League of Legends usando su Riot ID en el formato `gameName#tagLine`.

Captura:
![Home - Search](https://github.com/user-attachments/assets/f63da861-eb8b-41fe-9487-c8177f8054c9)

Comportamiento:
- Campo de búsqueda en la página de inicio
- Validación del formato de entrada (debe incluir `#`)
- Búsquedas recientes mostradas en la página de inicio
- Redirección automática al perfil del invocador encontrado

Ejemplo:
1. Abrir la página de inicio
2. Ingresar un Riot ID: `Player#EUW`
3. Presionar Enter o hacer clic en "Search"
4. El sistema redirige al perfil del invocador

---

### 1.2 Perfil del invocador

Descripción: Muestra la información completa del invocador incluyendo nivel, rango, estadísticas y maestrías de campeones.

Captura:
![Summoner Profile](https://github.com/user-attachments/assets/9a6220c3-e4ed-459a-a5f2-414312de0f7a)

Datos mostrados:
- Encabezado de perfil:
  - Icono de perfil (desde Data Dragon)
  - Riot ID completo
  - Nivel del invocador

- Estadísticas clasificadas:
  - División y liga (p. ej. Oro II)
  - LP (League Points)
  - Victorias y derrotas
  - Porcentaje de victorias calculado
  - Total de partidas jugadas

- Top 3 campeones:
  - Icono del campeón
  - Nombre del campeón
  - Nivel de maestría
  - Puntos de maestría

Fuente de datos: La mayor parte de los datos se obtienen en tiempo real desde la API de Riot Games y se cachean en la base de datos local para mejorar el rendimiento.

---

### 1.3 Historial de partidas

Descripción: Muestra las partidas recientes del invocador con detalles de rendimiento.

Captura:
*(Sección del perfil del invocador mostrando historial de partidas)*

Datos por partida:
- Resultado: Victoria (verde) o Derrota (rojo)
- Campeón jugado: icono y nombre
- KDA: asesinatos/muertes/asistencias
- Duración de la partida: minutos
- Marca temporal de la partida: hora de finalización

Paginación:
- 5 partidas por página por defecto
- Botones "Cargar más" para ver partidas anteriores
- Carga dinámica sin recargar la página

---

### 1.4 Búsquedas recientes

Descripción: La página de inicio lista las búsquedas más recientes de invocadores realizadas por cualquier usuario.

Comportamiento:
- Muestra los 10 invocadores más buscados recientemente
- Ordenado por fecha de búsqueda (más nuevo primero)
- Clic para abrir el perfil
- Se actualiza automáticamente con nuevas búsquedas

Características planificadas intermedias:
- Estadísticas agregadas por invocador usando datos de partidas cacheadas

Características avanzadas planificadas:
- Sistema de caché inteligente que minimice tiempos de carga y garantice frescura de datos
- Estrategia híbrida de acceso a datos que equilibre rendimiento y frescura

---

## Usuario registrado

Los usuarios registrados obtienen acceso a funcionalidades adicionales tras iniciar sesión.

### Funciones principales
- Panel personalizado
- Datos de partida detallados enriquecidos usando la API de Riot
- Ver campeones con mayor maestría y rendimiento personal

### Funciones intermedias
- Acceso a datos de rendimiento personal detallados para campeones favoritos
- Historial de partidas enriquecido con información contextual

### 2.1 Autenticación

Descripción: Inicio de sesión y registro usan autenticación basada en JWT.

Captura de inicio de sesión:
![Login](https://github.com/user-attachments/assets/381dfdd6-e915-4c34-ba98-b3cf9985855d)

Comportamiento de inicio de sesión:
- Validación de credenciales
- Emisión de token JWT
- Mensajes de error informativos:
  - Credenciales inválidas
  - Servidor no disponible
  - Errores de red
- Redirección automática al panel tras inicio exitoso

Comportamiento de registro:
- Validaciones del formulario:
  - Nombre de usuario requerido
  - Formato de email válido
  - Contraseña requerida
  - Confirmación de contraseña
- Las contraseñas deben coincidir
- Detección de usuario duplicado
- Inicio de sesión automático tras registro exitoso

Seguridad:
- Contraseñas hasheadas con BCrypt
- Tokens JWT con expiración
- Cookies HttpOnly usadas para almacenar tokens

---

### 2.2 Panel personal

Descripción: Panel personalizado con estadísticas y accesos rápidos.

Captura:
![Dashboard](https://github.com/user-attachments/assets/d63561f9-b167-4059-8c2e-c1dca6cbe1fe)

Secciones del panel:
- Perfil del usuario:
  - Nombre de usuario
  - Email registrado
  - Avatar (implementación parcial)

- Estadísticas personales:
  - Total de búsquedas realizadas
  - Invocadores favoritos guardados
  - Campeón más buscado (planificado)

- Acciones rápidas:
  - Buscar un invocador
  - Ver favoritos
  - Editar perfil

Nota: Algunas funciones del panel están planificadas para la v0.2 (gráficas, tendencias).

---

### 2.3 Gestión de favoritos

Descripción: Los usuarios pueden guardar invocadores favoritos para acceso rápido.

Estado actual (en desarrollo):
- Botón "Agregar a favoritos" en el perfil del invocador
- Lista de favoritos en el panel
- Notificaciones de actividad (planificadas para v0.2)
- Eliminar de favoritos

Estado: ✅ Modelo de datos implementado, UI en progreso

Funciones avanzadas planificadas:
- Panel personalizado con KPIs calculados desde el historial de partidas
- Caché inteligente que priorice la BD sobre llamadas externas costosas
- Validación automática de frescura con impacto mínimo en latencia

---

## Administrador

Los administradores tienen acceso completo a las funciones de gestión del sistema.

### 3.1 Panel de administración

Descripción: UI dedicada de administración con herramientas de gestión.

Captura:
![Admin Panel](https://github.com/user-attachments/assets/162964b0-f4f9-4521-837b-4e7b101fedd7)

Acceso:
- Requiere rol `ADMIN` en el token JWT
- Redirige automáticamente cuando faltan permisos
- Enlace en el menú visible solo para administradores

---

### 3.2 Gestión de usuarios

Descripción: Los admins pueden ver y gestionar todos los usuarios registrados.

Funciones:
- Listar usuarios:
  - Tabla con todos los usuarios registrados
  - Campos visibles: nombre, email, roles, estado
  - Búsqueda y filtros (en progreso)

- Activar / Desactivar usuarios:
  - Alternar la bandera `active` de un usuario
  - Usuarios desactivados no pueden iniciar sesión
  - Indicador visual del estado

- Eliminar usuarios:
  - Eliminación permanente
  - Confirmación antes de eliminar
  - Logs de auditoría (planificado para v0.2)

- Editar roles:
  - Asignar USER / ADMIN
  - Cambios de permisos inmediatos

Endpoints protegidos:
```http
GET  /admin/users                  # Listar usuarios
POST /admin/users/{id}/activate    # Activar
POST /admin/users/{id}/deactivate  # Desactivar
DELETE /admin/users/{id}           # Eliminar
```

---

### 3.3 Métricas del sistema

Descripción: Vista de métricas globales del sistema (planificado para v0.2).

Métricas planificadas:
- Total de usuarios registrados
- Total de búsquedas realizadas
- Invocadores más buscados
- Actividad por día/semana
- Uso de la API de Riot

Estado: 📋 Planificado para v0.2

---

## Notas técnicas

### Integración con la API de Riot

Todas las funciones de búsqueda dependen de las APIs oficiales de Riot Games:
- Account-v1: traducir Riot ID a PUUID
- Summoner-v4: datos del invocador
- League-v4: datos de clasificación
- Champion-Mastery-v4: estadísticas de maestría de campeones
- Match-v5: historial de partidas

### Base de datos

MySQL 8.0 es la única base de datos soportada (H2 ya no se usa):
- Almacena usuarios, invocadores, partidas y estadísticas
- Configurado con MySQL8Dialect
- Esquema autogenerado vía JPA/Hibernate
- Codificación UTF-8 (utf8mb4_unicode_ci)

### Caché

Para mejorar rendimiento y reducir llamadas externas:
- Los datos de invocador se cachean en MySQL
- `lastSearchedAt` se actualiza en cada búsqueda
- Las imágenes se sirven desde Data Dragon (CDN estático)

### Almacenamiento de archivos (MinIO)

MinIO se usa para almacenar avatares de usuario con validación estricta:
- Solo se aceptan archivos PNG (validación en 3 capas)
- Validación del encabezado PNG (`89 50 4E 47`)
- Verificaciones de extensión y Content-Type
- Bucket: `spiritblade-uploads`
- Región: `us-east-1`

### Seguridad

HTTPS requerido:
- El servidor funciona solo en HTTPS en el puerto 443
- Certificado SSL autofirmado para desarrollo
- JWT para autenticación (expiración 24h)
- Control de acceso por roles (USER, ADMIN)

Validación de archivos:
- Avatares solo PNG
- Verificación de "magic header"
- Tamaño máximo de archivo: 10MB

### Manejo de errores

La aplicación maneja escenarios de error comunes:
- Invocador no encontrado (404)
- Errores de la API de Riot (429 límite de tasa, 503 servicio no disponible)
- Errores de red
- Formato de Riot ID inválido
- Formato de archivo inválido (no PNG)
- JWT expirado o inválido

Todos los errores muestran mensajes informativos al usuario.

### Documentación interactiva de la API (Swagger UI)

Nuevo en v0.1: SPIRITBLADE incluye documentación REST interactiva usando Swagger UI.

Características:
- 📖 Exploración interactiva de todos los endpoints de la API
- 🔐 Autenticación JWT integrada en la UI
- 🧪 "Try it out" en vivo desde el navegador
- 📊 Esquemas de datos completos con ejemplos
- 🎨 UI moderna con filtro y búsqueda

Acceso (solo HTTPS):
- URL local: [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html)
- OpenAPI JSON: [https://localhost/v3/api-docs](https://localhost/v3/api-docs)
- OpenAPI YAML: [https://localhost/v3/api-docs.yaml](https://localhost/v3/api-docs.yaml)

⚠️ Importante: El servidor funciona solo en HTTPS en el puerto 443. Debes aceptar el certificado autofirmado la primera vez que lo visites.

Beneficios:
- Desarrolladores: explorar endpoints sin Postman
- Testers: ejercitar visualmente el comportamiento de la API
- Integradores: generar clientes desde OpenAPI
- Documentadores: documentación siempre actualizada que coincide con el código

Ejemplo de uso:
1. Iniciar la aplicación
2. Abrir [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html) (aceptar el certificado SSL)
3. Iniciar sesión mediante `POST /auth/login` para obtener un token
4. Hacer clic en "Authorize" y pegar el token
5. Probar cualquier endpoint autenticado con "Try it out"

Documentación completa:
- [API.md](API.md) - Guía rápida para acceder a Swagger UI
- [SWAGGER.md](SWAGGER.md) - Guía completa de Swagger
- [SWAGGER-QUICKSTART.md](SWAGGER-QUICKSTART.md) - Tutorial paso a paso

---

## Próximas funciones

Ver **[Funcionalidades Detalladas](Funcionalidades-Detalladas.md)** para la lista completa de funciones planificadas para futuras versiones.

---

[← Volver al README principal](../README.md)
