#!/bin/bash

if [ ! -f "Dockerfile" ]; then
	echo "Dockerfile not found"
	exit 1
fi

if [ ! -f "build/libs/spring-cloud-stream-module-launcher-0.0.1-SNAPSHOT.jar" ]; then
	echo "JAR not available; run gradlew build first"
	exit 1
fi

docker build -t 192.168.59.103:5000/module-launcher .
