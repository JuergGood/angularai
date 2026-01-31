# AWS Infrastructure Execution History (Internal)

This document contains the exact commands and resource IDs used to provision the H2 file storage infrastructure on AWS. **DO NOT COMMIT THIS FILE TO PUBLIC REPOSITORIES.**

*Note: All commands below assume `AWS_PAGER=""` is set to avoid interactive prompts.*

## 1. Environment Details

- **Region**: `eu-central-1`
- **VPC ID**: `vpc-0f288e78b7dfe61bc`
- **Backend Security Group ID**: `sg-0c9274d41b1f6ec9f` (Used as source for EFS ingress)
- **Subnets**: `subnet-0ab34e3ceb5b21d96`, `subnet-0b7ab9fba1f3b1459`

## 2. Execution Log

### 2.1 Security Group Setup

```bash
# Create Security Group for EFS
# Result: sg-0247eea5b0a04710a
aws ec2 create-security-group \
  --group-name angularai-efs-sg \
  --description "Security group for EFS access from backend" \
  --vpc-id vpc-0f288e78b7dfe61bc \
  --region eu-central-1 \
  --query 'GroupId' --output text

# Authorize NFS ingress from backend security group
aws ec2 authorize-security-group-ingress \
  --group-id sg-0247eea5b0a04710a \
  --protocol tcp \
  --port 2049 \
  --source-group sg-0c9274d41b1f6ec9f \
  --region eu-central-1
```

### 2.2 EFS Creation

```bash
# Create EFS File System
# Result: fs-0222ebe39fb56812f
aws efs create-file-system \
  --creation-token angularai-h2-data \
  --region eu-central-1 \
  --query 'FileSystemId' --output text

# Create EFS Access Point
# Result: fsap-0ee89669413efa3b4
aws efs create-access-point \
  --file-system-id fs-0222ebe39fb56812f \
  --posix-user Uid=1000,Gid=1000 \
  --root-directory "Path=/h2-data,CreationInfo={OwnerUid=1000,OwnerGid=1000,Permissions=755}" \
  --region eu-central-1 \
  --query 'AccessPointId' --output text
```

### 2.3 Mount Target Creation

```bash
# Subnet 1
aws efs create-mount-target \
  --file-system-id fs-0222ebe39fb56812f \
  --subnet-id subnet-0ab34e3ceb5b21d96 \
  --security-groups sg-0247eea5b0a04710a \
  --region eu-central-1

# Subnet 2
aws efs create-mount-target \
  --file-system-id fs-0222ebe39fb56812f \
  --subnet-id subnet-0b7ab9fba1f3b1459 \
  --security-groups sg-0247eea5b0a04710a \
  --region eu-central-1
```

### 2.4 Task Registration

```bash
aws ecs register-task-definition \
  --cli-input-json file://deploy/aws/backend-task-definition.json \
  --region eu-central-1

aws ecs register-task-definition \
  --cli-input-json file://deploy/aws/backend-test-task-definition.json \
  --region eu-central-1
```
