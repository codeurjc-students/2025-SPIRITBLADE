# Project Start — SPIRITBLADE

This document describes the initial objectives, methodology, and analysis of the SPIRITBLADE project as defined in Phase 1.

---

## 🎯 Objectives

### General Objective

Develop a full web application that enables League of Legends players to search, analyze, and visualize summoner and match statistics using data from the Riot Games public API, offering an intuitive platform similar to OP.GG or Porofessor.

### Functional Objectives

> 📝 Updated October 2025: Functional objectives were re-adjusted to align with available development time and to prioritize the system's core features. See [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) for full details.

SPIRITBLADE aims to provide different levels of functionality depending on the user type:

#### Anonymous users

Basic features:
- Search summoners and view their profile and rank
- View public match history with a caching layer
- Access basic champion statistics including masteries, most-played champions and general performance data

Intermediate features:
- Aggregated summoner statistics using cached match data

Advanced features:
- Intelligent caching system that minimizes load times while ensuring fresh data
- Hybrid data access strategy that balances performance and data freshness

#### Registered users

Basic features:
- Access to a customizable control panel (dashboard)
- View detailed recent-match data enriched from the Riot API
- View champion mastery and personal performance

Intermediate features:
- Access to detailed personal performance data for favorite champions
- Enriched match history with contextual information

Advanced features:
- Personalized dashboard with KPIs computed from match history
- Cache-first strategy prioritizing the database before expensive external API calls
- Automatic freshness validation with minimal impact on perceived load times

#### Administrators
- Full user management (enable, disable, delete)
- Admin panel with system metrics
- Moderation of user-generated content
- Audit logs

---

### Technical Objectives

The project focuses on software quality and engineering best practices:

#### Architecture & Technologies
- SPA (Single Page Application): Angular frontend + REST API backend
- Modern stack:
  - Frontend: Angular 17, TypeScript, SCSS
  - Backend: Spring Boot 3.4.3, Java 21
  - Database: MySQL 8.0
- Security: Spring Security + JWT for authentication and authorization
- External API: Integration with Riot Games API

#### Quality & Testing
- Test coverage target: minimum 55% overall
- Multi-level testing:
  - Unit: JUnit 5 + Mockito (backend), Jasmine + Karma (frontend)
  - Integration: Spring Boot Test
  - E2E: Selenium WebDriver
- Static analysis: SonarCloud integrated in CI
- Quality targets: 0 critical bugs, 0 vulnerabilities

#### DevOps & Deployment
- Automated CI/CD: GitHub Actions workflows for:
  - Tests and quality gates on every PR
  - Automatic deploy of `dev` image on merge to main
  - Release deployments with semantic versioning
- Containerization: Docker multi-stage builds
- Orchestration: Docker Compose for app + MySQL
- Publication: DockerHub with versioned images

#### Best Practices
- Version control with Git and branching strategy
- Mandatory code reviews via pull requests
- Conventional Commits for a clean history
- Up-to-date documentation
- Responsive design for desktop and mobile

---

## 📅 Methodology

### Development approach

The project follows an iterative and incremental agile methodology:

- Short iterations: 2–3 week cycles
- Frequent deliveries: deployable version at the end of each phase
- Continuous feedback: regular reviews and adjustments
- Continuous improvement: refactoring and optimization

### Planned phases

#### Phase 1: Feature & screen definition
Duration: until Sept 15  
Status: ✅ Completed

Deliverables:
- ✅ Definition of functional and technical objectives
- ✅ Prioritized feature list by user type
- ✅ Wireframes and mockups for main screens
- ✅ Domain entity analysis
- ✅ Permission and role definitions
- ✅ Preliminary REST API specification

---

#### Phase 2: Repository & CI setup
Duration: until Oct 15  
Status: ✅ Completed

Deliverables:
- ✅ GitHub repository with project structure
- ✅ GitHub Actions CI configuration
- ✅ Basic unit tests (backend and frontend)
- ✅ SonarCloud integration
- ✅ Development guide documentation
- ✅ Branch protection rules on `main`

---

#### Phase 3: Version 0.1 — Core features
Duration: until Dec 15  
Status: ✅ Completed

Deliverables:
- ✅ Backend REST API with:
  - JWT authentication
  - Endpoints for users, summoners, dashboard, admin
  - Riot API integration
  - Integration tests
- ✅ Angular frontend with:
  - Components: Home, Login, Dashboard, Summoner, Admin
  - Services and guards
  - Routing and navigation
  - Unit tests
- ✅ MySQL schema
- ✅ Optimized multi-stage Dockerfile
- ✅ Docker Compose deployment
- ✅ CI/CD workflows:
  - Quality control on PRs
  - Automatic DockerHub publish (dev + releases)
  - Manual build for testing
- ✅ Updated documentation

---

#### Phase 4: Version 0.2 — Intermediate features
Duration: until Mar 1  
Status: 📋 Planned

Goals:
- Advanced performance analysis with charts (Chart.js)
- Match notes system
- Full favorites management with notifications
- Moderation dashboard for admins
- Complete E2E tests with Selenium
- UI/UX improvements based on feedback

---

#### Phase 5: Version 1.0 — Advanced features
Duration: until Apr 15  
Status: 📋 Planned

Goals:
- Global community statistics
- Intelligent recommendations based on ML
- Personalized leaderboards
- Email reporting system (tentative)
- Predictive performance analysis
- Performance and scalability optimization

---

#### Phase 6: Project report (TFG thesis)
Duration: until May 15  
Status: 📋 Planned

