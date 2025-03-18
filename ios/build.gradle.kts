plugins {
    org.jetbrains.kotlin.multiplatform
    kotlin("native.cocoapods")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.skie)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = project.version.toString()
        summary = "HausFix ios module"

        homepage = "https://hausfix.devs-from.asia"
        ios.deploymentTarget = "17.6"
        podfile = project.file("Podfile")

        framework {
            baseName = "HausFix"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common)
                implementation(compose.foundation)
                implementation(compose.runtime)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xbinary-bundleid=eu.bsinfo.ios")
    }
}
