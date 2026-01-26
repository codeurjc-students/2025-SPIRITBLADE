# ==============================================================================
# SPIRITBLADE - Network Configuration for Oracle Cloud
# ==============================================================================

# Virtual Cloud Network (VCN)
resource "oci_core_vcn" "main" {
  cidr_block     = var.vcn_cidr
  compartment_id = var.compartment_ocid
  display_name   = "${var.project_name}-vcn"
  dns_label      = "spiritblade"
}

# Internet Gateway
resource "oci_core_internet_gateway" "main" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-igw"
  enabled        = true
}

# NAT Gateway (para nodos privados con salida a internet)
resource "oci_core_nat_gateway" "main" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-nat-gw"
}

# Service Gateway (para acceso a servicios de OCI sin internet)
resource "oci_core_service_gateway" "main" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-svc-gw"

  services {
    service_id = data.oci_core_services.all_services.services[0].id
  }
}

data "oci_core_services" "all_services" {
  filter {
    name   = "name"
    values = ["All .* Services In Oracle Services Network"]
    regex  = true
  }
}

# ==============================================================================
# Route Tables
# ==============================================================================

# Route Table para subnets públicas
resource "oci_core_route_table" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-public-rt"

  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.main.id
  }
}

# Route Table para subnets privadas
resource "oci_core_route_table" "private" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-private-rt"

  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_nat_gateway.main.id
  }

  route_rules {
    destination       = data.oci_core_services.all_services.services[0].cidr_block
    destination_type  = "SERVICE_CIDR_BLOCK"
    network_entity_id = oci_core_service_gateway.main.id
  }
}

# ==============================================================================
# Security Lists
# ==============================================================================

# Security List para subnet pública
resource "oci_core_security_list" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-public-sl"

  # Egress: permitir todo
  egress_security_rules {
    destination = "0.0.0.0/0"
    protocol    = "all"
  }

  # Ingress: SSH
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "0.0.0.0/0"
    tcp_options {
      min = 22
      max = 22
    }
  }

  # Ingress: HTTP
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "0.0.0.0/0"
    tcp_options {
      min = 80
      max = 80
    }
  }

  # Ingress: HTTPS
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "0.0.0.0/0"
    tcp_options {
      min = 443
      max = 443
    }
  }

  # Ingress: Kubernetes NodePort range
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "0.0.0.0/0"
    tcp_options {
      min = 30000
      max = 32767
    }
  }

  # Ingress: ICMP
  ingress_security_rules {
    protocol = "1" # ICMP
    source   = "0.0.0.0/0"
  }

  # REGLA CRÍTICA: Permitir que los nodos hablen con el Kubernetes API
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "10.0.10.0/24" # El CIDR de tu subnet de nodos
    tcp_options {
      min = 6443
      max = 6443
    }
    description = "Permitir registro de nodos al API Server"
  }

  # REGLA ADICIONAL: Puerto para salud del cluster (Kubelet)
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "10.0.10.0/24"
    tcp_options {
      min = 12250
      max = 12250
    }
    description = "Tráfico de control OKE"
  }

  ingress_security_rules {
    protocol    = "6" # TCP
    source      = "0.0.0.0/0" # Permite el acceso desde cualquier lugar (incluida tu casa)
    source_type = "CIDR_BLOCK"
    tcp_options {
      min = 6443
      max = 6443
    }
    description = "Acceso externo al API Server de Kubernetes para kubectl"
  }
}

# Security List para subnet privada
resource "oci_core_security_list" "private" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-private-sl"

  # Egress: permitir todo
  egress_security_rules {
    destination = "0.0.0.0/0"
    protocol    = "all"
  }

  # Ingress: desde la VCN
  ingress_security_rules {
    protocol = "all"
    source   = var.vcn_cidr
  }

  # Ingress: NodePort range desde internet (para servicios expuestos)
  ingress_security_rules {
    protocol = "6" # TCP
    source   = "0.0.0.0/0"
    tcp_options {
      min = 30000
      max = 32767
    }
  }
}

# ==============================================================================
# Subnets
# ==============================================================================

# Subnet pública para Load Balancers
resource "oci_core_subnet" "public" {
  cidr_block        = "10.0.1.0/24"
  compartment_id    = var.compartment_ocid
  vcn_id            = oci_core_vcn.main.id
  display_name      = "${var.project_name}-public-subnet"
  route_table_id    = oci_core_route_table.public.id
  security_list_ids = [oci_core_security_list.public.id]
  dns_label         = "public"
}

# Subnet para el API server de Kubernetes
resource "oci_core_subnet" "k8s_api" {
  cidr_block                 = "10.0.2.0/24"
  compartment_id             = var.compartment_ocid
  vcn_id                     = oci_core_vcn.main.id
  display_name               = "${var.project_name}-k8s-api-subnet"
  route_table_id             = oci_core_route_table.public.id
  security_list_ids          = [oci_core_security_list.public.id]
  dns_label                  = "k8sapi"
  prohibit_public_ip_on_vnic = false
}

# Subnet para los nodos de Kubernetes
resource "oci_core_subnet" "nodes" {
  cidr_block                 = "10.0.10.0/24"
  compartment_id             = var.compartment_ocid
  vcn_id                     = oci_core_vcn.main.id
  display_name               = "${var.project_name}-nodes-subnet"
  route_table_id             = oci_core_route_table.public.id # CAMBIO: Usar ruta pública para permitir NodePort externo
  security_list_ids          = [oci_core_security_list.private.id]
  dns_label                  = "nodes"
  prohibit_public_ip_on_vnic = false # Cambiar a true para producción
}

# Subnet para la base de datos
resource "oci_core_subnet" "database" {
  cidr_block                 = "10.0.20.0/24"
  compartment_id             = var.compartment_ocid
  vcn_id                     = oci_core_vcn.main.id
  display_name               = "${var.project_name}-database-subnet"
  route_table_id             = oci_core_route_table.private.id
  security_list_ids          = [oci_core_security_list.private.id]
  dns_label                  = "database"
  prohibit_public_ip_on_vnic = false # Cambiar a true después de configuración inicial
}

# ==============================================================================
# Outputs
# ==============================================================================

output "vcn_id" {
  description = "ID de la VCN"
  value       = oci_core_vcn.main.id
}

output "public_subnet_id" {
  description = "ID de la subnet pública"
  value       = oci_core_subnet.public.id
}

output "nodes_subnet_id" {
  description = "ID de la subnet de nodos"
  value       = oci_core_subnet.nodes.id
}

output "database_subnet_id" {
  description = "ID de la subnet de base de datos"
  value       = oci_core_subnet.database.id
}
