# Support for H2 File Storage on Local Docker and AWS Fargate

This document outlines the plan to support the `h2-file` Spring profile for persistent storage in both local Docker environments and AWS Fargate.

## 1. Local Docker Support

To support `h2-file` locally, we need to map a host directory to the container's data directory and set the active profile.

### 1.1 Update `docker-compose.yml`

Modify the `backend` service to include a volume and the correct profile:

```yaml
services:
  backend:
    # ... existing config ...
    environment:
      - SPRING_PROFILES_ACTIVE=h2-file
      # ... other env vars ...
    volumes:
      - ./data:/app/data
```

*Note: Ensure the `./data` directory exists on the host or is automatically created by Docker.*

## 2. AWS Fargate Support

Fargate tasks are ephemeral. For the `h2-file` profile to be useful, we must use Amazon EFS (Elastic File System) to persist the database file across task restarts.

### 2.1 AWS Infrastructure Setup (EFS)

The following steps must be performed via AWS Console or CLI:

1.  **Create an EFS File System**: In the same VPC as the ECS Cluster.
2.  **Create Mount Targets**: In each subnet used by the Fargate service.
3.  **Configure Security Groups**:
    - Allow inbound TCP port 2049 (NFS) on the EFS Security Group from the Backend Service Security Group.
4.  **Create an EFS Access Point**: (Recommended)
    - Root directory: `/angularai`
    - User/Group ID: 1000 (typical for alpine-based containers)
    - Permissions: 755

### 2.2 Update ECS Task Definition

Update `deploy/aws/backend-task-definition.json` to include the volume and mount point.

#### Add Volume Definition:
Add the `volumes` array to the root of the task definition JSON:
```json
{
  "volumes": [
    {
      "name": "h2-data",
      "efsVolumeConfiguration": {
        "fileSystemId": "fs-0222ebe39fb56812f",
        "transitEncryption": "ENABLED",
        "authorizationConfig": {
          "accessPointId": "fsap-0ee89669413efa3b4",
          "iam": "ENABLED"
        }
      }
    }
  ]
}
```

### 2.3 Provisioned AWS Resources

The following resources have been created:

- **EFS File System**: `fs-0222ebe39fb56812f`
- **EFS Access Point**: `fsap-0ee89669413efa3b4`
- **EFS Security Group**: `sg-0247eea5b0a04710a` (Allows port 2049 from `sg-0c9274d41b1f6ec9f`)

#### Add Mount Point to Container Definition:
Inside the `containerDefinitions` array, under the `backend` container, add the `mountPoints` array:
```json
{
  "mountPoints": [
    {
      "containerPath": "/app/data",
      "sourceVolume": "h2-data",
      "readOnly": false
    }
  ]
}
```

#### Update Environment Variable:
```json
{
  "name": "SPRING_PROFILES_ACTIVE",
  "value": "h2-file"
}
```

## 3. Implementation Steps

1.  **Prepare AWS EFS**: Create the file system and access points.
2.  **Update IAM Roles**: Ensure `ecsTaskExecutionRole` has permissions to describe EFS and that the task role has permissions to mount and write to EFS.
3.  **Modify Task Definition**: Apply the JSON changes mentioned above.
4.  **Test Deployment**: Register the new task definition and update the service.
5.  **Verify Persistence**: Restart the task and ensure data remains intact.

## 4. Execution History (AWS CLI)

The following commands were used to provision the infrastructure. Resource IDs have been replaced with placeholders for public documentation.

### 4.1 Networking & Security

```bash
# 1. Identify VPC and Subnets
aws ec2 describe-vpcs --region eu-central-1
aws ec2 describe-subnets --region eu-central-1

# 2. Create Security Group for EFS
EFS_SG_ID=$(aws ec2 create-security-group \
  --group-name angularai-efs-sg \
  --description "Security group for EFS access from backend" \
  --vpc-id <VPC_ID> \
  --region eu-central-1 \
  --query 'GroupId' --output text)

# 3. Allow inbound NFS (2049) from Backend Security Group
aws ec2 authorize-security-group-ingress \
  --group-id $EFS_SG_ID \
  --protocol tcp \
  --port 2049 \
  --source-group <BACKEND_SG_ID> \
  --region eu-central-1
```

### 4.2 EFS Provisioning

```bash
# 4. Create File System
FS_ID=$(aws efs create-file-system \
  --creation-token angularai-h2-data \
  --region eu-central-1 \
  --query 'FileSystemId' --output text)

# 5. Create Access Point (Root: /h2-data, UID/GID: 1000)
AP_ID=$(aws efs create-access-point \
  --file-system-id $FS_ID \
  --posix-user Uid=1000,Gid=1000 \
  --root-directory "Path=/h2-data,CreationInfo={OwnerUid=1000,OwnerGid=1000,Permissions=755}" \
  --region eu-central-1 \
  --query 'AccessPointId' --output text)

# 6. Create Mount Targets in Backend Subnets
aws efs create-mount-target \
  --file-system-id $FS_ID \
  --subnet-id <SUBNET_ID_1> \
  --security-groups $EFS_SG_ID \
  --region eu-central-1

aws efs create-mount-target \
  --file-system-id $FS_ID \
  --subnet-id <SUBNET_ID_2> \
  --security-groups $EFS_SG_ID \
  --region eu-central-1
```

### 4.3 Task Deployment

```bash
# 7. Register updated Task Definition
aws ecs register-task-definition \
  --cli-input-json file://deploy/aws/backend-task-definition.json \
  --region eu-central-1
```

## 5. Appendix: Why a Dedicated Security Group for EFS?

The creation of a dedicated security group for the Amazon EFS (Elastic File System) is a best practice for security and connectivity in AWS. Here are the primary reasons:

### 5.1 Granular Access Control (Least Privilege)
EFS uses the NFS protocol on TCP port 2049. By creating a specific security group for the EFS mount targets, we can strictly define who is allowed to connect to the storage. 
- The EFS Security Group is configured with a rule that **only** allows inbound traffic on port 2049 if it originates from the Backend Security Group.
- This ensures that no other resources in the VPC (or external actors) can attempt to mount or access the file system, even if they have the file system ID.

### 5.2 Decoupling and Management
Separating the storage security rules from the application security rules makes the infrastructure easier to manage and audit:
- **Scalability**: If you add more services that need access to the same H2 database file, you only need to add their specific security group IDs to the EFS security group's ingress rules.
- **Clarity**: It is immediately clear from the AWS Console or CLI which security group is responsible for protecting the data layer versus the application layer.

### 5.3 Fargate Networking Requirements
In AWS Fargate, tasks run in their own elastic network interface (ENI). For a Fargate task to communicate with an EFS mount target:
- The **outbound** rules of the Fargate task's security group must allow traffic to the EFS mount targets.
- The **inbound** rules of the EFS mount target's security group must allow traffic from the Fargate task.
Using dedicated security groups is the standard way to implement this "handshake" in an `awsvpc` network mode.

### 5.4 Avoiding Circular Dependencies
While technically possible to use the same security group for both the backend and the EFS, it is not recommended. It creates a "self-referencing" security group which can be confusing and might inadvertently open more ports than necessary (e.g., if all traffic within the security group is allowed, the backend could access EFS, but other backend instances could also access ports they shouldn't).
