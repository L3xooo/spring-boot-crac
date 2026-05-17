FROM maven:4.0.0-rc-5-eclipse-temurin-25-noble AS builder
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn clean install

FROM azul/zulu-openjdk:25-jdk-crac-latest
WORKDIR /app

COPY --from=builder /workspace/target/crac-demo-0.0.1-SNAPSHOT.jar /app/app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080
ENV MODE=normal
ENV CHECKPOINT_DIR=/checkpoint
ENV STARTUP_WAIT_SECONDS=1
VOLUME ["/checkpoint"]
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]




