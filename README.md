# SPIRITBLADE ⚔️

<p align="center">
  <img width="300" alt="SPIRITBLADE Logo" src="https://github.com/user-attachments/assets/1f73258c-5c4a-4d87-ade7-3aaa546827b9" />
</p>

<p align="center">
  <strong>Análisis y visualización de estadísticas de League of Legends</strong>
</p>

---

## 📋 Versión 1.0 - Funcionalidades Avanzadas

**SPIRITBLADE v1.0** es una aplicación web que permite a los jugadores de League of Legends buscar y visualizar estadísticas de invocadores en tiempo real. La aplicación se conecta a la API oficial de Riot Games para obtener información actualizada sobre perfil, rango, historial de partidas y estadísticas de campeones.

### Características principales:

✅ **Búsqueda de invocadores** por Riot ID (gameName#tagLine)  
✅ **Visualización de perfil** con nivel, icono y rango actual  
✅ **Historial de partidas** recientes con KDA y resultado  
✅ **Estadísticas de campeones** más jugados con nivel de maestría  
✅ **Sistema de autenticación** con JWT para usuarios registrados  
✅ **Panel de administración** para gestión de usuarios (rol ADMIN)  
✅ **Búsquedas recientes** en página de inicio  
✅ **Analisis con IA** básico de rendimiento en partidas
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

## 📸 Capturas de Pantalla (v1.0)

### Página Principal - Búsqueda v1.0
<p align="center">
  <img src="utils/images/Principal.JPG" alt="Home Screen v1.0" width="700"/>
</p>

### Perfil de Invocador v1.0
<p align="center">
  <img src="utils/images/Summoner.JPG" alt="Summoner Profile v1.0" width="700"/>
</p>

### Dashboard de Usuario v1.0
<p align="center">
  <img src="utils/images/Dashboard.JPG" alt="User Dashboard v1.0" width="700"/>
</p>

### Panel de Administración v1.0
<p align="center">
  <img src="utils/images/Admin.JPG" alt="Admin Panel v1.0" width="700"/>
</p>

---

## 🚧 Desarrollo Continuo

**SPIRITBLADE está en desarrollo activo.** La versión 1.0 representa el hito actual del proyecto, implementando las características completas de búsqueda y visualización. El desarrollo continúa siguiendo un proceso iterativo e incremental para añadir nuevas funcionalidades en versiones posteriores.

---

## 🎥 Video Demostración

> **[🎬 Ver video demostrativo](utils/demo/demo1-0.mp4)**

### Contenido del video:
El video demostrativo muestra las funcionalidades clave de SPIRITBLADE, incluyendo la búsqueda de invocadores, visualización de perfiles, historial de partidas y uso del panel de administración.


---

## 📚 Índice de Documentación

### Documentación Principal
- **[Funcionalidades](docs/Funcionalidades.md)** - Lista completa de funcionalidades implementadas y su descripción
- **[Ejecución](docs/Ejecucion.md)** - Instrucciones para ejecutar la aplicación con Docker
- **[Guía de Desarrollo](docs/Guia-Desarrollo.md)** - Guía técnica completa para desarrolladores

### Documentación de Gestión
- **[Seguimiento](docs/Seguimiento.md)** - Control de calidad, proceso de desarrollo y métricas
- **[Inicio del Proyecto](docs/Inicio-Proyecto.md)** - Objetivos, metodología y análisis inicial
- **[Autores](docs/Autores.md)** - Información sobre el equipo y el proyecto
- **[Reajuste de funcionalidades](docs/Reajuste-Funcionalidades.md)** - Cambios en el alcance y funcionalidades del proyecto

### Documentación Técnica Adicional
- **[API REST](docs/API.md)** - Documentación de endpoints
- **[Credenciales](docs/Credenciales.md)** - Gestión de roles y configuración de producción

---

## 🚀 Inicio Rápido

### 🔐 Configuración de Seguridad

**⚠️ IMPORTANTE**: Este proyecto usa variables de entorno para gestionar credenciales.


### 🔑 Credenciales de Desarrollo

**⚠️ Solo para demos y desarrollo local:**

- **Admin:** `admin` / `admin`
- **Usuario:** `user` / `pass`

**⚠️ NUNCA uses estas credenciales en producción**

---

### Ejecutar con Docker (Recomendado)

```bash
# 1. Clonar repositorio
git clone https://github.com/codeurjc-students/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales (ver SECURITY.md)

# 3. Iniciar aplicación
cd docker
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
| **Caché** | Redis, Spring Cache |
| **Seguridad** | Spring Security, JWT |
| **Despliegue** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |
| **Control de Calidad** | JUnit 5, Jasmine/Karma, SonarCloud |

---

## 📊 Estado del Proyecto

[![Build Status](https://github.com/codeurjc-students/2025-SPIRITBLADE/workflows/CI%2FCD%20-%20Quality%20Control%20%26%20Testing/badge.svg)](https://github.com/codeurjc-students/2025-SPIRITBLADE/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=codeurjc-students_2025-SPIRITBLADE&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=codeurjc-students_2025-SPIRITBLADE)

**Versión actual**: 1.0.0 
**Última actualización**: Noviembre 2025 
**Estado**: ✅ Funcional - Desarrollo Activo

---

## 🔗 Enlaces

- **Repositorio**: [GitHub - SPIRITBLADE](https://github.com/codeurjc-students/2025-SPIRITBLADE)
- **Docker Hub**: [Spiritblade Docker Hub](https://hub.docker.com/repository/docker/jae9104/spiritblade/general)
- **Blog del proyecto**: [Medium - SPIRITBLADE](https://medium.com/@j.andres.2022/fase-1-tfg-5ecf33a800e3)
- **Issues y Tareas**: [GitHub Projects](https://github.com/codeurjc-students/2025-SPIRITBLADE/projects)

---

## 👨‍💻 Autoría

**SPIRITBLADE** es un Trabajo de Fin de Grado (TFG) desarrollado para el doble grado en Ingeniería Informática e Ingeniería del Software en la ETSII (Universidad Rey Juan Carlos).

- **Estudiante**: Jorge Andrés Echevarría
- **Tutor**: Iván Chicano Capelo
- **Universidad**: URJC - ETSII
- **Curso**: 2025-2026

---

## 📄 Licencia

Este proyecto está bajo licencia. Ver archivo [LICENSE](LICENSE) para más detalles.

---

<p align="center">
  <sub>⚔️ SPIRITBLADE - Domina el campo de batalla con información</sub>
</p>
