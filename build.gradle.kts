import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

group = "doist"
version = "1.0-SNAPSHOT"

plugins {
    // Plugins for all subprojects.
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.kotlin.power.assert) apply false // Applied below.
    alias(libs.plugins.detekt) apply false // Applied and configured below.

    // Plugins for some subprojects.
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinx.benchmark) apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
    }
}

val generateDetektReport by tasks.registering(ReportMergeTask::class) {
    output.set(File("${rootProject.buildDir}/reports/detekt/merge.sarif"))
}

subprojects {
    afterEvaluate {
        // Apply kotlin-power-assert in all subprojects.
        // Build targets must be set before this happens.
        apply(plugin = libs.plugins.kotlin.power.assert.get().pluginId)

        // Apply and configure detekt in all subprojects.
        // Allows fetching source sets dynamically and having dedicated tasks for each.
        apply(plugin = libs.plugins.detekt.get().pluginId)
        configure<DetektExtension> {
            buildUponDefaultConfig = true
            val kotlinExtension = extensions.getByName("kotlin") as KotlinProjectExtension
            source = files(kotlinExtension.sourceSets.flatMap { it.kotlin.srcDirs })
            parallel = true
            reports.sarif.enabled = true
        }
        dependencies {
            val detektPlugins by configurations.getting
            detektPlugins(libs.detekt.formatting)
        }
        plugins.withType(DetektPlugin::class) {
            tasks.withType(Detekt::class) detekt@{
                finalizedBy(generateDetektReport)
                generateDetektReport.configure {
                    input.from(this@detekt.sarifReportFile)
                }
            }
        }
    }
}

// Install git hooks automatically.
gradle.taskGraph.whenReady {
    val from = File("${rootProject.rootDir}/config/detekt/pre-commit")
    val to = File("${rootProject.rootDir}/.git/hooks/pre-commit")
    from.copyTo(to, overwrite = true)
    to.setExecutable(true)
}
