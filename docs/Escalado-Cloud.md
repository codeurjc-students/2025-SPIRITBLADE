# Documentación de Fase 3: Escalabilidad y Alta Disponibilidad

## 1. Estrategia de Escalado (Free Tier Friendly)

El objetivo de esta fase es implementar mecanismos de autoescalado que respondan a picos de tráfico, manteniendo estrictamente los costes en **CERO (Oracle Cloud Always Free Tier)**.

### 1.1 Escalado de Aplicación (Horizontal Pod Autoscaler - HPA)
**Implementado**: Sí (`k8s/prod/hpa.yaml`).
**Mecanismo**: Kubernetes escala el número de Pods (réplicas) de Backend y Frontend basándose en el uso de CPU.

**Configuración Backend HPA**:
- **Replicas**: Min 1, Max 5
- **Métricas**:
  - CPU: 70% average utilization
- **Comportamiento de escalado**:
  - Scale Down: Estabilización 300s, máximo 50% reducción por ciclo
  - Scale Up: Estabilización 60s, máximo 100% incremento por ciclo

### 📝 Incidencia Documentada: Escalar por memoria en aplicaciones Java

Durante las pruebas de autoescalado, se detectó un problema de "falsos positivos" de carga causados por la configuración del **HPA basado en consumo de Memoria**. 

* **Contexto**: El HPA de Kubernetes calcula el uso porcentual de memoria en base al valor `requests` definido en el deployment, no del valor `limits`. Para el backend se definieron `requests.memory: 300Mi`.
* **Problema**: La JVM (Java Virtual Machine) sobre la que correo Spring Boot suele hacer una reserva agresiva de heap en su arranque (en torno a 400-450MiB). Al iniciar un Pod base o crear uno nuevo, el clúster observaba un uso promedio de 144% respecto a lo solicitado (430Mi sobre 300Mi). Esto provocaba que el HPA entrase en "pánico", disparando la creación continuada de réplicas hasta alcanzar el `Max (5)`, a pesar de que el sistema no estaba recibiendo tráfico HTTP.
* **Solución**: Se procedió a retirar la métrica de memoria del manifiesto del HPA para el backend. En aplicaciones Java, escalar únicamente por **utilización de CPU** es la buena práctica recomendada, dado que el Garbage Collector interno no devuelve la memoria RAM que "ya no usa" directamente al sistema operativo, lo que vuelve la monitorización RAM poco representativa de la carga real (Concurrency o Throughput) de la aplicación.

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
- **Límite**: Configurado con `max-nodes=4` (4 OCPUs 100% para K8s, ya que ADB usa su propia cuota independiente).

**Estrategia "On-Demand"**:
- **Estado Inicial**: 2 Nodos (2 OCPUs totales).
- **Disparador**: Cuando el **HPA** solicita nuevos Pods y no caben en los 2 nodos existentes (CPU/RAM insuficiente), se quedan en estado `Pending`.
- **Acción**: El Cluster Autoscaler detecta los Pods `Pending` y solicita a la API de OCI arrancar un **3er o 4º nodo**.
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

## 2. Escalabilidad de Base de Datos (Oracle Autonomous Database)

**Equivalencia RDS**: En AWS usaríamos RDS con Read Replicas y Multi-AZ.
**En OCI Free Tier**: Se usa **Oracle Autonomous Database (ADB) Always Free**, un servicio completamente gestionado.

- **Recurso Terraform**: `iac/terraform/adb.tf` — `is_free_tier = true`.
- **Escalado de conexiones**: ADB gestiona automáticamente el pool de conexiones. Múltiples réplicas del backend (HPA) se conectan simultáneamente sin configuración adicional.
- **Escalado de almacenamiento**: Administrado por OCI hasta 20 GB. Sin discos que gestionar manualmente.
- **Escalado de cómputo**: ADB puede ajustar ECPUs automáticamente según la carga (gestionado por OCI, no manual).
- **Alta disponibilidad**: OCI garantiza el SLA del servicio ADB. No hay riesgo de pérdida de datos por fallo de VM.
- **Cuota**: Usa cuota ADB independiente — **no consume OCPUs ARM**, permitiendo dedicarlos íntegramente al cluster K8s.
- **Conclusión**: ADB elimina la complejidad operativa de gestionar MySQL en una VM dedicada y mejora la resiliencia de la base de datos.

---

## 3. Escalabilidad de Caché (Redis)

**Estrategia**: `Deployment` (In-Memory).
- **Cambio**: Se migró de `StatefulSet` a `Deployment` para eliminar la dependencia de discos persistentes (Block Volumes).
- **Almacenamiento**: Uso de RAM (`emptyDir`) en lugar de disco.
    - **Ahorro**: 50 GB de almacenamiento persistente liberados.
    - **Coste**: 0€ (Usa la RAM ya asignada a los nodos).
- **Escalado**: Manual.
- **Optimización**: Usamos una instancia Redis optimizada como caché LRU (Least Recently Used).

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
| **CPUs** | 2-4 OCPUs (K8s) | 4 OCPUs | ✅ Seguro (100% Uso) |
| **RAM** | 12-24 GB (K8s) | 24 GB | ✅ Seguro (100% Uso) |
| **Load Balancer** | 1 Instancia Flexible:<br/>- Ingress NGINX: 10 Mbps (`143.47.39.213`) | 1 Instancia (10 Mbps) | ✅ **Optimizado** |
| **Volúmenes** | **150-200 GB** (50 GB x 3-4 Nodos) | 200 GB | ⚠️ **MAX con 4 nodos** |
| **ADB Storage** | 20 GB (cuota independiente) | 20 GB (ADB Always Free) | ✅ Cuota propia |
| **Ancho de Banda** | Salida pública (egress) | 10 TB/mes gratis | ✅ Seguro |
| **Certificados SSL** | Let's Encrypt (cert-manager) | Ilimitados gratis | ✅ Gratis |

**ESTADO ACTUAL: 100% GRATUITO** ✅
- **Redis**: `emptyDir` (RAM, sin disco).
- **ADB**: Cuota independiente, no compite con OCPUs/RAM/disco de K8s.
- **Autoscaler**: Activo (Min 2, Max 4 nodos).
    - Estado normal (2 Nodos): 100 GB consumo total de bloques.
    - Estado escalado (4 Nodos): 200 GB consumo total (Límite exacto).

**Límites Críticos a NO Superar**:
1. ❌ No crear más LoadBalancers (máximo: 1).
2. ❌ No superar 4 OCPUs en instancias Compute (ADB no cuenta).
3. ❌ **No crear PVCs (Volúmenes Persistentes) adicionales**: Con 4 nodos activos se alcanza el límite de 200 GB. Cualquier volumen extra causará fallo al arrancar nuevos nodos.

**Aviso**: Con el Cluster Autoscaler al máximo (4 nodos), se consumen exactamente los 200 GB de Block Volumes disponibles. No crear ningún PVC adicional.
