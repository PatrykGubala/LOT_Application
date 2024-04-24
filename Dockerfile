FROM maven:3.6.3-openjdk-8 as builder

COPY src /app/src
COPY pom.xml /app

WORKDIR /app

RUN mvn clean package

FROM openjdk:8-jre-slim

COPY --from=builder /app/target/LOT_App-1.0-SNAPSHOT-jar-with-dependencies.jar /app/application.jar

WORKDIR /app

CMD ["java", "-jar", "application.jar"]