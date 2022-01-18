import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id(libs.plugins.kotlin.js.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    implementation(project(":ffs-shared:db"))
    implementation(project(":ffs-shared:endpoints"))
    implementation(project(":ffs-shared:validators"))
    implementation(project(":ffs-shared:session-header"))

    implementation(libs.bundles.client.ktor)
    implementation(libs.bundles.client.react)
    implementation(libs.krypto.js)

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
            webpackTask {
                setKtorHostPort()
            }
            runTask {
                setKtorHostPort()
            }
        }
        binaries.executable()
    }
}

fun KotlinWebpack.setKtorHostPort() {
    val env = System.getenv()
    val development = env["KTOR_DEVELOPMENT"]?.toBoolean()
    val port = env["KTOR_DEPLOYMENT_PORT"]?.toIntOrNull()
    if (development == true && port != null) {
        args += listOf("--env", "host=localhost", "--env", "port=$port")
    }
    webpackConfigApplier {
        export = false
    }
}
