plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    js(IR) {
        nodejs()
        binaries.library()
    }

    iosX64()
    iosArm64()

    watchosX64()
    watchosArm32()
    watchosArm64()

    tvosX64()
    tvosArm64()

    macosX64()
    macosArm64()

    linuxX64()
    linuxArm64()

    mingwX64()
    
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test.base)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.jvm)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test.js)
            }
        }
    }
}