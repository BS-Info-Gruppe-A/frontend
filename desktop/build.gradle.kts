plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

dependencies {
    implementation(projects.common)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "eu.bsinfo.desktop.MainKt"
    }
}