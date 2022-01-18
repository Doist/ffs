# Running

There are a couple of ways to run the dashboard.

!!! info

    Run commands from the root folder of the project.

## Connected to the API

Run dashboard locally, leveraging FFS's production API:

```bash
./gradlew :ffs-dashboard:browserProductionRun # or browserDevelopmentRun
```

## Connected to a local server

Run the server and dashboard while specifying development mode and port:

```bash
KTOR_DEVELOPMENT=true KTOR_DEPLOYMENT_PORT=8080 ./gradlew :ffs-server:run :ffs-dashboard:browserProductionRun # or browserDevelopmentRun
```

This is a particularly good setup for local development, which can be complemented with `--continuous` to have Gradle automatically rebuild on changes:

```bash
export KTOR_DEVELOPMENT=true
export KTOR_DEPLOYMENT_PORT=8080
./gradlew :ffs-server:run &
./gradlew :ffs-dashboard:browserDevelopmentRun --continuous
```
