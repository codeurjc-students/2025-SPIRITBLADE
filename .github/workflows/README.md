# 🚀 GitHub Actions Workflows

Este directorio contiene los workflows de CI/CD para SPIRITBLADE.

## 📁 Workflows

### `build.yml` - Quality Control & Testing
- **Trigger**: Push a cualquier rama, PRs a main
- **Propósito**: Tests y análisis de calidad (SonarCloud)
- **Jobs**:
  - Basic Quality Control (feature branches)
  - Complete Quality Control (PR/main)
  - SonarCloud Analysis

### `build-push.yml` - Reusable Build & Push
- **Tipo**: Workflow reutilizable (workflow_call)
- **Propósito**: Construir y publicar imagen Docker + compose OCI
- **Parámetros**:
  - `image-tag`: Tag de la imagen Docker
  - `compose-tag`: Tag del compose OCI
  - `update-latest`: Si actualizar también el tag `latest`

### `deploy-dev.yml` - Deploy Dev
- **Trigger**: Push a `main`
- **Propósito**: Deploy automático de versión dev
- **Resultado**: `spiritblade:dev` y `spiritblade-compose:dev`

### `deploy-release.yml` - Deploy Release
- **Trigger**: GitHub Release publicado
- **Propósito**: Deploy de release oficial
- **Resultado**: `spiritblade:<version>`, `spiritblade:latest`, compose con tags correspondientes

### `manual-build.yml` - Manual Build
- **Trigger**: Workflow dispatch (manual)
- **Propósito**: Build personalizado desde cualquier rama/commit
- **Tag generado**: `<rama>-<timestamp>-<commit>`

## 🔐 Secrets requeridos

Configura estos secrets en GitHub Settings > Secrets and variables > Actions:

- `DOCKERHUB_USERNAME`: Tu usuario de DockerHub
- `DOCKERHUB_TOKEN`: Token de acceso de DockerHub (crear en DockerHub > Account Settings > Security)

## 🎯 Flujo de trabajo típico

1. **Desarrollo en feature branch** → Tests básicos ejecutados automáticamente
2. **PR a main** → Tests completos + SonarCloud
3. **Merge a main** → Build y push de imagen `dev`
4. **Crear GitHub Release** → Build y push de versión + `latest`

## 📚 Documentación adicional

Ver [RELEASE-PROCESS.md](../docs/RELEASE-PROCESS.md) para instrucciones detalladas del proceso de release.
