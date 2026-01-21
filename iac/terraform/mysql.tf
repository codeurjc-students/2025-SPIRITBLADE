# ==============================================================================
# SPIRITBLADE - MySQL Database Instance on OCI Compute
# ==============================================================================

# Instancia de Compute para MySQL
resource "oci_core_instance" "mysql_instance" {
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
  compartment_id      = var.compartment_ocid
  display_name        = "${var.project_name}-mysql"
  shape               = "VM.Standard.A1.Flex"

  shape_config {
    ocpus         = 1  # 1 OCPU (2 vCPUs)
    memory_in_gbs = 6  # 6GB RAM
  }

  create_vnic_details {
    subnet_id        = oci_core_subnet.database.id
    assign_public_ip = true # Para acceso inicial, puede eliminarse después
    nsg_ids          = [oci_core_network_security_group.mysql_nsg.id]
    hostname_label   = "mysql"
  }

  source_details {
    source_id   = data.oci_core_images.ol8_arm_compute.images[0].id # Oracle Linux 8 ARM64
    source_type = "image"
    
    boot_volume_size_in_gbs = 100 # Disco para el sistema y MySQL
  }

  metadata = {
    ssh_authorized_keys = file(var.public_key_path)
    
    # Script de inicialización para instalar y configurar MySQL
    user_data = base64encode(templatefile("${path.module}/mysql-init.sh", {
      mysql_root_password = var.mysql_root_password
      mysql_database      = "spiritblade"
    }))
  }

  lifecycle {
    ignore_changes = [
      source_details[0].source_id
    ]
  }
}

# Network Security Group para MySQL
resource "oci_core_network_security_group" "mysql_nsg" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-mysql-nsg"
}

# Permitir MySQL desde la VCN (acceso desde OKE)
resource "oci_core_network_security_group_security_rule" "mysql_ingress_from_vcn" {
  network_security_group_id = oci_core_network_security_group.mysql_nsg.id
  direction                 = "INGRESS"
  protocol                  = "6" # TCP
  source                    = var.vcn_cidr
  source_type               = "CIDR_BLOCK"
  
  tcp_options {
    destination_port_range {
      min = 3306
      max = 3306
    }
  }
}

# Permitir SSH para administración
resource "oci_core_network_security_group_security_rule" "mysql_ssh" {
  network_security_group_id = oci_core_network_security_group.mysql_nsg.id
  direction                 = "INGRESS"
  protocol                  = "6" # TCP
  source                    = "0.0.0.0/0" # CAMBIAR en producción a IP específica
  source_type               = "CIDR_BLOCK"
  
  tcp_options {
    destination_port_range {
      min = 22
      max = 22
    }
  }
}

# Permitir tráfico saliente
resource "oci_core_network_security_group_security_rule" "mysql_egress" {
  network_security_group_id = oci_core_network_security_group.mysql_nsg.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
}

# Variable para la contraseña de MySQL
variable "mysql_root_password" {
  description = "Contraseña root para MySQL"
  type        = string
  sensitive   = true
}

# Outputs
output "mysql_connection_string" {
  description = "String de conexión para MySQL desde Kubernetes"
  value       = "jdbc:mysql://${oci_core_instance.mysql_instance.private_ip}:3306/spiritblade?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
  sensitive   = false
}

output "mysql_private_ip" {
  description = "IP privada de MySQL"
  value       = oci_core_instance.mysql_instance.private_ip
}

output "mysql_public_ip" {
  description = "IP pública de MySQL (para administración)"
  value       = oci_core_instance.mysql_instance.public_ip
}

output "mysql_ssh_command" {
  description = "Comando SSH para conectar a MySQL"
  value       = "ssh opc@${oci_core_instance.mysql_instance.public_ip}"
}
