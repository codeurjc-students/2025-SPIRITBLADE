# ==============================================================================
# SPIRITBLADE - Object Storage (S3-compatible) Configuration
# ==============================================================================

# Bucket para almacenar archivos de la aplicación
resource "oci_objectstorage_bucket" "bucket" {
  compartment_id = var.compartment_ocid
  name           = "${var.project_name}-bucket"
  namespace      = data.oci_objectstorage_namespace.ns.namespace
  
  # Sin acceso público (acceso solo mediante API)
  access_type = "NoPublicAccess"
  
  # Tier estándar (hay también Archive para backups)
  storage_tier = "Standard"
  
  # Versionado de objetos (útil para rollback)
  versioning = "Enabled"
  
  # Auto-tiering para optimizar costos
  auto_tiering = "InfrequentAccess"
}

# Customer Secret Key para acceso S3-compatible
resource "oci_identity_customer_secret_key" "s3_credentials" {
  display_name = "${var.project_name}-s3-key"
  user_id      = var.user_ocid
}

# Data source para obtener el namespace
data "oci_objectstorage_namespace" "ns" {
  compartment_id = var.compartment_ocid
}

# Variables necesarias
variable "oci_region_shortcode" {
  description = "Código corto de la región (ej: fra para Frankfurt, iad para Ashburn)"
  type        = string
  default     = "fra"
}

# Outputs
output "object_storage_namespace" {
  description = "Namespace del Object Storage"
  value       = data.oci_objectstorage_namespace.ns.namespace
}

output "object_storage_bucket_name" {
  description = "Nombre del bucket"
  value       = oci_objectstorage_bucket.bucket.name
}

output "s3_compatible_endpoint" {
  description = "Endpoint S3-compatible"
  value       = "https://${data.oci_objectstorage_namespace.ns.namespace}.compat.objectstorage.${var.region}.oraclecloud.com"
}

output "s3_access_key" {
  description = "Access Key para S3-compatible API"
  value       = oci_identity_customer_secret_key.s3_credentials.id
  sensitive   = false
}

output "s3_secret_key" {
  description = "Secret Key para S3-compatible API"
  value       = oci_identity_customer_secret_key.s3_credentials.key
  sensitive   = true
}

output "minio_config_for_k8s" {
  description = "Configuración de MinIO para Kubernetes"
  value = <<-EOT
    MINIO_ENDPOINT=https://${data.oci_objectstorage_namespace.ns.namespace}.compat.objectstorage.${var.region}.oraclecloud.com
    MINIO_BUCKET_NAME=${oci_objectstorage_bucket.bucket.name}
    MINIO_ACCESS_KEY=${oci_identity_customer_secret_key.s3_credentials.id}
    MINIO_SECRET_KEY=${oci_identity_customer_secret_key.s3_credentials.key}
  EOT
  sensitive = true
}