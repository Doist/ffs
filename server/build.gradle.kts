import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.sqldelight.get().pluginId)
    application
    id(libs.plugins.kotlin.power.assert.get().pluginId)
}

dependencies {
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)

    implementation(libs.sqldelight.driver.sqlite)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.kotlin.test.base)
    testImplementation(libs.ktor.server.test.host)
}

application {
    mainClass.set("ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

sqldelight {
    database("Database") {
        packageName = "doist.ffs"
    }
}
