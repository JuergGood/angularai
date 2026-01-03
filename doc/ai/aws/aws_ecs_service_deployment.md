To create the ECS Services for your frontend and backend using the AWS Console (v2), follow these steps:

### 1. Access the ECS Cluster
1.  Open the [Amazon ECS Console](https://eu-central-1.console.aws.amazon.com/ecs/v2/clusters).
2.  Click on the **Cluster** you created for this deployment.

---

### 2. Create the Backend Service
*Since the backend typically resides in a private subnet and doesn't need direct public access if using an ALB.*

1.  In the **Services** tab, click **Create**.
2.  **Deployment configuration**:
    *   **Application type**: Service.
    *   **Family**: Select the backend task definition family (e.g., `backend-task`).
    *   **Revision**: Select the latest revision.
    *   **Service name**: Enter `backend-service`.
    *   **Desired tasks**: `1`.
3.  **Networking**:
    *   **VPC**: Select your project VPC.
    *   **Subnets**: Select **Private subnets**.
    *   **Security group**: Ensure it allows inbound traffic on port `8080` (from the Frontend/ALB security group).
4.  **Load balancing (Optional for Backend)**:
    *   If you are routing through an ALB (recommended), select **Application Load Balancer**.
    *   **Container**: `backend-container 8080:8080`.
    *   **Listener**: Create or select a listener (e.g., Port 80 or 443).
    *   **Target group**: Create a new target group. **Path pattern**: `/api/*`.
5.  Click **Create**.

---

### 3. Create the Frontend Service
*The frontend needs to be accessible via the Load Balancer. It should share the SAME Load Balancer as the backend.*

1.  Return to your Cluster and click **Create** in the Services tab.
2.  **Deployment configuration**:
    *   **Application type**: Service.
    *   **Family**: Select the frontend task definition family (e.g., `frontend-task`).
    *   **Revision**: Select the latest revision.
    *   **Service name**: Enter `frontend-service`.
    *   **Desired tasks**: `1`.
3.  **Networking**:
    *   **VPC**: Select the same VPC.
    *   **Subnets**: Select **Public subnets** (or Private if the ALB is public).
    *   **Security group**: Ensure it allows inbound traffic on port `80` from the ALB.
4.  **Load balancing**:
    *   **Load balancer type**: Application Load Balancer.
    *   **Container**: `frontend-container 80:80`.
    *   **Listener**: Select the **SAME listener** used for the backend (e.g., Port 80).
    *   **Target group**: Create a new target group (e.g., `frontend-tg`). **Path pattern**: `/` (Default/Catch-all).
5.  Click **Create**.

---

### 4. Verify Single ALB Configuration
**CRITICAL**: Both services must be behind the same ALB DNS name for relative `/api` calls to work correctly.

1.  Go to the **EC2 Console** -> **Load Balancers**.
2.  Select your shared Load Balancer.
3.  Go to **Listeners** -> **View/edit rules**.
4.  Ensure you have a rule for `/api/*` forwarding to the **backend** and a default rule forwarding to the **frontend**.

### 5. Verify Deployment
1.  Go to the **Tasks** tab within each service to ensure the status changes to `RUNNING`.
2.  Check the **Logs** tab if a task fails to start (Common issues: missing environment variables or incorrect IAM roles).
3.  Once both are running, access your application using the **DNS name of the Application Load Balancer**.

4.  Test [frontend LB](http://angular-boot-lb-frontend-991865735.eu-central-1.elb.amazonaws.com).
5. Test [Backend Login](http://angular-boot-lb-frontend-991865735.eu-central-1.elb.amazonaws.com/api/login).
6. Test [Backend Tasks](http://angular-boot-lb-frontend-991865735.eu-central-1.elb.amazonaws.com/api/tasks).