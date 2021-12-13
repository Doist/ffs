plugins {
    id(libs.plugins.kotlin.js.get().pluginId)
}

dependencies {
    implementation(libs.bundles.kotlin.react)
    
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
