package doist.ffs

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.db.Flag
import doist.ffs.db.Organization
import doist.ffs.db.Project
import kotlinx.datetime.Instant
import java.util.logging.Logger

var defaultDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

/**
 * Sets the default database file path. When blank, an in-memory database is used.
 */
fun setDefaultDatabasePath(path: String) {
    defaultDriver = JdbcSqliteDriver("${JdbcSqliteDriver.IN_MEMORY}$path")
}

/**
 * Returns a [Database] instance for [driver].
 */
fun getDatabase(driver: SqlDriver = defaultDriver): Database {
    migrateIfNeeded(driver)
    driver.execute(null, "PRAGMA foreign_keys=ON", 0)
    return Database(
        driver = driver,
        organizationAdapter = Organization.Adapter(instantAdapter, instantAdapter),
        projectAdapter = Project.Adapter(instantAdapter, instantAdapter),
        flagAdapter = Flag.Adapter(instantAdapter, instantAdapter),
    )
}

/**
 * Runs [block] with [Database] as its argument.
 */
inline fun <T> withDatabase(
    driver: SqlDriver = defaultDriver,
    crossinline block: (database: Database) -> T
): T = driver.use {
    return block(getDatabase(it))
}

/**
 * Runs [block], typically an insert, and returns the id of the last inserted row.
 */
fun Database.capturingLastInsertId(block: Database.() -> Unit) =
    transactionWithResult<Long> {
        block()
        internalQueries.lastInsertId().executeAsOne()
    }

private val log = Logger.getLogger("sqldelight")

private val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long) = Instant.fromEpochSeconds(databaseValue, 0)
    override fun encode(value: Instant) = value.epochSeconds
}

private fun migrateIfNeeded(driver: SqlDriver) {
    val oldVersion =
        driver.executeQuery(null, "PRAGMA user_version", 0)
            .takeIf { it.next() }?.getLong(0)?.toInt() ?: 0

    val newVersion = Database.Schema.version

    if (oldVersion == 0) {
        log.info("Creating database version $newVersion")
        Database.Schema.create(driver)
        driver.execute(null, "PRAGMA user_version=$newVersion", 0)
    } else if (oldVersion < newVersion) {
        log.info("Migrating database from version $oldVersion to $newVersion")
        Database.Schema.migrate(driver, oldVersion, newVersion)
        driver.execute(null, "PRAGMA user_version=$newVersion", 0)
    }
}
