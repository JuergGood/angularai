---
marp: true
theme: default
paginate: true
header: 'AngularAI & Junie: AI-Driven Full-Stack'
footer: '© 2026 AngularAI Project'
---

# Accelerating Full-Stack Development with Junie AI
## A Case Study on the AngularAI Project Ecosystem

---

# Application Overview
### The "AngularAI" Ecosystem

- **Unified Experience**: Seamless task management across Web and Mobile.
- **Web App**: Modern Angular dashboard using Material Design.
- **Android App**: Native experience built with Jetpack Compose.
- **Features**: Auth, Task Management, User Administration, Profile Settings.

---

# Architecture Overview

- **Client Layer**: Angular (Web) & Jetpack Compose (Android).
- **API Layer**: Spring Boot REST API (Stateless).
- **Persistence**: PostgreSQL (AWS RDS) managed via Flyway.
- **Infrastructure**: Containerized deployment on AWS ECS Fargate.
- **Communication**: HTTPS / REST / Basic Auth.

---

# Backend Excellence
### Spring Boot 4 & JPA

- **Clean Code**: DTO-based communication (fromEntity mapping).
- **Security**: RBAC with Spring Security and BCrypt.
- **Efficiency**: Automated database migrations with Flyway.
- **Junie's Role**: Scaffolding controllers, repositories, and optimizing JPA queries.

---

# Modern Frontend
### Angular & Material

- **Reactive State**: Angular Signals for fine-grained updates.
- **Standalone Architecture**: Modular, tree-shakable components.
- **UX/UI**: Material 3 (indigo-pink) for a professional look.
- **Junie's Role**: UI scaffolding and Signal-based service generation.

---

# Native Power
### Android Jetpack Compose

- **Declarative UI**: Modern Kotlin-based UI development.
- **Offline First**: Local caching with Room Database.
- **Reactive Flow**: Coroutines and Flow for data sync.
- **Junie's Role**: Translating web requirements to native mobile components.

---

# Local Development Setup
### Integrated Ecosystem on Windows

- **IntelliJ IDEA + Junie**: Full-stack core (Spring Boot & Angular).
- **Android Studio + Gemini**: Native mobile dev in Emulator.
- **Docker on Windows**: Local PostgreSQL persistence.
- **Test Client**: Validating endpoints locally or on AWS cloud.
- **Hot Reloading**: Instant feedback for Web, Mobile, and Backend.

---

# AI-Driven Development with Junie

- **Context Awareness**: Multi-language project understanding.
- **Unit Test Generation**: Automated JUnit 5 and Vitest coverage.
- **Command Execution**: Direct terminal interaction for builds/deploys.
- **Productivity**: 40% reduction in repetitive coding tasks.

---

# Case Study
### Seamless Database Evolution

- **Problem**: Syncing schema changes between Backend and Mobile.
- **Process**: Modify JPA Entity -> Junie generates Flyway SQL.
- **Sync**: Junie updates Android Room Entity and Migration scripts.
- **Result**: Consistent data state across all platforms.

---

# Infrastructure & AWS Deployment

- **Docker**: Identical dev/prod environments via Compose.
- **AWS Fargate**: Serverless container orchestration.
- **CI/CD**: Automated PowerShell scripts for ECR/ECS.
- **Junie's Role**: Infrastructure as Code (Task Definitions) and log analysis.

---

# Conclusion
### The Productivity Multiplier

- **Speed**: Faster time-to-market for full-stack features.
- **Quality**: Consistent standards and high test coverage.
- **Integration**: Bridging the gap between Web, Mobile, and Backend.
- **Motto**: Build smarter, not harder, with Junie.
