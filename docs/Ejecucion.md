# Ejecución - SPIRITBLADE

Este documento proporciona instrucciones detalladas para ejecutar la aplicación SPIRITBLADE utilizando Docker, partiendo de las imágenes publicadas en DockerHub.

---

## 📋 Requisitos Previos

# Execution — SPIRITBLADE

This document provides detailed instructions to run the SPIRITBLADE application using Docker, based on the images published on Docker Hub.

---

## Prerequisites

### Docker

SPIRITBLADE uses Docker for deployment. Please install Docker according to your operating system:

- Windows: Docker Desktop (requires WSL2 on Windows 10/11 Pro, Enterprise or Education)
- macOS: Docker Desktop (Intel and Apple Silicon supported)
- Linux: Docker Engine + Docker Compose (install via your distro package manager)

Official Docker installation guides:

- https://docs.docker.com/desktop/install/
- https://docs.docker.com/engine/install/

---

## Quick start

### Option A — Run the published images (recommended)

This is the fastest way to run SPIRITBLADE using the published Docker images.

1) Create an empty folder and download the compose file:

```powershell
mkdir spiritblade; cd spiritblade
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml" -OutFile docker-compose.yml
```

2) Create a `.env` file next to `docker-compose.yml` with the required environment variables (example):

```text
DOCKER_USERNAME=codeurjcstudents
RIOT_API_KEY=RGAPI-your-riot-api-key-here
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=spiritblade_db
MYSQL_USER=spiritblade_user
MYSQL_PASSWORD=spiritblade_pass
JWT_SECRET=your-secure-jwt-secret-min-256-bits
SERVER_PORT=443
```

3) Start the stack:

```powershell
docker compose up -d
```

4) Follow the app logs (optional):

```powershell
docker compose logs -f app
```

Open the app in your browser at: https://localhost:443

> Note: In development the server uses a self-signed certificate. Your browser will show a security warning — accept it to proceed.

---

### Option B — Build from source

If you prefer to build the image locally:

```powershell
# Clone the repository
git clone https://github.com/codeurjc-students/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE

# Build the Docker image
docker build -f docker/Dockerfile -t spiritblade:custom .

# Edit docker/docker-compose.yml to use the local image instead of the published one
# (replace image: ${DOCKER_USERNAME}/spiritblade:0.1 with image: spiritblade:custom)

# Copy and edit environment variables
Copy-Item docker/.env.example docker/.env
# Edit docker/.env with your values, then start the stack
cd docker
docker compose up -d
```

---

## Configuration

Place a `.env` file next to `docker-compose.yml` with at least these variables:

```text
# Docker image namespace (optional)
DOCKER_USERNAME=codeurjcstudents

# Riot Games API key (required)
RIOT_API_KEY=RGAPI-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

# MySQL configuration
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=spiritblade_db
MYSQL_USER=spiritblade_user
MYSQL_PASSWORD=spiritblade_pass

# JWT secret (use a secure random value, min ~32 chars)
JWT_SECRET=your-very-secure-secret-key-min-256-bits-long

# Server port (default 443 for HTTPS)
SERVER_PORT=443
```

Obtain a Riot Games API key at: https://developer.riotgames.com/

Note: Development keys expire frequently and are rate limited (approx. 20 req/s, 100 req/2min). For production, request a production key from Riot.

---

## Docker layout

The compose stack runs a small set of services:

- spiritblade-mysql (MySQL 8.0)
  - internal port: 3306
  - persistent volume: spiritblade_mysql_data
  - healthcheck: mysqladmin ping

- spiritblade-app (Spring Boot + Angular)
  - published port: 443 (HTTPS)
  - waits for MySQL healthcheck
  - environment: RIOT_API_KEY, JWT_SECRET, etc.
  - healthcheck: curl -k https://localhost:443/actuator/health

---

## Accessing the application

URL: https://localhost:443

Because the development setup uses a self-signed SSL certificate you will need to accept the browser warning:

- Chrome: Advanced → Proceed to localhost (unsafe)
- Firefox: Advanced → Accept the risk and continue

### Test credentials (pre-configured sample users)

