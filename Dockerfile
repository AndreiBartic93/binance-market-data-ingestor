# Build stage
# We use a JDK image here because we need Java + Gradle
# to compile the Spring Boot application and generate the final JAR.
FROM eclipse-temurin:21-jdk AS builder

#Set the working directory inside the Docker image.
WORKDIR /app

#Copy Gradle wrapper files first.
#This helps Docker cache dependencies better between builds
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

#Make sure the Grdle wrapper is executable
RUN chmod +x ./gradlew

#Copy the application source code
COPY src src

#Build the Spring Boot executable JAR.
#--no-daemon is recommended in Docker builds because the container is short-lived
RUN ./gradlew clean bootJar --no-daemon

#Runtime stage:
FROM eclipse-temurin:21-jre

#Set the working directory for the runtime container.
WORKDIR /app

#Copy the generated JAR from the build stage
COPY --from=builder /app/build/libs/*.jar app.jar

#Expose the Spring Boot default port
EXPOSE 8080

#Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
