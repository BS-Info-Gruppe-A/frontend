plugins {
    alias(libs.plugins.kotlin.compose) apply false
}
allprojects {
    version = "1.0.0"
    group = "eu.bsinfo"

    repositories {
        mavenCentral()
        google()
        maven("https://europe-west3-maven.pkg.dev/mik-music/mikbot")
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}
