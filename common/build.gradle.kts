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
        optIn.addAll("kotlin.uuid.ExperimentalUuidApi", "androidx.compose.material3.ExperimentalMaterial3Api")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.resources)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose)

                implementation(libs.kotlin.string.similarity)

                api(libs.kotlinx.datetime)
                api(libs.kotlin.logging)
                api(compose.foundation)
                api(compose.runtime)
                api(compose.ui)
                api(compose.materialIconsExtended)
                api(compose.material3)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}