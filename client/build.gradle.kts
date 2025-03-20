plugins {
    `multiplatform-module`
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    macosArm64()
    macosX64()

    linuxX64()
    linuxArm64()

    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlin.logging)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.resources)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.xmlutil)
                implementation(libs.kotlinx.serialization.csv)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.java)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        appleMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        mingwMain {
            dependencies {
                implementation(libs.ktor.client.winhttp)
            }
        }
        linuxMain {
            dependencies {
                implementation(libs.ktor.client.curl)
            }
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}
