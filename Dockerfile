# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# All environment variables should be set in Digital Ocean App Platform
# Do not hardcode them here as they will override the runtime configuration

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
