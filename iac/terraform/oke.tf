# ==============================================================================
# SPIRITBLADE - OKE (Oracle Kubernetes Engine) Configuration
# ==============================================================================

resource "oci_containerengine_cluster" "k8s_cluster" {
  compartment_id     = var.compartment_ocid
  kubernetes_version = "v1.31.1" # Versión soportada en eu-madrid-1
  name               = "${var.project_name}-cluster"
  vcn_id             = oci_core_vcn.main.id

  endpoint_config {
    is_public_ip_enabled = true
    subnet_id            = oci_core_subnet.k8s_api.id
  }

  options {
    add_ons {
      is_kubernetes_dashboard_enabled = false
      is_tiller_enabled               = false
    }
    
    # Subnet para los Load Balancers de los servicios
    service_lb_subnet_ids = [oci_core_subnet.public.id]
    
    # Configuración de red del cluster
    kubernetes_network_config {
      pods_cidr     = "10.244.0.0/16"
      services_cidr = "10.96.0.0/16"
    }
  }
}

resource "oci_containerengine_node_pool" "node_pool" {
  cluster_id         = oci_containerengine_cluster.k8s_cluster.id
  compartment_id     = var.compartment_ocid
  kubernetes_version = "v1.31.1"
  name               = "${var.project_name}-node-pool"
  
  # VM.Standard.A1.Flex es ARM Ampere (Free Tier: 4 OCPUs y 24GB RAM total)
  node_shape = "VM.Standard.A1.Flex"

  node_config_details {
    placement_configs {
      availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
      subnet_id           = oci_core_subnet.nodes.id
    }
    
    # 2 nodos para alta disponibilidad
    size = 2
    
    # Configuración de Node Security Group
    nsg_ids = [oci_core_network_security_group.node_nsg.id]
  }

  node_shape_config {
    memory_in_gbs = 6  # 6GB por nodo (12GB total)
    ocpus         = 1  # 1 OCPU = 2 vCPUs por nodo (2 OCPUs total)
  }

  node_source_details {
    source_type = "IMAGE"
    image_id    = local.oke_image_id
    
    # Tamaño del disco de boot
    boot_volume_size_in_gbs = 50
  }

  initial_node_labels {
    key   = "environment"
    value = "production"
  }

  initial_node_labels {
    key   = "project"
    value = var.project_name
  }

  # SSH key para acceso a los nodos (debugging)
  ssh_public_key = file(var.public_key_path)
}

# Network Security Group para los nodos
resource "oci_core_network_security_group" "node_nsg" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-node-nsg"
}

# Permitir tráfico entre nodos
resource "oci_core_network_security_group_security_rule" "node_to_node" {
  network_security_group_id = oci_core_network_security_group.node_nsg.id
  direction                 = "INGRESS"
  protocol                  = "all"
  source                    = oci_core_network_security_group.node_nsg.id
  source_type               = "NETWORK_SECURITY_GROUP"
}

# Permitir tráfico desde el API server
resource "oci_core_network_security_group_security_rule" "api_to_node" {
  network_security_group_id = oci_core_network_security_group.node_nsg.id
  direction                 = "INGRESS"
  protocol                  = "6" # TCP
  source                    = oci_core_subnet.k8s_api.cidr_block
  source_type               = "CIDR_BLOCK"
}

# Permitir ICMP para health checks
resource "oci_core_network_security_group_security_rule" "node_icmp" {
  network_security_group_id = oci_core_network_security_group.node_nsg.id
  direction                 = "INGRESS"
  protocol                  = "1" # ICMP
  source                    = var.vcn_cidr
  source_type               = "CIDR_BLOCK"
}

# CRÍTICO: Permitir EGRESS a Internet para registro de nodos
resource "oci_core_network_security_group_security_rule" "node_egress_internet" {
  network_security_group_id = oci_core_network_security_group.node_nsg.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
}

# CRÍTICO: Permitir tráfico de nodos al API server
resource "oci_core_network_security_group_security_rule" "node_to_api" {
  network_security_group_id = oci_core_network_security_group.node_nsg.id
  direction                 = "EGRESS"
  protocol                  = "6" # TCP
  destination               = oci_core_subnet.k8s_api.cidr_block
  destination_type          = "CIDR_BLOCK"
  
  tcp_options {
    destination_port_range {
      min = 6443
      max = 6443
    }
  }
}

# Data source para obtener availability domains
data "oci_identity_availability_domains" "ads" {
  compartment_id = var.compartment_ocid
}

# Output para el comando de configuración de kubectl
output "kubeconfig_setup" {
  description = "Comandos para configurar kubectl"
  value = <<-EOT
    # 1. Instalar OCI CLI si no lo tienes:
    #    https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm
    
    # 2. Configurar el kubeconfig:
    oci ce cluster create-kubeconfig \
      --cluster-id ${oci_containerengine_cluster.k8s_cluster.id} \
      --file $HOME/.kube/config \
      --region ${var.region} \
      --token-version 2.0.0 \
      --kube-endpoint PUBLIC_ENDPOINT
    
    # 3. Verificar conexión:
    kubectl get nodes
  EOT
}
