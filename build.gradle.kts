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
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import ru.vyarus.gradle.plugin.mkdocs.task.MkdocsTask

group = "doist"
version = "0.1-SNAPSHOT"

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.mkdocs)

    // Plugins for all subprojects.
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.kotlin.power.assert) apply false // Applied below.
    alias(libs.plugins.detekt) apply false // Applied and configured below.

    // Plugins for multiple, but not all, subprojects.
    // Multiplatform is loaded indirectly in buildSrc/build.gradle.kts.
    alias(libs.plugins.kotlin.serialization) apply false
}

plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().apply {
        lockFileDirectory = project.rootDir.resolve("js")
    }
}

plugins.withType<NodeJsRootPlugin> {
    the<NodeJsRootExtension>().nodeVersion = libs.versions.node.get()
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
        configure<com.bnorm.power.PowerAssertGradleExtension> {
            functions = listOf(
                "kotlin.assert",
                "kotlin.test.assertEquals",
                "kotlin.test.assertTrue",
            )
        }

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

        // Apply and configure dokka in all subprojects.
        apply(plugin = libs.plugins.dokka.get().pluginId)
        tasks.withType<DokkaTaskPartial>().configureEach {
            moduleName.set(project.name.removePrefix("ffs-").replace('-', ' ').capitalize())
            dokkaSourceSets {
                configureEach {
                    sourceLink {
                        localDirectory.set(rootDir)
                        remoteUrl.set(java.net.URL("https://github.com/Doist/ffs/blob/main"))
                        remoteLineSuffix.set("#L")
                    }
                }
            }
        }
    }
}

// Configure documentation.
python.pip("mkdocs-awesome-pages-plugin:${libs.versions.mkdocs.plugin.awesome.pages.get()}")
mkdocs {
    sourcesDir = "docs"
    strict = false
    publish.docPath = ""
}
tasks.withType<MkdocsTask>().configureEach {
    val dokkaHtmlMultiModule by tasks.getting
    dependsOn(dokkaHtmlMultiModule)
}


// Install git hooks automatically.
gradle.taskGraph.whenReady {
    val from = File("${rootProject.rootDir}/config/detekt/pre-commit")
    val to = File("${rootProject.rootDir}/.git/hooks/pre-commit")
    from.copyTo(to, overwrite = true)
    to.setExecutable(true)
}
