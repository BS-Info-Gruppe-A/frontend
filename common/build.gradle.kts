import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("skia") {
                withWasmJs()
            }
        }
    }
    jvm()
    wasmJs {
        browser()
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.resources)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.lifecycle.viewmodel.compose)

                api(libs.kotlinx.datetime)
                api(compose.foundation)
                api(compose.runtime)
                api(compose.ui)
                api(compose.materialIconsExtended)
                api(compose.material3)
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}