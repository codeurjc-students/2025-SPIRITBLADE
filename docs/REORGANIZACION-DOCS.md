# Reorganización de Documentación - SPIRITBLADE

**Fecha**: Octubre 2025  
**Objetivo**: Reestructurar la documentación según los requisitos de la Fase 3 (v0.1)

---

## 📋 Estructura Objetivo

Según los requisitos, la documentación debe quedar así:

### 1. README.md (Página Principal) ✅
**Ubicación**: Raíz del proyecto  
**Contenido**:
- Título y descripción de la aplicación
- Párrafo resumen funcionalidad v0.1
- Capturas de pantalla v0.1
- Indicación de desarrollo continuo
- Vídeo demostrativo 1 minuto (separado por tipos de usuario)
- Párrafo funcionalidades futuras (v0.2, v1.0)
- Índice con enlaces al resto de documentación

### 2. Funcionalidades.md ✅
**Ubicación**: `docs/Funcionalidades.md`  
**Contenido**:
- Descripción de funcionalidades v0.1
- Capturas de pantalla ilustrativas
- Breve descripción de cada funcionalidad
- Organizado por tipo de usuario (Anónimo, Registrado, Admin)

### 3. Funcionalidades-Detalladas.md ✅
**Ubicación**: `docs/Funcionalidades-Detalladas.md`  
**Contenido**:
- Lista completa de funcionalidades
- Estado de implementación (✅ ✚ 📋)
- Descripción del comportamiento implementado
- Funcionalidades pendientes para v0.2 y v1.0

### 4. Ejecucion.md ✅
**Ubicación**: `docs/Ejecucion.md`  
**Contenido**:
- Instrucciones partiendo del docker-compose en DockerHub
- Requisitos: Docker Desktop (Win/Mac), Docker + Compose (Linux)
- Enlaces a instalación de Docker
- Credenciales de acceso (datos de ejemplo)
- Descripción de datos de ejemplo

### 5. Guia-Desarrollo.md 🔄
**Ubicación**: `docs/Guia-Desarrollo.md`  
**Contenido ACTUALIZADO**:
- Introducción [actualizado]
- Tecnologías [actualizado]
- Herramientas [actualizado]
- **Arquitectura** [NUEVO]
  - Modelo del dominio (diagrama entidades + relaciones)
  - API REST [actualizada]
  - Arquitectura del servidor (diagrama capas)
  - Arquitectura del cliente (diagrama componentes Angular)
- Control de Calidad [actualizado]
- **Despliegue** [NUEVO]
  - Empaquetado y distribución (Docker, compose)
  - URL artefactos (DockerHub)
- Proceso de desarrollo [actualizado]
  - Gestión de tareas [actualizado]
  - Git [actualizado]
  - Integración y entrega continua [actualizado con CD]
  - **Versionado** [NUEVO]: releases, fechas, funcionalidades
- Ejecución y edición de código [actualizado]

### 6. Seguimiento.md ✅
**Ubicación**: `docs/Seguimiento.md`  
**Contenido**: NO CAMBIA
- Control de calidad
- Métricas de desarrollo
- CI/CD workflows

### 7. Inicio-Proyecto.md 🔄
**Ubicación**: `docs/Inicio-Proyecto.md`  
**Contenido**:
- Objetivos [no cambia]
- Metodología [no cambia]
- **Funcionalidades iniciales** [AÑADIR]: Sección Funcionalidades-Detalladas de Fase 1
- Análisis [no cambia]

### 8. Autores.md ✅
**Ubicación**: `docs/Autores.md`  
**Contenido**: NO CAMBIA
- Información del equipo

---

## 🗑️ Documentos a ELIMINAR/CONSOLIDAR

Estos documentos contienen información redundante o desactualizada:

### Eliminar Completamente
- ❌ **STORAGE_IMPLEMENTATION_SUMMARY.md.bak** - Backup obsoleto
- ❌ **EXECUTIVE-SUMMARY.md** - Resumen ejecutivo redundante con README
- ❌ **Project-Status.md** - Estado del proyecto redundante con Seguimiento.md
- ❌ **SETUP-CHECKLIST.md** - Checklist ya superado (Fase 2)
- ❌ **FINAL-VERIFICATION.md** - Verificación obsoleta
- ❌ **README.md** (dentro de docs/) - Redundante con README principal

