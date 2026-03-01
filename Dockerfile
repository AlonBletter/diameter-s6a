FROM gradle:8.8-jdk21 AS build

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts /workspace/
COPY gradle /workspace/gradle

COPY src /workspace/src

RUN chmod +x ./gradlew \
 && ./gradlew --no-daemon clean installDist

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/build/install/diameter-s6a /app/

ENTRYPOINT ["/app/bin/diameter-s6a"]
