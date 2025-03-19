package eu.bsinfo.build

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


fun KotlinMultiplatformExtension.uiTargets() {
    jvm()
    androidTarget()

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
