# Proposal: REST API Web UI Integration

This document proposes integrating a web-based UI to expose and document the REST API of the AngularAI application.

## 1. Objective
To provide an interactive, auto-generated documentation for all REST endpoints, allowing developers and testers to explore and test the API directly from the browser.

## 2. Proposed Solution: SpringDoc OpenAPI
We recommend using **springdoc-openapi** for the Spring Boot backend. It is the modern successor to SpringFox and is designed to work seamlessly with Spring Boot 3+ (and 4+).

### Key Features:
- **Swagger UI**: Automatic generation of the Swagger UI at `/swagger-ui.html`.
- **OpenAPI 3.1**: Generates API definitions in standard OpenAPI 3.1 format.
- **Integration**: Supports Spring Security, JSR-303 validation, and generic types.
- **Build Integration**: Can be configured to generate the OpenAPI JSON/YAML during the Maven build phase.

## 3. Implementation Plan

### 3.1 Backend Changes (Spring Boot)

#### Dependency Addition
Add the following dependency to `backend/pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version> <!-- Or latest compatible version for Spring Boot 4 -->
</dependency>
```

#### Security Configuration
Update `SecurityConfig.java` to allow public access to the Swagger UI and OpenAPI endpoints:
```java
.requestMatchers(
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html"
).permitAll()
```

#### API Documentation
Use OpenAPI annotations in controllers (e.g., `AdminUserController`, `TaskController`) to provide detailed descriptions, response codes, and schemas.
```java
@Operation(summary = "Get all tasks", description = "Returns a list of tasks for the current user")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
```

### 3.2 Build Integration
To generate the OpenAPI specification file as part of the build, we will use the `springdoc-openapi-maven-plugin`. This ensures that the documentation is always in sync with the code.

```xml
<plugin>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-maven-plugin</artifactId>
    <version>1.4</version>
    <executions>
        <execution>
            <phase>integration-test</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 4. Expected Outcome
- **Interactive UI**: Accessible at `http://localhost:8080/swagger-ui.html`.
- **API Spec**: JSON version available at `http://localhost:8080/v3/api-docs`.
- **Automated Docs**: No manual documentation updates required for new endpoints.
