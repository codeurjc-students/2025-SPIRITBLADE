# SPIRITBLADE

<img width="25%" height="25%" alt="logoNoBckgroung" src="https://github.com/user-attachments/assets/1f73258c-5c4a-4d87-ade7-3aaa546827b9" />

## Summary

**SPIRITBLADE** is a web application designed as a Final Degree Project (TFG) for the double degree in Computer Engineering and Software Engineering at ETSII (URJC). The aim is to provide League of Legends players with a platform similar to OP.GG or Porofessor, allowing them to analyze and visualize player and match statistics using data obtained from Riot Games' public API.

This document covers the definition of the main objectives, features, screens, technical goals, and planned methodology.

---

## Objectives

### Functional Objectives

SPIRITBLADE aims to offer an intuitive web platform for searching, analyzing, and visualizing League of Legends statistics for both anonymous and registered users. The application will provide essential information about summoners, their match histories, and champion statistics, while also allowing registered users to manage personalized data.

#### Functionalities (prioritized by user type & phase)

**User types:**
- Anonymous user
- Registered user
- Administrator

**Basic Functionality**

| User type         | Functionality                                                                                 |
|-------------------|----------------------------------------------------------------------------------------------|
| Anonymous         | - Search for summoners and view their profile and rank                                       |
|                   | - View public match history                                                                  |
|                   | - View basic champion stats (KDA, winrate, most played)                                      |
| Registered        | - Associate a League of Legends account to their profile                                     |
|                   | - Save favorite summoners                                                                    |
|                   | - Personalized statistics and simple performance graphs                                      |
|                   | - Upload/edit avatar                                                                         |
| Administrator     | - User management (activate/deactivate, delete users)                                        |

**Intermediate Functionality**

| User type         | Functionality                                                                                 |
|-------------------|----------------------------------------------------------------------------------------------|
| Anonymous         | - View aggregated statistics of searched summoners                                           |
| Registered        | - Advanced personal performance analytics (trends, comparisons)                              |
|                   | - Notes/comments on matches                                                                  |
| Administrator     | - Moderate user content                                                                      |

**Advanced Functionality**

| User type         | Functionality                                                                                 |
|-------------------|----------------------------------------------------------------------------------------------|
| Anonymous         | - View global champion pick/ban rates (from app usage data)                                  |
| Registered        | - Receive periodic email reports (tentative)                                                 |
|                   | - Build recommendations based on playstyle                                                   |
| Administrator     | - System statistics and audit logs                                                           |


### Technical Objectives

SPIRITBLADE will be implemented as a Single Page Application (SPA) using modern web technologies, automated testing, continuous integration/continuous deployment (CI/CD), and containerization for easy deployment.

#### Technical Goals

- SPA architecture: Angular (frontend) + REST API (backend, Spring Boot)
- MySQL as the primary database
- Integration with Riot Games public API
- Automated unit and integration tests (backend and frontend)
- Static code analysis (Sonar or similar)
- CI/CD pipelines via GitHub Actions
- Dockerized deployment and Docker Compose
- Secure authentication and authorization with role-based access
- Responsive design for desktop and mobile

---

## Methodology

The project will follow an iterative and incremental development process with the following tentative phases:

1. **Phase 1:** Definition of functionalities and screens (until 15 September)
2. **Phase 2:** Repository setup, basic tests, and CI configuration (until 15 October)
3. **Phase 3:** Version 0.1 – Basic functionality + Docker (until 15 December)
4. **Phase 4:** Version 0.2 – Intermediate functionality (until 1 March)
5. **Phase 5:** Version 1.0 – Advanced functionality (until 15 April)
6. **Phase 6:** Drafting of the project report (until 15 May)
7. **Phase 7:** TFG defense (until 15 June)

---

## Detailed Features

### Entities and Relationships

- **User:** Stores login, roles, profile image, and links to favorite summoners.
- **Summoner:** Represents a LoL account, with profile info, ranking, and stats.
- **Match:** Represents a single LoL match; stores participants, stats, and outcomes.
- **ChampionStat:** Aggregates statistics of a summoner for each champion played.

Main relationships:
- A user may have many favorite summoners.
- A summoner is linked to multiple matches.
- A summoner has multiple champion stats.

### Permissions

- **Anonymous:** Read-only access to public data.
- **Registered:** Access to own profile, favorite summoners, and personal statistics.
- **Admin:** Full control over users and moderation features.

### Images

- Users can upload/edit their avatar.
- Summoner and champion images are retrieved from Riot API. (tentative)

### Graphs

- Personal performance over time (line/bar charts)
- Champion usage and winrates (pie/bar charts)
- App-wide aggregated statistics (bar/line charts, advanced phase)

### Complementary Technology

- REST integration with Riot Games API
- Chart.js (or similar) for graphical data representation
- Email service for notifications (tentative as stated before)
- Static code analysis tool (Sonar)

### Advanced Algorithm

- Trend detection and suggestions based on historical match data. (tentative)
- Aggregation and ranking of champion stats from user activity. (tentative)

---

## Screens & Mockups

*Below are preliminary wireframes for the main screens. Navigation and design will be refined with user feedback.*

- **Home/Search:** Search bar for summoners, list of recent searches
- **Summoner Profile:** Profile info, match history, champion stats
- **User Dashboard:** Favorite summoners, personal stats, profile settings
- **Admin Panel:** User management, moderation tools, app stats
- **Error/Not found:** Styled error screens for navigation

*(TODO)*

---

## Tracking & Collaboration

- **Medium blog:** Progress and milestones will be published at (TODO) [Your Blog URL]
- **GitHub Project:** Task and progress tracking at [[GitHub Project URL](https://github.com/codeurjc-students/2025-SPIRITBLADE)]

---

## Author

This project is developed as part of the Final Degree Project (TFG) for the double degree in Computer Engineering and Software Engineering at ETSII, Universidad Rey Juan Carlos.

- **Student:** [Jorge Andrés Echevarría]
- **Advisor:** [Iván Chicano Capelo]

---
