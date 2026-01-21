# OCI Infrastructure (Terraform) - FREE TIER EDITION

This directory contains Terraform code to provision the **Oracle Cloud Infrastructure (OCI)** resources for SPIRITBLADE, utilizing the **Always Free Tier**.

## Resources Created

1.  **VCN & Networking:** Public and Private Subnets, Internet Gateway, Security Lists.
2.  **OKE Cluster:** Oracle Container Engine for Kubernetes.
3.  **Node Pool:** Uses **Ampere A1 (ARM)** instances (VM.Standard.A1.Flex) which are free (up to 4 CPUs, 24GB RAM).
4.  **MySQL Database:** Hosted on a separate free Ampere Compute instance to avoid paid MySQL Service costs.
5.  **Object Storage:** Free Tier compatible bucket.

## Prerequisites

1.  **Terraform installed.**
2.  **OCI Account (Free Tier).**
3.  **OCI API Key Pair:** Generate this in your OCI Console (User Settings -> API Keys).
4.  **SSH Key Pair:** Generate locally (`ssh-keygen -t rsa -b 4096 -f oci_key`) for Instance access.

## Setup

1.  **Initialize Terraform:**
    ```bash
    terraform init
    ```

2.  **Create `terraform.tfvars`:**
    Create a file named `terraform.tfvars` with your OCI details:
    ```hcl
    tenancy_ocid     = "ocid1.tenancy.oc1..."
    user_ocid        = "ocid1.user.oc1..."
    fingerprint      = "xx:xx:xx..."
    private_key_path = "/path/to/your/oci_api_key.pem"
    region           = "us-ashburn-1" # Or your region
    compartment_ocid = "ocid1.compartment.oc1..."
    node_image_id    = "ocid1.image.oc1..." # Find the "Oracle Linux 8 Cloud Developer" (aarch64) OCID for your region
    public_key_path  = "/path/to/oci_key.pub"
    ```

3.  **Plan & Apply:**
    ```bash
    terraform plan
    terraform apply
    ```

## Post-Setup

1.  **Get Kubeconfig:**
    ```bash
    oci ce cluster create-kubeconfig --cluster-id <CLUSTER_ID> --file $HOME/.kube/config --region <REGION> --token-version 2.0.0 
    ```
2.  **Access MySQL:**
    SSH into the MySQL instance using its Public IP:
    ```bash
    ssh -i oci_key opc@<MYSQL_PUBLIC_IP>
    ```

## Cleanup
```bash
terraform destroy
```
