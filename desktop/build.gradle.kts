plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

dependencies {
    implementation(projects.common)
    implementation(compose.desktop.currentOs)


    // Logging
    implementation(libs.groovy)
    implementation(libs.jansi)
    implementation(libs.logback)
}

compose.desktop {
    application {
        mainClass = "eu.bsinfo.desktop.MainKt"
    }
}