plugins {
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":ffs-shared:env"))
                implementation(project(":ffs-shared:sse"))
                implementation(libs.bundles.client.ktor)
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
                implementation(libs.bundles.server.ktor)
                implementation(libs.ktor.client.engine.cio)
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
}