### Consolidar en Guia-Desarrollo.md
- 📦 **CI-CD-IMPLEMENTATION.md** → Mover a sección "Integración y entrega continua"
- 📦 **QUICK-START-CICD.md** → Mover a sección "Proceso de desarrollo"
- 📦 **WORKFLOWS-VERIFICATION.md** → Mover a sección "CI/CD workflows"
- 📦 **RELEASE-PROCESS.md** → Mover a sección "Versionado"

### Mantener pero Actualizar
- ✅ **API.md** - Documentación API (actualizar con OpenAPI)
- ✅ **REAJUSTE-FUNCIONALIDADES.md** - Documento de reajuste (mantener como histórico)

---

## 📊 Plan de Acción

### Fase 1: Limpieza ✅
1. Eliminar archivos obsoletos
2. Crear backup de archivos a consolidar

### Fase 2: Consolidación 🔄
1. Extraer contenido relevante de archivos a eliminar
2. Integrar en Guia-Desarrollo.md y Seguimiento.md
3. Eliminar archivos consolidados

### Fase 3: Actualización 🔄
1. README.md - Añadir video, mejorar índice
2. Guia-Desarrollo.md - Añadir arquitectura, despliegue, versionado
3. Inicio-Proyecto.md - Añadir "Funcionalidades iniciales"

### Fase 4: Verificación ✅
1. Verificar todos los enlaces internos
2. Asegurar coherencia entre documentos
3. Verificar que no hay información duplicada

---

## 📝 Notas de Implementación

### Contenido a Extraer de Archivos a Consolidar

**De CI-CD-IMPLEMENTATION.md**:
- Descripción workflows GitHub Actions
- Configuración SonarCloud
- Secretos y variables de entorno

**De RELEASE-PROCESS.md**:
- Proceso de versionado semántico
- Pasos para crear release
- Tags y publicación DockerHub

**De WORKFLOWS-VERIFICATION.md**:
- Verificación de workflows
- Comandos útiles
- Troubleshooting CI/CD

---

## ✅ Checklist Final

Estructura final de `docs/`:
```
docs/
├── API.md                          ✅ Mantener actualizado
├── Autores.md                      ✅ No cambia
├── Ejecucion.md                    ✅ Actualizado
├── Funcionalidades.md              ✅ Actualizado
├── Funcionalidades-Detalladas.md   ✅ Actualizado
├── Guia-Desarrollo.md              🔄 Actualizar (añadir secciones)
├── Inicio-Proyecto.md              🔄 Actualizar (añadir funcionalidades iniciales)
├── Seguimiento.md                  ✅ No cambia
├── REAJUSTE-FUNCIONALIDADES.md     ✅ Mantener (histórico)
└── REORGANIZACION-DOCS.md          📝 Este documento
```

**Archivos eliminados**:
- CI-CD-IMPLEMENTATION.md (consolidado)
- EXECUTIVE-SUMMARY.md (eliminado)
- FINAL-VERIFICATION.md (eliminado)
- Project-Status.md (consolidado en Seguimiento.md)
- QUICK-START-CICD.md (consolidado)
- README.md (docs/) (eliminado)
- RELEASE-PROCESS.md (consolidado)
- SETUP-CHECKLIST.md (eliminado)
- STORAGE_IMPLEMENTATION_SUMMARY.md.bak (eliminado)
- WORKFLOWS-VERIFICATION.md (consolidado)

---

## 🔗 Enlaces Actualizados

Verificar que todos los enlaces internos apunten correctamente:
- README.md → docs/
- Guia-Desarrollo.md ↔ otros docs
- Funcionalidades.md ↔ Funcionalidades-Detalladas.md
- Etc.

---

**Fecha de implementación**: Octubre 2025  
**Responsable**: Jorge Andrés Echevarría

