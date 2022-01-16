plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
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
                implementation(project(":ffs-shared:client"))
                implementation(project(":ffs-shared:rule"))
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
                    implementation(libs.ktor.client.engine.cio)
                }
            }

            val jsPrefixes = listOf("js")
            if (jsPrefixes.any { name.startsWith(it) } && name.endsWith("Main")) {
                dependencies {
                    implementation(libs.ktor.client.engine.js)
                }
            }

            val curlPrefixes = listOf("mingw")
            if (curlPrefixes.any { name.startsWith(it) } && name.endsWith("Main")) {
                dependencies {
                    implementation(libs.ktor.client.engine.curl)
                }
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.js.ExperimentalJsExport")
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
}
