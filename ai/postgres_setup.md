I have added the PostgreSQL configuration to the project. This includes the necessary driver dependency and a dedicated properties file.

### 1. New Configuration File
An additional configuration file has been created at:
`backend/src/main/resources/application-postgres.properties`

This file is configured to connect to a local PostgreSQL instance:
*   **URL**: `jdbc:postgresql://localhost:5432/angularai`
*   **Username/Password**: `postgres` / `postgres` (Default)
*   **DDL Auto**: `update` (Automatically manages the schema)

To use this configuration when starting the application, you can set the active profile:
*   **Via IntelliJ**: Add `-Dspring.profiles.active=postgres` to the VM options.
*   **Via CLI**: `java -jar app.jar --spring.profiles.active=postgres`

### 2. PostgreSQL Setup Instructions

Follow these steps to prepare your local PostgreSQL database:

#### Step A: Install PostgreSQL
Ensure PostgreSQL is installed and running on your system. You can download it from [postgresql.org](https://www.postgresql.org/download/).

#### Step B: Create the Database
Open your terminal or a tool like `pgAdmin` or `psql` and execute the following commands:

```sql
-- Connect as the default postgres user
-- Create the database used in the properties file
CREATE DATABASE angularai;
```

#### Step C: Database Schema Initialization
You do **not** need to manually create tables or the schema. 
The application is configured with `spring.jpa.hibernate.ddl-auto=update`. When you start the Spring Boot application with the `postgres` profile active:
1.  Hibernate will connect to the `angularai` database.
2.  It will automatically detect the entities (`User`, `Task`) defined in the code.
3.  It will create the corresponding tables (`users`, `tasks`) and constraints (unique emails, etc.) if they do not exist.

### 3. Project Updates
*   Updated `backend/pom.xml` to include the `org.postgresql:postgresql` runtime dependency.
*   The `DataInitializer` remains active and will populate your local PostgreSQL database with sample admin and user accounts upon the first successful startup.