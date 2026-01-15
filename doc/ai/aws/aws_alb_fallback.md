# AWS ALB Fallback Configuration

This guide explains how to configure a fallback page for your Application Load Balancer (`angular-boot-lb-frontend`) when your ECS Fargate services are shut down for cost reasons.

When you set the desired count of your services to `0`, the Load Balancer will find no healthy targets and will typically return a `503 Service Unavailable` error to users. You can customize this behavior using ALB Listener Rules.

---

## Option 1: Simple Fixed Response (Recommended)

This is the easiest way to show a "Maintenance" or "Off-hours" message without managing additional infrastructure like S3.

### 1. Via AWS Management Console
1.  Navigate to the **EC2 Console** -> **Load Balancers**.
2.  Select `angular-boot-lb-frontend`.
3.  Go to the **Listeners** tab and select the listener (usually HTTP:80).
4.  Click **Manage rules**.
5.  Locate the **Default rule** (the one that forwards to your frontend target group).
6.  Edit the rule and change the **Action** from "Forward to..." to **"Return fixed response"**.
7.  Set the following values:
    *   **Response code**: `503` (or `200` if you want it to look "successful").
    *   **Content-type**: `text/plain` or `text/html`.
    *   **Response body**: 
        ```html
        <html>
        <head><title>AngularAI Offline</title></head>
        <body style="font-family: Arial; text-align: center; padding: 50px;">
          <h1>Application is currently offline</h1>
          <p>The AngularAI service is shut down during off-hours to save costs.</p>
          <p>Please check back later!</p>
        </body>
        </html>
        ```
8.  Save the changes.

### 2. Via AWS CLI

To automate this when you stop your services, use the following commands.

#### For Linux/Bash (macOS, WSL, Linux)
```bash
# Update HTTP (Port 80) Listener
aws elbv2 modify-listener \
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/46b92b1b6c16c338 \
  --default-actions Type=fixed-response,FixedResponseConfig="{StatusCode=503,ContentType=text/html,MessageBody='<html><body style=\"font-family:Arial;text-align:center;padding:50px;\"><h1>Application Offline</h1><p>Service is shut down during off-hours.</p></body></html>'}"

# Update HTTPS (Port 443) Listener
aws elbv2 modify-listener \
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69 \
  --default-actions Type=fixed-response,FixedResponseConfig="{StatusCode=503,ContentType=text/html,MessageBody='<html><body style=\"font-family:Arial;text-align:center;padding:50px;\"><h1>Application Offline</h1><p>Service is shut down during off-hours.</p></body></html>'}"
```

#### For Windows (PowerShell)
*Note: PowerShell requires triple quotes (`"""`) for internal JSON values to preserve them when calling the AWS CLI.*

> **Tip:** If the "Run" icon is missing for `powershell` blocks:
> 1. In **Settings > Languages & Frameworks > Markdown**, check **Language Mappings**. Ensure `powershell` or `pwsh` is mapped to **PowerShell**.
> 2. Try using the `pwsh` tag instead of `powershell`.
> 3. You can often run the block by placing your cursor inside it and pressing **Alt + Enter** > **Run Code Block**.

```powershell
# Update HTTP (Port 80) Listener
aws elbv2 modify-listener `
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/46b92b1b6c16c338 `
  --default-actions 'Type=fixed-response,FixedResponseConfig={StatusCode=503,ContentType=text/html,MessageBody="""<html><body style=\"font-family:Arial;text-align:center;padding:50px;\"><h1>Application Offline</h1><p>Service is shut down during off-hours.</p></body></html>"""}'

# Update HTTPS (Port 443) Listener
aws elbv2 modify-listener `
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69 `
  --default-actions 'Type=fixed-response,FixedResponseConfig={StatusCode=503,ContentType=text/html,MessageBody="""<html><body style=\"font-family:Arial;text-align:center;padding:50px;\"><h1>Application Offline</h1><p>Service is shut down during off-hours.</p></body></html>"""}'
```

---

## Option 2: Redirect to S3 Static Page

If you want a more complex fallback page with images and styling, you can host a static site in an S3 bucket and configure the ALB to redirect there.

### 1. Create S3 Bucket
1.  Create an S3 bucket (e.g., `angularai-fallback-page`).
2.  Enable **Static website hosting**.
3.  Upload your `index.html`.
4.  Set bucket policy to allow public read access.

