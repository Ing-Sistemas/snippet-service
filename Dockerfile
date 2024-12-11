FROM openjdk:21-jdk
RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml
ARG JAR_FILE=build/libs/Snippet-Service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /home/app.jar
ENTRYPOINT ["java","-javaagent:/usr/local/newrelic/newrelic.jar","-jar","/home/app.jar"]
