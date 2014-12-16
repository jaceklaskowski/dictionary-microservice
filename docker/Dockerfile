FROM debian:wheezy
MAINTAINER Kamil Domański <kamil@domanski.co>

RUN apt-get -y update && \
    apt-get install --no-install-recommends -y -qq openjdk-7-jre-headless && \
    apt-get clean all && \
    rm -rf /var/lib/apt/lists/*

COPY dictionary-microservice-assembly-0.1-SNAPSHOT.jar /dictionary-microservice-assembly-0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/dictionary-microservice-assembly-0.1-SNAPSHOT.jar", "0.0.0.0"]
