# Proposal: Database Increment Handling (Flyway & Room)

This document outlines the strategy for managing database schema changes (increments) for the AngularAI project, covering both the Spring Boot backend (Postgres) and the Android mobile client (Room).

## 1. Backend: Recommendation & Setup (Flyway)

We recommend using **Flyway** for backend database migrations. Flyway is the industry standard for Java/Spring Boot applications, offering robust versioning and seamless integration.

### 1.1. Why Flyway?
- **Version Control for DB**: Every change is a SQL script in your VCS (Git).
- **Automatic Execution**: Migrations run automatically on application startup.
- **Environment Consistency**: Ensures all environments (Local, Docker, AWS) are in the same state.
- **Verification**: Checksums prevent accidental modification of already applied scripts.

### 1.2. Setup in Spring Boot
1.  **Dependency**: Add to `backend/pom.xml`:
    ```xml
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    ```
2.  **Configuration**: Modify `application.properties`:
    ```properties
    # Disable Hibernate's automatic schema generation in favor of Flyway
    spring.jpa.hibernate.ddl-auto=validate
    
    # Flyway settings
    spring.flyway.enabled=true
    spring.flyway.locations=classpath:db/migration
    spring.flyway.baseline-on-migrate=true
    ```
3.  **Directory Structure**:
    Create `backend/src/main/resources/db/migration/` for migration scripts.

### 1.3. Migration Naming Convention
Scripts follow the pattern: `V<Version>__<Description>.sql`
- `V1__init_schema.sql`
- `V2__add_status_to_tasks.sql`

## 2. From Local to AWS Postgres

Flyway manages the transition from local development to AWS RDS automatically.

### 2.1. Local Development
1.  Developer creates a new SQL script in `db/migration`.
2.  Backend is started locally (`postgres` profile).
3.  Flyway applies the script to the local Postgres container/instance.
4.  Developer tests the change and commits the script to Git.

### 2.2. AWS Deployment
1.  The CI/CD (GitHub Actions/Manual script) builds the Docker image containing the new SQL script.
2.  The image is pushed to AWS ECR.
3.  The ECS Service (Fargate) is restarted.
4.  **On Startup**: The Spring Boot application connects to the AWS RDS instance. Flyway checks the `flyway_schema_history` table, detects the new script, and executes it against the AWS database before the application fully starts.

**Security Note**: Ensure the RDS Security Group allows the Fargate task to perform DDL operations (usually standard for the application user in smaller setups).

## 3. Android Room Migrations

Android Room requires manual migration handling to preserve user data on the device.

### 3.1. Implementation
When changing the Android database schema:
1.  Update the `@Database(version = X)` in `AppDatabase.kt`.
2.  Implement a `Migration` object:
    ```kotlin
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN new_field TEXT")
        }
    }
    ```
3.  Add the migration to the Room builder:
    ```kotlin
    Room.databaseBuilder(...)
        .addMigrations(MIGRATION_2_3)
        .build()
    ```

### 3.2. Auto-Migrations (Alternative)
For simple changes (adding columns/tables), Room 2.4+ supports `autoMigrations`:
```kotlin
@Database(
    version = 3,
    entities = [TaskEntity::class],
    autoMigrations = [AutoMigration(from = 2, to = 3)]
)
```

## 4. Coordination Strategy

To sync Backend and Android changes:

1.  **Backwards Compatibility**: The Backend change must be deployed **first**. Ensure the API is backwards compatible (e.g., new fields are nullable or have defaults) so old Android versions don't crash.
2.  **Version Check**: Implement a `/api/system/info` endpoint (already exists) that returns the "Minimum Required Android Version" or "DB Schema Version" if critical breaks occur.
3.  **Deployment Window**:
    - Step 1: Deploy Backend with Flyway script.
    - Step 2: Publish Android update to Play Store/Testing.
    - Android Room migration will trigger as soon as the user updates and opens the app.

## 5. Summary of Recommended Workflow

1.  **Design**: Define new fields/tables.
2.  **Backend**: Write `V<N>__....sql` script -> Run locally -> Test.
3.  **Android**: Update `TaskEntity` -> Update `AppDatabase` version -> Write `Migration` object -> Test.
4.  **Deploy**: Push code -> Backend updates AWS DB via Flyway -> User updates Android app -> Room migrates local DB.

---
*Implementation completed on 2026-01-09*

## 7. Current Implementation Status

### 7.1. Backend (Implemented)
- **Flyway** is integrated into the `backend` module.
- **Initial Schema**: `V1__init_schema.sql` creates `users`, `tasks`, and `action_log` tables.
- **Configuration**: Managed via `FlywayConfig.java` to ensure correct bean initialization order and `application.properties` with `spring.jpa.hibernate.ddl-auto=none`.

### 7.2. Android (Implemented)
- **Room Persistence**: Configured in `AppDatabase.kt`.
- **Migrations**: `MIGRATION_1_2` is already in place to support `status` and `position` fields in `TaskEntity`.

## 6. Automated Migration Script Generation

Generating Flyway scripts manually can be error-prone. There are several ways to automate or assist in this process.

### 6.1. Using Junie (AI-Assisted)
Junie can generate the SQL migration script by analyzing the changes in your `@Entity` classes since the last release or commit.

**Workflow:**
1.  Modify your `@Entity` classes (e.g., add a field, change a column).
2.  Ask Junie: *"Create a Flyway migration script based on the changes to Task.java"*.
3.  Junie will compare the current state with the previous one (via Git) and provide the `V<N>__....sql` content.

### 6.2. JpaBuddy (IntelliJ Plugin)
For developers using IntelliJ IDEA, **JpaBuddy** is the recommended tool for local development.
-   **Feature**: "Diff Entities and Database".
-   **Action**: It compares your current JPA entities with your local database schema and generates a Flyway migration script automatically.
-   **Pros**: Visual, handles complex mappings, very fast for developers.

### 6.3. Hibernate Schema Generation (Automated Export)
You can configure Hibernate to export the schema change to a file during the build process using the `javax.persistence.schema-generation.scripts.action` property.

However, a more integrated way for Spring Boot is using the `hibernate-ant` or specialized Maven plugins to generate the "drop-and-create" script and then diffing it.

### 6.4. Recommended Process for "Release to Release"
If you are moving from version `0.0.1` to `0.0.2`:

1.  **Baseline**: Ensure your local DB matches the `0.0.1` schema.
2.  **Develop**: Update Entities in `0.0.2`.
3.  **Generate**: Use JpaBuddy or Junie to create the diff script.
4.  **Review**: Always manually review the generated SQL (especially for `NOT NULL` constraints without defaults).
5.  **Commit**: Save to `src/main/resources/db/migration/`.
