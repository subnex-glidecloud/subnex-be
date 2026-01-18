FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/email-service-0.0.1-SNAPSHOT.jar email-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "email-service.jar"]
