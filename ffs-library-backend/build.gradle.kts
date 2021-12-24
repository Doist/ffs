plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
    explicitApi()

    enableMultiplatformTargets(
        configureJs = {
            binaries.library()
        }
    )

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ffs-shared:client-library"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
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
