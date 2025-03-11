import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.konan.target.HostManager

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

val collectProguardConfigs by tasks.registering(Copy::class) {
    dependsOn(":native_helper:jar", ":common:jvmJar")
    into(layout.buildDirectory.dir("proguard-files"))
    include("META-INF/proguard/*.pro")
    eachFile { path = name }

    from({
        configurations.runtimeClasspath.get().map {
            zipTree(it).matching {
                include("META-INF/proguard/*.pro")
            }
        }
    })
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
        named("proguardReleaseJars") {
            dependsOn(collectProguardConfigs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "eu.bsinfo.desktop.MainKt"
        jvmArgs("--enable-native-access=ALL-UNNAMED")

        nativeDistributions {
            modules("java.naming", "java.net.http")
            appResourcesRootDir.set(layout.buildDirectory.dir("dll"))

            when {
                HostManager.hostIsLinux -> targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
                HostManager.hostIsMac -> targetFormats(TargetFormat.Pkg)
                HostManager.hostIsMingw -> targetFormats(TargetFormat.Msi)
                else -> targetFormats(TargetFormat.AppImage)
            }
        }

        buildTypes {
            release {
                proguard {
                    version = libs.versions.proguard
                    obfuscate = true
                    configurationFiles.from(
                        fileTree(collectProguardConfigs.map { it.destinationDir }) {
                            include("*.pro")
                        },
                        project.file("rules.pro")
                    )
                }
            }
        }
    }
}
