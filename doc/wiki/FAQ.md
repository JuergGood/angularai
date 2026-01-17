# Frequently Asked Questions (FAQ)

## General
### Q: What is AngularAI?
A: AngularAI is a task management application featuring a modern Angular frontend, a Spring Boot backend, and an Android mobile app.

### Q: How do I get started?
A: Register for an account, log in, and start creating tasks on the Task Management page.

## Accounts and Security
### Q: I forgot my password. How can I reset it?
A: Currently, password reset must be handled by an administrator. Please contact your system administrator.

### Q: Can I change my role?
A: User roles can only be changed by an administrator via the User Administration panel.

## Task Management
### Q: Can I reorder my tasks?
A: Yes, you can use the drag handle on the left side of each task card to reorder them manually. Note that manual reordering is disabled when a status filter is active.

### Q: What do the different task priorities mean?
A:
- **High**: Urgent tasks that should be addressed immediately.
- **Medium**: Important tasks that should be completed soon.
- **Low**: Non-urgent tasks.

## Troubleshooting
### Q: The application is not loading. What should I do?
A: Ensure that both the backend and frontend services are running. If you are using Docker, run `docker compose up` to start all services.

### Q: I am getting an "Access Denied" error.
A: This error occurs if you try to access a page for which you do not have the required permissions (e.g., trying to access User Administration as a regular user).
