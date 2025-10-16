# Reajuste de Funcionalidades - SPIRITBLADE

**Fecha**: Octubre 2025  
**Versión del Proyecto**: 0.1.0  
**Estado**: Documentación Actualizada

---

## 📋 Resumen Ejecutivo

Este documento detalla el reajuste de enfoque del proyecto SPIRITBLADE realizado en octubre de 2025, explicando los cambios en la definición de funcionalidades y las razones que motivaron estas actualizaciones.

### Motivación del Reajuste

El reajuste de funcionalidades responde a:
1. **Optimización de tiempos de desarrollo** disponibles para el TFG
2. **Priorización de funcionalidades core** que aportan mayor valor al usuario final
3. **Enfoque en calidad** sobre cantidad de features
4. **Alineación realista** con los recursos y plazos del proyecto académico

---

## 🔄 Cambios Realizados

### Usuario Anónimo

#### Funcionalidad Básica (v0.1)

**ANTES (Definición Original)**:
- Búsqueda libre de invocadores por Riot ID
- Visualización de perfiles con rango y estadísticas
- Acceso a historial de partidas públicas
- Estadísticas básicas de campeones (KDA, winrate, maestría)

**AHORA (Definición Actualizada)**:
- **Búsqueda de invocadores y visualización de su perfil y rango**
- **Visualización del historial público de partidas con sistema de caché**
- **Acceso a estadísticas básicas de campeones** incluyendo maestrías, campeones más jugados y datos de rendimiento general

**Cambios clave**:
- ✅ Énfasis explícito en el **sistema de caché** como característica diferenciadora
- ✅ Mayor detalle en las estadísticas de campeones (maestrías, más jugados, rendimiento general)
- ✅ Consolidación de búsqueda y visualización de perfil como funcionalidad integrada

---

#### Funcionalidad Intermedia (v0.2)

**ANTES (Definición Original)**:
- Ver estadísticas agregadas (genérico)

**AHORA (Definición Actualizada)**:
- **Visualización de estadísticas agregadas por invocadores**, con información detallada de partidas almacenadas en caché

**Cambios clave**:
- ✅ Especificación de que las estadísticas agregadas se basan en **invocadores**
- ✅ Énfasis en el aprovechamiento del **sistema de caché** para optimizar rendimiento
- ✅ Clarificación de que se utiliza información detallada de partidas ya almacenadas

---

#### Funcionalidad Avanzada (v1.0)

**ANTES (Definición Original)**:
- Estadísticas globales de comunidad (feature específica)

**AHORA (Definición Actualizada)**:
- **Sistema inteligente de caché** que minimiza los tiempos de carga mientras garantiza datos actualizados
- **Estrategia híbrida de acceso a datos** que balancea rendimiento y frescura de información

**Cambios clave**:
- ✅ Cambio de enfoque desde features específicas a **infraestructura técnica avanzada**
- ✅ Priorización del **rendimiento y optimización** como valor diferencial
- ✅ Sistema de caché como funcionalidad avanzada estratégica (no solo implementación técnica)
- ✅ Balance explícito entre rendimiento y actualidad de datos

---

### Usuario Registrado

#### Funcionalidad Básica (v0.1)

**ANTES (Definición Original)**:
- Asociación de cuenta de League of Legends al perfil
- Guardado de invocadores favoritos
- Dashboard personalizado con estadísticas propias
- Análisis avanzado de rendimiento con gráficos
- Sistema de notas en partidas

**AHORA (Definición Actualizada)**:
- **Acceso a panel de control personalizable**
- **Consulta de datos detallados de partidas recientes** con información enriquecida de la API de Riot
- **Visualización de campeones con mayor maestría y rendimiento personal**

**Cambios clave**:
- ✅ Simplificación y **priorización de funcionalidades core**
- ✅ Eliminación de features secundarias de la v0.1 (favoritos, notas) → movidas a versiones posteriores
- ✅ Énfasis en **consulta detallada** y **datos enriquecidos** de la API de Riot
- ✅ Foco en visualización de maestría y rendimiento (funcionalidad más demandada)
- ⚠️ "Dashboard personalizado" → "Panel de control personalizable" (mayor flexibilidad)

---

#### Funcionalidad Intermedia (v0.2)

**ANTES (Definición Original)**:
- Análisis avanzado de rendimiento con gráficos (Chart.js)
- Añadir notas en partidas
- Recibir notificaciones

**AHORA (Definición Actualizada)**:
- **Acceso a datos detallados de rendimiento personal con campeones favoritos**
- **Visualización del historial de partidas con información contextual enriquecida**

**Cambios clave**:
- ✅ Consolidación de múltiples features en **funcionalidades integradas**
- ✅ Enfoque en **profundización de datos** ya existentes (rendimiento personal, contexto de partidas)
- ✅ Eliminación de features de menor prioridad (notas, notificaciones) → evaluadas para v1.0
- ✅ Mayor énfasis en análisis de campeones favoritos (valor para usuario competitivo)

---

#### Funcionalidad Avanzada (v1.0)