Goals:
- Complete project report
- Exhaustive technical documentation
- Results analysis
- Conclusions and future work

---

#### Phase 7: Defense
Duration: until Jun 15  
Status: 📋 Planned

Goals:
- Presentation preparation
- Live demonstration
- Defense before the panel

---

## 📐 Initial analysis

### Initial features

> 📝 Note: This section documents the features as originally defined in Phase 1. For the current implementation status and updated features, see [Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md).

The full planned feature list with status (✅ implemented, 🚧 in progress, 📋 planned) is available in **[Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)**.

#### Feature summary by version

**Version 0.1 — Core features** (✅ Completed):
- Anonymous users: summoner search, profile and rank view, cached match history, basic champion stats
- Registered users: customizable dashboard, detailed match data, champion mastery views
- Admin: admin panel, user management, system metrics

**Version 0.2 — Intermediate features** (📋 Planned):
- Anonymous users: aggregated summoner stats with cache
- Registered users: detailed personal performance, enriched match history

**Version 1.0 — Advanced features** (📋 Planned):
- Anonymous users: intelligent caching, hybrid data access strategy
- Registered users: KPI dashboards, prioritized cache strategy, automatic freshness validation

For more details:
- **[Funcionalidades.md](Funcionalidades.md)** — UI descriptions with screenshots
- **[Funcionalidades-Detalladas.md](Funcionalidades-Detalladas.md)** — full feature matrix

#### Users & permissions (Phase 1 analysis)

User types:
1. Anonymous: read-only access to public data
2. Registered: access to personal profile and favorites
3. Administrator: full system control

Permissions by type:
- Anonymous: search and view profiles and matches
- Registered: above + personal dashboard, favorites, notes
- Admin: above + user management, moderation, system metrics

---

### Domain entities

Conceptual entity diagram:

```
┌─────────────┐           ┌──────────────┐
│    User     │───────────│  Summoner    │
│             │  favorites│              │
├─────────────┤           ├──────────────┤
│ id          │           │ id           │
│ name        │           │ puuid        │
│ email       │           │ riotId       │
│ encodedPwd  │           │ name         │
│ roles[]     │           │ level        │
│ active      │           │ tier         │
│ profilePic  │           │ rank         │
└─────────────┘           │ lp           │
                          │ wins         │
                          │ losses       │
                          └──────────────┘
                                 │
                                 │ 1:N
                                 ▼
                          ┌──────────────┐
                          │    Match     │
                          ├──────────────┤
                          │ matchId      │
                          │ championId   │
                          │ kills        │
                          │ deaths       │
                          │ assists      │
                          │ win          │
                          │ gameDuration │
                          │ timestamp    │
                          └──────────────┘
```

Main relationships:
- User N:M Summoner (favorites)
- Summoner 1:N Match (history)
- User 1:N Match (notes on matches — future)

---

### Images & static assets

Image sources:
- User avatars: blob stored in MySQL (field `profilePic`)
- LoL profile icons: Riot Data Dragon CDN
- Champion images: Riot Data Dragon CDN
- Item and rune icons: Riot Data Dragon CDN (future)

Image management:
- User uploads: validated by type and max size (5MB)
- External images: URLs generated dynamically from Data Dragon

---

### Charts and data visualization

Chosen library: Chart.js

Planned chart types:
- Line: KDA evolution, win rate over time
- Bar: Most-played champions, stat comparisons
- Pie: Role distribution, match types
- Radar: Skill profile (CS, vision score, kill participation)

Implementation planned for v0.2

---

### Complementary technology

#### Riot Games API integration

Endpoints used:
- Account-v1: `/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}`
- Summoner-v4: `/lol/summoner/v4/summoners/by-puuid/{puuid}`
- League-v4: `/lol/league/v4/entries/by-puuid/{puuid}`
- Champion-Mastery-v4: `/lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}/top`
- Match-v5: `/lol/match/v5/matches/by-puuid/{puuid}/ids` and `/lol/match/v5/matches/{matchId}`

Considerations:
- Rate limits: 20 req/s, 100 req/2min (development API key)
- Region: EUW by default, configurable
- Local cache to reduce calls

---

#### Static analysis (SonarCloud)

Configuration:
- Integrated in GitHub Actions
- Custom Quality Gate with strict metrics
- Analysis for Java, TypeScript, HTML, CSS

Metrics:
- Coverage: ≥55%
- Bugs: 0 critical
- Vulnerabilities: 0
- Code smells: <50
- Duplication: <5%

---

#### Advanced algorithm (future)

Performance prediction:
- ML model trained with historical data
- Features: team composition, picks, bans, elo, recent stats
- Output: win probability
- Framework: TensorFlow / scikit-learn (tentative)

Status: ⏸️ Tentative for v1.0

---

### Mockups & wireframes

Initial wireframes were developed in static HTML/CSS and are available in `utils/wireframes/`:

- `index.html` — Main search page
- `summoner.html` — Summoner profile
- `dashboard.html` — Registered user dashboard
- `admin.html` — Admin panel
- `login.html` — Login and registration screens

These mockups served as a reference for the Angular frontend design.

---

## 🔗 References

- Riot API docs: https://developer.riotgames.com/docs/lol
- Data Dragon: https://ddragon.leagueoflegends.com/
- Spring Boot: https://spring.io/projects/spring-boot
- Angular: https://angular.io/
- Docker: https://docs.docker.com/

---

[← Back to main README](../README.md) | [View Methodology →](Seguimiento.md)
