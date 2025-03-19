import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        optIn.add("org.jetbrains.kotlin.gradle.ExperimentalWasmDsl")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.build.tools.gradle)
}
