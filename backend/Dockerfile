# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Environment variables with default values for local testing
# These will be overridden by Render environment variables
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/greenfox
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV SPRING_DATA_REDIS_HOST=localhost
ENV SPRING_DATA_REDIS_PORT=6379

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

