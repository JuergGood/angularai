# ALB and Frontend-Backend Connectivity Troubleshooting

This document explains how to verify and fix the connectivity between your Angular frontend and Spring Boot backend when running on AWS ECS Fargate.

### 1. Improved Error Messages
The `LoginComponent` now displays specific error messages to help you distinguish between authentication failures and infrastructure issues:
- **"Invalid login or password"**: This is a `401 Unauthorized` response. The ALB correctly reached the backend, but the credentials were rejected.
- **"An error occurred: Not Found"**: This is a `404 Not Found`. This usually happens if the ALB HTTPS listener is missing the `/api/*` rule and is forwarding the request to the frontend container (Nginx) instead of the backend.
- **"An error occurred: Bad Gateway"**: This is a `502 Bad Gateway`. This can happen for two reasons:
    - **Missing ALB Rule**: The ALB forwarded the request to the Frontend (Nginx), but Nginx could not reach the backend (e.g., in AWS, the "backend" hostname doesn't exist). Look for `backend could not be resolved` in the frontend logs.
    - **Backend Down**: The ALB is trying to reach the backend, but the backend is either down or its security group is blocking the ALB.

### 2. The "Single ALB" Strategy
Based on your report, you have two different Load Balancer URLs:
- `angular-boot-lb-frontend-...`
- `angular-boot-lb-backend-...`

**This is likely the cause of the issue.** In a standard production setup (and as configured in our `nginx.conf`), the frontend and backend should share **ONE** Application Load Balancer. 

The frontend sends requests to `/api/...`. If the frontend is on one domain and the backend is on another, the browser will block the requests due to **CORS** (Cross-Origin Resource Sharing) or the frontend will try to reach `/api` on the *frontend's* load balancer, which doesn't know about the backend.

### 2. How to Verify ALB Routing Rules
To verify that your ALB is correctly routing `/api/*` to the backend and everything else to the frontend, follow these steps in the AWS Console:

1.  Open the **EC2 Console** and go to **Load Balancers**.
2.  Select the **Load Balancer** you are using for the **Frontend**.
3.  Click on the **Listeners** tab.
4.  Select the listener (usually **HTTP:80** or **HTTPS:443**).
5.  Click **View/edit rules**.
6.  You should see at least two rules:
    - **Rule 1**: IF `Path is /api/*` THEN `Forward to target group: angularai-backend-tg`.
    - **Last (Default) Rule**: IF `Requests otherwise not routed` THEN `Forward to target group: angularai-frontend-tg`.

⚠️ **Note for HTTPS Users**: If you have enabled HTTPS, you must perform these steps for **BOTH** the HTTP (80) and HTTPS (443) listeners.

**If Rule 1 or the Target Group is missing:**
- Follow the [Creating a Backend Target Group](create-target-group.md) guide to create the group and add the rule.

### 3. Verify Target Group Health
If the routing rules are correct but you still get a `502 Bad Gateway` or `504 Gateway Timeout`:

1.  Go to **Target Groups** in the EC2 Console.
2.  Select your **Backend Target Group**.
3.  Click the **Targets** tab.
4.  Check the **Health status**:
    - If it's **Unhealthy**: The ALB cannot reach your Spring Boot container.
    - **Common Fix**: Ensure the health check path is set to an endpoint that exists (e.g., `/api/auth/login` or just `/api/tasks` if they return 401/405, or create a `/actuator/health` endpoint).

### 4. Security Group Chaining
Ensure the network traffic can flow:

1.  **ALB Security Group**: Must allow Inbound `80/443` from `0.0.0.0/0`.
2.  **Backend Service Security Group**:
    - Must allow Inbound `8080` from the **Security Group ID of the ALB**.
    - Do **not** use `0.0.0.0/0` here; specifically allow the ALB's SG to ensure only the load balancer can talk to the backend.

### 5. Frontend Service Configuration
Ensure your `frontend/nginx.conf` is using the correct service name if you are using Docker Compose, but on AWS, the ALB handles the routing. The frontend code uses relative paths like `/api/auth/login`, so it will automatically use the same host as the frontend.

**To fix your current state:**
1.  Choose one ALB (the frontend one).
2.  Add the `/api/*` rule to its listener pointing to the backend target group.
3.  Update your frontend to point only to that single ALB URL.
