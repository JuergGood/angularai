# Creating an AWS Target Group for the Backend

This guide explains how to create a Target Group for your Spring Boot backend and associate it with your ECS Fargate service when using a shared Application Load Balancer (ALB).

### 1. Create the Target Group
1.  Open the **EC2 Console** and navigate to **Target Groups** (under *Load Balancing*).
2.  Click **Create target group**.
3.  **Basic configuration**:
    *   **Target type**: Select **IP addresses** (Required for Fargate).
    *   **Target group name**: Enter `angularai-backend-tg`.
    *   **Protocol**: `HTTP`.
    *   **Port**: `8080`.
    *   **VPC**: Select the same VPC where your ALB and ECS cluster reside.
    *   **Protocol version**: `HTTP1`.
4.  **Health checks**:
    *   **Health check protocol**: `HTTP`.
    *   **Health check path**: `/api/auth/login` (or `/api/tasks` - it just needs to respond, even if it's 401 or 405).
    *   *Advanced health check settings*:
        *   **Healthy threshold**: `2`.
        *   **Unhealthy threshold**: `3`.
        *   **Timeout**: `5 seconds`.
        *   **Interval**: `30 seconds`.
        *   **Success codes**: `200-499` (This ensures that even a 401 Unauthorized or 405 Method Not Allowed counts as "healthy" for the ALB).
5.  Click **Next**.
6.  **Register targets**:
    *   **Do NOT** manually register any IPs here. ECS will automatically manage the IP addresses of your tasks.
7.  Click **Create target group**.

### 2. Associate Target Group with ECS Service
If you have already created the service without a load balancer, you cannot add one after the service is created. You must **recreate** the service or update it if it was already configured with a placeholder.

**To Recreate/Update the Service**:
1.  Go to the **ECS Console** -> **Clusters** -> your cluster.
2.  Select the **backend-service** and click **Delete** (or create a new one with a different name).
3.  Click **Create** to start the service creation wizard again.
4.  In the **Load balancing** section:
    *   **Load balancer type**: Application Load Balancer.
    *   **Load balancer**: Select your existing frontend ALB.
    *   **Container to load balance**: `backend 8080:8080`.
    *   **Target group**: Select **Use an existing target group** and pick `angularai-backend-tg`.
5.  Complete the service creation.

### 3. Update ALB Listener Rules
1.  Go to **Load Balancers** in the EC2 Console.
2.  Select your shared ALB.
3.  Go to the **Listeners** tab and select your listener (Port 80).
4.  Click **View/edit rules**.
5.  Click the **+** (Add rules) icon and then **Insert Rule**.
6.  **IF**: Path is `/api/*`.
7.  **THEN**: Forward to `angularai-backend-tg`.
8.  Ensure this rule is **above** the default rule (which should point to the frontend).
9.  Click **Save**.

### 4. Verification
1.  Wait for the ECS task to reach the `RUNNING` state.
2.  Check the **Target Group** -> **Targets** tab. The health status should eventually become **Healthy**.
3.  Access the application via the ALB DNS name. The frontend should now be able to communicate with the backend via the `/api` path.
