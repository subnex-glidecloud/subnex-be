FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "auth-service.jar"]