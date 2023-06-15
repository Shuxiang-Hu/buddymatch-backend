FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests
EXPOSE 8080
# Run the web service on container startup.
CMD ["java","-jar","/app/target/buddy-match-backend-0.0.1-SNAPSHOT.jar"]