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

ENV PORT=8080 \
    SPRING_DATASOURCE_URL=jdbc:h2:file:./data/zaplink \
    SPRING_DATASOURCE_USERNAME=sa \
    SPRING_DATASOURCE_PASSWORD= \
    ZAPLINK_SHORT_URL_BASE= \
    ZAPLINK_JWT_SECRET=change-this-secret \
    ZAPLINK_JWT_EXPIRATION_MS=86400000 \
    ZAPLINK_ALLOWED_ORIGINS=http://localhost:8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
