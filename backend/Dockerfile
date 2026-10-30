FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl services/dashboard-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/services/dashboard-service/target/dashboard-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