User account (regular):

```
username: testuser
password: password
```

Administrator account:

```
username: admin
password: admin
```

---

## Sample data and basic checks

On first run the app seeds example users and sample data.

Preconfigured users:

- `admin` (role: ADMIN, password: `admin`)
- `testuser` (role: USER, password: `password`)

Try searching real summoners (EUW region by default), for example:

- Player#EUW
- Faker#KR1
- G2Caps#EUW

---

## Useful commands

Follow these in a PowerShell terminal (or adapt to your shell):

```powershell
# Tail logs for all services
docker compose logs -f

# Tail only the app logs
docker compose logs -f app

# Tail only MySQL logs
docker compose logs -f mysql

# Show the last 100 lines of the app logs
docker compose logs --tail=100 app

# Show container status
docker compose ps

# Stop (keeps volumes)
docker compose stop

# Restart
docker compose restart

# Stop and remove containers (keeps volumes)
docker compose down

# Remove everything including volumes (DATA LOSS)
docker compose down -v

# Pull updated images and recreate
docker compose pull
docker compose up -d

# Execute a command in the running app container
docker compose exec app java -jar /app/app.jar --version
```

---

## Health checks and verification

Confirm the backend is healthy:

```powershell
# Accept the self-signed cert with -k
curl -k https://localhost:443/actuator/health

# Expected response: {"status":"UP"}
```

Verify the database from within the MySQL container:

```powershell
docker compose exec mysql mysql -u spiritblade_user -p spiritblade_db
# then inside mysql: SHOW TABLES; SELECT COUNT(*) FROM USERS;
```

Search logs for errors:

```powershell
docker compose logs app | Select-String "ERROR"
docker compose logs app | Select-String "WARN"
```

---

## Troubleshooting

### Port 443 already in use

If another service is listening on 443 (nginx, Apache, etc.), change the host port mapping in `docker-compose.yml`:

```yaml
services:
  app:
    ports:
      - "8443:443" # map external 8443 -> internal 443
```

Then open https://localhost:8443

### MySQL is not responding

```powershell
docker compose ps
docker compose restart mysql
docker compose logs mysql
```

### Riot API key errors

Errors like:

```
ERROR: Riot API error: 401 Unauthorized
ERROR: Riot API error: 403 Forbidden
```

Steps:
1) Confirm `RIOT_API_KEY` is correctly set in `.env`
2) Regenerate the development key (it expires frequently)
3) Observe rate limits (20 req/s, 100 req/2min)

### Application fails to start

Check the app logs for details:

```powershell
docker compose logs app
```

Common causes:

- JWT_SECRET too short — use a secure random string (recommend ~32+ chars)
- MySQL not ready — wait for the healthcheck to pass
- Missing environment variables — review `.env`

### SSL certificate warnings

Self-signed certificates are expected in development. For production use a CA-signed certificate (Let's Encrypt example):

```bash
# Obtain certificate with certbot (production server with domain)
certbot certonly --standalone -d yourdomain.com

# Convert to PKCS12 then to JKS if you need a Java keystore
openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out keystore.p12
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 \
  -destkeystore keystore.jks
```

---

## Remote deployment (server)

Minimum server requirements:

- OS: Ubuntu 20.04+/Debian 11+/RHEL 8+
- RAM: 2GB (4GB recommended)
- Disk: 10GB free
- Docker & Docker Compose installed
- Ports: 443 (HTTPS) and optionally 3306 (MySQL)

Basic steps (example):

```bash
# SSH into the server
ssh user@your-server

# Install Docker (Ubuntu example)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Deploy
mkdir /opt/spiritblade && cd /opt/spiritblade
curl -O https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml
nano .env # fill with production-safe values
docker compose up -d

# Allow HTTPS through the firewall
sudo ufw allow 443/tcp
sudo ufw enable
```

---

## Additional documentation

- [Development Guide](Guia-Desarrollo.md)
- [API REST](API.md)
- [Docker README](../docker/README.md)

---

[← Back to main README](../README.md)
**Esto es normal en desarrollo.** El certificado es autofirmado.
