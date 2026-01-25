To stop and restart your ECS Fargate services, you can use the following AWS CLI commands.

### 1. Backend Service (`angularai-backend-test-service`)

**Deploy a Specific Version ($VERSION):**
To deploy a new version (e.g., `1.0.5`), you must first update the Task Definition. See [Push Instructions](ecs-push-instructions.md#updating-ecs-task-definitions) for details on pushing images and registering new task definitions.

```bash
# 1. Register new task definition (after updating the 'image' tag in your JSON)
# This will return the new numeric REVISION
aws ecs register-task-definition --cli-input-json file://deploy/aws/backend-task-definition.json --query "taskDefinition.revision" --output text
```
```bash
# 2. Update service to use the new revision
# Replace REVISION with the number returned by the command above
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --task-definition angularai-backend:4 --query "service.taskDefinition" --output text
```

**Stop and Restart Service (Full Reset):**
To ensure all old instances are stopped before starting the new one, you can chain the commands (setting count to 0 first):
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 0 --query "service.serviceName" --output text; aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```

**Stop Service Only:**
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 0 --query "service.serviceName" --output text
```

**Start/Restart Service (Incremental):**
```bash
# This sets the count to 1 and forces a fresh deployment. 
# Note: ECS might keep old instances running until new ones are healthy unless you use the reset command above.
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```

### 2. Frontend Service (`angularai-frontend-service`)

**Deploy a Specific Version ($VERSION):**
```bash
# 1. Register new task definition (after updating the 'image' tag in your JSON)
aws ecs register-task-definition --cli-input-json file://deploy/aws/frontend-task-definition.json --query "taskDefinition.revision" --output text
```
```bash
# 2. Update service to use the new revision
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --task-definition angularai-frontend:2 --query "service.taskDefinition" --output text
```

**Stop and Restart Service (Full Reset):**
To ensure all old instances are stopped before starting the new one, you can chain the commands (setting count to 0 first):
```bash
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 0 --query "service.serviceName" --output text; aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```

**Stop Service Only:**
```bash
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 0 --query "service.serviceName" --output text
```

**Start/Restart Service (Incremental):**
```bash
# This sets the count to 1 and forces a fresh deployment.
# Note: ECS might keep old instances running until new ones are healthy unless you use the reset command above.
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```

### 3. Fallback Page (When Stopped)
When you stop your services for cost reasons, it is recommended to configure a **Fallback Page** on the Load Balancer to avoid showing users a generic `503 Service Unavailable` error.

See [AWS ALB Fallback Configuration](alb-fallback.md) for instructions on how to set this up.

---

### Option 1: Stop and Restart Service (Full Reset)
If your service is already running but you want to ensure all old tasks are stopped **before** the new one starts (to avoid having multiple instances running simultaneously):

```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 0 --query "service.serviceName" --output text; aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```
*(Repeat for `angularai-frontend-service` if needed)*

### Option 2: Force a New Deployment Only (Rolling Update)
If you want to restart the tasks but don't mind if the old task stays running until the new one is healthy (standard ECS rolling update):

**Via AWS CLI:**
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```
*(Repeat for `angularai-frontend-service` if needed)*

**Via AWS Management Console:**
1.  Navigate to the **Amazon ECS Console**.
2.  Select your **Cluster**.
3.  In the **Services** tab, select the checkbox for `angularai-backend-test-service` or `angularai-frontend-service`.
4.  Click **Update**.
5.  Check the box **Force new deployment**.
6.  Click **Next step** through the wizard and finally **Update service**.

---

### Option 2: Manually Stop the Failed Task
If the service is still in the process of retrying, it might have one or more tasks in a `STOPPED` or `PROVISIONING` state.

1.  Go to the **Tasks** tab of your cluster or service.
2.  If you see a task that is currently failing, select it and click **Stop**.
3.  Because your service has a "Desired count" of `1`, ECS will automatically notice the task has stopped and attempt to launch a new one. Since the CloudWatch log group now exists, the new task should reach the `RUNNING` state.

---

### Option 3: Wait for Automatic Retry
ECS Services have a built-in scheduler that automatically retries launching tasks when they fail.
- Once the underlying issue (the missing log group) is resolved, the next automatic retry by the ECS scheduler will succeed.
- **Note**: ECS uses an exponential backoff strategy for retries, so if it has failed many times, it might take a few minutes before the next attempt occurs. Using **Option 1** bypasses this wait.

### Verification
Once you have triggered the rerun:
1.  Go to the **Tasks** tab of the `angularai-backend-test-service` or `angularai-frontend-service`.
2.  Wait for the **Last status** to change to `RUNNING`.
3.  Click on the Task ID and go to the **Logs** tab to see the Spring Boot startup logs, confirming that the H2 database and application are initializing correctly.
4.  **Verify Version**: Access the system info endpoint to confirm the correct version is deployed:
    - `https://<your-alb-dns>/api/system/info`
    - Expected output: `{"version":"1.0.5","mode":"Postgres"}` (or "H2" depending on profile)
    - **Note**: If you still see `0.0.1-SNAPSHOT`, ensure you have pushed the new image to ECR and updated the Task Definition to point to the new version tag. See the Troubleshooting section in [Full Deployment Guide](full-deployment.md#62-troubleshooting-old-version-still-displayed).

