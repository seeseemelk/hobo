## Stage 1 : build with maven builder image with native capabilities
#FROM quay.io/quarkus/ubi-quarkus-native-image:22.2-java11 AS build
FROM ghcr.io/graalvm/native-image:22.2.0 as build
ENTRYPOINT /bin/sh
USER root
RUN microdnf install findutils
WORKDIR /code
COPY gradlew /code/gradlew
COPY gradle /code/gradle
COPY gradle.properties /code/gradle.properties
COPY settings.gradle /code/settings.gradle
COPY buildSrc /code/buildSrc
COPY common /code/common
COPY core /code/core
COPY tasmota-connector /code/tasmota-connector
ARG APP
RUN ./gradlew :$APP:quarkusBuild -Dquarkus.package.type=native -Dquarkus.native.native-image-xmx=2G

## Stage 2 : create the docker final image
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
ARG APP
COPY --from=build /code/$APP/build/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
