## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-native-image:22.2-java11 AS build
USER root
RUN microdnf install findutils
USER quarkus
WORKDIR /code
COPY --chown=quarkus:quarkus gradlew /code/gradlew
COPY --chown=quarkus:quarkus gradle /code/gradle
RUN ./gradlew
COPY --chown=quarkus:quarkus gradle.properties /code/gradle.properties
COPY --chown=quarkus:quarkus settings.gradle /code/settings.gradle
COPY --chown=quarkus:quarkus buildSrc /code/buildSrc
COPY --chown=quarkus:quarkus common /code/common
COPY --chown=quarkus:quarkus core /code/core
COPY --chown=quarkus:quarkus tasmota-connector /code/tasmota-connector
RUN ./gradlew jar
ARG APP
RUN ./gradlew :$APP:quarkusBuild -Dquarkus.package.type=native -Dquarkus.native.native-image-xmx=8G

## Stage 2 : create the docker final image
FROM quay.io/quarkus/quarkus-micro-image:1.0
WORKDIR /work/
COPY --from=build /code/build/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
