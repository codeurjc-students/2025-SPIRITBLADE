# ==============================================================================
# SPIRITBLADE - Oracle Cloud Infrastructure with Terraform
# ==============================================================================

terraform {
  required_version = ">= 1.0"
  
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = ">= 5.0.0"
    }
  }
}

provider "oci" {
  tenancy_ocid     = var.tenancy_ocid
  user_ocid        = var.user_ocid
  fingerprint      = var.fingerprint
  private_key_path = var.private_key_path
  region           = var.region
}

# ==============================================================================
# Variables
# ==============================================================================

variable "tenancy_ocid" {
  description = "OCID del tenancy de Oracle Cloud"
  type        = string
}

variable "user_ocid" {
  description = "OCID del usuario"
  type        = string
}

variable "fingerprint" {
  description = "Fingerprint de la API key"
  type        = string
}

variable "private_key_path" {
  description = "Ruta a la clave privada de la API"
  type        = string
}

variable "region" {
  description = "Región de Oracle Cloud (ej: eu-frankfurt-1, us-ashburn-1)"
  type        = string
  default     = "eu-frankfurt-1"
}

variable "compartment_ocid" {
  description = "OCID del compartment donde crear los recursos"
  type        = string
}

variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "spiritblade"
}

variable "vcn_cidr" {
  description = "CIDR block para la VCN"
  type        = string
  default     = "10.0.0.0/16"
}

variable "node_image_id" {
  description = "OCID de la imagen para los nodos de OKE (Oracle Linux 8 ARM64)"
  type        = string
  # Este valor debe obtenerse para tu región específica
  # Busca: "Oracle-Linux-8.x-aarch64-*" en la consola de OCI
}

variable "public_key_path" {
  description = "Ruta a la clave SSH pública para acceso a instancias"
  type        = string
}
