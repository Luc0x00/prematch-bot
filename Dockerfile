# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/Bettings-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-cp", "app.jar", "org.example.Main"]