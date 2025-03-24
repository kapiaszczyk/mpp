# Dockerfile for building the mpp-core module
FROM maven:3.9.9-eclipse-temurin-23 as build

# Set the working directory
WORKDIR /app

# Copy the POM file
COPY pom.xml .

# Download dependencies to cache them
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Final image with the JAR
FROM eclipse-temurin:23-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/mpp-core-*.jar /app/mpp-core.jar

# Expose the port
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "mpp-core.jar"]


