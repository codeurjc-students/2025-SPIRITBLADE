# Reajuste de Funcionalidades — SPIRITBLADE

**Fecha**: Octubre 2025  
**Versión del Proyecto**: 0.1.0  
**Estado**: Documentación actualizada

---

## 📋 Resumen Ejecutivo

Este documento describe el reajuste de funcionalidades realizado para el proyecto SPIRITBLADE en octubre de 2025, explicando los cambios en las definiciones de funcionalidades y las razones detrás de ellos.

### Motivación

El reajuste responde a:
1. Optimización del tiempo de desarrollo disponible para el TFG
2. Priorización de funcionalidades core que aportan mayor valor a los usuarios
3. Enfoque en la calidad por encima de la cantidad de features
4. Alineación realista con recursos académicos y plazos

---

## Cambios realizados

### Usuario anónimo

#### Funcionalidades core (v0.1)

Antes (definición original):
- Búsqueda libre de invocador por Riot ID
- Páginas de perfil con rango y estadísticas
- Acceso público al historial de partidas
- Estadísticas básicas de campeones (KDA, ratio de victorias, maestría)

Ahora (definición actualizada):
- Búsqueda de invocador y vista de perfil
- Historial público de partidas con cacheo
- Estadísticas básicas de campeones incluyendo maestrías, campeones más jugados y rendimiento general

Cambios clave:
- ✅ Énfasis explícito en el sistema de cacheo como diferencial
- ✅ Estadísticas de campeones más detalladas (maestría, más jugados, rendimiento)
- ✅ Consolidación de la búsqueda y la vista de perfil en una única funcionalidad integrada

---

#### Funcionalidades intermedias (v0.2)

Antes (definición original):
- Ver estadísticas agregadas (genérico)

Ahora (definición actualizada):
- Estadísticas agregadas por invocador, usando datos detallados de partidas almacenados en cache

Cambios clave:
- ✅ Aclaración de que las estadísticas agregadas se construyen a partir de datos del invocador
- ✅ Énfasis en el uso del sistema de cacheo para optimizar rendimiento
- ✅ Aclaración de que se utilizarán datos detallados de partidas ya almacenados en cache

---

#### Funcionalidades avanzadas (v1.0)

Antes (definición original):
- Estadísticas globales de la comunidad (feature específico)

Ahora (definición actualizada):
- Sistema de cacheo inteligente que minimiza tiempos de carga garantizando datos frescos
- Estrategia híbrida de acceso a datos que equilibra rendimiento y frescura

Cambios clave:
- ✅ Cambio de features específicos hacia infraestructura técnica avanzada
- ✅ Prioridad en rendimiento y optimización como diferenciadores principales
- ✅ Tratar el sistema de cacheo como una característica estratégica (no solo un detalle de implementación)
- ✅ Balance explícito entre rendimiento y frescura de datos

---

### Usuarios registrados

#### Funcionalidades core (v0.1)

Antes (definición original):
- Vincular cuenta de League of Legends al perfil
- Guardar invocadores favoritos
- Dashboard personalizado con estadísticas personales
- Análisis de rendimiento avanzado con gráficos
- Sistema de notas para partidas

Ahora (definición actualizada):
- Acceso a un panel de control personalizable
- Datos detallados de partidas recientes enriquecidos desde la API de Riot
- Visualización de campeones con mayor maestría y rendimiento personal

Cambios clave:
- ✅ Simplificación y priorización de funcionalidades core
- ✅ Funciones secundarias (favoritos, notas) movidas a versiones posteriores
- ✅ Énfasis en consultas detalladas y datos enriquecidos desde la API de Riot
- ✅ Foco en vistas de maestría y rendimiento como features de alto valor
- ⚠️ “Dashboard personalizado” → “panel de control personalizable” (más flexible)

---

#### Funcionalidades intermedias (v0.2)

Antes (definición original):
- Análisis avanzado de rendimiento con Chart.js
- Añadir notas a partidas
- Recibir notificaciones

Ahora (definición actualizada):
- Acceso a datos personales detallados de rendimiento por campeones favoritos
- Historial de partidas enriquecido con información contextual

Cambios clave:
- ✅ Consolidación de múltiples pequeñas funcionalidades en capacidades integradas
- ✅ Enfoque en profundizar los datos existentes (rendimiento personal, contexto de partidas)
- ✅ Funciones de menor prioridad (notas, notificaciones) aplazadas a v1.0
- ✅ Mayor énfasis en análisis por campeones favoritos (valioso para usuarios competitivos)

---

#### Funcionalidades avanzadas (v1.0)