### 2. Configure ALB Redirect
1.  In the ALB Listener Rules (same as Option 1), edit the **Default rule**.
2.  Change the **Action** to **"Redirect to URL"**.
3.  Enter the URL of your S3 website endpoint (e.g., `http://angularai-fallback-page.s3-website.eu-central-1.amazonaws.com`).
4.  Set the redirect to **Permanent (301)** or **Temporary (302)**.

## Option 3: Manage a Specific Listener Rule (Enable/Disable)

If you have a specific rule (e.g., a path-based rule for `/api/*`) that you want to switch between maintenance mode and normal forwarding, you can use the `modify-rule` command.

**Specific Rule ARN:** `arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener-rule/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69/6d5a138969cbadf5`

### 1. Enable Maintenance Mode (Fixed Response)
This "disables" the normal forwarding by replacing the action with a fixed response.

#### For Linux/Bash
```bash
aws elbv2 modify-rule \
  --rule-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener-rule/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69/6d5a138969cbadf5 \
  --actions Type=fixed-response,FixedResponseConfig="{StatusCode=503,ContentType=text/html,MessageBody='<html><body><h1>API Maintenance</h1></body></html>'}"
```

#### For Windows (PowerShell)
```powershell
aws elbv2 modify-rule `
  --rule-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener-rule/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69/6d5a138969cbadf5 `
  --actions 'Type=fixed-response,FixedResponseConfig={StatusCode=503,ContentType=text/html,MessageBody="""<html><body><h1>API Maintenance</h1></body></html>"""}'
```

### 2. Disable Maintenance Mode (Restore Forwarding)
This restores the rule to its normal state (forwarding to the target group).

#### For Linux/Bash
```bash
aws elbv2 modify-rule \
  --rule-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener-rule/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69/6d5a138969cbadf5 \
  --actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:426141506813:targetgroup/angular-boot-frontend2/5c1b885c9fdcac54
```

#### For Windows (PowerShell)
```powershell
aws elbv2 modify-rule `
  --rule-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener-rule/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69/6d5a138969cbadf5 `
  --actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:426141506813:targetgroup/angular-boot-frontend2/5c1b885c9fdcac54
```

---

## Restoring Service

When you are ready to start the services again (as described in [aws_rerun_service.md](aws_rerun_service.md)), you **must revert the ALB rules** to forward traffic back to the Frontend Target Group.

### 1. Via AWS Management Console
1.  Navigate to the **EC2 Console** -> **Load Balancers**.
2.  Select `angular-boot-lb-frontend`.
3.  Go to the **Listeners** tab.
4.  For both listeners (80 and 443), click **Manage rules**.
5.  Edit the **Default rule** and change the **Action** back to **"Forward to..."**.
6.  Select the `angularai-frontend-tg` target group.
7.  Save changes.

### 2. Via AWS CLI
Use the following commands to restore forwarding to the frontend target group.

#### For Linux/Bash (macOS, WSL, Linux)
```bash
# Restore HTTP (Port 80)
aws elbv2 modify-listener \
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/46b92b1b6c16c338 \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:426141506813:targetgroup/angular-boot-frontend2/5c1b885c9fdcac54

# Restore HTTPS (Port 443)
aws elbv2 modify-listener \
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69 \
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:426141506813:targetgroup/angular-boot-frontend2/5c1b885c9fdcac54
```

#### For Windows (PowerShell)
> **Tip:** If the run icon is missing, see the instructions in the "Option 1" section above for enabling PowerShell in your IDE.

```powershell
# Restore HTTP (Port 80)
aws elbv2 modify-listener `
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/46b92b1b6c16c338 `
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:426141506813:targetgroup/angular-boot-frontend2/5c1b885c9fdcac54

# Restore HTTPS (Port 443)
aws elbv2 modify-listener `
  --listener-arn arn:aws:elasticloadbalancing:eu-central-1:426141506813:listener/app/angular-boot-lb-frontend/935a795fe8bb72ca/9521f3b60cb2ee69 `
  --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-central-1:426141506813:targetgroup/angular-boot-frontend2/5c1b885c9fdcac54
```
*Note: Replace `YOUR_FRONTEND_TG_ARN` with the actual ARN of your frontend target group (e.g., the one for `angularai-frontend-tg`).*

**Important**: If you don't revert these rules, users will continue to see the fallback page even after your containers are running.
