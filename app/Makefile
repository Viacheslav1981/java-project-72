clean:
	./gradlew clean

build:
	./gradlew clean build

install:
	./gradlew clean install

start:
	APP_ENV=development ./gradlew run

run-dist:
	./build/install/app/bin/app

start-dist:
	APP_ENV=production ./build/install/app/bin/app

run:
	./gradlew run

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

update-deps:
	./gradlew useLatestVersions

stage:
	./gradlew stage

.PHONY: build