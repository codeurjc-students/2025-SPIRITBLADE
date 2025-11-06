# Detailed Features — SPIRITBLADE

This document contains the full list of features planned for the SPIRITBLADE project, indicating their implementation status and a detailed description.

> 📝 Updated October 2025: This document was revised to reflect a refocus of the project scope based on available development time and prioritization of core features. See [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) for full change details.

---

## Implementation status

### Legend
- ✅ Implemented — Feature complete in v0.1
- 🚧 In progress — Started but not finished
- 📋 Planned — Scheduled for future versions
- ⏸️ Tentative — Under consideration

---

## 1. Core Features (v0.1)

### 1.1 Anonymous user

#### Scope (updated)
Basic functionality: search summoners and view their profile and rank. Public match history is available with caching. Basic champion stats (masteries, most-played champions, general performance metrics) are exposed.

| ID | Feature | Status | Behavior description |
|----|---------|--------|---------------------|
| F1.1.1 | Search summoners | ✅ | The user inputs a Riot ID (gameName#tagLine) in the search field. The system validates the format, queries Riot Games API and displays the full profile. If the summoner doesn't exist, a clear error message is shown. Caching is implemented to improve performance. |
| F1.1.2 | View summoner profile and rank | ✅ | Profile page shows avatar, level, Riot ID, current rank (tier/division/LP), wins/losses, win rate and total matches. Data is fetched from Riot API and cached locally with an intelligent refresh strategy. |
| F1.1.3 | View public match history | ✅ | Lists the latest matches (paginated, 5 per page). Each match shows result (win/loss), champion icon, KDA, duration and date. Caching reduces API calls. Users can load more matches with a "Load more" button. |
| F1.1.4 | View basic champion stats | ✅ | Access champion mastery: top 3 most-played champions with mastery level and points (1–7). Shows icon, name, mastery level and total mastery points. Data comes from Champion-Mastery-v4. |
| F1.1.5 | Recent searches | ✅ | Home page lists the 10 most recent summoner searches by any user, sorted by date (newest first). Each item links to the summoner profile. |

---

### 1.2 Registered user

#### Scope (updated)
Basic functionality: customizable personal dashboard, detailed match data enriched from Riot API, and display of personal champion mastery/performance.

| ID | Feature | Status | Behavior description |
|----|---------|--------|---------------------|
| F1.2.1 | Register | ✅ | Registration form with username, email, password + confirm. Validations: required fields, proper email format, matching passwords, unique username. On success the account is created (password hashed with BCrypt) and the user is logged in automatically. |
| F1.2.2 | Login | ✅ | Login form (username + password). Backend validates credentials (Spring Security), issues a JWT valid for 24 hours and stores it as an HttpOnly cookie. Clear error messages for invalid credentials, server unavailable, or network errors. |
| F1.2.3 | Logout | ✅ | User logs out from the menu. JWTs (access and refresh) are invalidated by removing cookies, security context cleared and user redirected to home. |
| F1.2.4 | Customizable dashboard | ✅ | Personal dashboard with profile info (name, email), basic stats (searches, favorites), quick actions (search summoner, view favorites, edit profile). Dashboard is configurable per user preferences and requires a valid JWT. |
| F1.2.5 | Detailed recent-match data | ✅ | Enriched match history with detailed champion stats, item build, objective participation and damage dealt. Presented in a clear, accessible format, powered by Riot API data. |
| F1.2.6 | Personal champion mastery view | ✅ | Dashboard showing user's favorite champions with mastery and performance stats: mastery level, accumulated points, average KDA, win rate per champion. Updated on each search. |
| F1.2.7 | Save favorite summoners | 🚧 | "Add to favorites" button on summoner profiles. Favorites stored in DB (User–Summoner relationship). Dashboard shows quick access list. Data model implemented, UI in progress. |
| F1.2.8 | Link LoL account | 📋 | Users can link their League of Legends account using Riot ID. System verifies and associates the account for automated personal stats analysis. Planned for v0.2. |

---

### 1.3 Admin

| ID | Feature | Status | Behavior description |
|----|---------|--------|---------------------|
| F1.3.1 | Access admin panel | ✅ | Requires ADMIN role in the JWT. Panel shows user management, system metrics and logs. Unauthorized users are redirected with an error message. |
| F1.3.2 | List all users | ✅ | Table of all registered users showing ID, name, email, roles, active/inactive state, and registration date. Endpoint `/admin/users` protected with `@PreAuthorize("hasRole('ADMIN')")`. |
| F1.3.3 | Enable/disable users | ✅ | Toggle to change the `active` flag. Disabled users cannot log in (checked in UserLoginService). Change applies immediately via API call. |
| F1.3.4 | Delete users | ✅ | Delete button with confirmation. DELETE `/admin/users/{id}` removes the user from the DB (cascade for relations). Admins cannot delete their own account. |
| F1.3.5 | Edit user roles | 🚧 | Admin can switch roles (USER ↔ ADMIN) via inline select + save button. PUT `/admin/users/{id}/roles`. In progress. |
| F1.3.6 | Moderate user content | 📋 | Review and remove inappropriate notes/comments. Moderation dashboard with automated flags. Planned for v0.2. |

---

## 2. Intermediate Features (v0.2)

### 2.1 Anonymous user

#### Scope (updated)
Intermediate: aggregated statistics for summoners with cached match data for performance.

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| F2.1.1 | Aggregated summoner statistics | 📋 | Aggregation engine combining data from multiple searched summoners: average win rate per champion, average KDA by role, popular champion pick rates. Public dashboard with charts, optimized with cached match details to reduce load times. |

---

### 2.2 Registered user

#### Scope (updated)
Intermediate: deeper personal performance insights and enriched match history context.

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| F2.2.1 | Personal performance for favorite champions | 📋 | Detailed analysis panels for frequently played champions: performance trends, season comparisons, strengths/weaknesses using historical data. |
| F2.2.2 | Enriched match history | 📋 | Enhanced match timeline with events, early/mid/late phase analysis, and comparisons with other players in the match. Integrates multiple Riot API sources. |
| F2.2.3 | Add notes to matches | 📋 | Free-text notes per match tied to a Match + User. Notes can be edited and deleted. |
| F2.2.4 | Receive notifications | 📋 | Real-time notifications (WebSocket) when: a favorite plays a match, a favorite ranks up, or a new mastery milestone is achieved. Notifications pane in dashboard. |

---

### 2.3 Admin

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| F2.3.1 | Moderation dashboard | 📋 | Interface to review user reports, automatically flagged content and suspicious activity logs. |
| F2.3.2 | System metrics | 📋 | Metrics: active users, searches per day, most-searched summoners, API usage (rate limits), HTTP errors. Charts powered by Chart.js. |

---

## 3. Advanced Features (v1.0)

### 3.1 Anonymous user

#### Scope (updated)
Advanced: intelligent caching system that minimizes latency while keeping data fresh; hybrid data access strategy balancing performance and freshness.

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| F3.1.1 | Intelligent caching system | 📋 | Multi-layer caching with adaptive strategies: in-memory (Redis), persistent cache (MySQL), and intelligent invalidation based on time/events. Reduces latency while keeping data up-to-date. |
| F3.1.2 | Hybrid data access strategy | 📋 | Algorithm that automatically balances performance vs. freshness: prefers recent cached data, selectively refreshes critical data, and uses prefetching. Improves user experience. |
| F3.1.3 | Global community statistics | 📋 | Public dashboard with aggregated application-wide stats: top searched summoners, champions with highest win rate, regional statistics — backed by intelligent cache. |

---

### 3.2 Registered user

#### Scope (updated)
Advanced: personalized KPI dashboard derived from full match history; cache-first strategy to minimize expensive external API calls; automatic freshness validation.

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| F3.2.1 | KPI dashboard | 📋 | Advanced dashboard that computes and shows key performance indicators over time, trend analysis and pattern detection based on the user's full match history stored in the DB. |
| F3.2.2 | Prioritized cache-first strategy | 📋 | Data access strategy that prefers local DB/cache before calling external APIs: freshness verification with timestamps, selective update of stale entries, minimize Riot API calls. |
| F3.2.3 | Automatic freshness validation | 📋 | Automatic checks and background updates for stale data: age analysis, async background refresh, and optional update notifications to users. |
| F3.2.4 | Build recommendations | 📋 | Algorithm suggests optimal builds, runes and item paths based on playstyle (champions, role, KDA). Integrates community trends. |
| F3.2.5 | Custom leaderboards | 📋 | Private leaderboards among friends/favorites. Compare stats, win rates and masteries in private tables. |

---

### 3.3 Admin

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| F3.3.1 | Audit logs | 📋 | Detailed audit records: who changed what, when and from which IP. Searchable, filterable and exportable to CSV. |
| F3.3.2 | API key management | 📋 | Tooling to rotate Riot API keys, monitor rate limits and manage multiple keys for load balancing. |

---

## 4. Technical Features

### 4.1 Security

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| FT.1 | JWT authentication | ✅ | HS256-signed tokens, 24h expiration, refresh token 7 days, stored in HttpOnly cookies. |
| FT.2 | Role-based authorization | ✅ | Spring Security with `@PreAuthorize`. Roles: USER, ADMIN. Angular guards for protected routes. |
| FT.3 | Password hashing | ✅ | BCryptPasswordEncoder (strength 10). Passwords are never stored in plain text. |
| FT.4 | HTTPS | ✅ | Self-signed JKS certificate for development, TLS 1.3 in production. Port 443. |
| FT.5 | Input validation | ✅ | `@Valid` on DTOs with Hibernate Validator. String sanitization to prevent XSS/SQL injection. |

---

### 4.2 External API integration

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| FT.6 | Riot Games API | ✅ | Full integration with Account-v1, Summoner-v4, League-v4, Champion-Mastery-v4, Match-v5. Uses RestTemplate with retry logic. |
| FT.7 | Data Dragon CDN | ✅ | Static assets (champion, item, rune images) loaded from Riot Data Dragon. Version 14.1.1. |
| FT.8 | Rate limiting | 📋 | Implement rate limiting for Riot API (20 req/s, 100 req/2min). Proposed Bucket4j + Redis solution for v0.2. |

---

### 4.3 Performance & scalability

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| FT.9 | Summoner cache | ✅ | MySQL field `lastSearchedAt`. Data is refreshed only if >5 minutes since last search. |
| FT.10 | Distributed cache | 📋 | Redis for API responses and sessions to reduce DB and external API load (v0.2). |
| FT.11 | Lazy loading | 📋 | Angular module lazy-loading to reduce initial bundle size (v0.2). |

---

### 4.4 Quality & testing

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| FT.12 | Backend unit tests | ✅ | JUnit 5 + Mockito. Coverage target ≥60%. Tests for services, controllers and mappers. |
| FT.13 | Frontend unit tests | ✅ | Jasmine + Karma. Coverage target ≥50%. Component, service and guard tests. |
| FT.14 | Integration tests | ✅ | Spring Boot Test with `@SpringBootTest`. Endpoint tests using MockMvc. |
| FT.15 | E2E tests | 🚧 | Selenium WebDriver for end-to-end flows: login → search → profile. In progress. |
| FT.16 | Static analysis | ✅ | SonarCloud in CI pipeline. Metrics: bugs, code smells, vulnerabilities. Quality Gate configured. |

---

### 4.5 Deployment & DevOps

| ID | Feature | Status | Description |
|----|---------|--------|-------------|
| FT.17 | Docker multi-stage | ✅ | Dockerfile with 3 stages: Node build (Angular), Maven build (Spring Boot), JRE runtime. Target image <200MB. |
| FT.18 | Docker Compose | ✅ | Orchestrates app + MySQL with healthchecks, depends_on and persistent volumes. |
| FT.19 | CI/CD (GitHub Actions) | ✅ | Workflows: build (tests + quality), deploy-dev (main), deploy-release (releases), manual-build. |
| FT.20 | Publish to DockerHub | ✅ | Automated workflow publishes images with tags: dev, version (0.1.0), latest. OCI artifacts for compose. |
| FT.21 | Kubernetes manifests | 📋 | K8s manifests for deployments, services and ingress. HPA for horizontal scaling (v1.0). |

---

## Status summary

| Status | Count | Approx % |
|--------|-------:|--------:|
| ✅ Implemented | 30 | ~50% |
| 🚧 In progress | 4 | ~7% |
| 📋 Planned | 24 | ~40% |
| ⏸️ Tentative | 2 | ~3% |
| **TOTAL** | **60** | **100%** |

---

[← Back to main README](../README.md) | [View features with screenshots →](Funcionalidades.md)
