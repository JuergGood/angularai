# Stage 1: Build the Angular frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm install --legacy-peer-deps
COPY frontend/ ./
RUN npm run build -- --configuration production

# Stage 2: Build the Spring Boot backend
FROM maven:3.9.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app

COPY pom.xml .
COPY backend/pom.xml backend/
COPY test-client/pom.xml test-client/
COPY dependency-check-suppressions.xml .

# Pre-fetch dependencies to improve build reliability and use caching
# We use the root pom.xml to resolve all common parent dependencies first
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline dependency:resolve-plugins -B || true
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f backend/pom.xml dependency:go-offline dependency:resolve-plugins -B || true
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f test-client/pom.xml dependency:go-offline dependency:resolve-plugins -B || true

# Copy backend source
COPY backend/src backend/src

# Copy frontend build output to backend static resources
# Modern Angular (17+) might output to dist/frontend/browser
RUN --mount=type=bind,from=frontend-build,source=/app/frontend/dist,target=/app/frontend/dist \
    mkdir -p /app/backend/src/main/resources/static && \
    if [ -d "/app/frontend/dist/frontend/browser" ]; then \
        cp -r /app/frontend/dist/frontend/browser/* /app/backend/src/main/resources/static/; \
    else \
        cp -r /app/frontend/dist/frontend/* /app/backend/src/main/resources/static/; \
    fi

# Create data directory for dependency-check to allow volume mounting
# and build the package. Use a secret for NVD_API_KEY if available.
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=secret,id=NVD_API_KEY \
    mkdir -p data/dependency-check && \
    if [ -f /run/secrets/NVD_API_KEY ]; then \
        export NVD_API_KEY=$(cat /run/secrets/NVD_API_KEY); \
    fi && \
    mvn -f backend/pom.xml clean package -DskipTests -Dcheckstyle.skip

# Stage 3: Final runtime image
FROM eclipse-temurin:21-jre-alpine
# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=backend-build /app/backend/target/aibackend-*.jar app.jar
# Create data directory and set permissions
RUN mkdir -p data/dependency-check && chown -R spring:spring /app
EXPOSE 8080
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]
