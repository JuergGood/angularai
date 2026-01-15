To stop and restart your ECS Fargate services, you can use the following AWS CLI commands.

### 1. Backend Service (`angularai-backend-test-service`)

**Stop Service:**
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 0
```

**Start/Restart Service:**
```bash
# This sets the count to 1 and forces a fresh deployment
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 1 --force-new-deployment
```

**Start/Restart (Short Output):**
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```

### 2. Frontend Service (`angularai-frontend-service`)

**Stop Service:**
```bash
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 0
```

**Start/Restart Service:**
```bash
# This sets the count to 1 and forces a fresh deployment
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 1 --force-new-deployment
```

**Start/Restart (Short Output):**
```bash
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --desired-count 1 --force-new-deployment --query "service.serviceName" --output text
```

---

### Option 1: Force a New Deployment Only (If already running)
If your service is already running but you want to restart the tasks (e.g., to pick up a new image tag or clear a stuck state):

**Via AWS CLI:**
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --force-new-deployment
```

**Via AWS CLI (Short Output):**
```bash
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --force-new-deployment --query "service.serviceName" --output text
```
*(Repeat for `angularai-frontend-service` if needed)*

**Note for PowerShell users:** If you use angle brackets like `<CLUSTER_NAME>`, PowerShell might throw a `ParserError`. Use plain text placeholders instead.

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