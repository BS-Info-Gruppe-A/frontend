import com.android.kotlin.multiplatform.ide.models.serialization.androidTargetKey
import eu.bsinfo.build.androidCompileSdk
import eu.bsinfo.build.androidMinSdk
import eu.bsinfo.build.javaVersion
import eu.bsinfo.build.kotlinJvmTarget

plugins {
    com.android.application
    org.jetbrains.kotlin.android
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

dependencies {
    implementation(projects.common)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)
}

android {
    compileSdk = androidCompileSdk
    namespace = "eu.bsinfo.android.app"

    kotlinOptions {
        jvmTarget = kotlinJvmTarget.target
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    defaultConfig {
        minSdk = androidMinSdk
        targetSdk = androidCompileSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "rules.pro"
            )
        }
    }
}
