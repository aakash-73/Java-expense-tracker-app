# -------- Stage 1: Build --------
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy source code and pom.xml
COPY . .

# Build the Spring Boot application
RUN mvn clean package -DskipTests

# -------- Stage 2: Run --------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy only the built JAR from the previous stage
COPY --from=build /app/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port used by auth-service
EXPOSE 8081

# Command to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
