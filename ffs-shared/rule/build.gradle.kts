kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.ffsShared.env)
                implementation(libs.kotlinx.datetime)
                implementation(libs.better.parse)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.murmurhash)
                implementation(libs.ionspin.bignum)
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
}
