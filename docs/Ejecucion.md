# Ejecución - SPIRITBLADE

Este documento proporciona instrucciones detalladas para ejecutar la aplicación SPIRITBLADE usando Docker, partiendo de las imágenes publicadas en Docker Hub.

---

## 📋 Requisitos previos

### Docker

SPIRITBLADE utiliza Docker para el despliegue. Instala Docker según tu sistema operativo:

- Windows: Docker Desktop (requiere WSL2 en Windows 10/11 Pro, Enterprise o Education)
- macOS: Docker Desktop (compatibilidad con Intel y Apple Silicon)
- Linux: Docker Engine + Docker Compose (instala vía el gestor de paquetes de tu distribución)

Guías oficiales de instalación de Docker:

- https://docs.docker.com/desktop/install/
- https://docs.docker.com/engine/install/

---

## Inicio rápido

### Opción A — Ejecutar las imágenes publicadas (recomendado)

Esta es la forma más rápida de ejecutar SPIRITBLADE usando las imágenes publicadas.

1) Crea una carpeta vacía y descarga el archivo compose:

```powershell
mkdir spiritblade; cd spiritblade
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml" -OutFile docker-compose.yml
```

2) Crea un archivo `.env` junto a `docker-compose.yml` con las variables de entorno requeridas (ejemplo):

```text
DOCKER_USERNAME=codeurjcstudents
# Consulte .env.example. Copie los valores en .env y NO comitee .env
RIOT_API_KEY=RGAPI-your-riot-api-key-here
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=spiritblade_db
MYSQL_USER=spiritblade_user
MYSQL_PASSWORD=spiritblade_pass
JWT_SECRET=your-secure-jwt-secret-min-256-bits
SERVER_PORT=443
```

3) Inicia la pila:

```powershell
docker compose up -d
```

4) Sigue los logs de la aplicación (opcional):

```powershell
docker compose logs -f app
```

Abre la aplicación en tu navegador en: https://localhost:443

> Nota: en desarrollo el servidor usa un certificado autofirmado. Tu navegador mostrará una advertencia de seguridad — acéptala para continuar.

---

### Opción B — Compilar desde el código fuente

Si prefieres construir la imagen localmente:

```powershell
# Clona el repositorio
git clone https://github.com/codeurjc-students/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE

# Construye la imagen Docker
docker build -f docker/Dockerfile -t spiritblade:custom .

# Edita docker/docker-compose.yml para usar la imagen local en lugar de la publicada
# (reemplaza image: ${DOCKER_USERNAME}/spiritblade:0.1 por image: spiritblade:custom)

# Copia y edita variables de entorno
Copy-Item docker/.env.example docker/.env
# Edita docker/.env con tus valores, luego inicia la pila
cd docker
docker compose up -d
```

---

## Configuración

Coloca un archivo `.env` junto a `docker-compose.yml` con al menos estas variables:

```text
# Namespace de las imágenes Docker (opcional)
DOCKER_USERNAME=codeurjcstudents

# Clave de la API de Riot Games (requerida)
RIOT_API_KEY=RGAPI-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

# Configuración MySQL
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=spiritblade_db
MYSQL_USER=spiritblade_user
MYSQL_PASSWORD=spiritblade_pass

# Secreto JWT (usa un valor aleatorio seguro, mínimo ~32 caracteres)
JWT_SECRET=your-very-secure-secret-key-min-256-bits-long

# Puerto del servidor (por defecto 443 para HTTPS)
SERVER_PORT=443
```

Obtén una clave de Riot Games en: https://developer.riotgames.com/

Nota: Las claves de desarrollo expiran con frecuencia y tienen límites de tasa (aprox. 20 req/s, 100 req/2min). Para producción, solicita una clave de producción a Riot.

---

## Estructura del stack Docker

La pila de compose ejecuta un conjunto pequeño de servicios:

- spiritblade-mysql (MySQL 8.0)
  - puerto interno: 3306
  - volumen persistente: spiritblade_mysql_data
  - healthcheck: mysqladmin ping

- spiritblade-app (Spring Boot + Angular)
  - puerto publicado: 443 (HTTPS)
  - espera al healthcheck de MySQL
  - variables de entorno: RIOT_API_KEY, JWT_SECRET, etc.
  - healthcheck: curl -k https://localhost:443/actuator/health

---

## Acceder a la aplicación

URL: https://localhost:443

Como la configuración de desarrollo usa un certificado autofirmado necesitarás aceptar la advertencia del navegador:

- Chrome: Advanced → Proceed to localhost (unsafe)
- Firefox: Advanced → Accept the risk and continue

### Credenciales de prueba (usuarios de ejemplo preconfigurados)

Cuenta de usuario (normal):

```
username: testuser
password: password
```

Cuenta de administrador:

