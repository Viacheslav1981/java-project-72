FROM eclipse-temurin:20-jdk

ARG GRADLE_VERSION=8.2

WORKDIR /app

COPY . /app/.

RUN ./gradlew installDist

CMD ./build/install/app/bin/app