@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import eu.bsinfo.build.androidCompileSdk
import eu.bsinfo.build.androidMinSdk
import eu.bsinfo.build.javaVersion
import eu.bsinfo.build.kotlinJvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    com.android.library
    org.jetbrains.kotlin.multiplatform
}

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("fileChooser") {
                withJvm()
            }
            group("skia") {
                withWasmJs()
            }
        }
    }

    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget = kotlinJvmTarget
        }
    }
    wasmJs {
        browser()
    }
}

android {
    compileSdk = androidCompileSdk
    namespace = "eu.bsinfo.${project.name}"

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    defaultConfig {
        minSdk = androidMinSdk
    }
}
