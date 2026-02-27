# ==============================================================================
# SPIRITBLADE - Oracle Autonomous Database (Always Free)
# Servicio gestionado: hasta 20 GB, escalable, sin mantenimiento de VM
# Tier gratuito permanente: 2 instancias ADB por tenancy
#
# LIMITACIÓN del Free Tier: private endpoints NO disponibles.
# Se usa endpoint público con TLS (sin wallet, mTLS desactivado).
# Acceso protegido por contraseña ADMIN (véase adb_admin_password).
# ==============================================================================

resource "oci_database_autonomous_database" "main" {
  compartment_id = var.compartment_ocid
  display_name   = "${var.project_name}-adb"

  # Nombre de la base de datos (solo alfanumérico, máx 14 chars)
  db_name = "SPIRITBLADE"

  # ============================================================
  # Always Free tier: gratis permanentemente
  # - 2 ECPUs, 20 GB almacenamiento
  # - Backup automático, parches gestionados por Oracle
  # ============================================================
  is_free_tier = true

  db_workload   = "OLTP"
  compute_model = "ECPU"
  compute_count = 2
  data_storage_size_in_gb = 20

  admin_password = var.adb_admin_password

  # Endpoint público (Free Tier no soporta private endpoint)
  # mTLS desactivado → conexión JDBC directa sin fichero wallet
  is_mtls_connection_required = false

  # Sin restricción de IPs (los pods OKE salen con IP dinámica por NAT Gateway)
  # Oracle exige una ACL cuando mTLS está desactivado: 0.0.0.0/0 = acepta todo
  # La seguridad la provee la contraseña ADMIN + TLS en tránsito obligatorio
  whitelisted_ips = ["0.0.0.0/0"]

  db_version    = "19c"
  license_model = "LICENSE_INCLUDED"

  lifecycle {
    prevent_destroy = true
  }
}

# ==============================================================================
# Outputs
# ==============================================================================

output "adb_connection_strings" {
  description = "Cadenas de conexión disponibles (low/medium/high/tp/tpurgent)"
  value       = oci_database_autonomous_database.main.connection_strings
  sensitive   = true
}

output "adb_jdbc_url" {
  description = "JDBC URL para Spring Boot (perfil TP, TLS sin wallet)"
  value       = "jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-madrid-1.oraclecloud.com))(connect_data=(service_name=ge32c8a2145bafd_spiritblade_tp.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))"
}

output "adb_ocid" {
  description = "OCID del Autonomous Database"
  value       = oci_database_autonomous_database.main.id
}

variable "adb_admin_password" {
  description = "Contraseña del usuario ADMIN del Autonomous Database (min 12 chars, mayúscula, minúscula, número y símbolo)"
  type        = string
  sensitive   = true
  validation {
    condition     = length(var.adb_admin_password) >= 12
    error_message = "La contraseña de ADB debe tener al menos 12 caracteres."
  }
}
