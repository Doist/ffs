# Running

There are a few ways to run the server.

!!! info

    Run commands from the root folder of the project.

## Using Gradle

Start the server using Gradle:

```bash
./gradlew :ffs-server:application:run
```

## Using a JAR

Create an executable JAR and start it:

```bash
./gradlew :ffs-server:shadowJar
java -jar ffs-server/build/libs/ffs-server-all.jar
```

## Using Docker

Build an image and run it:

```bash
docker build -t ffs-server -f ffs-server/Dockerfile .
docker run -p 8080:8080 ffs-server
```
