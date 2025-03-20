import eu.bsinfo.build.uiTargets
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    `multiplatform-module`
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("fileChooser") {
                withJvm()
            }
            group("noFileChooser") {
                withWasmJs()
                withAndroidTarget()
                withApple()
                group("apple")
            }
            group("nativeDarkMode") {
                withWasmJs()
                withAndroidTarget()
                withApple()
            }
        }
    }
    uiTargets()

    compilerOptions {
        optIn.addAll(
            "kotlin.uuid.ExperimentalUuidApi",
            "androidx.compose.material3.ExperimentalMaterial3Api"
        )
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.fileDialog)
                api(projects.client)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.resources)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose)

                implementation(libs.kotlin.string.similarity)
                implementation(libs.xmlutil)
                implementation(libs.kotlinx.serialization.csv)

                api(libs.kotlinx.datetime)
                api(libs.kotlin.logging)
                api(compose.foundation)
                api(compose.runtime)
                api(compose.ui)
                api(compose.materialIconsExtended)
                api(compose.material3)
                implementation(compose.components.resources)
            }
        }

        jvmMain {
            dependencies {
                api(projects.nativeHelper)
                implementation(libs.ktor.client.java)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
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
    }
}
