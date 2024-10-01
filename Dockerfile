FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/Snippet-Service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /home/app.jar
ENTRYPOINT ["java","-jar","/home/app.jar"]