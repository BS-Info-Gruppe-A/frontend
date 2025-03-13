package eu.bsinfo.build

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val kotlinJvmTarget = JvmTarget.JVM_21
val javaVersion = JavaVersion.toVersion(kotlinJvmTarget.target)

val androidCompileSdk = 35
val androidMinSdk = 23