#FROM openjdk:21-jdk
#
#WORKDIR /app
#COPY target/*.jar app.jar
#COPY src/main/resources/application.properties application.properties
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
FROM maven:latest
WORKDIR /app
COPY . .
RUN mkdir -p /var/logs
RUN mvn clean package -DskipTests
CMD ["java", "-jar", "target/*.jar"]