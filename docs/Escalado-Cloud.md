# Documentación de Fase 3: Escalabilidad y Alta Disponibilidad

## 1. Estrategia de Escalado (Free Tier Friendly)

El objetivo de esta fase es implementar mecanismos de autoescalado que respondan a picos de tráfico, manteniendo estrictamente los costes en **CERO (Oracle Cloud Always Free Tier)**.

### 1.1 Escalado de Aplicación (Horizontal Pod Autoscaler - HPA)
**Implementado**: Sí (`k8s/prod/hpa.yaml`).
**Mecanismo**: Kubernetes escala el número de Pods (réplicas) de Backend y Frontend basándose en el uso de CPU y memoria.

**Configuración Backend HPA**:
- **Replicas**: Min 1, Max 3
- **Métricas**:
  - CPU: 70% average utilization
  - Memoria: 80% average utilization
- **Comportamiento de escalado**:
  - Scale Down: Estabilización 300s, máximo 50% reducción por ciclo
  - Scale Up: Estabilización 60s, máximo 100% incremento por ciclo

**Configuración Frontend HPA**:
- **Replicas**: Min 1, Max 3
- **Métricas**:
  - CPU: 70% average utilization
- **Comportamiento de escalado**:
  - Scale Down: Estabilización 300s
  - Scale Up: Estabilización 30s, máximo 2 pods por ciclo

**Requisitos**:
- `metrics-server` instalado y funcionando (`kubectl get deployment metrics-server -n kube-system`)
- Pods con `resources.requests` definidos (CPU y memoria)

**Verificación**:
```bash
# Ver estado del HPA
kubectl get hpa -n prod

# Salida esperada:
NAME           REFERENCE             TARGETS         MINPODS   MAXPODS   REPLICAS
backend-hpa    Deployment/backend    4%/70%, 87%/80%  1         3         1
frontend-hpa   Deployment/frontend   2%/70%           1         3         1

# Ver métricas en tiempo real
kubectl top pods -n prod
```

### 1.2 Escalado de Infraestructura (Cluster Autoscaler)
**Estado**: **Implementado y Activo**.

Se ha implementado el **Cluster Autoscaler** para gestionar dinámicamente el tamaño del cluster, respetando el límite estricto de 4 OCPUs del Free Tier.

- **Componente**: `cluster-autoscaler` (v1.31.0) ejecutándose en `kube-system`.
- **Configuración**: Configurado para escalar un Node Pool específico de OKE.
- **Límite**: Configurado con `max-nodes=3` (3 OCPUs en K8s + 1 OCPU en DB = 4 OCPUs Límite).

**Estrategia "On-Demand"**:
- **Estado Inicial**: 2 Nodos (2 OCPUs totales).
- **Disparador**: Cuando el **HPA** solicita nuevos Pods y no caben en los 2 nodos existentes (CPU/RAM insuficiente), se quedan en estado `Pending`.
- **Acción**: El Cluster Autoscaler detecta los Pods `Pending` y solicita a la API de OCI arrancar un **3er nodo**.
- **Latency**: El arranque del nuevo nodo en OCI (Cold Start) toma aproximadamente **3-5 minutos**.

**Verificación**:
```bash
# Verificar que el pod del autoscaler está corriendo
kubectl get pods -n kube-system -l app=cluster-autoscaler

# Ver logs de actividad
kubectl logs -n kube-system -l app=cluster-autoscaler
```

**Implementación Técnica**:
1.  **Node Pool**: Identificado con OCID de OKE Resource (`ocid1.nodepool...`).
2.  **Permisos**: Policy IAM (`autoscaler-iam.tf`) vinculada al Dynamic Group de los nodos.
3.  **Despliegue**: `k8s/prod/cluster-autoscaler.yaml` con soporte OCI y volumen de certificados SSL ajustado para Oracle Linux.

Esto cumple estrictamente el requisito de demostrar escalabilidad dinámica de infraestructura.

### 1.4 Metrics Server (Componente Crítico)

**Función**: Recopila métricas de uso de recursos (CPU, memoria) de pods y nodos en tiempo real.
**Instalación**:
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

**Verificación**:
```bash
# Ver deployment
kubectl get deployment metrics-server -n kube-system

# Ver pods
kubectl get pods -n kube-system -l k8s-app=metrics-server

# Probar métricas
kubectl top nodes
kubectl top pods -n prod
```

**Importancia**: Sin Metrics Server, el HPA **NO puede funcionar** (mostrará `<unknown>` en las métricas de CPU/memoria).

---

## 2. Escalabilidad de Base de Datos (MySQL)

**Equivalencia RDS**: En AWS usaríamos RDS con Read Replicas y Multi-AZ.
**En OCI Free Tier**:
- Usamos una Instancia Compute con MySQL instalado (`iac/terraform/mysql.tf`).
- **Escalado Vertical**: Limitado a 4 OCPUs totales en el tenant. Actualmente usa 1 OCPU/6GB RAM. Se podría escalar a 2 OCPUs si reducimos los nodos de K8s, pero no es dinámico.
- **Escalado Horizontal**: No viable automáticamente en Free Tier (requiere múltiples VMs y orquestación compleja).
- **Conclusión**: Para el TFG, confiamos en la robustez de MySQL en una VM dedicada (evitando contendores K8s efímeros para la DB) y el uso agresivo de Caché (Redis).

