# FFS server

Server that provides the API for the FFS clients and dashboard. It uses [Ktor](https://ktor.io/), a coroutines-based library for building web applications in Kotlin, and [SQLDelight](https://cashapp.github.io/sqldelight/), a data storage and access library where the database is the canonical source of instances in production.

## Running

There are a few ways to run the server. For configuration, see [below](#configuration).

_Note: Run commands from the root folder of the project._

### Using Gradle

Start the server using Gradle:

```bash
./gradlew :ffs-server:application:run
```

### Using a JAR

Create an executable JAR and start it:

```bash
./gradlew :ffs-server:shadowJar
java -jar ffs-server/build/libs/ffs-server-all.jar
```

### Using Docker

```bash
docker build -t ffs-server -f ffs-server/Dockerfile .
docker run -p 8080:8080 my-application
```

## Configuration

The server can be configured using environment variables or command-line switches:

| Environment variable                               | Command-line switch                                  | Default                                                     | Description                                                                                                                                                                                                         |
|----------------------------------------------------|------------------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <sub>KTOR_DEVELOPMENT</sub>                        | <sub>-Dio.ktor.development=</sub>                    | <sub>true</sub>                                             | <sub>Run in development mode, enabling auto-reload and increasing verbosity.</sub>                                                                                                                                  |
| <sub>KTOR_DEPLOYMENT_PORT</sub>                    | <sub>-port=</sub>                                    | <sub>8080</sub>                                             | <sub>Port to listen on.</sub>                                                                                                                                                                                       |
| <sub>KTOR_DEPLOYMENT_SSL_PORT</sub>                | <sub>-sslPort=</sub>                                 |                                                             | <sub>SSL port to listen on.<br/><em>Note: KTOR_SECURITY_* must be set with this.</em></sub>                                                                                                                         |
| <sub>KTOR_SECURITY_SSL_KEY_STORE</sub>             | <sub>-sslKeyStore=</sub>                             |                                                             | <sub>Path to the SSL key store.</sub>                                                                                                                                                                               |
| <sub>KTOR_SECURITY_SSL_KEY_STORE_PASSWORD</sub>    | <sub>-P:ktor.security.ssl.keyStorePassword=</sub>    |                                                             | <sub>Password for the SSL key store.</sub>                                                                                                                                                                          |
| <sub>KTOR_SECURITY_SSL_KEY_ALIAS</sub>             | <sub>-P:ktor.security.ssl.keyAlias=</sub>            |                                                             | <sub>Key alias in the key store.</sub>                                                                                                                                                                              |
| <sub>KTOR_SECURITY_SSL_PRIVATE_KEY_PASSWORD</sub>  | <sub>-P:ktor.security.ssl.privateKeyPassword=</sub>  |                                                             | <sub>Password for the private key.</sub>                                                                                                                                                                            |
| <sub>SQLDELIGHT_DIALECT</sub>                      | <sub>-PsqldelightDialect=</sub>                      | <sub>sqlite:3.18</sub>                                      | <sub>SQL dialect from SQLDelight's [available options][1].<br/><em>Note: Read at compile time, not runtime.</em><br/><em>Note: Must be consistent with the datasource below.</em></sub>                             |
| <sub>HIKARI_DATASOURCE_CLASSNAME</sub>             | <sub>-P:hikari.dataSourceClassName=</sub>            | <sub>org.sqlite.SQLiteDataSource</sub>                      | <sub>Class name provided by the JDBC driver. See [Hikari's options](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby).<br/><em>Note: must be consistent with the dialect above.</em></sub> |
| <sub>HIKARI_DATASOURCE_URL</sub>                   | <sub>-P:hikari.dataSource.url=</sub>                 | <sub>jdbc:sqlite:</sub><br/><sub><em>(in-memory)</em></sub> | <sub>Location of the database file.<br/>Applicable to file-based databases, like SQLite.</sub>                                                                                                                      |
| <sub>HIKARI_DATASOURSE_DATABASE_NAME</sub>         | <sub>-P:hikari.dataSource.databaseName=</sub>        | <sub><em>Depends on the JDBC driver.</em></sub>             | <sub>Name of the database.<br/>Applicable to connection-based databases, like PostgreSQL or MariaDB.</sub>                                                                                                          |
| <sub>HIKARI_DATASOURCE_SERVER_NAME</sub>           | <sub>-P:hikari.dataSource.serverName=</sub>          | <sub>localhost</sub>                                        | <sub>Name of the server.<br/>Applicable to connection-based databases, like PostgreSQL or MariaDB.</sub>                                                                                                            |
| <sub>HIKARI_DATASOURCE_PORT_NUMBER</sub>           | <sub>-P:hikari.dataSource.portNumber=</sub>          | <em><sub>Depends on the JDBC driver.</sub></em>             | <sub>Port number.<br/>Applicable to connection-based databases, like PostgreSQL or MariaDB.</sub>                                                                                                                   |
| <sub>HIKARI_DATASOURCE_USERNAME</sub>              | <sub>-P:hikari.dataSource.username=</sub>            |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_PASSWORD</sub>              | <sub>-P:hikari.dataSource.password=</sub>            |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_AUTO_COMMIT</sub>           | <sub>-P:hikari.dataSource.autoCommit=</sub>          |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_CONNECTION_TIMEOUT</sub>    | <sub>-P:hikari.dataSource.connectionTimeout=</sub>   |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_IDLE_TIMEOUT</sub>          | <sub>-P:hikari.dataSource.idleTimeout=</sub>         |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_KEEPALIVE_TIMEOUT</sub>     | <sub>-P:hikari.dataSource.keepAliveTimeout=</sub>    |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_MAX_LIFETIME</sub>          | <sub>-P:hikari.dataSource.maxLifetime=</sub>         |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_CONNECTION_TEST_QUERY</sub> | <sub>-P:hikari.dataSource.connectionTestQuery=</sub> |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_MINIMUM_IDLE</sub>          | <sub>-P:hikari.dataSource.minimumIdle=</sub>         |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_MAXIMUM_POOL_SIZE</sub>     | <sub>-P:hikari.dataSource.maximumPoolSize=</sub>     |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |
| <sub>HIKARI_DATASOURCE_POOL_NAME</sub>             | <sub>-P:hikari.dataSource.poolName=</sub>            |                                                             | <sub>Refer to [Hikari's options][2].</sub>                                                                                                                                                                          |

[1]: https://github.com/cashapp/sqldelight/blob/ea6652521765ded7ea91e19a351f827582239377/sqldelight-gradle-plugin/src/main/kotlin/app/cash/sqldelight/gradle/SqlDelightDatabase.kt#L61-L68
[2]: https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby

The defaults are adequate for local development. For production, disable development mode, and switch to an adequate database configuration and dialect.
