import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

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
            group("hasFileChooser") {
                withJvm()
            }
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
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
                lwjglDependency(libs.lwjgl)
                lwjglDependency(libs.lwjgl.nfd)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

fun KotlinDependencyHandler.lwjglDependency(dependency: ProviderConvertible<MinimalExternalModuleDependency>) =
    lwjglDependency(dependency.asProvider())

fun KotlinDependencyHandler.lwjglDependency(dependency: Provider<MinimalExternalModuleDependency>) {
    implementation(dependency)
    when (val host = HostManager.host) {
        KonanTarget.MACOS_ARM64 -> implementation(project.dependencies.variantOf(dependency) {
            classifier("natives-macos-arm64")
        })

        KonanTarget.MACOS_X64 -> implementation(project.dependencies.variantOf(dependency) {
            classifier("natives-macos")
        })

        KonanTarget.MINGW_X64 -> implementation(project.dependencies.variantOf(dependency) {
            classifier("natives-windows")
        })

        KonanTarget.LINUX_ARM64 -> implementation(project.dependencies.variantOf(dependency) {
            classifier("natives-linux-arm")
        })

        KonanTarget.LINUX_ARM64 -> implementation(project.dependencies.variantOf(dependency) {
            classifier("natives-linux-arm64")
        })

        KonanTarget.LINUX_X64 -> implementation(project.dependencies.variantOf(dependency) {
            classifier("natives-linux")
        })

        else -> error("Unsupported Target: $host")
    }
}