---

## 3. Escalabilidad de Caché (Redis)

**Estrategia**: `StatefulSet`.
- Redis corre como un StatefulSet en Kubernetes.
- **Escalado**: Manual.
- **Operadores**: Descartados para Free Tier. Los operadores de Redis (ej. Spotahome) suelen requerir 3 nodos Sentinel + 3 Nodos Redis para HA real. Esto consumiría ~6 Pods adicionales, saturando la RAM (12GB total) del cluster innecesariamente.
- **Optimización**: Usamos una instancia Redis optimizada como caché LRU (Least Recently Used) para aliviar la carga de la Base de Datos.

## 4. Ingress y Gestión de Tráfico

### 4.1 NGINX Ingress Controller

**Función**: 
- Enrutamiento HTTP/HTTPS basado en dominios y paths
- Terminación TLS (certificados SSL/TLS)
- Load balancing a nivel de aplicación (L7)

**Load Balancer Público**:
- **IP Externa**: `143.47.39.213` (OCI LoadBalancer flexible, 10 Mbps)
- **Puertos**: 80 (HTTP redirect) y 443 (HTTPS)
- **Dominio**: `spiritblade.dev` → Apunta a esta IP vía DNS

**Ventajas sobre LoadBalancer directo en Service**:
- Un solo LoadBalancer para múltiples servicios (ahorro de IPs públicas)
- Gestión centralizada de certificados SSL
- Reglas avanzadas de enrutamiento (path-based, host-based)
- Renovación automática de certificados con cert-manager

**Instalación**:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml
```

### 4.2 cert-manager y Let's Encrypt

**Función**: Automatiza la emisión y renovación de certificados SSL/TLS.

**Configuración**:
- **ClusterIssuer**: `letsencrypt-prod` (usa servidor de producción de Let's Encrypt)
- **Challenge Method**: HTTP-01 (via Ingress)
- **Certificados gestionados**: `spiritblade.dev`, `www.spiritblade.dev`
- **Renovación**: Automática cada 60 días (certificados válidos 90 días)

**Instalación**:
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.2/cert-manager.yaml
```

**Verificación**:
```bash
# Ver certificados
kubectl get certificate -n prod

# Ver estado detallado
kubectl describe certificate spiritblade-tls -n prod
```

---

## 5. Pruebas de Carga (Artillery)

Se ha incluido una configuración de Artillery en `tests/load/artillery-config.yaml` para simular tráfico.

### Ejecutar Pruebas
1. Instalar Artillery: `npm install -g artillery`
2. Configurar IP objetivo en `tests/load/artillery-config.yaml`.
3. Ejecutar:
   ```bash
   artillery run tests/load/artillery-config.yaml --output report.json
   ```
4. Generar reporte HTML:
   ```bash
   artillery report report.json
   ```

### Escenario de Prueba
- **Fase 1**: Calentamiento (1-5 usuarios/seg) durante 1 minuto.
- **Fase 2**: Carga sostenida (5-10 usuarios/seg) durante 2 minutos.
- **Fase 3**: Pico de estrés (10-30 usuarios/seg) para forzar el HPA.

---

## 6. Costes y Límites

| Recurso | Configuración Actual | Límite Free Tier | Estado |
|---------|-----------------------|------------------|--------|
| **CPUs** | 3 OCPUs (1 DB + 2 K8s) | 4 OCPUs | ✅ Seguro (1 libre) |
| **RAM** | 18 GB (6 DB + 12 K8s) | 24 GB | ✅ Seguro (6 GB libres) |
| **Load Balancer** | 1 Instancia Flexible:<br/>- Ingress NGINX: 10 Mbps (`143.47.39.213`) | 1 Instancia (10 Mbps) | ✅ **Optimizado** |
| **Volúmenes** | ~160 GB (50x2 Nodos + 50 DB + 10 Redis) | 200 GB | ✅ Seguro (~40 GB libres) |
| **Ancho de Banda** | Salida pública (egress) | 10 TB/mes gratis | ✅ Seguro |
| **Certificados SSL** | Let's Encrypt (cert-manager) | Ilimitados gratis | ✅ Gratis |

**CONFIRMADO: 100% GRATUITO** ✅
- Todos los servicios (`backend`, `frontend`, `mysql-external`) usan **ClusterIP** (sin IPs públicas)
- Acceso externo **únicamente** vía Ingress Controller (`143.47.39.213`)
- Certificados SSL renovados automáticamente por cert-manager
- Metrics Server y HPA usando recursos existentes (0 coste adicional)

**Límites Críticos a NO Superar**:
1. ❌ No crear más LoadBalancers (máximo: 1, actual: 1) ✅
2. ❌ No superar 4 OCPUs ARM (actual: 3) ✅
3. ❌ No cambiar LoadBalancer shape a > 10 Mbps ✅
4. ❌ No superar 200 GB en volúmenes (actual: ~160 GB) ✅

**Nota**: El `oci-load-balancer-shape` del Ingress está configurado en `flexible` con min/max 10Mbps. Subirlo a 100Mbps o 400Mbps saldría del Free Tier y generaría costes.
