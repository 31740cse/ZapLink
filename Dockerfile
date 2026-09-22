# Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Run the packaged application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV ZAPLINK_JWT_EXPIRATION_MS=86400000

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
