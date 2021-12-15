import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    id(libs.plugins.sqldelight.get().pluginId)
    application
    id(libs.plugins.kotlinx.benchmark.get().pluginId)
}

sourceSets.create("benchmark")

repositories {
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

dependencies {
    implementation(libs.bundles.ktor.server)

    implementation(libs.sqldelight.driver.sqlite)
    implementation(libs.sqldelight.coroutines.extensions)
    implementation(libs.kotlinx.datetime)

    implementation(libs.better.parse)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlinx.benchmark.runtime)

    val benchmarkImplementation by configurations.getting
    benchmarkImplementation(sourceSets.main.get().let { it.output + it.compileClasspath })
    benchmarkImplementation(libs.kotlinx.benchmark.runtime)
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

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalStdlibApi")
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
}

sqldelight {
    database("Database") {
        packageName = "doist.ffs"
    }
}

benchmark {
    configurations.named("main") {
        warmups = 2
        iterations = 3
        iterationTime = 5
        reportFormat = "csv"
        advanced("nativeGCAfterIteration", "true")
    }

    targets {
        register("benchmark")
    }
}
