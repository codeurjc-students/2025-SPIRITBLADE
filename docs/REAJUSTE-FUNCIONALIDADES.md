# Reajuste de Funcionalidades — SPIRITBLADE

**Fecha**: Octubre 2025  
**Versión del Proyecto**: 0.1.0  
**Estado**: Documentación actualizada

---

## 📋 Resumen Ejecutivo

Este documento describe el reajuste de funcionalidades realizado para el proyecto SPIRITBLADE en octubre de 2025, explicando los cambios en las definiciones de funcionalidades y las razones detrás de ellos.

### Motivación

El reajuste responde a:
1. Optimización del tiempo de desarrollo disponible para los 2 TFG
2. Priorización de funcionalidades core que aportan mayor valor a los usuarios
3. Enfoque en la calidad por encima de la cantidad de features
4. Alineación realista con recursos académicos y plazos

---

## Cambios realizados

### Usuario anónimo

#### Funcionalidades básica (v0.1)

Antes (definición original):
- Buscar invocadores y ver su perfil y clasificación
- Ver historial de partidas público
- Acceder a estadísticas básicas de campeones incluyendo maestrías, campeones más jugados y datos de rendimiento generales

Ahora (definición actualizada):
- Búsqueda de invocador y vista de su perfil (Elo, nivel, icono, Numero de partidas, Clasificatorias ganadas...)
- Estadísticas de campeones (maestrías y campeones más jugados)
- Lista de summoners buscados recientemente

---

#### Funcionalidades intermedias (v1.0)

Antes (definición original):
- Estadísticas agregadas de invocadores usando datos de partidas 

Ahora (definición actualizada):
- Winrate del invocador, usando datos detallados de partidas 
- Detalles completos de cada partida del historial.


---

#### Funcionalidades avanzadas (v1.0)

Antes (definición original):
- Analizar y mostrar estadísticas agregadas de invocadores

Ahora (definición actualizada):
- Historial público de partidas paginable


---

### Usuarios registrados

#### Funcionalidades core (v0.1)

Antes (definición original):
- Acceso a un panel de control personalizable (dashboard)
- Ver datos detallados de partidas recientes enriquecidos desde la API de Riot
- Ver maestría de campeones y rendimiento personal

Ahora (definición actualizada):
- Acceso a un panel de control
- Enlazar tu summoner de lol con tu cuenta de usuario
- Gestion de foto de perfil propia


---

#### Funcionalidades intermedias (v1.0)

Antes (definición original):
- Acceso a datos detallados de rendimiento personal por campeones favoritos
- Historial de partidas enriquecido con información contextual


Ahora (definición actualizada):
- Estadisticas sobre el summoner vinculado (Rango actual, LP ganado, rol principal, campeon favorito)
- Sistema de permite marcar como favorito otros summoner para seguimiento rápido
- Analisis por inteligencia artificial de rendimiento del summoner vinculado


---

#### Funcionalidades avanzadas (v1.0)

Antes (definición original):
- Dashboard personalizado con KPIs calculados a partir del historial de partidas
- Validación automática de frescura con impacto mínimo en tiempos percibidos de carga

Ahora (definición actualizada):
- Grafico de evolución de rango del summoner vinculado por cada cola de clasificatoria

---

### Usuarios administradores

#### Funcionalidad
Antes (definición original):
- Gestión completa de usuarios (habilitar, deshabilitar, eliminar)
- Panel de administración con métricas del sistema
- Moderación de contenido generado por usuarios
- Logs de auditoría

Ahora (definición actualizada):
- Gestión de usuarios (habilitar, deshabilitar, eliminar, editar)
- Filtros de búsqueda avanzada en el panel de administración y paginación
- Creación de usuarios con personalización de rol

---


## Razonamiento

Los cambios realizados han sido motivados por el aprendizaje obtenido durante el desarrollo inicial y la necesidad de ajustar el alcance del proyecto a los recursos y tiempo disponibles.

En primer lugar, la complejidad de la coordinacion entre 3 APIs externas y el sistema de cacheo a nivel sistema (no a nivel usuario) ha requerido un esfuerzo considerable no previsto inicialmente.

Adicionalmente, debido a la falta de un endpoint específico para obtener el LP de cada summoner por partida, se ha tenido que implementar la lógica para obtener esta información de manera indirecta, lo que ha requerido un esfuerzo adicional significativo para poder mostrar el gráfico.

Por otro lado, llegar a los estandares mínimos de cobertura de código con una bateria de test robusta ha requerido un tiempo considerable. 

---

## Impacto en el proyecto

Actualmente, se ha optado por realizar en versión 1.1 un sistema de cacheo adicional a nivel de usuario con Redis y Spring Cache, que permite almacenar datos temporalmente y reducir la carga en las APIs externas, mejorando el rendimiento y la experiencia del usuario.

---

## Referencias

- Documento de features: [Funcionalidades.md](Funcionalidades.md)
- Inicio del proyecto: [Inicio-Proyecto.md](Inicio-Proyecto.md)
