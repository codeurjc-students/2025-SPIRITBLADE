# Ejecución - SPIRITBLADE

Este documento proporciona instrucciones detalladas para ejecutar la aplicación SPIRITBLADE utilizando Docker, partiendo de las imágenes publicadas en DockerHub.

---

## 📋 Requisitos Previos

### Instalación de Docker

**SPIRITBLADE** requiere Docker para ejecutarse. Las instrucciones de instalación varían según el sistema operativo:

#### Windows
- **Docker Desktop para Windows** (recomendado)
- Requisitos: Windows 10/11 Pro, Enterprise o Education con WSL 2
- [📥 Descargar Docker Desktop](https://docs.docker.com/desktop/install/windows-install/)
- [📖 Guía de instalación oficial](https://docs.docker.com/desktop/install/windows-install/)

#### macOS
- **Docker Desktop para Mac**
- Compatible con macOS 11 o superior (Intel y Apple Silicon)
- [📥 Descargar Docker Desktop](https://docs.docker.com/desktop/install/mac-install/)
- [📖 Guía de instalación oficial](https://docs.docker.com/desktop/install/mac-install/)

#### Linux
- **Docker Engine** + **Docker Compose**
- Instalación según distribución:
  - Ubuntu/Debian: [Guía oficial](https://docs.docker.com/engine/install/ubuntu/)
  - Fedora: [Guía oficial](https://docs.docker.com/engine/install/fedora/)
  - Otras: [Documentación Docker](https://docs.docker.com/engine/install/)
- Docker Compose: [Instalación](https://docs.docker.com/compose/install/)

---

## 🚀 Inicio Rápido

### Opción 1: Usar imagen de DockerHub (Recomendado)

Esta es la forma más sencilla de ejecutar SPIRITBLADE. La imagen ya está compilada y publicada.

```bash
# 1. Crear directorio para el proyecto
mkdir spiritblade
cd spiritblade

# 2. Descargar docker-compose.yml
curl -O https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml

# 3. Crear archivo .env con variables de entorno
cat > .env << EOF
DOCKER_USERNAME=codeurjcstudents
RIOT_API_KEY=RGAPI-your-riot-api-key-here
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_PASSWORD=spiritblade_pass
MYSQL_USER=spiritblade_user
JWT_SECRET=your-secure-jwt-secret-min-256-bits
SERVER_PORT=443
EOF

# 4. Iniciar la aplicación
docker-compose up -d

# 5. Ver logs (opcional)
docker-compose logs -f app
```

**Acceder a la aplicación**: https://localhost:443

---

### Opción 2: Compilar desde código fuente

Si deseas compilar la aplicación desde el código fuente:

```bash
# 1. Clonar repositorio
git clone https://github.com/codeurjc-students/2025-SPIRITBLADE.git
cd 2025-SPIRITBLADE

# 2. Compilar imagen Docker
docker build -f docker/Dockerfile -t spiritblade:custom .

# 3. Modificar docker-compose.yml para usar tu imagen
# Cambiar: image: ${DOCKER_USERNAME}/spiritblade:0.1
# Por: image: spiritblade:custom

# 4. Configurar .env y ejecutar
cp docker/.env.example docker/.env
# Editar docker/.env con tus valores
cd docker
docker-compose up -d
```

---

## ⚙️ Configuración

### Variables de Entorno Requeridas

Crea un archivo `.env` en el mismo directorio que `docker-compose.yml` con las siguientes variables:

```bash
# Imagen Docker (usa la imagen oficial o tu usuario)
DOCKER_USERNAME=codeurjcstudents

# API Key de Riot Games (OBLIGATORIA)
# Obtener en: https://developer.riotgames.com/
RIOT_API_KEY=RGAPI-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

# Configuración de MySQL
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=spiritblade_db
MYSQL_USER=spiritblade_user
MYSQL_PASSWORD=spiritblade_pass

# Configuración JWT (genera una clave segura aleatoria)
JWT_SECRET=your-very-secure-secret-key-min-256-bits-long

# Puerto del servidor (por defecto 443 para HTTPS)
SERVER_PORT=443
```

### Obtener API Key de Riot Games

1. Crear cuenta en [Riot Developer Portal](https://developer.riotgames.com/)
2. Generar Development API Key (válida 24h, se regenera automáticamente)
3. Para producción, solicitar Production API Key con formulario
4. Copiar la key y añadirla en `.env` como `RIOT_API_KEY`

**Nota**: La Development API Key tiene límites estrictos (20 req/s, 100 req/2min).

---

## 📂 Estructura de Docker

El despliegue utiliza dos contenedores orquestados con Docker Compose:

```
spiritblade-mysql (MySQL 8.0)
  ├── Puerto: 3306 (interno)
  ├── Volumen: spiritblade_mysql_data (persistencia)
  └── Healthcheck: mysqladmin ping

spiritblade-app (Spring Boot + Angular)
  ├── Puerto: 443:443 (HTTPS)
  ├── Dependencia: espera a MySQL (healthcheck)
  ├── Variables: RIOT_API_KEY, JWT_SECRET, etc.
  └── Healthcheck: curl https://localhost:443/actuator/health
```

---

## 🔐 Acceso a la Aplicación

### URL de Acceso

Una vez iniciada la aplicación, accede en:

**https://localhost:443**

**Nota**: Como usa un certificado SSL autofirmado, el navegador mostrará advertencia de seguridad. Es normal en desarrollo:
- Chrome: Clic en "Avanzado" → "Acceder a localhost (sitio no seguro)"
- Firefox: "Avanzado" → "Aceptar el riesgo y continuar"

### Credenciales de Acceso

#### Usuario de prueba (Usuario Registrado)
```
Usuario: testuser
Contraseña: password
```

#### Administrador
```
Usuario: admin
Contraseña: admin
```

---

## 🧪 Datos de Ejemplo

La aplicación se inicializa con datos de ejemplo al arrancar por primera vez:

### Usuarios Preconfigurados

| Usuario | Rol | Contraseña | Descripción |
|---------|-----|------------|-------------|
| `admin` | ADMIN | `admin` | Acceso total al sistema, panel de administración |
| `testuser` | USER | `password` | Usuario estándar para pruebas |

### Invocadores de Ejemplo

Puedes buscar invocadores reales de League of Legends. Ejemplos para probar:

```
Player#EUW    (formato correcto con #)
Faker#KR1     
G2Caps#EUW
```

**Nota**: Los invocadores deben existir en la región EUW (Europe West) ya que la configuración por defecto usa esta región.

### Funcionalidades para Probar

1. **Usuario anónimo**:
   - Buscar invocador en la página principal
   - Ver perfil con rango y estadísticas
   - Explorar historial de partidas
   - Ver top campeones

2. **Usuario registrado** (login con `testuser`/`password`):
   - Acceder al dashboard personal
   - Guardar favoritos (en desarrollo)
   - Ver búsquedas recientes

3. **Administrador** (login con `admin`/`admin`):
   - Acceder al panel de administración
   - Listar usuarios
   - Activar/desactivar usuarios
   - Eliminar usuarios de prueba

---

## 🛠️ Comandos Útiles

### Ver Logs

```bash
# Logs de todos los contenedores
docker-compose logs -f

# Solo logs de la aplicación
docker-compose logs -f app

# Solo logs de MySQL
docker-compose logs -f mysql

# Últimas 100 líneas
docker-compose logs --tail=100 app
```

### Estado de los Contenedores

```bash
# Ver estado
docker-compose ps

# Verificar healthchecks
docker inspect spiritblade-app | grep -A 5 Health
```

### Detener y Reiniciar

```bash
# Detener (mantiene datos)
docker-compose stop

# Reiniciar
docker-compose restart

# Detener y eliminar contenedores (mantiene volúmenes)
docker-compose down

# Eliminar todo incluyendo volúmenes (PERDERÁS LOS DATOS)
docker-compose down -v
```

### Actualizar a Nueva Versión

```bash
# Descargar nueva imagen
docker-compose pull

# Recrear contenedores con nueva imagen
docker-compose up -d

# Verificar versión
docker-compose exec app java -jar /app/app.jar --version
```

---

## 🔍 Verificación de Funcionamiento

### Health Check del Backend

```bash
# Con curl (aceptar certificado autofirmado)
curl -k https://localhost:443/actuator/health

# Debería responder:
# {"status":"UP"}
```

### Verificar Base de Datos

```bash
# Conectar a MySQL desde el contenedor
docker-compose exec mysql mysql -u spiritblade_user -p spiritblade_db

# Dentro de MySQL, verificar tablas:
SHOW TABLES;
SELECT COUNT(*) FROM USERS;
```

### Verificar Logs de Errores

```bash
# Buscar errores en logs
docker-compose logs app | grep ERROR
docker-compose logs app | grep WARN
```

---

## 🐛 Solución de Problemas

### Puerto 443 Ocupado

Si el puerto 443 está ocupado (por Apache, nginx, etc.):

```yaml
# Editar docker-compose.yml
services:
  app:
    ports:
      - "8443:443"  # Cambiar puerto externo a 8443
```

Acceder en: https://localhost:8443

### MySQL No Responde

```bash
# Verificar healthcheck
docker-compose ps

# Si mysql está unhealthy, reiniciar
docker-compose restart mysql

# Verificar logs
docker-compose logs mysql
```

### Error de API Key de Riot

Si ves errores relacionados con Riot API:

```
ERROR: Riot API error: 401 Unauthorized
ERROR: Riot API error: 403 Forbidden
```

**Soluciones**:
1. Verificar que `RIOT_API_KEY` en `.env` es correcta
2. Regenerar Development API Key (expira cada 24h)
3. Verificar límites de rate (20 req/s, 100 req/2min)

### Aplicación No Arranca

```bash
# Ver logs detallados
docker-compose logs app

# Errores comunes:
# - JWT_SECRET demasiado corto: usar mínimo 256 bits (32 caracteres)
# - MySQL no disponible: esperar a que healthcheck pase
# - Variables de entorno faltantes: revisar .env
```

### Certificado SSL No Confiable

**Esto es normal en desarrollo.** El certificado es autofirmado.

**Para producción**, reemplazar con certificado de CA (Let's Encrypt):

```bash
# Generar certificado con Let's Encrypt (en servidor con dominio)
certbot certonly --standalone -d tudominio.com

# Convertir a JKS
openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out keystore.p12
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 \
  -destkeystore keystore.jks
```

---

## 📊 Monitorización

### Endpoints de Actuator

Spring Boot Actuator expone endpoints de monitorización:

```bash
# Health (público)
curl -k https://localhost:443/actuator/health

# Info (requiere autenticación)
curl -k -H "Authorization: Bearer <token>" \
  https://localhost:443/actuator/info

# Metrics (requiere autenticación ADMIN)
curl -k -H "Authorization: Bearer <admin-token>" \
  https://localhost:443/actuator/metrics
```

---

## 🌐 Despliegue en Servidor Remoto

### Requisitos del Servidor

- OS: Ubuntu 20.04+ / Debian 11+ / RHEL 8+
- RAM: Mínimo 2GB (recomendado 4GB)
- Disco: 10GB libres
- Docker y Docker Compose instalados
- Puertos abiertos: 443 (HTTPS), 3306 (MySQL, opcional)

### Pasos de Despliegue

```bash
# 1. Conectar al servidor
ssh user@tu-servidor.com

# 2. Instalar Docker (Ubuntu example)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 3. Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 4. Clonar o copiar archivos
mkdir /opt/spiritblade
cd /opt/spiritblade
curl -O https://raw.githubusercontent.com/codeurjc-students/2025-SPIRITBLADE/main/docker/docker-compose.yml

# 5. Configurar .env (usar variables seguras en producción)
nano .env

# 6. Iniciar aplicación
docker-compose up -d

# 7. Configurar firewall
sudo ufw allow 443/tcp
sudo ufw enable
```

---

## 📖 Documentación Adicional

- **[Guía de Desarrollo](Guia-Desarrollo.md)** - Para desarrolladores que quieren modificar el código
- **[API REST](API.md)** - Documentación de endpoints
- **[Docker README](../docker/README.md)** - Información técnica del Dockerfile

---

**[← Volver al README principal](../README.md)**
