# ==============================================================================
# SPIRITBLADE - Image Data Sources
# ==============================================================================

# Buscar la imagen más reciente de Oracle Linux 8 ARM64 para compute general
data "oci_core_images" "ol8_arm_compute" {
  compartment_id           = var.compartment_ocid
  operating_system         = "Oracle Linux"
  operating_system_version = "8"
  shape                    = "VM.Standard.A1.Flex"
  sort_by                  = "TIMECREATED"
  sort_order               = "DESC"
  state                    = "AVAILABLE"
}

# Buscar imagen de OKE (Oracle Kubernetes Engine) para node pools
data "oci_containerengine_node_pool_option" "oke_options" {
  node_pool_option_id = oci_containerengine_cluster.k8s_cluster.id
  compartment_id      = var.compartment_ocid
}

# Buscar imágenes OKE que soporten ARM64 y K8s v1.31.1
locals {
  # Filtrar imágenes OKE compatibles con ARM y v1.31.1
  oke_arm_images = [
    for source in data.oci_containerengine_node_pool_option.oke_options.sources :
    source if can(regex("Oracle-Linux.*aarch64.*OKE-1\\.31\\.1", source.source_name))
  ]
  
  # Seleccionar la imagen más reciente
  oke_image_id = length(local.oke_arm_images) > 0 ? local.oke_arm_images[0].image_id : ""
}

# Salida para debug
output "available_ol8_compute_images" {
  value = data.oci_core_images.ol8_arm_compute.images[*].display_name
  description = "Imágenes Oracle Linux 8 ARM64 disponibles para compute"
}

output "selected_compute_image_id" {
  value = data.oci_core_images.ol8_arm_compute.images[0].id
  description = "ID de la imagen seleccionada para MySQL"
}

output "oke_image_options" {
  value = [
    for source in data.oci_containerengine_node_pool_option.oke_options.sources : 
    source.source_name if can(regex("Oracle-Linux.*aarch64", source.source_name))
  ]
  description = "Imágenes OKE disponibles para ARM64"
}

output "selected_oke_image_id" {
  value = local.oke_image_id
  description = "ID de imagen OKE seleccionada para node pool"
}
