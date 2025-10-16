# Use an OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory in container
WORKDIR /app

# Copy the built jar file into container
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
