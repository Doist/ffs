plugins {
    kotlin("js")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.252-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.252-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:5.2.0-pre.252-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-redux:4.1.1-pre.252-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-redux:7.2.4-pre.252-kotlin-1.5.31")
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
