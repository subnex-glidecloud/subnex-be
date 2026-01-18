FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/payment-service-1.0.0.jar payment-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "payment-service.jar"]
