# Este archivo define la Policy necesaria para que el Cluster Autoscaler
# pueda gestionar el Instance Pool de OCI automáticamente.

resource "oci_identity_dynamic_group" "oke_nodes" {
  compartment_id = var.tenancy_ocid
  name           = "${var.project_name}-oke-nodes"
  description    = "Dynamic Group para nodos del cluster OKE"
  matching_rule  = "ALL {instance.compartment.id = '${var.compartment_ocid}'}"
}

resource "oci_identity_policy" "autoscaler_policy" {
  compartment_id = var.compartment_ocid
  name           = "${var.project_name}-autoscaler-policy"
  description    = "Permitir a los nodos escalar el instance pool"
  
  statements = [
    "Allow dynamic-group ${oci_identity_dynamic_group.oke_nodes.name} to manage instance-pools in compartment id ${var.compartment_ocid}",
    "Allow dynamic-group ${oci_identity_dynamic_group.oke_nodes.name} to manage instance-configurations in compartment id ${var.compartment_ocid}",
    "Allow dynamic-group ${oci_identity_dynamic_group.oke_nodes.name} to inspect instances in compartment id ${var.compartment_ocid}"
  ]
}
