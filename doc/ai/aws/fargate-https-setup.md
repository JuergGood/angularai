# Enabling HTTPS for an AWS Fargate Application (Already Running on HTTP)

This guide explains how to add an **HTTPS endpoint** to an existing **AWS Fargate** application that is already accessible over **HTTP**.

The standard and recommended approach is:
- Terminate TLS at an **Application Load Balancer (ALB)**
- Use **AWS Certificate Manager (ACM)** for SSL certificates
- Forward traffic from ALB to containers over HTTP

---

## 1. Request an SSL/TLS Certificate (ACM)

1. Open **AWS Certificate Manager (ACM)**
2. Request a **Public Certificate**
3. Add your domain name (e.g. `api.example.com`)
4. Choose **DNS validation** (recommended)
5. Complete validation (automatic if using Route 53)

⚠️ The certificate **must be in the same region as the ALB**.

---

## 2. Confirm ECS Service Uses an Application Load Balancer

In **Amazon ECS**:
- Cluster → Service → **Load balancing**
- Ensure the service is attached to an **Application Load Balancer**
- Confirm the service forwards to a **Target Group**
- The target group should forward to the container port (e.g. `80`, `3000`, `8080`)

If your service currently uses a public IP directly on the task, migrate it behind an ALB first.

---

## 3. Add an HTTPS Listener (443) to the ALB

1. EC2 → Load Balancers → select your ALB
2. Go to **Listeners**
3. Add listener:
   - Protocol: `HTTPS`
   - Port: `443`
4. Select your **ACM certificate**
5. Forward traffic to the existing target group

⚠️ **CRITICAL STEP: Port-Path Routing**
If you are using the "Single ALB" strategy (recommended), you must ensure that the HTTPS listener has the same path-based routing rules as the HTTP listener:
- **Rule 1**: IF `Path is /api/*` THEN `Forward to target group: angularai-backend-tg`.
- **Default Rule**: `Forward to target group: angularai-frontend-tg`.

Failure to add the `/api/*` rule to the HTTPS listener will cause API calls to be sent to the frontend container (Nginx), resulting in `404 Not Found` or `502 Bad Gateway` errors during login.

### Optional but Recommended: Redirect HTTP → HTTPS
Modify the existing `HTTP : 80` listener:
- Action: **Redirect**
- Protocol: `HTTPS`
- Port: `443`
- Status code: `301`

---

## 4. Configure Security Groups

### ALB Security Group
Inbound rules:
- Allow `TCP 443` from `0.0.0.0/0` and `::/0`
- (Optional) Allow `TCP 80` for HTTP → HTTPS redirect

### ECS Task / Service Security Group
Inbound rules:
- Allow **only** the application port (e.g. `80`, `3000`, `8080`)
- Source: **ALB security group**
- ❌ Do NOT allow traffic directly from the internet

This is the most common cause of `502` errors or unhealthy targets.

---

## 5. Configure Health Checks

Target Group → Health checks:
- Protocol: `HTTP`
- Path: `/health` or `/`
- Port: `traffic port`
- Success codes: `200–399`

Ensure your container responds correctly on the configured path.

---

## 6. Configure DNS

### Route 53
- Create an **A / AAAA Alias record**
- Point the record to the ALB

### Other DNS Providers
- Create a `CNAME` record pointing to the ALB DNS name

---

## 7. Application Configuration Notes

- ALB forwards:
  - `X-Forwarded-Proto: https`
- Configure your app to **trust proxy headers**
- Update OAuth redirect URLs or absolute links to use `https://`

---

## Common Issues & Fixes

| Symptom | Likely Cause |
|-------|------------|
| HTTPS returns `502` | Task security group not allowing traffic from ALB |
| Targets unhealthy | Wrong port, health check path, or SG issue |
| Certificate error | Wrong domain, unvalidated cert, or region mismatch |
| HTTP works, HTTPS doesn't | Missing 443 listener or SG rule |

---

## Summary Architecture

```
Internet
   |
HTTPS (443)
   |
Application Load Balancer (TLS termination)
   |
HTTP
   |
ECS Fargate Tasks
```

---

End of document.
