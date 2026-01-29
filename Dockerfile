# Stage 1: Build the Angular frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ ./
RUN npm run build -- --configuration production

# Stage 2: Build the Spring Boot backend
FROM maven:3-eclipse-temurin-25-alpine AS backend-build
WORKDIR /app
COPY pom.xml .
COPY backend/pom.xml backend/
COPY backend/src backend/src
# Copy frontend build output to backend static resources
COPY --from=frontend-build /app/frontend/dist/frontend /app/backend/src/main/resources/static
RUN mvn -f backend/pom.xml clean package -DskipTests

# Stage 3: Final runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/aibackend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
