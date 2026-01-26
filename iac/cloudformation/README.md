# AWS Infrastructure (CloudFormation)

This directory contains CloudFormation templates for provisioning the AWS infrastructure for SPIRITBLADE.

## Templates

1.  **`network.yaml`**: Creates the VPC, Subnets (Public/Private), Internet Gateway, and NAT Gateway.
2.  **`eks-cluster.yaml`**: Creates the EKS Cluster and Node Group. Depends on `network.yaml`.
3.  **`rds.yaml`**: Creates the RDS MySQL instance. Depends on `network.yaml`.
4.  **`s3.yaml`**: Creates the S3 bucket for object storage.

## Deployment Order

Deploy the stacks in the following order:

1.  **Network Stack:**
    ```bash
    aws cloudformation create-stack --stack-name spiritblade-network --template-body file://network.yaml --parameters ParameterKey=EnvironmentName,ParameterValue=spiritblade-dev
    ```

2.  **EKS Cluster Stack:**
    ```bash
    aws cloudformation create-stack --stack-name spiritblade-eks --template-body file://eks-cluster.yaml --capabilities CAPABILITY_NAMED_IAM --parameters ParameterKey=EnvironmentName,ParameterValue=spiritblade-dev
    ```

3.  **RDS Stack:**
    ```bash
    aws cloudformation create-stack --stack-name spiritblade-rds --template-body file://rds.yaml --parameters ParameterKey=EnvironmentName,ParameterValue=spiritblade-dev ParameterKey=DBPassword,ParameterValue=YOUR_PASSWORD
    ```

4.  **S3 Stack:**
    ```bash
    aws cloudformation create-stack --stack-name spiritblade-s3 --template-body file://s3.yaml --parameters ParameterKey=EnvironmentName,ParameterValue=spiritblade-dev
    ```

## Cleanup
Delete the stacks in reverse order: S3 -> RDS -> EKS -> Network.
