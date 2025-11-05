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
# Feature Re-adjustment — SPIRITBLADE

**Date**: October 2025  
**Project version**: 0.1.0  
**Status**: Documentation updated

---

## Executive summary

This document describes the feature re-adjustment performed for the SPIRITBLADE project in October 2025, explaining the changes to feature definitions and the reasons behind them.

### Motivation

The re-adjustment responds to:
1. Optimization of available development time for the thesis
2. Prioritization of core features that deliver the most value to users
3. Focus on quality over the number of features
4. Realistic alignment with academic resources and deadlines

---

## Changes made

### Anonymous users

#### Core features (v0.1)

Before (original definition):
- Free-form summoner search by Riot ID
- Profile pages with rank and statistics
- Public match history access
- Basic champion stats (KDA, win rate, mastery)

Now (updated definition):
- Summoner search and profile view
- Public match history with caching
- Basic champion stats including masteries, most-played champions and general performance

Key changes:
- ✅ Explicit emphasis on the caching system as a differentiator
- ✅ More detailed champion statistics (mastery, most-played, general performance)
- ✅ Consolidation of search and profile viewing into a single integrated feature

---

#### Intermediate features (v0.2)

Before (original definition):
- View aggregated statistics (generic)

Now (updated definition):
- Aggregated statistics per summoner, using cached detailed match data

Key changes:
- ✅ Clarified that aggregated statistics are built from summoner data
- ✅ Emphasis on using the caching system to optimize performance
- ✅ Clarified that detailed match data already stored in cache will be used

---

#### Advanced features (v1.0)

Before (original definition):
- Global community statistics (specific feature)

Now (updated definition):
- Intelligent caching system that minimizes load times while ensuring fresh data
- Hybrid data access strategy that balances performance and data freshness

Key changes:
- ✅ Shift from specific features to advanced technical infrastructure
- ✅ Prioritization of performance and optimization as a core differentiator
- ✅ Treat the caching system as a strategic advanced feature (not merely an implementation detail)
- ✅ Explicit balance between performance and data freshness

---

### Registered users

#### Core features (v0.1)

Before (original definition):
- Link League of Legends account to profile
- Save favorite summoners
- Personalized dashboard with personal stats
- Advanced performance analysis with charts
- Match notes system

Now (updated definition):
- Access to a customizable control panel
- Detailed recent-match data enriched from the Riot API
- View champions with highest mastery and personal performance

Key changes:
- ✅ Simplification and prioritization of core features
- ✅ Secondary features (favorites, notes) moved to later versions
- ✅ Emphasis on detailed querying and enriched Riot API data
- ✅ Focus on mastery and performance views as high-value features
- ⚠️ “Personalized dashboard” → “customizable control panel” (more flexible)

---

#### Intermediate features (v0.2)

Before (original definition):
- Advanced performance analysis with Chart.js
- Add notes to matches
- Receive notifications

Now (updated definition):
- Access to detailed personal performance data for favorite champions
- Enriched match history with contextual information

Key changes:
- ✅ Consolidation of multiple small features into integrated capabilities
- ✅ Focus on deepening existing data (personal performance, match context)
- ✅ Lower-priority features (notes, notifications) deferred to v1.0
- ✅ Increased emphasis on favorite-champion analysis (valuable for competitive users)

---

#### Advanced features (v1.0)

Before (original definition):
- Receive email reports (tentative)
- Build recommendations
- Personalized leaderboards
- Predictive analysis (tentative)

Now (updated definition):
- Personalized dashboard with KPIs computed from match history
- Intelligent caching system that prioritizes the DB before expensive external API calls
- Automatic freshness validation with minimal impact on load times

Key changes:
- ✅ Shift from specific features to advanced architectural capabilities
- ✅ Prioritization of KPIs and performance analysis over experimental features (email, ML)
- ✅ Intelligent caching as a key technical differentiator
- ✅ Emphasis on cost optimization (fewer external API calls)
- ✅ Automatic validation of data freshness to ensure quality without compromising performance
- ⚠️ Features like builds, leaderboards and predictive analysis remain potential future extensions

---

## Approach comparison

### Original approach
- Feature breadth: broad catalogue of specific features
- Strategy: cover many use cases with dedicated features
- Complexity: high number of independent implementations

### Updated approach
- Quality of implementation: core features implemented with technical excellence
- Strategy: robust caching and data access as a product differentiator
- Complexity: solid technical architecture that simplifies future scaling

---

## Rationale

### Technical reasons

1. Optimization of resources
   - The intelligent caching system drastically reduces calls to the Riot API
   - Lower operational cost and improved overall performance
   - More sustainable infrastructure in the long term

2. Scalability
   - A solid data architecture makes adding features easier
   - The caching system is reusable across features
   - Strong technical foundation for future development

3. User experience
   - Reduced load times (intelligent cache)
   - Fresh data through automated validation
   - Optimal balance between performance and data freshness

### Project reasons

1. Alignment with thesis timeline
   - Focus on demonstrating technical excellence in core areas
   - Avoid spreading effort across many low-impact features
   - Allow more time for quality and testing

2. Value prioritization
   - Updated features cover 90% of primary use cases
   - Caching is a more valuable technical differentiator than many small features
   - Better balance between technical complexity and practical utility

3. Academic focus
   - Greater emphasis on software architecture and design patterns
   - Demonstration of optimization and performance skills
   - Work better aligned with academic learning outcomes

---

## Project impact

### Updated documentation

The following documents have been updated to reflect the new approach:

- ✅ `Funcionalidades.md` — Feature descriptions with screenshots
- ✅ `Funcionalidades-Detalladas.md` — Feature tables by version
- ✅ `Inicio-Proyecto.md` — Project objectives
- 📋 Other technical docs as required

---

### Implementation

Current state (v0.1):
- ✅ Basic cache implemented (MySQL)
- ✅ Core anonymous features implemented
- ✅ Core registered-user features implemented
- ✅ Customizable dashboard in development

Next steps (v0.2):
- 📋 Improve caching (Redis + multi-level strategy)
- 📋 Aggregated summoner statistics
- 📋 Detailed personal performance for favorite champions
- 📋 Enriched match history with context

Future (v1.0):
- 📋 Intelligent caching with automatic validation
- 📋 KPI dashboard
- 📋 Hybrid data access strategy

---

## References

- Feature doc: [Funcionalidades.md](Funcionalidades.md)
- Detailed features: [Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)
- Project start: [Inicio-Proyecto.md](Inicio-Proyecto.md)
- Project status: [Project-Status.md](Project-Status.md)

---

## Change log

| Date | Version | Changes |
|------|---------|---------|
| Oct 2025 | 1.0 | Document created. Full re-adjustment of core, intermediate and advanced features. |

---

[← Back to main README](../README.md) | [View updated features →](Funcionalidades.md)
