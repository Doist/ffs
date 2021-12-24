import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma
import java.io.File

/**
 * Uses the first available browser to run tests.
 *
 * Prefers headless variants and chooses Chrome when that option is available.
 */
fun KotlinKarma.useAnyBrowser() {
    // List all browsers from karma.config.d/select-browser.js, so that the Kotlin plugin
    // downloads their runners and captures failures. "karma-detect-browsers" (added below)
    // figures out  which browser to enable, depending on what's installed in the base system.
    useChromeHeadless()
    useChromiumHeadless()
    useFirefoxHeadless()
    useFirefoxDeveloperHeadless()
    useOpera()
    useSafari()
    useIe()

    // Use a common karma.config.d/ in the root.
    useConfigDirectory(File(compilation.project.rootProject.projectDir, "karma.config.d"))

    // Depend on "karma-detect-browsers" for browser selection.
    compilation.dependencies {
        implementation(npm("karma-detect-browsers", "^2.0"))
    }
}
