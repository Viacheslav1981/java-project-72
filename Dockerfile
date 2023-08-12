FROM eclipse-temurin:20-jdk

ARG GRADLE_VERSION=8.2

WORKDIR /app

COPY /app .

RUN gradle installDist

CMD app/build/install/app/bin/app