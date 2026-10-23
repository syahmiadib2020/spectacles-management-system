FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:17-jre
RUN groupadd --system meteor && useradd --system --gid meteor meteor
WORKDIR /app
RUN mkdir -p /app/data && chown -R meteor:meteor /app
COPY --from=build --chown=meteor:meteor /build/target/meteor-eyewear-1.0.0.jar /app/app.jar
USER meteor
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8080
VOLUME ["/app/data"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
