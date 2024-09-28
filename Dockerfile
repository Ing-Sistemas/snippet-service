FROM gradle:8.7.0-jdk21 AS build

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradle/ ./

COPY src ./src

WORKDIR /home/gradle/src

RUN gradle build

EXPOSE ${PORT}

ENTRYPOINT ["java","-jar","/home/gradle/src/build/libs/Snippet-Service-0.0.1-SNAPSHOT.jar"]