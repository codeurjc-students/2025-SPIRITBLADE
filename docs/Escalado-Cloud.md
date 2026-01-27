# Documentación de Fase 3: Escalabilidad y Alta Disponibilidad

## 1. Estrategia de Escalado (Free Tier Friendly)

El objetivo de esta fase es implementar mecanismos de autoescalado que respondan a picos de tráfico, manteniendo estrictamente los costes en **CERO (Oracle Cloud Always Free Tier)**.

### 1.1 Escalado de Aplicación (Horizontal Pod Autoscaler - HPA)
**Implementado**: Sí (`k8s/prod/hpa.yaml`).
**Mecanismo**: Kubernetes escala el número de Pods (réplicas) de Backend y Frontend basándose en el uso de CPU.
- **Backend HPA**: Min 1, Max 3 réplicas. Objetivo CPU: 70%.
- **Frontend HPA**: Min 1, Max 3 réplicas. Objetivo CPU: 70%.
- **Requisito**: `metrics-server` debe estar instalado (`k8s/prod/metrics-server.yaml`).

### 1.2 Escalado de Infraestructura (Cluster Autoscaler)
**Estrategia "On-Demand" (Escalado de 2 a 3 nodos)**:

Se ha implementado el **Cluster Autoscaler** para gestionar dinámicamente el tamaño del cluster, respetando el límite estricto de 4 OCPUs del Free Tier.

- **Estado Inicial**: 2 Nodos (2 OCPUs totales).
- **Disparador**: Cuando el **HPA** solicita nuevos Pods y no caben en los 2 nodos existentes (CPU/RAM insuficiente), se quedan en estado `Pending`.
- **Acción**: El Cluster Autoscaler detecta los Pods `Pending` y solicita a la API de OCI arrancar un **3er nodo**.
- **Límite**: Configurado con `max-nodes=3` (3 OCPUs en K8s + 1 OCPU en DB = 4 OCPUs Límite).
- **Latency**: El arranque del nuevo nodo en OCI (Cold Start) toma aproximadamente **3-5 minutos**.

**Implementación Técnica**:
1.  **OCI Instance Pool**: Gestionado por Terraform con `size=2` inicial.
2.  **Permisos**: Se añade una `InstancePrincipal` o `WorkloadIdentity` para que el cluster tenga permisos `manage instance-pools` en el compartimento.
3.  **Componente K8s**: Desplegamos el pod `cluster-autoscaler` oficial configurado para OCI.
    *   *Nota: En OKE Basic, esto requiere instalación manual del deployment yaml, ya que no es un checkbox en la consola.*

Esto cumple estrictamente el requisito de demostrar escalabilidad dinámica de infraestructura.

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

---

## 4. Pruebas de Carga (Artillery)

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

## 5. Costes y Límites

| Recurso | Configuración Actual | Límite Free Tier | Estado |
|---------|-----------------------|------------------|--------|
| **CPUs** | 3 OCPUs (1 DB + 2 K8s) | 4 OCPUs | ✅ Seguro (1 libre) |
| **RAM** | 18 GB (6 DB + 12 K8s) | 24 GB | ✅ Seguro (6 GB libres) |
| **Load Balancer** | 1 Instancia Flexible (10Mbps) | 1 Instancia | ✅ Seguro |
| **Volúmenes** | ~150 GB (50x2 Nodos + 50 DB) | 200 GB | ✅ Seguro (~50 GB libres) |

**IMPORTANTE**: No modificar el `oci-load-balancer-shape` en `frontend-deployment.yaml`. Mantener `flexible` con min/max 10Mbps. Subirlo a 100Mbps o 400Mbps podría incurrir en costes si se supera la capa gratuita de ancho de banda o instancias.