```
username: admin
password: admin
```

---

## Datos de ejemplo y comprobaciones básicas

En la primera ejecución la aplicación inserta usuarios de ejemplo y datos de muestra.

Usuarios preconfigurados:

- `admin` (rol: ADMIN, contraseña: `admin`)
- `testuser` (rol: USER, contraseña: `password`)

Prueba a buscar invocadores reales (región EUW por defecto), por ejemplo:

- Player#EUW
- Faker#KR1
- G2Caps#EUW

---

## Comandos útiles

Usa estos comandos en PowerShell (o adáptalos a tu shell):

```powershell
# Ver logs de todos los servicios
docker compose logs -f

# Ver solo los logs de la app
docker compose logs -f app

# Ver solo los logs de MySQL
docker compose logs -f mysql

# Mostrar las últimas 100 líneas de los logs de la app
docker compose logs --tail=100 app

# Mostrar estado de los contenedores
docker compose ps

# Parar (mantiene volúmenes)
docker compose stop

# Reiniciar
docker compose restart

# Parar y eliminar contenedores (mantiene volúmenes)
docker compose down

# Eliminar todo incluyendo volúmenes (PÉRDIDA DE DATOS)
docker compose down -v

# Descargar imágenes actualizadas y recrear
docker compose pull
docker compose up -d

# Ejecutar un comando en el contenedor de la app en ejecución
docker compose exec app java -jar /app/app.jar --version
```

---

## Comprobaciones de estado y verificación

Confirma que el backend está sano:

```powershell
# Acepta el certificado autofirmado con -k
curl -k https://localhost:443/actuator/health

# Respuesta esperada: {"status":"UP"}
```

Verifica la base de datos desde dentro del contenedor MySQL:

```powershell
docker compose exec mysql mysql -u spiritblade_user -p spiritblade_db
# luego dentro de mysql: SHOW TABLES; SELECT COUNT(*) FROM USERS;
```

Busca errores en los logs:

```powershell
docker compose logs app | Select-String "ERROR"
docker compose logs app | Select-String "WARN"
```

---

## Solución de problemas

### El puerto 443 ya está en uso

Si otro servicio escucha en 443 (nginx, Apache, etc.), cambia el mapeo de puertos en `docker-compose.yml`:

```yaml
services:
  app:
    ports:
      - "8443:443" # mapea 8443 externo -> 443 interno
```

Luego abre https://localhost:8443

### MySQL no responde

```powershell
docker compose ps
docker compose restart mysql
docker compose logs mysql
```

### Errores con la clave de Riot API

Errores como:

```
ERROR: Riot API error: 401 Unauthorized
ERROR: Riot API error: 403 Forbidden
```

Pasos:
1) Comprueba que `RIOT_API_KEY` está correctamente establecido en `.env`
2) Regenera la clave de desarrollo (expira con frecuencia)
3) Observa los límites de tasa (20 req/s, 100 req/2min)

### La aplicación no se inicia

Revisa los logs de la app para más detalles:

```powershell
docker compose logs app
```

Causas comunes:

- JWT_SECRET demasiado corto — usa una cadena aleatoria segura (recomendado ~32+ caracteres)
- MySQL no está listo — espera a que pase el healthcheck
- Variables de entorno faltantes — revisa `.env`

### Advertencias de certificado SSL

Los certificados autofirmados son esperados en desarrollo. Para producción usa un certificado firmado por una CA (ejemplo con Let's Encrypt):

```bash
# Obtener certificado con certbot (servidor de producción con dominio)
certbot certonly --standalone -d yourdomain.com

# Convertir a PKCS12 luego a JKS si necesitas un keystore Java
openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out keystore.p12
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 \
  -destkeystore keystore.jks
```

---

## Despliegue remoto (servidor)

Requisitos mínimos del servidor:

- SO: Ubuntu 20.04+/Debian 11+/RHEL 8+
- RAM: 2GB (4GB recomendado)
- Disco: 10GB libres
- Docker & Docker Compose instalados
- Puertos: 443 (HTTPS) y opcionalmente 3306 (MySQL)

Pasos básicos (ejemplo):

```bash
# Conéctate por SSH al servidor
ssh user@your-server

# Instalar Docker (ejemplo para Ubuntu)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Desplegar
mkdir /opt/spiritblade && cd /opt/spiritblade
curl -O https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml
nano .env # rellena con valores seguros para producción
docker compose up -d

# Permitir HTTPS en el firewall
sudo ufw allow 443/tcp
sudo ufw enable
```

---

## Documentación adicional

- [Guía de desarrollo](Guia-Desarrollo.md)
- [API REST](API.md)
- [Docker README](../docker/README.md)

---

[← Volver al README principal](../README.md)

**Esto es normal en desarrollo.** El certificado es autofirmado.
