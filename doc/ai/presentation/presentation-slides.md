# Slide 1: Title
layout: title
subtitle: Accelerating Full-Stack Development with Junie AI

---

# Slide 2: Application Overview
layout: two_content
left: |
  - Unified Experience: Seamless task management.
  - Web App: Modern Angular dashboard.
  - Android App: Native experience.
right:
  image: files/angular_ui_login.png
  caption: Angular Material Login Screen

---

# Slide 3: Architecture Overview
layout: title_and_content
content: |
  - Client Layer: Angular & Jetpack Compose.
  - API Layer: Spring Boot REST API.
  - Persistence: PostgreSQL (AWS RDS).
  - Infrastructure: Containerized on AWS ECS Fargate.
image: files/architecture_overview.png

---

# Slide 4: Database Schema (ER Diagram)
layout: title_and_content
content: |
  - Automated Schema Generation.
  - Entities: User, Task, Priority, TaskStatus.
  - Relationships: User (1) to Task (N).
  - Migration: Managed via Flyway.
image: files/er_diagram.png

---

# Slide 5: Local Development Setup
layout: two_content
left: |
  - **IntelliJ IDEA + Junie**: Backend & Frontend core.
  - **Android Studio + Gemini**: Native mobile dev.
  - **Docker on Windows**: Local PostgreSQL.
right:
  image: files/local_dev_setup.png
  caption: Integrated Dev Environment

---

# Slide 6: Backend Excellence
layout: title_and_content
content: |
  - Spring Boot 4 & JPA.
  - Clean Code: DTO-based communication.
  - Security: Role-Based Access Control.
  - Junie's Role: Scaffolding and Query Optimization.

---

# Slide 7: Modern Frontend
layout: title_and_content
content: |
  - Angular 21+ with Signals.
  - Standalone Components.
  - Material 3 Design.
  - Junie's Role: Rapid UI scaffolding.

---

# Slide 8: AI-Driven Development with Junie
layout: title_and_content
content: |
  - Context Awareness across the stack.
  - Unit Test Generation (JUnit, Vitest).
  - Command Execution from IDE.
  - 40% Productivity Gain.

---

# Slide 9: Case Study: DB Evolution
layout: title_and_content
content: |
  - Problem: Syncing schema between Backend and Mobile.
  - Process: Modify Entity -> Junie generates Flyway SQL.
  - Sync: Junie updates Android Room scripts.
  - Result: Zero-friction schema updates.

---

# Slide 10: Conclusion
layout: title
subtitle: Build Smarter, Not Harder, with Junie AI