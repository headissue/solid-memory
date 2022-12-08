FROM maven:3.8.6-eclipse-temurin-11-alpine as build
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml /build
#Download all required dependencies into one layer
RUN mvn -B dependency:resolve dependency:resolve-plugins
#Copy source code
COPY src /build/src
# Build application
RUN mvn package

FROM adoptopenjdk/openjdk11
COPY --from=build /build/target/solid-memory-*.jar app.jar
EXPOSE 8080
RUN mkdir -p /tmp
ENV FILE_STORE_DIR=/tmp; STAGE=local
ENTRYPOINT java -jar app.jar