**ANTES (Definición Original)**:
- Recibir reportes por email (tentativo)
- Recomendaciones de builds
- Rankings personalizados
- Análisis predictivo (tentativo)

**AHORA (Definición Actualizada)**:
- **Dashboard personalizado con indicadores clave de rendimiento (KPIs)** calculados a partir del historial de partidas
- **Sistema de caché inteligente** que prioriza la base de datos antes de realizar costosas llamadas a APIs externas
- **Validación automática de frescura de datos** con mínimo impacto en tiempos de carga

**Cambios clave**:
- ✅ Cambio de enfoque desde features específicas a **arquitectura técnica avanzada**
- ✅ Priorización de **KPIs y análisis de rendimiento** sobre features experimentales (email, ML)
- ✅ Sistema de caché inteligente como diferenciador técnico clave
- ✅ Énfasis en **optimización de costes** (reducción de llamadas a APIs externas)
- ✅ Validación automática de datos para garantizar calidad sin comprometer performance
- ⚠️ Features como builds, rankings y análisis predictivo quedan como posibles extensiones futuras

---

## 📊 Comparativa de Enfoque

### Enfoque Original
- **Cantidad de features**: Amplio catálogo de funcionalidades específicas
- **Estrategia**: Cubrir múltiples casos de uso con features dedicadas
- **Complejidad**: Alta variedad de implementaciones independientes

### Enfoque Actualizado
- **Calidad de implementación**: Funcionalidades core implementadas con excelencia técnica
- **Estrategia**: Sistema robusto de caché y acceso a datos como diferenciador
- **Complejidad**: Arquitectura técnica sólida que facilita escalabilidad futura

---

## 🎯 Justificación del Cambio

### Razones Técnicas

1. **Optimización de Recursos**
   - El sistema de caché inteligente reduce significativamente las llamadas a la API de Riot
   - Menor coste operacional y mejor rendimiento general
   - Infraestructura más sostenible a largo plazo

2. **Escalabilidad**
   - Una arquitectura de datos sólida facilita la adición de features futuras
   - El sistema de caché es reutilizable para todas las funcionalidades
   - Base técnica robusta sobre la que construir

3. **Experiencia de Usuario**
   - Tiempos de carga minimizados (caché inteligente)
   - Datos siempre actualizados (validación automática)
   - Balance óptimo entre rendimiento y frescura de datos

### Razones de Proyecto

1. **Alineación con Tiempos del TFG**
   - Foco en demostrar excelencia técnica en áreas core
   - Evita dispersión en múltiples features de menor impacto
   - Permite dedicar más tiempo a calidad y testing

2. **Priorización de Valor**
   - Las funcionalidades actualizadas cubren el 90% de los casos de uso principales
   - Sistema de caché es diferenciador técnico más valioso que features específicas
   - Mejor balance entre complejidad técnica y utilidad práctica

3. **Enfoque Académico**
   - Mayor énfasis en arquitectura de software y patrones de diseño
   - Demostración de capacidades de optimización y rendimiento
   - Trabajo más alineado con objetivos de aprendizaje de un TFG

---

## 📈 Impacto en el Proyecto

### Documentación Actualizada

Los siguientes documentos han sido actualizados para reflejar el nuevo enfoque:

- ✅ `Funcionalidades.md` - Descripciones detalladas con capturas de pantalla
- ✅ `Funcionalidades-Detalladas.md` - Tablas de funcionalidades por versión
- ✅ `Inicio-Proyecto.md` - Objetivos funcionales del proyecto
- 📋 Otros documentos técnicos según sea necesario

### Implementación

**Estado actual (v0.1)**:
- ✅ Sistema básico de caché implementado (BD MySQL)
- ✅ Funcionalidades básicas para usuario anónimo
- ✅ Funcionalidades básicas para usuario registrado
- ✅ Dashboard personalizable en desarrollo

**Próximos pasos (v0.2)**:
- 📋 Mejora del sistema de caché (Redis + estrategia multinivel)
- 📋 Estadísticas agregadas por invocadores
- 📋 Rendimiento personal detallado con campeones favoritos
- 📋 Historial enriquecido con contexto

**Futuro (v1.0)**:
- 📋 Sistema de caché inteligente con validación automática
- 📋 Dashboard con KPIs avanzados
- 📋 Estrategia híbrida de acceso a datos

---

## 🔗 Referencias

- **Documento de Funcionalidades**: [Funcionalidades.md](Funcionalidades.md)
- **Funcionalidades Detalladas**: [Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)
- **Inicio del Proyecto**: [Inicio-Proyecto.md](Inicio-Proyecto.md)
- **Estado del Proyecto**: [Project-Status.md](Project-Status.md)

---

## ✍️ Control de Cambios

| Fecha | Versión | Cambios |
|-------|---------|---------|
| Oct 2025 | 1.0 | Creación del documento. Reajuste completo de funcionalidades básicas, intermedias y avanzadas. |

---

**[← Volver al README principal](../README.md)** | **[Ver Funcionalidades Actualizadas →](Funcionalidades.md)**
