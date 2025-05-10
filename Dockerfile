FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/Air_Ticket-0.0.1-SNAPSHOT.jar /app/Air_Ticket.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/Air_Ticket.jar"]