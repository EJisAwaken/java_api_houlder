# Use OpenJDK 17 slim as the base image
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the source code to the container
COPY src/ /app/src/

# Compile the Java code
RUN mkdir -p /app/classes && \
    javac -d /app/classes $(find /app/src -name "*.java") && \
    mkdir -p /app/classes/static && \
    cp -r /app/src/main/resources/static/* /app/classes/static/

# Set the classpath and run the application
CMD ["java", "-cp", "/app/classes", "org.example.Main"]

# Expose port 8080
EXPOSE 8080
