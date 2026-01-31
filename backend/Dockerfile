# Build stage
FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY backend/pom.xml backend/
COPY backend/src backend/src
RUN mvn -f backend/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine
# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /app/backend/target/aibackend-*.jar app.jar
# Set permissions
RUN chown -R spring:spring /app
EXPOSE 8080
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]
