import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    application
}

dependencies {
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test.jvm)
    testImplementation(libs.ktor.server.test.host)
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
