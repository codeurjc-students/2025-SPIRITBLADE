# Features - SPIRITBLADE v0.1

This document describes the features implemented in SPIRITBLADE v0.1, illustrated with screenshots and detailed behavior descriptions.

---

## 📑 Contents
1. [Anonymous user](#anonymous-user)
2. [Registered user](#registered-user)
3. [Administrator](#administrator)

---

## Anonymous user

Anonymous visitors can use the search and view features without registering.

> **Update note (October 2025)**: The features in this document were adjusted to match the project's refocused scope and available development time. See [REAJUSTE-FUNCIONALIDADES.md](REAJUSTE-FUNCIONALIDADES.md) for details.

### Core features
- Search summoners and view their profile and rank
- View public match history with a caching layer
- Access basic champion statistics (mastery, most played champions, basic performance metrics)

### 1.1 Summoner search

Description: Users can search any League of Legends summoner using their Riot ID in the format `gameName#tagLine`.

Screenshot:
![Home - Search](https://github.com/user-attachments/assets/f63da861-eb8b-41fe-9487-c8177f8054c9)

Behavior:
- Search field on the home page
- Input format validation (must include `#`)
- Recent searches shown on the home page
- Automatic redirect to the found summoner profile

Example:
1. Open the home page
2. Enter a Riot ID: `Player#EUW`
3. Press Enter or click "Search"
4. The system redirects to the summoner profile

---

### 1.2 Summoner profile

Description: Shows the summoner's full information including level, rank, statistics and champion masteries.

Screenshot:
![Summoner Profile](https://github.com/user-attachments/assets/9a6220c3-e4ed-459a-a5f2-414312de0f7a)

Displayed data:
- Profile header:
  - Profile icon (from Data Dragon)
  - Full Riot ID
  - Summoner level

- Ranked stats:
  - Tier and division (e.g. Gold II)
  - LP (League Points)
  - Wins and losses
  - Calculated win rate
  - Total matches played

- Top 3 champions:
  - Champion icon
  - Champion name
  - Mastery level
  - Mastery points

Data source: Most data is fetched in real-time from the Riot Games API and cached in the local database to improve performance.

---

### 1.3 Match history

Description: Shows the summoner's recent matches with performance details.

Screenshot:
*(Section of the summoner profile showing match history)*

Per-match data:
- Result: Win (green) or Loss (red)
- Champion played: icon and name
- KDA: kills/deaths/assists
- Match duration: minutes
- Match timestamp: end time

Pagination:
- 5 matches per page by default
- "Load more" buttons to view older matches
- Dynamic loading without page refresh

---

### 1.4 Recent searches

Description: The home page lists the most recent summoner searches performed by any user.

Behavior:
- Shows the 10 most recent searched summoners
- Sorted by search date (newest first)
- Click to open the profile
- Automatically updates on new searches

Planned intermediate features:
- Aggregated statistics per summoner using cached match data

Planned advanced features:
- Smart caching system that minimizes load times while ensuring data freshness
- Hybrid data access strategy that balances performance and freshness

---

## Registered user

Registered users get access to additional features after logging in.

### Core features
- Personalized dashboard
- Detailed match data enriched using Riot API
- View champions with highest mastery and personal performance

### Intermediate features
- Access to detailed personal performance data for favorite champions
- Enriched match history with contextual information

### 2.1 Authentication

Description: Login and registration use JWT-based authentication.

Login screenshot:
![Login](https://github.com/user-attachments/assets/381dfdd6-e915-4c34-ba98-b3cf9985855d)

Login behavior:
- Credentials validation
- JWT token issuance
- Informative error messages:
  - Invalid credentials
  - Server unavailable
  - Network errors
- Automatic redirect to dashboard after successful login

Registration behavior:
- Form validations:
  - Username required
  - Valid email format
  - Password required
  - Password confirmation
- Passwords must match
- Duplicate user detection
- Automatic login after successful registration

Security:
- Passwords hashed with BCrypt
- JWT tokens with expiration
- HttpOnly cookies used to store tokens

---

### 2.2 Personal dashboard

Description: Personalized panel with stats and quick access links.

Screenshot:
![Dashboard](https://github.com/user-attachments/assets/d63561f9-b167-4059-8c2e-c1dca6cbe1fe)

Dashboard sections:
- User profile:
  - Username
  - Registered email
  - Avatar (partial implementation)

- Personal stats:
  - Total searches performed
  - Saved favorite summoners
  - Most searched champion (planned)

- Quick actions:
  - Search a summoner
  - View favorites
  - Edit profile

Note: Some dashboard features are planned for v0.2 (charts, trends).

---

### 2.3 Favorites management

Description: Users can save favorite summoners for quick access.

Current status (in development):
- "Add to favorites" button on the summoner profile
- Favorites list in the dashboard
- Activity notifications (planned for v0.2)
- Remove from favorites

Status: ✅ Data model implemented, UI in progress

Planned advanced features:
- Personalized dashboard with KPIs computed from match history
- Smart cache that prioritizes DB over expensive external API calls
- Automatic freshness validation with minimal impact on latency

---

## Administrator

Administrators have full access to system management features.

### 3.1 Admin panel

Description: Dedicated admin UI with management tools.

Screenshot:
![Admin Panel](https://github.com/user-attachments/assets/162964b0-f4f9-4521-837b-4e7b101fedd7)

Access:
- Requires `ADMIN` role in the JWT token
- Redirects automatically when lacking permissions
- Menu link visible only to admins

---

### 3.2 User management

Description: Admins can view and manage all registered users.

Features:
- List users:
  - Table with all registered users
  - Visible fields: name, email, roles, status
  - Search and filters (in progress)

- Activate / Deactivate users:
  - Toggle the `active` flag for a user
  - Deactivated users cannot log in
  - Visual indicator for status

- Delete users:
  - Permanent deletion
  - Confirmation before delete
  - Audit logs (planned for v0.2)

- Edit roles:
  - Assign USER / ADMIN
  - Immediate permission changes

Protected endpoints:
```http
GET  /admin/users                # List users
POST /admin/users/{id}/activate  # Activate
POST /admin/users/{id}/deactivate# Deactivate
DELETE /admin/users/{id}         # Delete
```

---

### 3.3 System metrics

Description: Global system metrics view (planned for v0.2).

Planned metrics:
- Total registered users
- Total searches performed
- Most searched summoners
- Activity per day/week
- Riot API usage

Status: 📋 Planned for v0.2

---

## Technical notes

### Riot API integration

All summoner search features rely on the official Riot Games APIs:
- Account-v1: translate Riot ID to PUUID
- Summoner-v4: summoner data
- League-v4: ranked data
- Champion-Mastery-v4: champion mastery stats
- Match-v5: match history

### Database

MySQL 8.0 is the only supported database (H2 is no longer used):
- Stores users, summoners, matches and statistics
- Configured with MySQL8Dialect
- Schema auto-generated via JPA/Hibernate
- UTF-8 encoding (utf8mb4_unicode_ci)

### Caching

To improve performance and reduce external API calls:
- Summoner data is cached in MySQL
- `lastSearchedAt` is updated on every search
- Images are sourced from Data Dragon (static CDN)

### File storage (MinIO)

MinIO is used to store user avatars with strict validation:
- Only PNG files are accepted (3-layer validation)
- PNG file header validation (`89 50 4E 47`)
- Extension and Content-Type checks
- Bucket: `spiritblade-uploads`
- Region: `us-east-1`

### Security

HTTPS required:
- Server runs HTTPS only on port 443
- Self-signed SSL certificate for development
- JWT for authentication (24h expiration)
- Role-based access control (USER, ADMIN)

File validation:
- PNG-only avatars
- Magic header verification
- Max file size: 10MB

### Error handling

The application handles common error scenarios:
- Summoner not found (404)
- Riot API errors (429 rate limit, 503 service unavailable)
- Network errors
- Invalid Riot ID format
- Invalid file format (non-PNG)
- Expired or invalid JWT

All errors expose informative messages to the user.

### Interactive API documentation (Swagger UI)

New in v0.1: SPIRITBLADE includes interactive REST API documentation using Swagger UI.

Features:
- 📖 Interactive exploration of all API endpoints
- 🔐 JWT authentication integrated in the UI
- 🧪 Live "Try it out" testing from the browser
- 📊 Complete data schemas with examples
- 🎨 Modern UI with filter and search

Access (HTTPS only):
- Local URL: [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html)
- OpenAPI JSON: [https://localhost/v3/api-docs](https://localhost/v3/api-docs)
- OpenAPI YAML: [https://localhost/v3/api-docs.yaml](https://localhost/v3/api-docs.yaml)

⚠️ Important: The server runs HTTPS only on port 443. You must accept the self-signed certificate the first time you visit.

Who benefits:
- Developers: explore endpoints without Postman
- Testers: visually exercise API behavior
- Integrators: generate clients from OpenAPI
- Documenters: always up-to-date API docs matching the code

Example usage:
1. Start the application
2. Open [https://localhost/swagger-ui.html](https://localhost/swagger-ui.html) (accept the SSL cert)
3. Log in via `POST /auth/login` to obtain a token
4. Click "Authorize" and paste the token
5. Test any authenticated endpoint with "Try it out"

Full documentation:
- [API.md](API.md) - Quick guide to access Swagger UI
- [SWAGGER.md](SWAGGER.md) - Full Swagger guide
- [SWAGGER-QUICKSTART.md](SWAGGER-QUICKSTART.md) - Step-by-step tutorial

---

## Upcoming features

See **[Funcionalidades Detalladas](Funcionalidades-Detalladas.md)** for the full list of planned features for future releases.

---

[← Back to main README](../README.md)
