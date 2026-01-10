# Kubernetes POC Setup

This document proposes a local Kubernetes setup for the AngularAI project and provides instructions on how to deploy the frontend and backend to a local cluster.

## 1. Local Kubernetes Proposal: Kind (Kubernetes in Docker)

For a Proof of Concept (POC) running in Docker, **Kind** is an excellent choice. It is lightweight, easy to set up, and runs Kubernetes nodes as Docker containers.

### Alternatives
- **Minikube**: A more feature-rich but slightly heavier option.
- **Docker Desktop Kubernetes**: If you already use Docker Desktop, you can enable Kubernetes in the settings.

## 2. Monitoring Tool: Kubernetes Dashboard

To monitor the status of your pods in a UI, we recommend the official **Kubernetes Dashboard**. It provides a comprehensive web-based interface for managing and troubleshooting your cluster.

### Other options:
- **Lens**: A powerful desktop application for Kubernetes management.
- **K9s**: A terminal-based UI (not a web UI, but very efficient).

## 3. Deployment Instructions

### Prerequisites
- Docker installed
- `kubectl` installed
- `kind` installed (if choosing Kind)

### Step 1: Create a Cluster (using Kind)
```bash
kind create cluster --name angularai
```

### Step 2: Build Docker Images
Build the images locally:
```bash
docker build -t angularai-backend:latest -f backend/Dockerfile .
docker build -t angularai-frontend:latest -f frontend/Dockerfile .
```

### Step 3: Load Images into Kind
Since we are using Kind, we need to load the local images into the cluster nodes:
```bash
kind load docker-image angularai-backend:latest --name angularai
kind load docker-image angularai-frontend:latest --name angularai
```

### Step 4: Deploy Manifests
Apply the Kubernetes manifests:
```bash
kubectl apply -f deploy/k8s/backend.yaml
kubectl apply -f deploy/k8s/frontend.yaml
```

### Step 5: Install Kubernetes Dashboard
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```
Create a proxy to access the dashboard:
```bash
kubectl proxy
```
Access at: `http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/`

### Step 6: Accessing the Dashboard (Login)
To log in, you need a Bearer Token.

1. **Create an Admin User**:
   Apply the dashboard admin manifest:
   ```bash
   kubectl apply -f deploy/k8s/dashboard-admin.yaml
   ```

2. **Generate Token**:
   Run the following command to get the token for the `admin-user`:
   ```bash
   kubectl -n kubernetes-dashboard create token admin-user
   ```

3. **Login**:
   Copy the generated token and paste it into the "Token" field on the Dashboard login page.

### Step 7: Accessing the Application
The frontend service is configured as a `LoadBalancer`. On local clusters like Kind, you might need to use port-forwarding:
```bash
kubectl port-forward service/frontend 8080:80
```
Then access the frontend at `http://localhost:8080`.

## 4. Troubleshooting & Logs

If you encounter issues (e.g., "Unknown Error" during login), you should check the logs of both the frontend and backend pods.

### Accessing Logs via `kubectl`

1. **Find the Pod Names**:
   ```bash
   kubectl get pods
   ```

2. **View Backend Logs**:
   ```bash
   # Replace <backend-pod-name> with the actual pod name from the previous command
   kubectl logs <backend-pod-name>
   ```
   To follow the logs in real-time:
   ```bash
   kubectl logs -f <backend-pod-name>
   ```

3. **View Frontend (Nginx) Logs**:
   ```bash
   # Replace <frontend-pod-name> with the actual pod name
   kubectl logs <frontend-pod-name>
   ```

### Accessing Logs via Kubernetes Dashboard

1. Log in to the Dashboard (see Step 6).
2. Select **Pods** from the left menu.
3. Find your pod (e.g., `backend-deployment-...`).
4. Click on the **Logs** icon (three horizontal lines) in the top-right corner of the pod details or list view.
5. You can switch between containers if there are multiple, and download or search the logs.

## 5. Common Issues

- **Unknown Error on Login**: 
  - Check backend logs for authentication failures or database connection issues.
  - Check frontend logs to see if the request is correctly proxied to `http://backend:8080`.
  - Ensure the backend service is running: `kubectl get svc backend`.
  - Verify that the frontend can reach the backend: `kubectl exec <frontend-pod-name> -- curl -v http://backend:8080/api/system/info`.

## 6. Manifest Files
The deployment manifests are located in `deploy/k8s/`:
- `backend.yaml`: Contains the Deployment and Service for the Spring Boot backend.
- `frontend.yaml`: Contains the Deployment and Service for the Angular frontend.
