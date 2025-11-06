# SPIRITBLADE - Docker Deployment

## Descripción

Aplicación web SPIRITBLADE empaquetada en Docker con:
- **Backend**: Spring Boot API REST en puerto 443 (HTTPS)
- **Frontend**: Angular servido como recurso estático desde el backend
- **Base de datos**: MySQL 8.0

## Estructura

```
docker/
├── Dockerfile              # Imagen multi-stage (Frontend + Backend)
├── docker-compose.yml      # Versión producción (tag 0.1)
├── docker-compose-dev.yml  # Versión desarrollo (tag dev)
├── .env.example            # Variables de entorno
└── ssl/
    └── keystore.p12        # Certificado SSL
```

## Requisitos Previos

- Docker 20.10+
- Docker Compose 2.0+
- JDK 21 (para generar certificado SSL)

## Configuración Inicial

### 1. Generar Certificado SSL

```bash
cd docker/ssl
keytool -genkeypair -alias spiritblade -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -storepass spiritblade \
  -validity 365 -dname "CN=localhost" \
  -ext "SAN=DNS:localhost,IP:127.0.0.1"
```

### 2. Configurar Variables de Entorno

```bash
cd docker
cp .env.example .env
# Editar .env con tus valores
```

**Variables requeridas:**
- `DOCKER_USERNAME`: Tu usuario de DockerHub
- `RIOT_API_KEY`: Tu API key de Riot Games
- `MYSQL_PASSWORD`: Contraseña de MySQL
- `JWT_SECRET`: Clave secreta para JWT

## Construcción de la Imagen

```bash
# Desde la raíz del proyecto
docker build -f docker/Dockerfile -t yourusername/spiritblade:0.1 .

# Tag para desarrollo
docker tag yourusername/spiritblade:0.1 yourusername/spiritblade:dev
```

## Ejecución

### Producción (tag 0.1)

```bash
cd docker
docker-compose up -d
```

Acceder en: **https://localhost/**

### Desarrollo (tag dev)

```bash
cd docker
docker-compose -f docker-compose-dev.yml up -d
```

Acceder en: **https://localhost:8443/**

## Detener la Aplicación

```bash
docker-compose down
```

## Publicar en DockerHub

```bash
docker login
docker push yourusername/spiritblade:0.1
docker push yourusername/spiritblade:dev
```

## Verificación

```bash
# Ver logs
docker-compose logs -f

# Health check
curl -k https://localhost/actuator/health

# Estado de servicios
docker-compose ps
```

## Características Técnicas

### Dockerfile Multi-Stage

1. **Stage 1**: Build Angular (Node 20)
2. **Stage 2**: Build Spring Boot + copiar Angular a recursos estáticos
3. **Stage 3**: Runtime JRE 21 con healthcheck

### Docker Compose

- **MySQL**: 
  - Healthcheck con mysqladmin ping
  - Volumen persistente para datos
  
- **App**: 
  - Espera a MySQL (depends_on con condition: service_healthy)
  - Variables de entorno para configuración
  - HTTPS en puerto 443
  - Certificado SSL montado como volumen

## Troubleshooting

### Puerto 443 ocupado
Cambiar el puerto en docker-compose.yml:
```yaml
ports:
  - "8443:443"
```

### Regenerar certificado SSL
```bash
cd docker/ssl
rm keystore.p12
# Ejecutar comando keytool de nuevo
```

### Ver logs de errores
```bash
docker-compose logs app | grep ERROR
docker-compose logs mysql
```

### Base de datos no responde
```bash
docker exec spiritblade-mysql mysqladmin ping -h localhost
docker-compose restart mysql
```

## Notas

- El certificado SSL es auto-firmado para desarrollo. Para producción, usar certificado de CA.
- No commitear el archivo `.env` ni `keystore.p12` a Git.
- El archivo `.gitignore` ya está configurado para excluir estos archivos.
