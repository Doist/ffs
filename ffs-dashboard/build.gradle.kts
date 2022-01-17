import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id(libs.plugins.kotlin.js.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    implementation(project(":ffs-shared:endpoints"))
    implementation(project(":ffs-shared:validators"))

    implementation(libs.bundles.client.ktor)
    implementation(libs.bundles.client.react)

    implementation(npm("@doist/reactist", "^11.0.0"))
    // Peer dependency of Reactist.
    implementation(npm("classnames", "^2.2.5"))

    testImplementation(libs.kotlin.test)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
}
