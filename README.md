# SPIRITBLADE ⚔️

<p align="center">
  <img width="300" alt="SPIRITBLADE Logo" src="https://github.com/user-attachments/assets/1f73258c-5c4a-4d87-ade7-3aaa546827b9" />
</p>

<p align="center">
  <strong>Análisis y visualización de estadísticas de League of Legends</strong>
</p>

---

## 📋 Versión 0.1 - Funcionalidades Básicas

**SPIRITBLADE v0.1** es una aplicación web que permite a los jugadores de League of Legends buscar y visualizar estadísticas de invocadores en tiempo real. La aplicación se conecta a la API oficial de Riot Games para obtener información actualizada sobre perfil, rango, historial de partidas y estadísticas de campeones.

### Características principales de la v0.1:

✅ **Búsqueda de invocadores** por Riot ID (gameName#tagLine)  
✅ **Visualización de perfil** con nivel, icono y rango actual  
✅ **Historial de partidas** recientes con KDA y resultado  
✅ **Estadísticas de campeones** más jugados con nivel de maestría  
✅ **Sistema de autenticación** con JWT para usuarios registrados  
✅ **Panel de administración** para gestión de usuarios (rol ADMIN)  
✅ **Búsquedas recientes** en página de inicio  
✅ **Despliegue con Docker** listo para producción

---

## 📸 Capturas de Pantalla (v0.1)

### Página Principal - Búsqueda
<p align="center">
  <img src="https://github.com/user-attachments/assets/f63da861-eb8b-41fe-9487-c8177f8054c9" alt="Home Screen" width="700"/>
</p>

### Perfil de Invocador
<p align="center">
  <img src="https://github.com/user-attachments/assets/9a6220c3-e4ed-459a-a5f2-414312de0f7a" alt="Summoner Profile" width="700"/>
</p>

### Dashboard de Usuario
<p align="center">
  <img src="https://github.com/user-attachments/assets/d63561f9-b167-4059-8c2e-c1dca6cbe1fe" alt="User Dashboard" width="700"/>
</p>

### Panel de Administración
<p align="center">
  <img src="https://github.com/user-attachments/assets/162964b0-f4f9-4521-837b-4e7b101fedd7" alt="Admin Panel" width="700"/>
</p>

### Login
<p align="center">
  <img src="https://github.com/user-attachments/assets/381dfdd6-e915-4c34-ba98-b3cf9985855d" alt="Login Screen" width="700"/>
</p>

---

## 🚧 Desarrollo Continuo

**SPIRITBLADE está en desarrollo activo.** La versión 0.1 representa el primer hito funcional del proyecto, implementando las características básicas de búsqueda y visualización. El desarrollo continúa siguiendo un proceso iterativo e incremental para añadir nuevas funcionalidades en versiones posteriores.

---

## 🎥 Video Demostración (v0.1)

> **[🎬 Ver video demostrativo - 1 minuto](link-pendiente)**

### Contenido del video:
- **Usuario anónimo** (00:00-00:25): Búsqueda de invocadores, visualización de perfil, historial de partidas
- **Usuario registrado** (00:25-00:45): Login, dashboard personal, favoritos
- **Administrador** (00:45-01:00): Panel de administración, gestión de usuarios

---

## 🔮 Próximas Versiones

Las siguientes versiones incluirán funcionalidades avanzadas para enriquecer la experiencia de usuario:

### Versión 0.2 (Funcionalidades Intermedias)
- 📊 **Análisis avanzado de rendimiento** con gráficos de tendencias
- 📝 **Sistema de notas** en partidas
- ⭐ **Gestión de favoritos** mejorada
- 🔔 **Notificaciones** de actividad de invocadores

### Versión 1.0 (Funcionalidades Avanzadas)
- 📈 **Estadísticas globales** de la comunidad
- 🤖 **Recomendaciones inteligentes** basadas en estilo de juego
- 📧 **Reportes por email** periódicos (tentativo)
- 🏆 **Rankings personalizados**
- 📊 **Análisis predictivo** de rendimiento

---

## 📚 Índice de Documentación

### Documentación Principal
- **[Funcionalidades](docs/Funcionalidades.md)** - Capturas de pantalla y descripción de cada funcionalidad
- **[Funcionalidades Detalladas](docs/Funcionalidades-Detalladas.md)** - Lista completa de funcionalidades implementadas y pendientes
- **[Ejecución](docs/Ejecucion.md)** - Instrucciones para ejecutar la aplicación con Docker
- **[Guía de Desarrollo](docs/Guia-Desarrollo.md)** - Guía técnica completa para desarrolladores

### Documentación de Gestión
- **[Seguimiento](docs/Seguimiento.md)** - Control de calidad, proceso de desarrollo y métricas
- **[Inicio del Proyecto](docs/Inicio-Proyecto.md)** - Objetivos, metodología y análisis inicial
- **[Autores](docs/Autores.md)** - Información sobre el equipo y el proyecto

### Documentación Técnica Adicional
- **[API REST](docs/API.md)** - Documentación de endpoints
- **[Despliegue Docker](docker/README.md)** - Instrucciones de despliegue
- **[Proceso de Release](docs/RELEASE-PROCESS.md)** - Guía de versionado y publicación

---

## 🚀 Inicio Rápido

### 🔐 Credenciales de Desarrollo

**⚠️ Para demos y desarrollo local:**

- **Admin:** `admin` / `admin`
- **Usuario:** `user` / `pass`

📖 **[Ver guía completa de credenciales](CREDENTIALS.md)** - Incluye gestión de roles, configuración de producción y seguridad.

---

### Ejecutar con Docker (Recomendado)

```bash
# Descargar docker-compose.yml
curl -O https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml

# Configurar variables de entorno
echo "DOCKER_USERNAME=yourusername" > .env
echo "RIOT_API_KEY=your-riot-api-key" >> .env
echo "MYSQL_PASSWORD=your-password" >> .env

# Iniciar aplicación
docker-compose up -d
```

Acceder a: **https://localhost:443**

Ver **[Guía de Ejecución completa](docs/Ejecucion.md)** para más detalles.

---

## 🛠️ Tecnologías

| Componente | Tecnología |
|------------|------------|
| **Frontend** | Angular 17, TypeScript, SCSS |
| **Backend** | Spring Boot 3.4.3, Java 21 |
| **Base de Datos** | MySQL 8.0 |
| **Seguridad** | Spring Security, JWT |
| **Despliegue** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |
| **Control de Calidad** | JUnit 5, Jasmine/Karma, SonarCloud |

---

## 📊 Estado del Proyecto

[![Build Status](https://github.com/codeurjc-students/2025-SPIRITBLADE/workflows/CI%2FCD%20-%20Quality%20Control%20%26%20Testing/badge.svg)](https://github.com/codeurjc-students/2025-SPIRITBLADE/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=codeurjc-students_2025-SPIRITBLADE&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE)

**Versión actual**: 0.1.0  
**Última actualización**: Diciembre 2024  
**Estado**: ✅ Funcional - Desarrollo Activo

---

## 🔗 Enlaces

- **Repositorio**: [GitHub - SPIRITBLADE](https://github.com/codeurjc-students/2025-SPIRITBLADE)
- **Docker Hub**: [spiritblade:latest](https://hub.docker.com/r/yourusername/spiritblade)
- **Blog del proyecto**: [Medium - SPIRITBLADE](https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3)
- **Issues y Tareas**: [GitHub Projects](https://github.com/codeurjc-students/2025-SPIRITBLADE/projects)

---

## 👨‍💻 Autoría

**SPIRITBLADE** es un Trabajo de Fin de Grado (TFG) desarrollado para el doble grado en Ingeniería Informática e Ingeniería del Software en la ETSII (Universidad Rey Juan Carlos).

- **Estudiante**: Jorge Andrés Echevarría
- **Tutor**: Iván Chicano Capelo
- **Universidad**: URJC - ETSII
- **Curso**: 2024-2025

---

## 📄 Licencia

Este proyecto está bajo licencia MIT. Ver archivo [LICENSE](LICENSE) para más detalles.

---

<p align="center">
  <sub>⚔️ SPIRITBLADE - Domina el campo de batalla con información</sub>
</p>
