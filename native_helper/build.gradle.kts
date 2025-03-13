import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    java
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

val jextractOutput: Provider<Directory> = layout.buildDirectory.dir("generated/jextract/main/java")

sourceSets {
    main {
        java.srcDir(jextractOutput)
        resources.srcDir(layout.buildDirectory.dir("generated/resources/main"))
    }
}

dependencies {
    implementation(projects.fileDialog)
    implementation(libs.kotlinx.serialization.json)
}

tasks {
    val compileRust by registering(Exec::class) {
        inputs.dir("src")
        outputs.dir("target")

        commandLine("cargo", "build", "--release")
    }

    val generateHeaders by registering(Exec::class) {
        dependsOn(compileRust)
        inputs.dir("src")
        outputs.file("target/native_helper.h")

        commandLine("cargo", "run", "--features", "headers", "--bin", "generate-headers")
    }

    val generateBindingsWithJextract by registering(Exec::class) {
        dependsOn(generateHeaders)
        val header = "target/native_helper.h"
        inputs.file(header)
        outputs.dir(jextractOutput)

        val command = if (HostManager.hostIsMingw) {
            "jextract.bat"
        } else {
            "jextract"
        }

        val libraryPath = if (System.getenv("GITHUB_REF") != null) {
            "resources"
        } else {
            file("target/release").absolutePath
        }
        val name = when {
            HostManager.hostIsMac -> "/libnative_helper.dylib"
            HostManager.hostIsMingw -> "\\native_helper.dll"
            HostManager.hostIsLinux -> "/libnativehelper.so"
            else -> error("Unknown host")
        }

        commandLine(
            command,
            "--header-class-name", "NativeHelper",
            "--target-package", "eu.bsinfo.native_helper.generated",
            "--library", ":$libraryPath$name",
            "--output", jextractOutput.get().asFile.absolutePath,
            "--include-function", "free_c_string",
            "--include-function", "open_file",
            "--include-function", "save_file",
            "--include-typedef", "uint8_t",
            "--include-typedef", "size_t",
            "--include-struct", "Vec_uint8",
            "--include-struct", "Vec_uint8_t",
            "--include-struct", "Filter",
            "--include-struct", "slice_ref_Filter",
            "--include-struct", "slice_ref_Vec_uint8",
            header,
        )
    }

    compileJava {
        dependsOn(generateBindingsWithJextract)
    }

    compileKotlin {
        dependsOn(generateBindingsWithJextract)
    }
}

tasks {
    withType<KotlinJvmCompile>().configureEach {
        jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_22
}
