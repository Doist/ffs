import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.Detekt
import java.io.FileFilter

group = "doist"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("multiplatform") version "1.5.31" apply false
    id("io.gitlab.arturbosch.detekt") version "1.18.1" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

val generateDetektReport by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.buildDir.resolve("reports/detekt/merge.sarif"))
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    configure<DetektExtension> {
        buildUponDefaultConfig = true
        source = files(*projectDir.resolve("src").listFiles(FileFilter { it.isDirectory }).orEmpty())
        parallel = true
        reports.sarif.enabled = true
    }
    dependencies {
        val detektPlugins by configurations
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.18.1")
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
