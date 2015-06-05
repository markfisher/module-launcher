# Module Launcher

The Module Launcher provides a single entry point that bootstraps module JARs located in a home directory. A single Docker image that contains such a directory of module JARs can then be used to launch any of those JARs based on an environment variable. When running standalone, a system property may be used instead of an environment variable, so that multiple instances of the Module Launcher may run on a single machine. The following examples demonstrate running the modules for the *ticktock* stream (`time | log`).

## Prerequisites

1: clone and build this project:

````
git clone https://github.com/markfisher/module-launcher.git
cd module-launcher
./gradlew build
cd ..
````

2: clone and build the spring-bus project:

````
git clone https://github.com/spring-projects/spring-bus.git
cd spring-bus
mvn package
cd ..
````

3: copy the spring-bus source and sink sample JARs to `/opt/spring/modules`:

````
mkdir -p /opt/spring/modules
cp spring-bus/spring-xd-samples/source/target/spring-xd-module-runner-sample-source-1.0.0.BUILD-SNAPSHOT.jar /opt/spring/modules/
cp spring-bus/spring-xd-samples/sink/target/spring-xd-module-runner-sample-sink-1.0.0.BUILD-SNAPSHOT.jar /opt/spring/modules/
````

4: start redis locally via `redis-server` (optionally start `redis-cli` and use the `MONITOR` command to watch activity)

## Running Standalone

````
java -Dmodule=time -jar module-launcher/build/libs/module-launcher-0.0.1-SNAPSHOT.jar
java -Dmodule=log -jar  module-launcher/build/libs/module-launcher-0.0.1-SNAPSHOT.jar
````

The time messages will be emitted every 5 seconds. The console for the log module will display each:

````
2015-06-05 12:39:58.896  INFO 51078 --- [hannel-adapter1] config.ModuleDefinition                  : Received: 2015-06-05 12:39:58
2015-06-05 12:40:02.699  INFO 51078 --- [hannel-adapter1] config.ModuleDefinition                  : Received: 2015-06-05 16:39:52
2015-06-05 12:40:03.897  INFO 51078 --- [hannel-adapter1] config.ModuleDefinition                  : Received: 2015-06-05 12:40:03
````

## Running with Docker

1: build the module-launcher Docker image, including a copy of the module directory:

````
cd module-launcher
mkdir artifacts
cp -r /opt/spring/modules artifacts/
./dockerize.sh
````

2: run each module as a docker process by passing environment variables for the module name as well as the host machine's IP address for the redis connection to be established within the container:

````
docker run -p 8080:8080 -e MODULE=time -e SPRING_REDIS_HOST=<host.ip> 192.168.59.103:5000/module-launcher
docker run -p 8081:8081 -e MODULE=log -e SPRING_REDIS_HOST=<host.ip> 192.168.59.103:5000/module-launcher
````
