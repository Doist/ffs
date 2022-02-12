# Running

There are a few ways to run the dashboard.

!!! info

    Run commands from the root folder of the project.

## Using Gradle

Run the dashboard using Gradle:

```bash
./gradlew :ffs-dashboard:browserProductionRun # or browserDevelopmentRun
```

By default, this will connect to a locally running server on `http://localhost:80`. Connect to a different port by specifying `SERVER_PORT`:

```bash
SERVER_PORT=8080 ./gradlew :ffs-dashboard:browserDevelopmentRun
```

And to a different host by specifying `SERVER_HOST`. For example, to connect to FFS's production API:

```bash
SERVER_HOST="https://api.ffs.delivery/v1" ./gradlew :ffs-dashboard:browserProductionRun
```

## Using Docker

Build an image and run it:

```bash
docker build -t ffs-dashboard -f ffs-dashboard/Dockerfile .
docker run -p 3000:3000 ffs-dashboard
```

To specify a different port or host, pass the `SERVER_PORT` or `SERVER_HOST` build arguments:

```bash
docker build --build-arg SERVER_HOST="https://api.ffs.delivery/v1" -t ffs-dashboard -f ffs-dashboard/Dockerfile .
docker run -p 3000:3000 ffs-dashboard
```
