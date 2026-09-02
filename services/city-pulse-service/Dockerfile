# Multi-stage build: compile with the Maven Wrapper, run on a small trusted JRE image,
# and run as a non-root user (CLAUDE.md section 33).
#
# Build:  docker build -t citypulse/city-pulse-service .
# Run:    docker run -i --rm -p 8080:8080 citypulse/city-pulse-service

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw && ./mvnw -q package -DskipTests

FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24
ENV LANGUAGE='en_US:en'

COPY --chown=185 --from=build /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /workspace/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /workspace/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
