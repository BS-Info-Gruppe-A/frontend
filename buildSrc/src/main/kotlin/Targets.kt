package eu.bsinfo.build

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.uiTargets() {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    wasmJs {
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
}

fun KotlinMultiplatformExtension.cliTargets() {
    linuxX64()
    linuxArm64()

    mingwX64()

    macosX64()
    macosArm64()
}

fun KotlinMultiplatformExtension.allTargets() {
    uiTargets()
    cliTargets()
}
