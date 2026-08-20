FROM eclipse-temurin:25-jre-alpine

WORKDIR /opt/runner-app

COPY runner-app/build/libs/runner-app-all.jar runner-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "runner-app.jar"]
