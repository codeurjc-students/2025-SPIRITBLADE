# Funcionalidades - SPIRITBLADE v0.1

Este documento describe las funcionalidades implementadas en la versión 0.1 de SPIRITBLADE, ilustradas con capturas de pantalla y explicaciones detalladas de su comportamiento.

---

## 📑 Índice
1. [Usuario Anónimo](#usuario-anónimo)
2. [Usuario Registrado](#usuario-registrado)
3. [Administrador](#administrador)

---

## Usuario Anónimo

Los usuarios anónimos pueden acceder a las funcionalidades de búsqueda y visualización sin necesidad de registrarse.

> **Nota de Actualización (Octubre 2025)**: Las funcionalidades descritas en este documento han sido reajustadas según el nuevo enfoque del proyecto y los tiempos de desarrollo disponibles. Consulta el documento [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) para más detalles sobre los cambios realizados.

### Funcionalidad Básica
- **Búsqueda de invocadores y visualización de su perfil y rango**
- **Visualización del historial público de partidas con sistema de caché**
- **Acceso a estadísticas básicas de campeones** incluyendo maestrías, campeones más jugados y datos de rendimiento general

### 1.1 Búsqueda de Invocadores

**Descripción**: Los usuarios pueden buscar cualquier invocador de League of Legends utilizando su Riot ID en formato `gameName#tagLine`.

**Captura de pantalla**:
![Home - Búsqueda](https://github.com/user-attachments/assets/f63da861-eb8b-41fe-9487-c8177f8054c9)

**Funcionalidad**:
- Campo de búsqueda en la página principal
- Validación del formato correcto (debe incluir `#`)
- Búsqueda recientes mostradas en la página de inicio
- Redirección automática al perfil encontrado

**Ejemplo de uso**:
1. Acceder a la página principal
2. Introducir un Riot ID: `Player#EUW`
3. Presionar Enter o hacer clic en "Buscar"
4. El sistema redirige al perfil del invocador

---

### 1.2 Visualización de Perfil de Invocador

**Descripción**: Muestra información completa del invocador incluyendo nivel, rango, estadísticas y maestrías de campeones.

**Captura de pantalla**:
![Perfil de Invocador](https://github.com/user-attachments/assets/9a6220c3-e4ed-459a-a5f2-414312de0f7a)

**Información mostrada**:
- **Cabecera del perfil**:
  - Icono de perfil (obtenido de Data Dragon)
  - Riot ID completo
  - Nivel del invocador
  
- **Estadísticas de ranked**:
  - Tier y división (ej: Gold II)
  - LP (League Points)
  - Victorias y derrotas
  - Tasa de victorias calculada
  - Total de partidas jugadas

- **Top 3 campeones**:
  - Icono del campeón
  - Nombre del campeón
  - Nivel de maestría
  - Puntos de maestría

**Fuente de datos**: Los datos se obtienen en tiempo real de la API de Riot Games y se cachean en la base de datos local para mejorar el rendimiento.

---

### 1.3 Historial de Partidas

**Descripción**: Muestra las partidas recientes del invocador con información detallada de rendimiento.

**Captura de pantalla**:
*(Sección del perfil de invocador que muestra el historial)*

**Información por partida**:
- **Resultado**: Victoria (verde) o Derrota (rojo)
- **Campeón jugado**: Icono y nombre
- **KDA**: Kills/Deaths/Assists
- **Duración de la partida**: En minutos
- **Fecha de la partida**: Timestamp de finalización

**Paginación**:
- 5 partidas por página por defecto
- Botones "Cargar más" para ver partidas anteriores
- Carga dinámica sin recargar la página

---

### 1.4 Búsquedas Recientes

**Descripción**: En la página principal se muestran los últimos invocadores buscados por cualquier usuario.

**Funcionalidad**:
- Lista de los 10 últimos invocadores buscados
- Ordenados por fecha de búsqueda (más reciente primero)
- Click rápido para acceder al perfil
- Se actualiza automáticamente con cada nueva búsqueda

### Funcionalidad Intermedia (Planificado)
- **Visualización de estadísticas agregadas por invocadores**, con información detallada de partidas almacenadas en caché

### Funcionalidad Avanzada (Planificado)
- **Sistema inteligente de caché** que minimiza los tiempos de carga mientras garantiza datos actualizados
- **Estrategia híbrida de acceso a datos** que balancea rendimiento y frescura de información

---

## Usuario Registrado

Los usuarios registrados acceden a funcionalidades adicionales tras iniciar sesión.

### Funcionalidad Básica
- **Acceso a panel de control personalizable**
- **Consulta de datos detallados de partidas recientes** con información enriquecida de la API de Riot
- **Visualización de campeones con mayor maestría y rendimiento personal**

### Funcionalidad Intermedia
- **Acceso a datos detallados de rendimiento personal** con campeones favoritos
- **Visualización del historial de partidas** con información contextual enriquecida

### 2.1 Sistema de Autenticación

**Descripción**: Sistema de login y registro con JWT para autenticación segura.

**Captura de pantalla - Login**:
![Login](https://github.com/user-attachments/assets/381dfdd6-e915-4c34-ba98-b3cf9985855d)

**Funcionalidad de Login**:
- Validación de credenciales
- Generación de token JWT
- Mensajes de error informativos:
  - Credenciales inválidas
  - Servidor no disponible
  - Errores de red
- Redirección automática al dashboard tras login exitoso

**Funcionalidad de Registro**:
- Formulario con validaciones:
  - Nombre de usuario requerido
  - Email con formato válido
  - Contraseña requerida
  - Confirmación de contraseña
- Validación de coincidencia de contraseñas
- Detección de usuarios duplicados
- Login automático tras registro exitoso

**Seguridad**:
- Contraseñas encriptadas con BCrypt
- Tokens JWT con expiración
- Cookies HttpOnly para almacenar tokens

---

### 2.2 Dashboard Personal

**Descripción**: Panel personalizado con estadísticas y acceso rápido a funciones.

**Captura de pantalla**:
![Dashboard](https://github.com/user-attachments/assets/d63561f9-b167-4059-8c2e-c1dca6cbe1fe)

**Secciones del dashboard**:
- **Perfil de usuario**:
  - Nombre de usuario
  - Email registrado
  - Avatar (pendiente implementación completa)
  
- **Estadísticas personales**:
  - Total de búsquedas realizadas
  - Invocadores favoritos guardados
  - Campeón más buscado (tentativo)

- **Accesos rápidos**:
  - Buscar nuevo invocador
  - Ver favoritos
  - Editar perfil

**Nota**: Algunas funcionalidades del dashboard están planificadas para la v0.2 (gráficos, tendencias).

---

### 2.3 Gestión de Favoritos

**Descripción**: Los usuarios pueden guardar invocadores favoritos para acceso rápido.

**Funcionalidad** (en desarrollo):
- Botón "Añadir a favoritos" en perfil de invocador
- Lista de favoritos en dashboard
- Notificaciones de actividad (planificado para v0.2)
- Eliminar de favoritos

**Estado**: ✅ Modelo de datos implementado, interfaz en desarrollo

### Funcionalidad Avanzada (Planificado)
- **Dashboard personalizado** con indicadores clave de rendimiento calculados a partir del historial de partidas
- **Sistema de caché inteligente** que prioriza la base de datos antes de realizar costosas llamadas a APIs externas
- **Validación automática de frescura de datos** con mínimo impacto en tiempos de carga

---

## Administrador

Los administradores tienen acceso completo a funciones de gestión del sistema.

### 3.1 Panel de Administración

**Descripción**: Interfaz dedicada para administradores con herramientas de gestión.

**Captura de pantalla**:
![Admin Panel](https://github.com/user-attachments/assets/162964b0-f4f9-4521-837b-4e7b101fedd7)

**Acceso**:
- Requiere rol `ADMIN` en el token JWT
- Redirección automática si no tiene permisos
- Link visible solo para administradores

---

### 3.2 Gestión de Usuarios

**Descripción**: Los administradores pueden ver y gestionar todos los usuarios del sistema.

**Funcionalidades**:
- **Listar usuarios**:
  - Tabla con todos los usuarios registrados
  - Información visible: nombre, email, roles, estado
  - Búsqueda y filtrado (en desarrollo)

- **Activar/Desactivar usuarios**:
  - Cambiar el estado `active` de un usuario
  - Los usuarios desactivados no pueden hacer login
  - Indicador visual del estado

- **Eliminar usuarios**:
  - Borrado permanente de usuarios
  - Confirmación antes de eliminar
  - Logs de auditoría (planificado v0.2)

- **Editar roles**:
  - Asignar rol USER/ADMIN
  - Cambio instantáneo de permisos

**Endpoints protegidos**:
```http
GET  /admin/users          # Listar usuarios
POST /admin/users/{id}/activate   # Activar
POST /admin/users/{id}/deactivate # Desactivar
DELETE /admin/users/{id}   # Eliminar
```

---

### 3.3 Estadísticas del Sistema

**Descripción**: Vista de métricas globales del sistema (planificado para v0.2).

**Métricas previstas**:
- Total de usuarios registrados
- Total de búsquedas realizadas
- Invocadores más buscados
- Actividad por día/semana
- Uso de la API de Riot

**Estado**: 📋 Planificado para versión 0.2

---

## Notas Técnicas

### Integración con Riot API

Todas las funcionalidades de búsqueda de invocadores utilizan la API oficial de Riot Games:

- **Account-v1**: Para obtener PUUID desde Riot ID
- **Summoner-v4**: Para datos del invocador
- **League-v4**: Para información de ranked
- **Champion-Mastery-v4**: Para estadísticas de campeones
- **Match-v5**: Para historial de partidas

### Caché de Datos

Para mejorar el rendimiento y reducir llamadas a la API:
- Los datos de invocadores se cachean en MySQL
- Se actualiza `lastSearchedAt` en cada búsqueda
- Las imágenes se obtienen de Data Dragon (CDN estático)

### Manejo de Errores

La aplicación maneja diversos escenarios de error:
- Invocador no encontrado (404)
- Error de API de Riot (429 rate limit, 503 servicio caído)
- Errores de red
- Formato de Riot ID inválido

Todos los errores muestran mensajes informativos al usuario.

---

## Próximas Funcionalidades

Ver **[Funcionalidades Detalladas](Funcionalidades-Detalladas.md)** para la lista completa de funcionalidades planificadas para versiones futuras.

---

**[← Volver al README principal](../README.md)**
