
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

/**
 * Enable all relevant multiplatform targets.
 */
fun KotlinMultiplatformExtension.enableMultiplatformTargets(
    configureJs: (KotlinJsTargetDsl.() -> Unit)? = null
) {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(IR) {
        browser {
            testTask {
                useKarma {
                    useAnyBrowser()
                }
            }
        }
        nodejs()
        configureJs?.invoke(this)
    }

    iosX64()
    iosArm64()

    watchosX64()
    watchosArm32()
    watchosArm64()

    tvosX64()
    tvosArm64()

    macosX64()
    macosArm64()

    linuxX64()
    //linuxArm64()

    //mingwX64()
}
