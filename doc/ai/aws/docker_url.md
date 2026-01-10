When running the application via Docker Compose (using `docker compose up`), you can access the components at the following URLs:

### 1. Frontend (Angular)
- **URL**: [http://localhost](http://localhost) (or [http://localhost:80](http://localhost:80))
- **Description**: This serves the Angular application through Nginx. The Nginx server also acts as a reverse proxy, forwarding any requests starting with `/api` to the backend container.

### 2. Backend API (Spring Boot)
- **Direct URL**: [http://localhost:8080](http://localhost:8080)
- **API Base**: [http://localhost:8080/api](http://localhost:8080/api)
- **Description**: While you can access the backend directly on port 8080, the frontend is configured to communicate with it via the proxy at `http://localhost/api`.

### 2. Backend API (Check if it's alive)
If you want to verify the backend is responding, try an existing API endpoint:
- **URL**: [http://localhost:8080/api/auth/login](http://localhost:8080/api/auth/login)
- **Expected Result**: You should see a `405 Method Not Allowed` (since it's a POST endpoint) or a login prompt. This confirms the API is listening.

1.  **Stop and rebuild the containers**:
    Run this command in the project root:
    ```bash
    docker compose up --build -d
    ```

2.  **Verify the initialization**:
    Check the backend logs to ensure the data was created:
    ```bash
    docker compose logs backend | grep DataInitializer
    ```
    You should see: `Sample data initialized: admin/admin123 and user/user123`.



```powershell
docker compose logs backend | Select-String "DataInitializer"
```

Alternatively, you can just view the last few lines of the logs to see the startup process:

```powershell
docker compose logs backend --tail 50
```

3.  **Access the H2 Console**:
    - **URL**: [http://localhost:8080/h2-console/](http://localhost:8080/h2-console/)
    - **JDBC URL**: `jdbc:h2:mem:testdb`
    - **User Name**: `sa`
    - **Password**: *(leave empty)*
    - Click **Connect**. You should now be able to see the `USERS` and `TASKS` tables.



2.  **Test the Proxy via Nginx (Port 80)**:
    Run this on your host to verify that Nginx is correctly forwarding your credentials:
    ```powershell
    curl.exe -i -u admin:admin123 -X POST http://localhost/api/auth/login
    ```
    - If this returns **HTTP 200 OK**, then the main application (Frontend) will now work.



### 3. H2 Console (Database)
- **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Description**: Available if the application is running with the default profile or a profile that enables H2. In the `docker-compose.yml`, the `prod` profile is active, so ensure H2 is enabled in your `application-prod.properties` if you wish to use it.

2.  **Start the application using Docker Compose**:
    Navigate to the project root directory and run:
    ```bash
    docker compose up -d
    ```
    *(The `-d` flag runs them in the background)*

### Summary of Mappings
| Service | Internal Port | Host Port | Access URL |
| :--- | :--- | :--- | :--- |
| **Frontend** | 80 | 80 | [http://localhost](http://localhost) |
| **Backend** | 8080 | 8080 | [http://localhost:8080](http://localhost:8080) |