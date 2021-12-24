plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
    enableMultiplatformTargets()
}

subprojects {
    // https://github.com/gradle/gradle/issues/16634
    val libs = rootProject.libs

    apply(plugin = libs.plugins.kotlin.multiplatform.get().pluginId)

    kotlin {
        enableMultiplatformTargets()

        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation(libs.kotlin.test)
                }
            }
        }
    }
}