Antes (definición original):
- Envío de reportes por email (tentativo)
- Generación de builds recomendadas
- Clasificaciones personalizadas
- Análisis predictivo (tentativo)

Ahora (definición actualizada):
- Panel personalizado con KPIs calculados a partir del historial de partidas
- Sistema de cacheo inteligente que prioriza la BD antes de llamadas externas costosas
- Validación automática de frescura con impacto mínimo en tiempos de carga

Cambios clave:
- ✅ Cambio de features específicos a capacidades arquitectónicas avanzadas
- ✅ Prioridad en KPIs y análisis de rendimiento sobre features experimentales (email, ML)
- ✅ Cacheo inteligente como diferenciador técnico clave
- ✅ Énfasis en optimización de costes (menos llamadas a APIs externas)
- ✅ Validación automática de frescura de datos para garantizar calidad sin comprometer rendimiento
- ⚠️ Features como builds, rankings y análisis predictivo siguen siendo extensiones potenciales

---

## Comparación de enfoques

### Enfoque original
- Amplitud de features: catálogo amplio de features específicos
- Estrategia: cubrir muchos casos de uso con features dedicadas
- Complejidad: elevado número de implementaciones independientes

### Enfoque actualizado
- Calidad de implementación: features core implementadas con excelencia técnica
- Estrategia: cacheo robusto y acceso a datos como diferenciador de producto
- Complejidad: arquitectura técnica sólida que facilita la escalabilidad futura

---

## Razonamiento

### Razones técnicas

1. Optimización de recursos
   - El sistema de cacheo inteligente reduce drásticamente llamadas a la API de Riot
   - Menor coste operativo y mejor rendimiento general
   - Infraestructura más sostenible a largo plazo

2. Escalabilidad
   - Una arquitectura de datos sólida facilita añadir nuevas funcionalidades
   - El sistema de cacheo es reutilizable entre features
   - Base técnica fuerte para desarrollo futuro

3. Experiencia de usuario
   - Menores tiempos de carga (cache inteligente)
   - Datos frescos mediante validación automatizada
   - Balance óptimo entre rendimiento y frescura de datos

### Razones de proyecto

1. Alineación con el calendario del TFG
   - Enfocar en demostrar excelencia técnica en áreas core
   - Evitar dispersar el esfuerzo en muchas features de bajo impacto
   - Permitir más tiempo para calidad y pruebas

2. Priorización de valor
   - Las funcionalidades actualizadas cubren ~90% de los casos de uso primarios
   - El cacheo es un diferenciador técnico más valioso que muchas features pequeñas
   - Mejor equilibrio entre complejidad técnica y utilidad práctica

3. Enfoque académico
   - Mayor énfasis en arquitectura de software y patrones de diseño
   - Demostración de habilidades en optimización y rendimiento
   - Trabajo mejor alineado con los resultados de aprendizaje académicos

---

## Impacto en el proyecto

### Documentación actualizada

Los siguientes documentos han sido actualizados para reflejar el nuevo enfoque:

- ✅ `Funcionalidades.md` — Descripciones de features con capturas
- ✅ `Funcionalidades-Detalladas.md` — Tablas de features por versión
- ✅ `Inicio-Proyecto.md` — Objetivos del proyecto
- 📋 Otros docs técnicos según sea necesario

---

### Implementación

Estado actual (v0.1):
- ✅ Cache básico implementado (MySQL)
- ✅ Features core para usuarios anónimos implementadas
- ✅ Features core para usuarios registrados implementadas
- ✅ Panel de control personalizable en desarrollo

Siguientes pasos (v0.2):
- 📋 Mejorar cache (Redis + estrategia multinivel)
- 📋 Estadísticas agregadas por invocador
- 📋 Rendimiento personal detallado para campeones favoritos
- 📋 Historial de partidas enriquecido con contexto

Futuro (v1.0):
- 📋 Cacheo inteligente con validación automática
- 📋 Panel de KPIs
- 📋 Estrategia híbrida de acceso a datos

---

## Referencias

- Documento de features: [Funcionalidades.md](Funcionalidades.md)
- Features detalladas: [Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)
- Inicio del proyecto: [Inicio-Proyecto.md](Inicio-Proyecto.md)
- Estado del proyecto: [Project-Status.md](Project-Status.md)

---

## Registro de cambios

| Fecha | Versión | Cambios |
|------|---------|---------|
| Oct 2025 | 1.0 | Documento creado. Reajuste completo de funcionalidades core, intermedias y avanzadas. |

---

[← Volver al README principal](../README.md) | [Ver funcionalidades actualizadas →](Funcionalidades.md)
