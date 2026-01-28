# Frequently Asked Questions (FAQ)

## General
### Q: What is AngularAI?
A: AngularAI is a task management application featuring a modern Angular frontend, a Spring Boot backend, and an Android mobile app.

### Q: How do I get started?
A: Register for an account, log in, and start creating tasks on the Task Management page.

## Accounts and Security
### Q: I forgot my password. How can I reset it?
A: You can use the "Forgot Password" link on the login page. Enter your email address, and you will receive a link to reset your password. If you encounter any issues, please contact your system administrator.

### Q: Can I change my role?
A: User roles (ROLE_USER, ROLE_ADMIN, ROLE_ADMIN_READ) can only be changed by an administrator via the User Administration panel.

### Q: What is the ROLE_ADMIN_READ role?
A: This role allows a user to view administrative data like user lists and system logs without the ability to modify or delete any information.

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

### Q: Why can't I access the User Administration?
A: User Administration and System Logs are only accessible to users with the `ROLE_ADMIN` or `ROLE_ADMIN_READ` roles.
