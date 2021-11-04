import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

group = "doist"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlin.power.assert) apply false
    alias(libs.plugins.kotlinx.benchmark) apply false

    alias(libs.plugins.kotlinx.kover)
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
    output.set(rootProject.buildDir.resolve("reports/detekt/merge.sarif"))
}

subprojects {
    afterEvaluate {
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
