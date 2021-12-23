plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        browser()
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
                implementation(project(":ffs-shared"))

                implementation(libs.bundles.ktor.client)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)

                implementation(libs.ktor.client.mock)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.ktor.server)
            }
        }

        // Configure Ktor's engine. It'd be CIO for all, but JS and Windows don't support it.
        configureEach {
            val cioPrefixes = listOf("jvm", "android", "ios", "watchos", "tvos", "macos", "linux")
            if (cioPrefixes.any { name.startsWith(it) } && name.endsWith("Main")) {
                dependencies {
                    implementation(libs.ktor.engine.cio)
                }
            }

            val jsPrefixes = listOf("js")
            if (jsPrefixes.any { name.startsWith(it) } && name.endsWith("Main")) {
                dependencies {
                    implementation(libs.ktor.engine.js)
                }
            }

            val curlPrefixes = listOf("mingw")
            if (curlPrefixes.any { name.startsWith(it) } && name.endsWith("Main")) {
                dependencies {
                    implementation(libs.ktor.engine.curl)
                }
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.js.ExperimentalJsExport")
}
