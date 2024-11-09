#FROM maven:latest
#WORKDIR /app
#COPY . .
#RUN mkdir -p /var/logs
#RUN mvn clean package -DskipTests
#CMD ["java", "-jar", "/app/target/healthcheck.jar"]

#
# Build stage
#
FROM maven:latest AS build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests clean package

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar /app/healthcheck.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/healthcheck.jar"]