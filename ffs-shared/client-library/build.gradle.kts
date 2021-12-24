plugins {
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ffs-shared:env"))
                implementation(project(":ffs-shared:sse"))
                implementation(libs.bundles.ktor.client)
                api(libs.kotlinx.serialization.json)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.ktor.server)
                implementation(libs.ktor.engine.cio)
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
}
