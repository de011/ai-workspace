# Base Image
FROM eclipse-temurin:21-jdk

# Working Directory
WORKDIR /app

# Copy JAR
COPY target/*.jar app.jar

# Expose Port
EXPOSE 8080

# Run Application
ENTRYPOINT ["java","-jar","app.jar"]