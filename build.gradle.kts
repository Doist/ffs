import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import ru.vyarus.gradle.plugin.mkdocs.task.MkdocsTask

group = "doist"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.mkdocs)

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

plugins.withType<NodeJsRootPlugin> {
    the<NodeJsRootExtension>().nodeVersion = "16.13.1"
}

allprojects {
    repositories {
        mavenCentral()
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
        }
        dependencies {
            val detektPlugins by configurations.getting
            detektPlugins(libs.detekt.formatting)
        }
        plugins.withType(DetektPlugin::class) {
            tasks.withType<Detekt>().configureEach detekt@{
                reports.sarif.required.set(true)
                finalizedBy(generateDetektReport)
                generateDetektReport.configure {
                    input.from(this@detekt.sarifReportFile)
                }
            }
        }

        // Log test output and results to standard streams.
        tasks.withType<AbstractTestTask>().configureEach {
            testLogging {
                events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL
                showStandardStreams = true
                showStackTraces = true
            }
        }

        // Apply and configure dokka in library subprojects.
        if (project.name.startsWith("ffs-library-")) {
            apply(plugin = libs.plugins.dokka.get().pluginId)
            tasks.withType<DokkaTaskPartial>().configureEach {
                moduleName.set(project.name.removePrefix("ffs-").replace('-', ' ').capitalize())
            }
        }
    }
}

// Configure documentation.
python.pip("mkdocs-awesome-pages-plugin:${libs.versions.mkdocs.plugin.awesome.pages.get()}")
mkdocs {
    sourcesDir = "docs"
    strict = false
}
tasks.withType<MkdocsTask>().configureEach {
    val dokkaGfmMultiModule by tasks.getting
    dependsOn(dokkaGfmMultiModule)
}


// Install git hooks automatically.
gradle.taskGraph.whenReady {
    val from = File("${rootProject.rootDir}/config/detekt/pre-commit")
    val to = File("${rootProject.rootDir}/.git/hooks/pre-commit")
    from.copyTo(to, overwrite = true)
    to.setExecutable(true)
}
