import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id(libs.plugins.kotlin.js.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    implementation(projects.ffsShared.db)
    implementation(projects.ffsShared.endpoints)
    implementation(projects.ffsShared.validators)
    implementation(projects.ffsShared.rule)
    implementation(projects.ffsShared.sessionHeader)

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
                setServerHostPort()
            }
            runTask {
                setServerHostPort()
            }
        }
        binaries.executable()
    }
}

fun KotlinWebpack.setServerHostPort() {
    val env = System.getenv()
    env["SERVER_HOST"]?.let { host ->
        args += listOf("--env", "host=\"$host\"")
    }
    env["SERVER_PORT"]?.let { port ->
        args += listOf("--env", "port=$port")
    }
    webpackConfigApplier {
        export = false
    }
}
