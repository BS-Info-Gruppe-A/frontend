plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

dependencies {
    implementation(projects.common)
    implementation(compose.desktop.currentOs)

    // Logging
    implementation(libs.jansi)
    implementation(libs.logback)
}

tasks {
    val copyDll by registering(Copy::class) {
        dependsOn(":native_helper:assemble")
        from(project(":native_helper").layout.projectDirectory.dir("target/release/"))
        include("*.dll", "*.dylib", "*.so")
        into(layout.buildDirectory.dir("dll/jvm"))
    }

    afterEvaluate {
        named("prepareAppResources") {
            dependsOn(copyDll)
        }
    }
}

compose.desktop {
    application {
        mainClass = "eu.bsinfo.desktop.MainKt"
        jvmArgs("--enable-native-access=ALL-UNNAMED")

        nativeDistributions {
            modules("java.naming")
            appResourcesRootDir.set(layout.buildDirectory.dir("dll"))
        }
    }
}
