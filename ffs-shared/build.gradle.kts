plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    js(IR) {
        browser {
            testTask {
                useKarma {
                    // List all browsers from test-resources.js, so that the Kotlin plugin downloads
                    // their runners and captures failures. "karma-detect-browsers" figures out
                    // which browser to enable, depending on what's installed in the base system.
                    useChromeHeadless()
                    useChromiumHeadless()
                    useFirefoxHeadless()
                    useFirefoxDeveloperHeadless()
                    useOpera()
                    useSafari()
                    useIe()
                }
            }
        }
        nodejs()
        binaries.library()
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

    mingwX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.better.parse)
                implementation(libs.kotlinx.murmurhash)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(npm("karma-detect-browsers", "^2.0"))
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
}
