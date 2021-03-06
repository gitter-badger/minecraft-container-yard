FROM java:8-jdk

MAINTAINER itzg

VOLUME /data /certs

COPY . /build
COPY certs /certs

RUN cd /build && ./mvnw -B package

RUN cp /build/target/mccy-swarm-*.jar /usr/local/bin/mccy-swarm.jar && rm -rf /build && rm -rf $HOME/.m2
RUN ls -l /usr/local/bin/mccy-swarm.jar /certs
WORKDIR /data

ENV SPRING_PROFILES_ACTIVE docker

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/local/bin/mccy-swarm.jar"]