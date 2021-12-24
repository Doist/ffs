kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.better.parse)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.murmurhash)
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
}
