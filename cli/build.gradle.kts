import eu.bsinfo.build.cliTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    org.jetbrains.kotlin.multiplatform
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

kotlin {
    cliTargets()

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.executable {
            entryPoint = "eu.bsinfo.cli.main"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.client)
                implementation(libs.kotlinx.io.core)
                implementation(libs.mosaic.runtime)
                implementation(libs.clikt)
            }
        }
    }

    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}
