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

2) Crea un archivo `.env` junto a `docker-compose.yml` con las variables de entorno requeridas (basarse en .env.example):


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

- spiritblade-Minio (Almacenamiento de objetos compatible con S3)
  - puerto interno: 9000
  - volumen persistente: spiritblade_minio_data

---

## Acceder a la aplicación

URL: https://localhost:443

Como la configuración de desarrollo usa un certificado autofirmado necesitarás aceptar la advertencia del navegador:

- Chrome: Advanced → Proceed to localhost (unsafe)
- Firefox: Advanced → Accept the risk and continue

### Credenciales de prueba (usuarios de ejemplo preconfigurados)

Revisar docs/Credenciales.md para más detalles.

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

## Documentación adicional

- [Guía de desarrollo](Guia-Desarrollo.md)
- [Despliegue en Kubernetes (Nube Gratuita)](Despliegue-Kubernetes.md)
- [API REST](API.md)

